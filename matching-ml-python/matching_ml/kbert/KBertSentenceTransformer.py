import json
from typing import Union, List, Dict, Tuple

import numpy as np
import numpy.typing as npt
import pandas as pd
import torch
from numpy.random import RandomState
from sentence_transformers import SentenceTransformer

from kbert.monkeypatches import albert_forward, pooling_forward, transformer_forward, bert_get_extended_attention_mask
from kbert.utils import print_time

SEED = 42

RANDOM_STATE = RandomState(seed=SEED)

ROLE_RANKS = pd.Series({'s': 1, 'o': 0}, name='rank')


def group_by_index(statements):
    return statements.groupby(statements.index)


def add_statement_texts(statements: pd.DataFrame) -> pd.DataFrame:
    """
    For subject statements, computes texts by concatenating subject + ' ' + predicate, for object statements by
    concatenating predicate + ' ' + object
    :param statements: pandas Dataframe with following columns:
    - p: text of statement's predicate
    - n: text of statement's neighbor (object or subject)
    - r: 's' if n is a subject, 'o' if it is an object
    :return: statements dataframe with new column 'text' containing textual representation of the statement
    """
    has_statement_subject = statements['r'] == 's'
    statements.loc[has_statement_subject, 'text'] = \
        statements.loc[has_statement_subject, 'text_n'] + ' ' + statements.loc[has_statement_subject, 'text_p']
    statements.loc[has_statement_subject, 'tokens'] = \
        statements.loc[has_statement_subject, 'tokens_n'] + statements.loc[has_statement_subject, 'tokens_p']

    has_statement_object = statements['r'] == 'o'
    statements.loc[has_statement_object, 'text'] = \
        statements.loc[has_statement_object, 'text_p'] + ' ' + statements.loc[has_statement_object, 'text_n']
    statements.loc[has_statement_object, 'tokens'] = \
        statements.loc[has_statement_object, 'tokens_p'] + statements.loc[has_statement_object, 'tokens_n']
    return statements


def molecules_from_texts(texts):
    molecules = pd.DataFrame([json.loads(text) for text in texts])
    return molecules


