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


def add_token_offsets(df: pd.DataFrame) -> pd.DataFrame:
    df['token_offset_next'] = df['n_tokens'].cumsum()
    df['token_offset'] = np.pad(df['token_offset_next'], (1, 0))[:-1]
    return df


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
    n_predicates_per_role_in_this_outer_round = predicates.groupby('r').sum()

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
    ) for role, role_predicates in predicates.groupby('r')])
    global_predicate_positions.name = predicates.name
    return global_predicate_positions


def group_by_index(statements):
    return statements.groupby(statements.index)


def prepare_statements(role, molecules):
    statement_dicts = molecules.explode().dropna()
    if statement_dicts.size == 0:
        statements = pd.DataFrame(columns=['p', 'n'])
    else:
        statements = pd.DataFrame(statement_dicts.tolist(), columns=['p', role], index=statement_dicts.index) \
            .rename(columns={role: 'n'})
    statements['r'] = role
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


def sort_statements_random(statements: pd.DataFrame) -> pd.DataFrame:
    """
    Given a dataframe of statements, randomly assigns a position to each statement and orders the statements by these
    positions
    :param statements: statements dataframe
    :return: the randomly shuffled dataframe
    """
    return statements.sample(frac=1, ignore_index=True)


def sort_statements_stratified(statements):
    """
    Given a dataframe of statements, sorts the dataframe such that taking the first n statements includes as many
    predicates in the sample as possible while also sampling in a balanced fashion from both subject and object
    statements. Randomizes the order of predicates and which statements with a specific predicate are sampled.
    :param statements: statements dataframe with following columns:
    - p: statement predicate text
    - n: statement neighbor text
    - role: whether n is object or subject
    - text: statement text representation
    - tokens: input ids of text's tokens
    - n_tokens: number of tokens in a statement
    :return: the statements dataframe, sorted by the computed positions
    """
    # determine which predicates are present in which outer rounds
    statements_grouped_by_role_and_predicate = statements.groupby(['r', 'p'], as_index=False)
    n_neighbors_per_role_and_predicate = statements_grouped_by_role_and_predicate.size()
    predicates_in_outer_rounds = pd.DataFrame(
        data=get_group_y_in_round_x(n_neighbors_per_role_and_predicate['size']),
        index=[n_neighbors_per_role_and_predicate['r'], n_neighbors_per_role_and_predicate['p']]
    )
    # get offsets for computing positions in each outer round
    max_predicates_per_role = n_neighbors_per_role_and_predicate.groupby('r').size().max()
    outer_round_offsets = np.pad(predicates_in_outer_rounds, ((0, 0), (1, 0)))[:, :-1].sum(axis=0).cumsum()
    global_predicate_positions = predicates_in_outer_rounds.apply(
        lambda predicates_in_outer_round: get_global_predicate_positions_for_outer_round(
            predicates=predicates_in_outer_round,
            max_predicates_per_role=max_predicates_per_role,
            outer_round_offset=outer_round_offsets[predicates_in_outer_round.name]
        ))
    # compute global statement positions
    statements = statements_grouped_by_role_and_predicate.apply(
        lambda predicate_df: add_neighbor_positions(
            predicate_df=predicate_df,
            neighbor_positions=global_predicate_positions.loc[predicate_df.name].values
        )).set_index('position').sort_index()
    return statements


