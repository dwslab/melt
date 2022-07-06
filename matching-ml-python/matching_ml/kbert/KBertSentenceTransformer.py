import json
from typing import Union, List, Dict, Tuple

import numpy as np
import numpy.typing as npt
import pandas as pd
import torch
from numpy.random import RandomState
from sentence_transformers import SentenceTransformer

from kbert.monkeypatches import albert_forward, pooling_forward, transformer_forward, bert_get_extended_attention_mask

SEED = 42

RANDOM_STATE = RandomState(seed=SEED)

ROLE_RANKS = pd.Series({'s': 1, 'o': 0}, name='rank')


def add_neighbor_positions(predicate_df: pd.DataFrame, neighbor_positions: npt.NDArray[int]) -> pd.DataFrame:
    """
    Given a dataframe of all neighbors for a specific predicate with a specific role, and the positions at which
    this predicate appears when iterating over both predicates and roles, randomly assigns a position to each
    neighbor
    :param predicate_df: dataframe containing neighbors of specific predicate with specific role
    :param neighbor_positions: array containing global positions at which predicate appears
    :return: the dataframe with added position column indicating the absolute position of each predicate
    neighbor when iterating over both roles and predicates
    """
    # shuffle neighbor positions
    RANDOM_STATE.shuffle(neighbor_positions)
    predicate_df['position'] = neighbor_positions[neighbor_positions != -1]
    return predicate_df


def get_global_predicate_positions_for_role_in_outer_round(predicates: pd.Series, inner_round_offsets: pd.Series,
                                                           outer_round_offset: int) -> pd.Series:
    """
    Given a role and its rank, computes the global position of the role's predicates still present in the given outer
    round by summing the inner round offsets with the outer round's offset, shuffles positions to randomize the order
    of predicates in each round, and marks predicates not appearing in the given outer round with -1.
    object first), named with the role's name
    :param predicates: pandas series, indexed with predicate labels, indicating for each predicate of the given role
    whether it predicate still appears in the given outer round
    :param outer_round_offset: the position offset from the outer round
    :param inner_round_offsets: pandas series containing position offsets for all inner rounds in the given outer round
    :return: absolute positions of the provided role's predicates that are still present in this outer round in the
    global ordering of statements
    """
    n_predicates = predicates.sum()
    global_predicate_positions = np.repeat(-1, predicates.size)

    # shuffle offsets to make predicates appear at different positions in each inner round
    shuffled_inner_round_offsets = RANDOM_STATE.permutation(inner_round_offsets[:n_predicates])

    # add outer round offset to inner round offset to get absolute positions
    global_predicate_positions[:n_predicates] = shuffled_inner_round_offsets + outer_round_offset

    predicates_sorted_by_presence = predicates.sort_values(ascending=False).index.get_level_values('p')

    global_predicate_positions = pd.Series(
        data=global_predicate_positions,
        index=[predicates.size * [inner_round_offsets.name], predicates_sorted_by_presence]
    )
    return global_predicate_positions


def get_global_predicate_positions_for_outer_round(predicates: pd.Series, max_predicates_per_role: int,
                                                   outer_round_offset: int) -> pd.Series:
    """
    Given an outer round's offset and predicates appearing in this round, computes for each predicate the global
    position at which it appears when iterating over both predicates and roles
    :param predicates: pandas series indicating for each combination of role and predicate, whether
    it still participates in this outer round through all predicates. The series's name is the index of the outer round
    this function is called from
    :param max_predicates_per_role: the group size of the larger group of predicates, grouped by role
    :param outer_round_offset: the outer round's position offset
    :return: pandas series indexed with combinations of role and predicate, indicating the global position at which each
    predicate appears when iterating over both predicates and roles
    """
    n_predicates_per_role_in_this_outer_round = predicates.groupby('role').sum()

    inner_round_offsets = pd.DataFrame(
        data=get_group_y_in_round_x(n_predicates_per_role_in_this_outer_round, max_group_size=max_predicates_per_role),
        index=n_predicates_per_role_in_this_outer_round.index
        # to generate correct offsets, we first shift appearance matrix by rank of each role, then sum up. To get
        # offsets of specific role, we then have to subtract (max rank - rank) again from these offsets
    ).apply(lambda row: np.pad(row, (ROLE_RANKS[row.name], 0))[:max_predicates_per_role], axis=1).sum(axis=0).cumsum()

    inner_round_offsets_by_role = pd.DataFrame(
        data=np.repeat(inner_round_offsets[np.newaxis, :], len(ROLE_RANKS), axis=0) -
             (ROLE_RANKS.max() - ROLE_RANKS.values)[:, np.newaxis],
        index=ROLE_RANKS.index
    )
    global_predicate_positions = pd.concat([get_global_predicate_positions_for_role_in_outer_round(
        predicates=role_predicates,
        inner_round_offsets=inner_round_offsets_by_role.loc[role],
        outer_round_offset=outer_round_offset
    ) for role, role_predicates in predicates.groupby('role')])
    global_predicate_positions.name = predicates.name
    return global_predicate_positions