class KBertSentenceTransformer(SentenceTransformer):
    def __init__(self, model_name_or_path, index_files=None, pooling_mode=None, sampling_mode='stratified', **kwargs):
        super().__init__(model_name_or_path, **kwargs)

        self.token_index = pd.DataFrame(columns=['text', 'tokens', 'n_tokens'])
        if index_files is not None:
            for f in index_files:
                self.extend_index(f)

        self.sampling_mode = sampling_mode

        transformer_module = self._first_module()
        self.max_seq_length = transformer_module.max_seq_length

        # KBert monkey patches
        transformer_module.forward = lambda features: transformer_forward(transformer_module, features)

        bert_model = transformer_module.auto_model
        bert_model.get_extended_attention_mask = \
            lambda attention_mask, input_shape: bert_get_extended_attention_mask(bert_model, attention_mask,
                                                                                 input_shape)
        bert_model.forward = \
            lambda input_ids, attention_mask, token_type_ids, position_ids, return_dict: albert_forward(
                self=bert_model,
                input_ids=input_ids,
                attention_mask=attention_mask,
                token_type_ids=token_type_ids,
                position_ids=position_ids,
                return_dict=return_dict
            )

        pooling_module = self._last_module()
        pooling_module.forward = lambda features: pooling_forward(pooling_module, features)

        pooling_module.pooling_mode_first_target = False
        pooling_module.pooling_mode_mean_target = False
        if pooling_mode is not None and pooling_mode in {'first_target', 'mean_target'}:
            pooling_module.pooling_mode_mean_tokens = False
            pooling_module.pooling_mode_max_tokens = False
            pooling_module.pooling_mode_cls_token = False
            if pooling_mode == 'first_target':
                pooling_module.pooling_mode_first_target = True
            if pooling_mode == 'mean_target':
                pooling_module.pooling_mode_mean_target = True

    def extend_index(self, index_file):
        index_extension = pd.read_csv(index_file, index_col=0, names=['text'])
        index_extension['tokens'] = self.tokenizer.batch_encode_plus(
            index_extension['text'].tolist(), add_special_tokens=False
        ).input_ids
        index_extension['n_tokens'] = index_extension['tokens'].apply(len)
        new_index = pd.concat([self.token_index, index_extension])
        self.token_index = new_index[~new_index.index.duplicated()]

    def tokenize(self, texts: Union[List[str], List[Dict], List[Tuple[str, str]]]) -> Dict[str, torch.Tensor]:
        # todo: check if I can use numba to make this even faster: http://numba.pydata.org/
        molecules = molecules_from_texts(texts)
        n_molecules = molecules.shape[0]

        # y = molecules, x = tokens
        shape_molecules_by_max_seq_length = (n_molecules, self.max_seq_length)
        input_ids = np.zeros(shape_molecules_by_max_seq_length, dtype=int)

        # add cls token ids
        cls_tokens_y = np.arange(n_molecules)
        cls_tokens_x = np.zeros(n_molecules, dtype=int)
        input_ids[cls_tokens_y, cls_tokens_x] = self.tokenizer.cls_token_id

        # get targets
        targets = self.targets_from_molecules(molecules)
        target_groups = group_by_index(targets)
        molecules['n_targets'] = target_groups.size()
        targets['position_within_molecule'] = np.concatenate([np.arange(i) for i in molecules['n_targets'].values])

        molecules['n_target_tokens'] = target_groups['n_tokens'].sum()

        max_targets_per_molecule = molecules['n_targets'].max()
        max_tokens_per_target = targets['n_tokens'].max()

        # get tensor with input ids of targets in molecules
        shape_token_by_target_by_molecule = (n_molecules, max_targets_per_molecule, max_tokens_per_target)
        input_id_by_target_by_molecule = np.zeros(shape_token_by_target_by_molecule, dtype=int) - 1
        molecules_z_for_each_target_token = np.concatenate(
            [np.repeat(i, n_target_tokens) for i, n_target_tokens in molecules['n_target_tokens'].iteritems()])
        targets_y_for_each_token = np.concatenate([
            np.repeat(position_within_molecule, n_tokens)
            for n_tokens, position_within_molecule in targets[['n_tokens', 'position_within_molecule']].values
        ])
        tokens_x = np.concatenate([np.arange(n_tokens) for n_tokens in targets['n_tokens'].values])
        input_id_by_target_by_molecule[
            molecules_z_for_each_target_token,
            targets_y_for_each_token,
            tokens_x
        ] = np.concatenate(targets['tokens'].values)

        # get input positions (offsets) of target tokens in molecules
        target_token_mask = (input_id_by_target_by_molecule != -1)
        offset_token_by_target_by_molecule = np.where(
            target_token_mask,
            target_token_mask.reshape(
                (n_molecules, max_targets_per_molecule * max_tokens_per_target)
            ).cumsum(axis=1).reshape(shape_token_by_target_by_molecule), -1)
        fits_token_in_input_by_target_by_molecule = (
                                                            offset_token_by_target_by_molecule < self.max_seq_length) & target_token_mask
        offset_token_by_target_by_molecule = np.where(
            fits_token_in_input_by_target_by_molecule,
            offset_token_by_target_by_molecule,
            -1
        )

        # mask target tokens that do not fit in input
        input_id_by_target_by_molecule = np.where(
            fits_token_in_input_by_target_by_molecule,
            input_id_by_target_by_molecule,
            -1
        )

        n_target_tokens_by_molecule = fits_token_in_input_by_target_by_molecule.sum((1, 2))

        y_molecule_per_target_token = np.concatenate(
            [np.repeat(i, n) for i, n in enumerate(n_target_tokens_by_molecule)])
        x_target_tokens = np.concatenate(
            [offsets_tokens_by_target[offsets_tokens_by_target != -1] for offsets_tokens_by_target in
             offset_token_by_target_by_molecule])
        input_ids[y_molecule_per_target_token, x_target_tokens] = np.concatenate(
            [token_by_target[token_by_target != -1] for token_by_target in
             input_id_by_target_by_molecule])

        statements = self.statements_from_molecules(molecules)
        statement_groups = group_by_index(statements)
        molecules['n_statements'] = statement_groups.size()
        molecules['n_statement_tokens'] = statement_groups['n_tokens'].sum()
        molecules['statement_offset'] = offset_token_by_target_by_molecule.max(axis=(1, 2))

        role_groups = statements.groupby([statements.index, statements['r']])
        roles = role_groups.size().rename('n_statements').to_frame()
        roles['n_statement_tokens'] = role_groups['n_tokens'].sum()
        roles = roles.reset_index(level=0, drop=True)
        roles.index = roles.index.map({'o': 0, 's': 1})
        statements['position_within_role_within_molecule'] = np.concatenate(
            [np.arange(i) for i in roles['n_statements'].values])

        max_statements_per_role_per_molecule = roles['n_statements'].max()
        max_tokens_per_statement = statements['n_tokens'].max()
        shape_token_by_statement_by_role_by_molecule = (
            n_molecules, 2, max_statements_per_role_per_molecule, max_tokens_per_statement)

        # get tensor with input ids of statements by role in molecules
        input_id_by_statement_by_role_by_molecule = np.zeros(shape_token_by_statement_by_role_by_molecule,
                                                             dtype=int) - 1
        z_molecule_for_roles_for_statements_4_tokens = np.concatenate(
            [np.repeat(i, n_statement_tokens) for i, n_statement_tokens in molecules['n_statement_tokens'].iteritems()])
        y_role_4_statements_4_tokens = np.concatenate(
            [np.repeat(i, n_statement_tokens) for i, n_statement_tokens in roles['n_statement_tokens'].iteritems()])
        x_statement_4_tokens = np.concatenate([
            np.repeat(position_within_role_within_molecule, n_tokens)
            for n_tokens, position_within_role_within_molecule in
            statements[['n_tokens', 'position_within_role_within_molecule']].values
        ])
        w_tokens = np.concatenate([np.arange(n_tokens) for n_tokens in statements['n_tokens'].values])
        input_id_by_statement_by_role_by_molecule[
            z_molecule_for_roles_for_statements_4_tokens,
            y_role_4_statements_4_tokens,
            x_statement_4_tokens,
            w_tokens
        ] = np.concatenate(statements['tokens'].values)

        # Sample statements
        # todo: later, for the flip, flip back etc. operations, I can implement context managers
        # todo: compare current sampling time with that before vectorization

        # 1. flip subject statements so that when a subject statement does not fit, leading tokens are cropped instead
        # of trailing ones
        mask = (input_id_by_statement_by_role_by_molecule != -1)
        mask[:, 1, :, :] = np.flip(mask[:, 1, :, :], axis=2)

        # 2. Flatten roles to sample across both roles
        max_statements_per_molecule = 2 * max_statements_per_role_per_molecule
        shape_token_by_statement_by_molecule = (n_molecules, max_statements_per_molecule, max_tokens_per_statement)
        mask = mask.reshape(shape_token_by_statement_by_molecule)

        # 3. shuffle
        statement_orders_by_molecule = np.tile(np.arange(max_statements_per_molecule)[np.newaxis, :],
                                               (n_molecules, 1))
        for r in statement_orders_by_molecule:
            RANDOM_STATE.shuffle(r)

        # todo: check again if I can't make this faster somehow :/ following lines each take > 0.2 seconds
        max_tokens_per_molecule = max_statements_per_molecule * max_tokens_per_statement
        z_molecule_4_statements_4_tokens = np.repeat(np.arange(n_molecules), max_tokens_per_molecule)
        y_statement_over_tokens = np.repeat(statement_orders_by_molecule, max_tokens_per_statement)
        x_tokens = np.tile(np.arange(max_tokens_per_statement),
                           n_molecules * 2 * max_statements_per_role_per_molecule)

        mask = mask[
            z_molecule_4_statements_4_tokens,
            y_statement_over_tokens,
            x_tokens
        ].reshape(shape_token_by_statement_by_molecule)

        # 4. Compute flags determining which tokens fit in input and which not
        # count through all statement tokens within each molecule to figure out which tokens still fit in the input
        offset_token_by_statement_by_molecule = np.where(
            mask,
            mask.reshape((n_molecules, max_tokens_per_molecule)).cumsum(
                axis=1)
            .reshape(shape_token_by_statement_by_molecule) +
            molecules['statement_offset'].values[:, np.newaxis, np.newaxis],
            -1
        )
        fits_token_in_input_by_statement_by_molecule = \
            (offset_token_by_statement_by_molecule < self.max_seq_length) & mask

        # 5. Shuffle back
        reverse_statement_orders_by_molecule = np.zeros(statement_orders_by_molecule.shape, dtype=int)
        y_molecule_per_statement = np.repeat(np.arange(n_molecules), max_statements_per_molecule)
        x_statements = statement_orders_by_molecule.ravel()
        reverse_statement_orders_by_molecule[y_molecule_per_statement, x_statements] = \
            np.tile(np.arange(max_statements_per_molecule), n_molecules)
        y_statement_over_tokens = np.repeat(reverse_statement_orders_by_molecule, max_tokens_per_statement)

        fits_token_in_input_by_statement_by_molecule = fits_token_in_input_by_statement_by_molecule[
            z_molecule_4_statements_4_tokens,
            y_statement_over_tokens,
            x_tokens
        ].reshape(shape_token_by_statement_by_molecule)

        # 6. unflatten roles
        fits_token_in_input_by_statement_by_role_by_molecule = \
            fits_token_in_input_by_statement_by_molecule.reshape(shape_token_by_statement_by_role_by_molecule)

        # todo: for stratifying, see https://stackoverflow.com/questions/47393362/concatenate-two-arrays-in-python-with-alternating-the-columns-in-numpy

        # 7. flip subject statement tokens back
        fits_token_in_input_by_statement_by_role_by_molecule[:, 1, :, :] = \
            np.flip(fits_token_in_input_by_statement_by_role_by_molecule[:, 1, :, :], axis=2)

        offset_token_by_statement_by_role_by_molecule = np.where(
            fits_token_in_input_by_statement_by_role_by_molecule,
            fits_token_in_input_by_statement_by_role_by_molecule.reshape(
                (n_molecules, max_tokens_per_molecule)
            ).cumsum(axis=1).reshape(shape_token_by_statement_by_role_by_molecule) +
            molecules['statement_offset'].values[:, np.newaxis, np.newaxis, np.newaxis],
            -1
        )

        # filter
        # mask statement tokens that do not fit in input
        input_id_by_statement_by_role_by_molecule = np.where(
            fits_token_in_input_by_statement_by_role_by_molecule,
            input_id_by_statement_by_role_by_molecule,
            -1
        )

        # Add input ids of statement tokens
        n_statement_tokens_by_molecule = fits_token_in_input_by_statement_by_role_by_molecule.sum((1, 2, 3))

        y_molecule_4_statement_tokens = np.concatenate(
            [np.repeat(i, n) for i, n in enumerate(n_statement_tokens_by_molecule)])
        x_statement_tokens = np.concatenate(
            [offsets_tokens_by_statement[offsets_tokens_by_statement != -1] for offsets_tokens_by_statement in
             offset_token_by_statement_by_role_by_molecule])
        input_ids[y_molecule_4_statement_tokens, x_statement_tokens] = np.concatenate([
            input_id_by_statement[input_id_by_statement != -1]
            for input_id_by_statement in input_id_by_statement_by_role_by_molecule
        ])

        # Compute position ids
        position_ids = np.zeros(shape_molecules_by_max_seq_length, dtype=int)

        # Add position ids of subject statement tokens
        fits_token_in_input_by_subject_statement_by_molecule = \
            fits_token_in_input_by_statement_by_role_by_molecule[:, 1, :, :]
        n_tokens_per_subject_statement_per_molecule = \
            fits_token_in_input_by_subject_statement_by_molecule.sum(2)
        max_subject_statement_tokens_per_molecule = n_tokens_per_subject_statement_per_molecule.max(1)

        # subject statements that are shorter than the longest one do not start at position one, but at a position such
        # that the last token is adjacent to the first target token
        offset_first_token_per_subject_statement_per_molecule = \
            max_subject_statement_tokens_per_molecule[:, np.newaxis] - n_tokens_per_subject_statement_per_molecule

        cumsum_token_by_subject_statement_by_molecule = \
            fits_token_in_input_by_subject_statement_by_molecule.cumsum(2)

        subject_statement_token_positions = np.where(
            fits_token_in_input_by_subject_statement_by_molecule,
            cumsum_token_by_subject_statement_by_molecule +
            offset_first_token_per_subject_statement_per_molecule[:, :, np.newaxis],
            -1
        )

        # enter position ids of subject statements
        n_statement_tokens_by_role_by_molecule = fits_token_in_input_by_statement_by_role_by_molecule.sum((2, 3))
        y_molecule_4_subject_statement_tokens = np.concatenate(
            [np.repeat(i, n) for i, n in enumerate(n_statement_tokens_by_role_by_molecule[:, 1])])
        x_subject_statement_tokens = np.concatenate([
            offsets_tokens_by_statement[offsets_tokens_by_statement != -1]
            for offsets_tokens_by_statement in offset_token_by_statement_by_role_by_molecule[:, 1]])
        position_ids[y_molecule_4_subject_statement_tokens, x_subject_statement_tokens] = \
            np.concatenate([p[p != -1] for p in subject_statement_token_positions])

        # add positions of target tokens
        cumsum_token_by_target_by_molecule = fits_token_in_input_by_target_by_molecule.cumsum(2)

        target_token_positions = np.where(
            fits_token_in_input_by_target_by_molecule,
            cumsum_token_by_target_by_molecule + max_subject_statement_tokens_per_molecule[:, np.newaxis, np.newaxis],
            -1
        )

        position_ids[y_molecule_per_target_token, x_target_tokens] = \
            np.concatenate([p[p != -1] for p in target_token_positions])

        # add position ids of object statement tokens
        fits_token_in_input_by_object_statement_by_molecule = \
            fits_token_in_input_by_statement_by_role_by_molecule[:, 0, :, :]
        cumsum_token_by_object_statement_by_molecule = \
            fits_token_in_input_by_object_statement_by_molecule.cumsum(2)
        max_target_token_position_by_molecule = target_token_positions.max((1, 2))
        object_statement_token_positions = np.where(
            fits_token_in_input_by_object_statement_by_molecule,
            cumsum_token_by_object_statement_by_molecule +
            max_target_token_position_by_molecule[:, np.newaxis, np.newaxis],
            -1)

        y_molecule_for_object_statement_tokens = np.concatenate([
            np.repeat(i, n) for i, n in enumerate(n_statement_tokens_by_role_by_molecule[:, 0])
        ])
        x_object_statement_tokens = np.concatenate([
            offsets_tokens_by_statement[offsets_tokens_by_statement != -1]
            for offsets_tokens_by_statement in offset_token_by_statement_by_role_by_molecule[:, 0]
        ])

        position_ids[y_molecule_for_object_statement_tokens, x_object_statement_tokens] = \
            np.concatenate([p[p != -1] for p in object_statement_token_positions])

        # todo: compute attention mask
        attention_masks = np.zeros((n_molecules, *2 * [self.max_seq_length]), dtype=int)

        # Add holes for cls token
        seq_length_by_molecule = np.concatenate([
            a[:, np.newaxis] for a in [
                offset_token_by_target_by_molecule.max((1, 2)),
                offset_token_by_statement_by_role_by_molecule.max((1, 2, 3))
            ]
        ], axis=1).max(1) + 1

        z_molecule_4_tokens_4_tokens = np.concatenate([
            np.repeat(i, seq_length)
            for i, seq_length in enumerate(seq_length_by_molecule)
        ])
        y_tokens_4_tokens = np.concatenate([
            np.arange(seq_length) for seq_length in seq_length_by_molecule
        ])
        x_tokens = np.zeros(seq_length_by_molecule.sum(), dtype=int)
        attention_masks[z_molecule_4_tokens_4_tokens, y_tokens_4_tokens, x_tokens] = 1
        attention_masks[z_molecule_4_tokens_4_tokens, x_tokens, y_tokens_4_tokens] = 1

        # each target sees all statements and is seen by all statements
        n_statement_seeing_tokens_by_molecule = n_target_tokens_by_molecule + 1
        n_target_tokens_times_n_statement_tokens_by_molecule = \
            n_target_tokens_by_molecule * n_statement_tokens_by_molecule
        z_molecule_4_tokens_4_tokens = np.concatenate([
            np.repeat(i, n_target_tokens_times_n_statement_tokens)
            for i, n_target_tokens_times_n_statement_tokens
            in enumerate(n_target_tokens_times_n_statement_tokens_by_molecule)
        ])
        y_tokens_4_tokens = np.concatenate([
            np.repeat(np.arange(1, n_statement_seeing_tokens), n_statement_tokens)
            for n_statement_seeing_tokens, n_statement_tokens
            in zip(n_statement_seeing_tokens_by_molecule, n_statement_tokens_by_molecule)
        ])
        x_tokens = np.concatenate([
            np.tile(np.arange(n_statement_seeing_tokens, seq_length), n_target_tokens)
            for n_statement_seeing_tokens, seq_length, n_target_tokens
            in zip(n_statement_seeing_tokens_by_molecule, seq_length_by_molecule, n_target_tokens_by_molecule)
        ])
        attention_masks[z_molecule_4_tokens_4_tokens, y_tokens_4_tokens, x_tokens] = 1
        attention_masks[z_molecule_4_tokens_4_tokens, x_tokens, y_tokens_4_tokens] = 1

        # add holes for targets (target tokens can see tokens of same target)
        n_tokens_by_target_by_molecule = fits_token_in_input_by_target_by_molecule.sum(2)
        sum_square_target_tokens_by_molecule = (n_tokens_by_target_by_molecule ** 2).sum(1)
        z_molecule_4_tokens_4_tokens = np.concatenate([
            np.repeat(i, sum_square_target_tokens)
            for i, sum_square_target_tokens in enumerate(sum_square_target_tokens_by_molecule)
        ])
        fits_target_in_input_by_molecule = fits_token_in_input_by_target_by_molecule.any(2)
        offset_by_target_by_molecule = np.where(
            fits_target_in_input_by_molecule,
            np.where(
                fits_token_in_input_by_target_by_molecule,
                offset_token_by_target_by_molecule,
                self.max_seq_length
            ).min(2),
            -1
        )
        offset_next_by_target_by_molecule = np.where(
            fits_target_in_input_by_molecule,
            offset_token_by_target_by_molecule.max(2) + 1,
            -1
        )
        y_tokens_4_tokens = np.concatenate([
            np.repeat(np.arange(offset, offset_next), offset_next - offset)
            for offset_by_target, offset_next_by_target
            in zip(offset_by_target_by_molecule, offset_next_by_target_by_molecule)
            for offset, offset_next
            in zip(offset_by_target, offset_next_by_target)
        ])
        x_tokens = np.concatenate([
            np.tile(np.arange(offset, offset_next), offset_next - offset)
            for offset_by_target, offset_next_by_target
            in zip(offset_by_target_by_molecule, offset_next_by_target_by_molecule)
            for offset, offset_next
            in zip(offset_by_target, offset_next_by_target)
        ])
        attention_masks[z_molecule_4_tokens_4_tokens, y_tokens_4_tokens, x_tokens] = 1

        # add holes for statements (like in targets, tokens can see each other)
        fits_token_in_input_by_statement_by_molecule = fits_token_in_input_by_statement_by_role_by_molecule.reshape(
            shape_token_by_statement_by_molecule)
        n_tokens_by_statement_by_molecule = fits_token_in_input_by_statement_by_molecule.sum(2)
        sum_square_statement_tokens_by_molecule = (n_tokens_by_statement_by_molecule ** 2).sum((1))
        z_molecule_4_tokens_4_tokens = np.concatenate([
            np.repeat(i, sum_square_statement_tokens)
            for i, sum_square_statement_tokens in enumerate(sum_square_statement_tokens_by_molecule)
        ])
        fits_statement_in_input_by_molecule = fits_token_in_input_by_statement_by_molecule.any(2)
        offset_token_by_statement_by_molecule = \
            offset_token_by_statement_by_role_by_molecule.reshape(shape_token_by_statement_by_molecule)
        offset_by_statement_by_molecule = np.where(
            fits_statement_in_input_by_molecule,
            np.where(
                fits_token_in_input_by_statement_by_molecule,
                offset_token_by_statement_by_molecule,
                self.max_seq_length
            ).min(2),
            -1
        )
        offset_next_by_statement_by_molecule = np.where(
            fits_statement_in_input_by_molecule,
            offset_token_by_statement_by_molecule.max(2) + 1,
            -1
        )
        y_tokens_4_tokens = np.concatenate([
            np.repeat(np.arange(offset, offset_next), offset_next - offset)
            for offset_by_statement, offset_next_by_statement
            in zip(offset_by_statement_by_molecule, offset_next_by_statement_by_molecule)
            for offset, offset_next
            in zip(offset_by_statement, offset_next_by_statement)
        ])
        x_tokens = np.concatenate([
            np.tile(np.arange(offset, offset_next), offset_next - offset)
            for offset_by_statement, offset_next_by_statement
            in zip(offset_by_statement_by_molecule, offset_next_by_statement_by_molecule)
            for offset, offset_next
            in zip(offset_by_statement, offset_next_by_statement)
        ])
        attention_masks[z_molecule_4_tokens_4_tokens, y_tokens_4_tokens, x_tokens] = 1

        # Next step: fix targets mask generation
        targets_mask = np.zeros((n_molecules, max_targets_per_molecule, self.max_seq_length))
        z_molecule_4_targets_4_tokens = np.concatenate([
            np.repeat(i, n_target_tokens) for i, n_target_tokens in enumerate(n_target_tokens_by_molecule)
        ])
        y_target_4_tokens = np.concatenate([
            np.repeat(i, n_tokens)
            for n_tokens_by_target in n_tokens_by_target_by_molecule
            for i, n_tokens in enumerate(n_tokens_by_target)
        ])
        x_tokens = np.concatenate(
            [np.arange(1, n_target_tokens + 1) for n_target_tokens in n_target_tokens_by_molecule])
        targets_mask[z_molecule_4_targets_4_tokens, y_target_4_tokens, x_tokens] = 1

        return {
            'input_ids': torch.IntTensor(input_ids),
            'position_ids': torch.IntTensor(position_ids),
            'attention_mask': torch.IntTensor(attention_masks),
            'token_type_ids': torch.IntTensor(np.zeros(shape_molecules_by_max_seq_length)),
            'targets_mask': torch.IntTensor(targets_mask)
        }

    def statements_from_molecules(self, molecules):
        statement_dicts = molecules['s'].explode().dropna()
        statements = pd.DataFrame(statement_dicts.tolist(), index=statement_dicts.index)
        statements[['n', 'p']] = statements[['n', 'p']].astype('int64')
        statements = statements.merge(
            self.token_index,
            left_on='n',
            right_index=True,
        ).merge(
            self.token_index,
            left_on='p',
            right_index=True,
            suffixes=('_n', '_p')
        )
        statements = add_statement_texts(statements)
        statements['n_tokens'] = statements['n_tokens_n'] + statements['n_tokens_p']
        statements['statement_seeing'] = False
        statements['all_seeing'] = False
        return statements.rename_axis('molecule_id').sort_values(by=['molecule_id', 'r'])

    def targets_from_molecules(self, molecules):
        targets = molecules.explode('t')[['t']]
        targets['t'] = targets['t'].astype('int64')
        targets = targets.merge(self.token_index[['text', 'tokens', 'n_tokens']], left_on='t', right_index=True).drop(
            columns='t')
        targets['statement_seeing'] = True
        targets['all_seeing'] = False
        targets['r'] = 't'
        return targets.sort_index()