def add_position_ids(atoms):
    """

    :param atoms:
    :return:
    """
    # Position IDs
    # [CLS] is at position 0
    atoms = atoms.reset_index(drop=True)
    atoms['position_ids'] = pd.Series(None, dtype='object')
    atoms.at[0, 'position_ids'] = [0]

    # Compute position ids of subject statements
    max_tokens_per_role = atoms.groupby('r')['n_tokens'].max()
    is_subject_statement = atoms['r'] == 's'
    if 's' in max_tokens_per_role:
        offset_targets = max_tokens_per_role['s'] + 1
        subject_positions = np.arange(1, offset_targets)
        atoms.loc[is_subject_statement, 'position_ids'] = atoms \
            .loc[is_subject_statement, 'n_tokens'] \
            .apply(lambda n: subject_positions[max_tokens_per_role['s'] - n:])
    else:
        offset_targets = 1

    # Compute position ids of targets
    is_target = atoms['r'] == 't'
    n_target_tokens = atoms.loc[is_target, 'n_tokens']
    max_target_tokens = n_target_tokens.max()
    target_positions = np.arange(offset_targets, offset_targets + max_target_tokens)
    atoms.loc[is_target, 'position_ids'] = n_target_tokens.apply(lambda n: target_positions[:n])

    # Compute position ids of object statements
    if 'o' in max_tokens_per_role:
        is_object_statement = atoms['r'] == 'o'
        offset_object_statements = offset_targets + max_target_tokens
        object_positions = np.arange(offset_object_statements,
                                     offset_object_statements + max_tokens_per_role['o'])
        atoms.loc[is_object_statement, 'position_ids'] = atoms \
            .loc[is_object_statement, 'n_tokens'] \
            .apply(lambda n: object_positions[:n])
    return atoms


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
        molecules = molecules_from_texts(texts)
        n_molecules = molecules.shape[0]

        targets = self.targets_from_molecules(molecules)
        target_groups = group_by_index(targets)
        molecules['n_target_tokens'] = target_groups['n_tokens'].sum()
        molecules['n_targets'] = target_groups.size()
        # todo make new item to make the apply() calls faster, this is too slow :(
        has_molecule_too_many_target_tokens = molecules['n_target_tokens'] > self.max_seq_length - 1
        molecules.loc[has_molecule_too_many_target_tokens, 'n_target_tokens'] = self.max_seq_length - 1
        statements = self.statements_from_molecules(molecules)
        atoms = group_by_index(statements).apply(
            lambda s: self.atoms_from_targets_and_statements(targets=targets.loc[[s.name]], statements=s))
        atoms.index = atoms.index.droplevel(1)

        targets_mask = np.zeros((n_molecules, molecules['n_targets'].max(), self.max_seq_length))
        targets_mask_holes = group_by_index(atoms[atoms['r'] == 't']).apply(
            lambda df: df.reset_index(drop=True).apply(
                lambda row: pd.Series({
                    'z': np.repeat(df.name, row['n_tokens']),
                    'y': np.repeat(row.name, row['n_tokens']),
                    'x': np.arange(row['token_offset'], row['token_offset_next']),
                }), axis=1)
        ).explode(['x', 'y', 'z']).astype(int).values
        targets_mask[tuple([*targets_mask_holes.T])] = 1

        encodings = group_by_index(atoms).apply(self.encodings_from_atoms)

        return {
                   label: torch.IntTensor(np.concatenate(col)) for label, col in encodings.iteritems()
               } | {
                   'targets_mask': torch.IntTensor(targets_mask)
               }

    def statements_from_molecules(self, molecules):
        statement_dicts = molecules['s'].explode().dropna()
        statements = pd.DataFrame(statement_dicts.tolist(), index=statement_dicts.index)
        statements[['n', 'p']] = statements[['n', 'p']].astype(int)
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
        return statements

    def targets_from_molecules(self, molecules):
        targets = molecules.explode('t')[['t']]
        targets['t'] = targets['t'].astype(int)
        targets = targets.merge(self.token_index[['text', 'tokens', 'n_tokens']], left_on='t', right_index=True).drop(columns='t')
        targets['statement_seeing'] = True
        targets['all_seeing'] = False
        targets['r'] = 't'
        return targets

    def encodings_from_atoms(self, atoms: pd.DataFrame):
        """
        Given atoms of a text molecule, generates inputs for a KBert transformer.
        :param atoms: dataframe containing all atoms for the given text molecule, columns:
        - text: the atoms text representation
        - tokens: list of tokens the atom is encoded to by original transformer's tokenizer
        - n_tokens: number of tokens the atom is encoded to by original transformer's tokenizer
        - all_seeing: whether an atom sees all other atoms
        - statement_seeing: whether an atom sees all statement atoms
        - r: 's' if the atom is a target, 's' if it is a subject statement, 'o' if it is an object statement, None if it
         is the [CLS] token
        - token_offset: position in model input at which an atom starts
        - token_offset_next: position in model input at which the next atom starts
        :return: pandas series with following fields:
        - input_ids: 2D-array of dimension (1, <transformer max sequence length>) containing all tokens from statements
          and target concept, padded with trailing 0s
        - position_ids: 2D-array of dimension (1, <transformer max sequence length>) containing soft-position ids
        - token_type: 2D-array of dimension (1, <transformer max sequence length>) containing only 0s
        - attention_mask: 3D-array of dimension
          (1, <transformer max sequence length>, <transformer max sequence length>) containing the visibility matrix for
          this text molecule
        """
        # Add padding if statements are shorter than max_seq_length
        seq_padding = np.repeat(0, self.max_seq_length - atoms['token_offset_next'].max())

        atoms = add_position_ids(atoms)

        attention_mask = self.attention_mask_from_atoms(atoms)

        return pd.Series({
            'input_ids': np.concatenate([
                *atoms['tokens'],
                seq_padding
            ])[np.newaxis, :],
            'position_ids': np.concatenate([
                *atoms['position_ids'],
                seq_padding
            ])[np.newaxis, :],
            'token_type_ids': np.zeros((1, self.max_seq_length)),
            'attention_mask': attention_mask[np.newaxis, :, :]
        })

    def attention_mask_from_atoms(self, atoms):
        # compute attention mask
        attention_mask = np.zeros(2 * [self.max_seq_length])

        # Add holes for target and cls (target and cls tokens can see each other and can see and be seen by all
        # statement tokens)
        # CLS sees and is seen by all tokens
        seq_length = atoms['token_offset_next'].max()
        cls_holes_x = np.repeat(0, seq_length)
        cls_holes_y = np.arange(seq_length)
        attention_mask[cls_holes_x, cls_holes_y] = 1
        attention_mask[cls_holes_y, cls_holes_x] = 1

        # each target sees all statements and is seen by all statements
        sum_target_tokens = atoms.loc[atoms['r'] == 't', 'n_tokens'].sum()
        n_statement_seeing_tokens = sum_target_tokens + 1
        if n_statement_seeing_tokens < self.max_seq_length:
            statement_seeing_holes_x = np.tile(
                np.arange(1, n_statement_seeing_tokens),
                seq_length - n_statement_seeing_tokens)
            statement_seeing_holes_y = np.repeat(np.arange(n_statement_seeing_tokens, seq_length), sum_target_tokens)
            attention_mask[statement_seeing_holes_y, statement_seeing_holes_x] = 1
            attention_mask[statement_seeing_holes_x, statement_seeing_holes_y] = 1

        # Add holes for statements and targets (tokens in statements and targets can see each other)
        atoms['hole_coordinates'] = atoms.apply(
            lambda row: np.arange(row['token_offset'], row['token_offset_next']), axis=1)
        self_seeing_text_holes = atoms.apply(
            lambda row: pd.Series({
                'y': np.tile(row['hole_coordinates'], row['token_offset_next']),
                'x': np.repeat(row['hole_coordinates'], row['token_offset_next'])
            }), axis=1
        ).reset_index(drop=True).apply(np.concatenate)
        attention_mask[self_seeing_text_holes['y'], self_seeing_text_holes['x']] = 1
        return attention_mask

    def sort_statements(self, statements: pd.DataFrame):
        """
        Given a dataframe of statements sorts the dataframe according to the configured sampling mode
        :param statements: statements dataframe
        :return: the sorted statements dataframe
        """
        if self.sampling_mode == 'stratified':
            return sort_statements_stratified(statements)
        else:
            return sort_statements_random(statements)

    def atoms_from_targets_and_statements(self, targets, statements):
        """
        Given statements and targets of a text molecule, computes all text atoms for it, including the respective
        offsets in the transformer inputs
        :param targets: dataframe containing all targets of the given text molecule
        :param statements: dataframe containing all statements of the given text molecule
        :return: dataframe containing all atoms for the given text molecule, columns:
        - text: the atoms text representation
        - tokens: list of tokens the atom is encoded to by original transformer's tokenizer
        - n_tokens: number of tokens the atom is encoded to by original transformer's tokenizer
        - all_seeing: whether an atom sees all other atoms
        - statement_seeing: whether an atom sees all statement atoms
        - r: 's' if the atom is a target, 's' if it is a subject statement, 'o' if it is an object statement, None if it
         is the [CLS] token
        - token_offset: position in model input at which an atom starts
        - token_offset_next: position in model input at which the next atom starts
        """
        n_target_tokens = targets['n_tokens'].sum()
        atoms = pd.DataFrame({
            'text': '[CLS]',
            'tokens': [[self.tokenizer.cls_token_id]],
            'n_tokens': [1],
            'all_seeing': True,
            'statement_seeing': True,
            'r': None
        })

        atoms = pd.concat((atoms, targets[atoms.columns]), ignore_index=True)

        if n_target_tokens <= self.max_seq_length:
            statements = self.sort_statements(statements)
            atoms = pd.concat((atoms, statements[atoms.columns]), ignore_index=True)

        atoms = add_token_offsets(atoms)

        atoms = atoms[atoms['token_offset'] < self.max_seq_length]

        atoms_max_index = atoms.index.max()
        last_atom = atoms.loc[atoms_max_index]

        # Crop last text if it only fits partially into the input
        if last_atom['token_offset_next'] > self.max_seq_length:
            n_tokens_2_crop = last_atom['token_offset_next'] - self.max_seq_length
            atoms.loc[atoms_max_index, ['n_tokens', 'token_offset_next']] = \
                atoms.loc[atoms_max_index, ['n_tokens', 'token_offset_next']] - n_tokens_2_crop
            if last_atom['r'] == 's':
                atoms.at[atoms_max_index, 'tokens'] = \
                    atoms.at[atoms_max_index, 'tokens'][n_tokens_2_crop:]
            else:
                atoms.at[atoms_max_index, 'tokens'] = \
                    atoms.at[atoms_max_index, 'tokens'][:-n_tokens_2_crop]
        return atoms