def group_by_index(statements):
    return statements.groupby(statements.index)


def prepare_statements(molecules, role):
    raw_statements = molecules[role].explode().dropna()
    statements = pd.DataFrame(raw_statements.tolist(), index=raw_statements.index).rename(columns={role: 'n'})
    statements['role'] = role
    return statements


def get_group_y_in_round_x(group_sizes: pd.Series, max_group_size: int = None) -> npt.NDArray[int]:
    """
    Given an array of group sizes, computes 2D-int-array where rows correspond to groups from provided array and columns
    correspond to rows. Cell (x, y) is 1 if group y still has some elements in round x when iterating over all
    groups, popping one element in each round
    :param group_sizes: pandas series containing sizes of groups to consider
    :param max_group_size: (optional) the size of the largest possible group (if not set, is set to the largest value in
     group_sizes)
    :return: the 2D-int-array
    """
    if max_group_size is None:
        max_group_size = group_sizes.max()
    group_y_in_round_x = np.zeros((len(group_sizes), max_group_size), dtype=int)
    ones_y = np.concatenate([np.repeat(i, s) for i, s in enumerate(group_sizes)])
    ones_x = np.concatenate([np.arange(s) for s in group_sizes])
    group_y_in_round_x[ones_y, ones_x] = 1
    return group_y_in_round_x


class KBertSentenceTransformer(SentenceTransformer):
    def __init__(self, model_name_or_path, **kwargs):
        super().__init__(model_name_or_path, **kwargs)

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


    def tokenize(self, texts: Union[List[str], List[Dict], List[Tuple[str, str]]]) -> Dict[str, torch.Tensor]:
        molecules = pd.DataFrame([json.loads(text) for text in texts])
        tokenizer = super(KBertSentenceTransformer, self).tokenizer

        target_tokens = tokenizer.batch_encode_plus(molecules['t'].tolist(), add_special_tokens=False)
        molecules['target_tokens'] = target_tokens.input_ids
        molecules['n_target_tokens'] = molecules['target_tokens'].apply(len)
        molecules['remaining_tokens'] = np.repeat(self.max_seq_length, len(molecules))
        molecules['remaining_tokens'] -= molecules['n_target_tokens']

        object_statements = prepare_statements(molecules, 'o')
        object_statements['text'] = object_statements['p'] + ' ' + object_statements['n']
        if object_statements.shape[0] > 0:
            object_statements['text'] = object_statements['n'] + ' ' + object_statements['p']

        subject_statements = prepare_statements(molecules, 's')
        if subject_statements.shape[0] > 0:
            subject_statements['text'] = subject_statements['n'] + ' ' + subject_statements['p']

        statements = pd.concat((
            object_statements, subject_statements
        ))

        tokenized_statements = tokenizer.batch_encode_plus(statements['text'].tolist(), add_special_tokens=False)
        statements['tokens'] = tokenized_statements.input_ids
        statements['n_tokens'] = statements['tokens'].apply(len)
        encodings = group_by_index(statements).apply(lambda s: self.encode_statements_for_target(
            target=molecules.loc[s.name],
            statements=s
        ))

        return {label: torch.IntTensor(np.concatenate(col)) for label, col in encodings.iteritems()}

    def encode_statements_for_target(self, target: pd.Series, statements: pd.DataFrame):
        """
        Given a target and its neighboring statements, generates inputs for a KBert transformer.
        :param target: pandas series representing the target concept to encode, with following fields:
        - target_tokens: list of tokens the target concept is encoded to by original transformer's tokenizer
        - n_target_tokens: number of tokens the target is encoded to by original transformer's tokenizer
        - remaining_tokens: number of tokens left to be filled before reaching transformer's max sequence length
        :param statements: dataframe containing all statements connected to the given target concept, with following
        columns:
        - p: the statement's predicate's label
        - role: 's' if the statement contains a subject, 'o' if it contains an object
        - tokens: list of tokens the statement is encoded to by original transformer's tokenizer
        - n_tokens: number of tokens the statement is encoded to by original transformer's tokenizer
        :return: pandas series with following fields:
        - input_ids: 2D-array of dimension (1, <transformer max sequence length>) containing all tokens from statements
          and target concept, padded with trailing 0s
        - position_ids: 2D-array of dimension (1, <transformer max sequence length>) containing soft-position ids
        - token_type: 2D-array of dimension (1, <transformer max sequence length>) containing only 0s
        - attention_mask: 3D-array of dimension
          (1, <transformer max sequence length>, <transformer max sequence length>) containing the visibility matrix for
          this text molecule
        """
        statements_grouped_by_role_and_predicate = statements.groupby(['role', 'p'], as_index=False)
        # pad role with fewer predicates to same size as larger role to make them equal in size
        n_neighbors_per_role_and_predicate = statements_grouped_by_role_and_predicate.size()
        n_neighbors_per_predicate_grouped_by_role = n_neighbors_per_role_and_predicate.groupby('role')
        max_predicates_per_role = n_neighbors_per_predicate_grouped_by_role.size().max()

        def pad_to_max_predicates_per_role(role_predicate_group_sizes):
            padding_size = max_predicates_per_role - role_predicate_group_sizes.shape[0]
            return role_predicate_group_sizes.append(pd.DataFrame({
                'size': pd.Series(padding_size * [0], dtype=int),
                'role': padding_size * [role_predicate_group_sizes.name],
                'p': [str(i) for i in range(padding_size)]
            }))

        n_neighbors_per_role_and_predicate = n_neighbors_per_predicate_grouped_by_role \
            .apply(pad_to_max_predicates_per_role)

        predicates_in_outer_rounds = pd.DataFrame(
            data=get_group_y_in_round_x(n_neighbors_per_role_and_predicate['size']),
            index=[n_neighbors_per_role_and_predicate['role'], n_neighbors_per_role_and_predicate['p']]
        )

        # get offsets for computing positions in each iteration through all predicates
        outer_round_offsets = np.pad(predicates_in_outer_rounds, ((0, 0), (1, 0)))[:, :-1].sum(axis=0).cumsum()

        global_predicate_positions = predicates_in_outer_rounds.apply(
            lambda predicates_in_outer_round: get_global_predicate_positions_for_outer_round(
                predicates=predicates_in_outer_round,
                max_predicates_per_role=max_predicates_per_role,
                outer_round_offset=outer_round_offsets[predicates_in_outer_round.name]
            ))

        # compute positions
        statements = statements_grouped_by_role_and_predicate.apply(
            lambda predicate_df: add_neighbor_positions(
                predicate_df=predicate_df,
                neighbor_positions=global_predicate_positions.loc[predicate_df.name].values
            )).set_index('position').sort_index()

        # Drop statements that do not fit into the input
        statements['n_tokens_cumsum'] = statements['n_tokens'].cumsum()
        statements = statements[statements['n_tokens_cumsum'] <= target['remaining_tokens']]

        seq_length = statements['n_tokens_cumsum'].max() + target['n_target_tokens']
        delta_seq_length = self.max_seq_length - seq_length
        seq_padding = np.repeat(0, delta_seq_length)

        is_subject_statement = statements['role'] == 's'
        max_tokens_per_role = statements.groupby('role')['n_tokens'].max()

        # Compute position ids of subject statements
        if 's' in max_tokens_per_role:
            subject_positions = np.arange(max_tokens_per_role['s'])
            statements.loc[is_subject_statement, 'position_ids'] = statements \
                .loc[is_subject_statement, 'n_tokens'] \
                .apply(lambda n: subject_positions[max_tokens_per_role['s'] - n:])
        else:
            max_tokens_per_role['s'] = 0

        # Compute position ids of target
        target['position_ids'] = np.arange(
            max_tokens_per_role['s'], max_tokens_per_role['s'] + target['n_target_tokens'])

        # Compute position ids of object statements
        if 'o' in max_tokens_per_role:
            offset_object_statements = max_tokens_per_role['s'] + target['n_target_tokens']
            object_positions = np.arange(offset_object_statements, offset_object_statements + max_tokens_per_role['o'])
            statements.loc[~is_subject_statement, 'position_ids'] = statements \
                .loc[~is_subject_statement, 'n_tokens'] \
                .apply(lambda n: object_positions[:n])

        # compute attention mask
        attention_mask = np.zeros(2 * [self.max_seq_length])

        # Add holes for statements (tokens in statements can see each other)
        statements['token_offset'] = np.pad(statements['n_tokens_cumsum'], (1, 0))[:-1]
        statements['hole_coordinates'] = statements.apply(
            lambda row: np.arange(row['token_offset'], row['n_tokens_cumsum']), axis=1)
        statement_holes = statements.apply(
            lambda row: pd.Series({
                'y': np.tile(row['hole_coordinates'], row['n_tokens_cumsum']),
                'x': np.repeat(row['hole_coordinates'], row['n_tokens_cumsum'])
            }), axis=1
        ).apply(np.concatenate)
        attention_mask[statement_holes['y'], statement_holes['x']] = 1

        # Add holes for target (target tokens can see each other and can see and be seen by all statement tokens)
        target_holes_x = np.tile(np.arange(seq_length - target['n_target_tokens'], seq_length), seq_length)
        target_holes_y = np.repeat(np.arange(seq_length), target['n_target_tokens'])
        attention_mask[target_holes_y, target_holes_x] = 1
        attention_mask[target_holes_x, target_holes_y] = 1

        return pd.Series({
            'input_ids': np.concatenate([
                *statements['tokens'],
                target['target_tokens'],
                seq_padding
            ])[np.newaxis, :],
            'position_ids': np.concatenate([
                *statements['position_ids'],
                target['position_ids'],
                seq_padding
            ])[np.newaxis, :],
            'token_type_ids': np.zeros((1, self.max_seq_length)),
            'attention_mask': attention_mask[np.newaxis, :, :]
        })
