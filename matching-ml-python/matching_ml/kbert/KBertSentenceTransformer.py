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


def prepare_statements(role, molecules):
    statement_dicts = molecules.explode().dropna()
    if statement_dicts.size == 0:
        statements = pd.DataFrame(columns=['p', 'n'])
    else:
        statements = pd.DataFrame(statement_dicts.tolist(), columns=['p', role], index=statement_dicts.index) \
            .rename(columns={role: 'n'})
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


def get_statement_texts(statements: pd.DataFrame) -> pd.DataFrame:
    """
    For subject statements, computes texts by concatenating subject + ' ' + predicate, for object statements by
    concatenating predicate + ' ' + object
    :param statements: pandas Dataframe with following columns:
    - p: text of statement's predicate
    - n: text of statement's neighbor (object or subject)
    - role: 's' if n is a subject, 'o' if it is an object
    :return: statements dataframe with new column 'text' containing textual representation of the statement
    """
    statements.loc[statements['role'] == 's', 'text'] = \
        statements.loc[statements['role'] == 's', 'n'] + ' ' + statements.loc[statements['role'] == 's', 'p']
    statements.loc[statements['role'] == 'o', 'text'] = \
        statements.loc[statements['role'] == 'o', 'p'] + ' ' + statements.loc[statements['role'] == 'o', 'n']
    return statements


class KBertSentenceTransformer(SentenceTransformer):
    def __init__(self, model_name_or_path, pooling_mode=None, **kwargs):
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

    def tokenize(self, texts: Union[List[str], List[Dict], List[Tuple[str, str]]]) -> Dict[str, torch.Tensor]:
        molecules = self.molecules_from_texts(texts)
        statements = self.statements_from_molecules(molecules)
        encodings = group_by_index(statements).apply(lambda s: self.encode_statements_for_target(
            target=molecules.loc[s.name],
            statements=s
        ))

        target_mask = np.zeros((molecules.shape[0], self.max_seq_length))
        mask_holes = molecules[['n_target_tokens']].apply(lambda row: pd.Series({
            'x': np.arange(1, row['n_target_tokens'] + 1),
            'y': np.repeat(row.name, row['n_target_tokens'])
        }), axis=1).apply(np.concatenate)
        target_mask[mask_holes['y'], mask_holes['x']] = 1

        return {
                   label: torch.IntTensor(np.concatenate(col)) for label, col in encodings.iteritems()
               } | {
                   'target_mask': torch.IntTensor(target_mask)
               }

    def statements_from_molecules(self, molecules):
        statements = pd.concat(prepare_statements(role, col) for role, col in molecules[['s', 'o']].iteritems())
        statements = get_statement_texts(statements)
        statements['tokens'] = self.tokenizer.batch_encode_plus(
            statements['text'].tolist(), add_special_tokens=False
        ).input_ids
        statements['n_tokens'] = statements['tokens'].apply(len)
        return statements

    def molecules_from_texts(self, texts):
        molecules = pd.DataFrame([json.loads(text) for text in texts])
        target_tokens = self.tokenizer.batch_encode_plus(molecules['t'].tolist(), add_special_tokens=False)
        molecules['target_tokens'] = target_tokens.input_ids
        molecules['n_target_tokens'] = molecules['target_tokens'].apply(len)
        return molecules

    def encode_statements_for_target(self, target: pd.Series, statements: pd.DataFrame):
        """
        Given a target and its neighboring statements, generates inputs for a KBert transformer.
        :param target: pandas series representing the target concept to encode, with following fields:
        - target_tokens: list of tokens the target concept is encoded to by original transformer's tokenizer
        - n_target_tokens: number of tokens the target is encoded to by original transformer's tokenizer
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
        n_all_seeing_tokens = target['n_target_tokens'] + 1
        statements = self.sample_statements(n_all_seeing_tokens, statements)

        seq_padding = np.array([])
        seq_length = self.max_seq_length

        # Crop last statement if it only fits partially into the input
        last_statement = statements.iloc[-1]
        if last_statement['n_tokens_cumsum'] > self.max_seq_length:
            n_tokens_2_crop = last_statement['n_tokens_cumsum'] - self.max_seq_length
            last_statement_index = last_statement.name
            statements.loc[last_statement_index, ['n_tokens', 'n_tokens_cumsum']] = \
                statements.loc[last_statement_index, ['n_tokens', 'n_tokens_cumsum']] - n_tokens_2_crop
            if last_statement['role'] == 's':
                statements.at[last_statement_index, 'tokens'] = \
                    statements.at[last_statement_index, 'tokens'][n_tokens_2_crop:]
            else:
                statements.at[last_statement_index, 'tokens'] = \
                    statements.at[last_statement_index, 'tokens'][:-n_tokens_2_crop]

        # Add padding if statements are shorter than max_seq_length
        elif last_statement['n_tokens_cumsum'] < self.max_seq_length:
            seq_length = statements['n_tokens_cumsum'].max()

            delta_seq_length = self.max_seq_length - seq_length
            seq_padding = np.repeat(0, delta_seq_length)

        # Compute position ids of subject statements
        max_tokens_per_role = statements.groupby('role')['n_tokens'].max()
        is_subject_statement = statements['role'] == 's'
        if 's' in max_tokens_per_role:
            offset_target = max_tokens_per_role['s'] + 1
            subject_positions = np.arange(1, offset_target)
            statements.loc[is_subject_statement, 'position_ids'] = statements \
                .loc[is_subject_statement, 'n_tokens'] \
                .apply(lambda n: subject_positions[max_tokens_per_role['s'] - n:])
        else:
            offset_target = 1

        # Compute position ids of target
        target['position_ids'] = np.arange(
            offset_target, offset_target + target['n_target_tokens'])

        # Compute position ids of object statements
        if 'o' in max_tokens_per_role:
            offset_object_statements = offset_target + target['n_target_tokens']
            object_positions = np.arange(offset_object_statements, offset_object_statements + max_tokens_per_role['o'])
            statements.loc[~is_subject_statement, 'position_ids'] = statements \
                .loc[~is_subject_statement, 'n_tokens'] \
                .apply(lambda n: object_positions[:n])

        # compute attention mask
        attention_mask = np.zeros(2 * [self.max_seq_length])

        # Add holes for target and cls (target and cls tokens can see each other and can see and be seen by all
        # statement tokens)
        all_seeing_holes_x = np.tile(np.arange(n_all_seeing_tokens), seq_length)
        all_seeing_holes_y = np.repeat(np.arange(seq_length), n_all_seeing_tokens)
        attention_mask[all_seeing_holes_y, all_seeing_holes_x] = 1
        attention_mask[all_seeing_holes_x, all_seeing_holes_y] = 1

        # Add holes for statements (tokens in statements can see each other)
        statements['hole_coordinates'] = statements.apply(
            lambda row: np.arange(row['n_tokens_cumsum_previous'], row['n_tokens_cumsum']), axis=1)
        statement_holes = statements.apply(
            lambda row: pd.Series({
                'y': np.tile(row['hole_coordinates'], row['n_tokens_cumsum']),
                'x': np.repeat(row['hole_coordinates'], row['n_tokens_cumsum'])
            }), axis=1
        ).apply(np.concatenate)
        attention_mask[statement_holes['y'], statement_holes['x']] = 1
        # todo: also make sure cls token has correct position and correct id and correct type

        return pd.Series({
            'input_ids': np.concatenate([
                [self.tokenizer.cls_token_id],
                target['target_tokens'],
                *statements['tokens'],
                seq_padding
            ])[np.newaxis, :],
            'position_ids': np.concatenate([
                [0],
                target['position_ids'],
                *statements['position_ids'],
                seq_padding
            ])[np.newaxis, :],
            'token_type_ids': np.zeros((1, self.max_seq_length)),
            'attention_mask': attention_mask[np.newaxis, :, :]
        })

    def sample_statements(self, n_reserved_spaces: int, statements: pd.DataFrame):
        """
        Given a dataframe of statements and the number of spaces already reserved, samples n statements for each target,
        including as many predicates in the sample as possible while also sampling in a balanced fashion from both
        subject and object statements. Randomizes the order of predicates and which statements with a specific predicate
        are sampled.
        :param n_reserved_spaces: number of spaces in max_seq_length that are reserved for tokens not in statements
        (target, [CLS])
        :param statements: statements dataframe with following columns:
        - p: statement predicate text
        - n: statement neighbor text
        - role: whether n is object or subject
        - text: statement text representation
        - tokens: input ids of text's tokens
        - n_tokens: number of tokens in a statement
        :return: the sample from the statements dataframe with following additional columns:
        - n_tokens_cumsum: cumulative sum over column n_tokens
        - n_tokens_cumsum_previous: n_tokens_cumsum from previous row, 0 in row 0
        """
        # determine which predicates are present in which outer rounds
        statements_grouped_by_role_and_predicate = statements.groupby(['role', 'p'], as_index=False)
        n_neighbors_per_role_and_predicate = statements_grouped_by_role_and_predicate.size()
        predicates_in_outer_rounds = pd.DataFrame(
            data=get_group_y_in_round_x(n_neighbors_per_role_and_predicate['size']),
            index=[n_neighbors_per_role_and_predicate['role'], n_neighbors_per_role_and_predicate['p']]
        )
        # get offsets for computing positions in each outer round
        max_predicates_per_role = n_neighbors_per_role_and_predicate.groupby('role').size().max()
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
        # Drop statements that do not fit into the input
        statements['n_tokens_cumsum'] = statements['n_tokens'].cumsum() + n_reserved_spaces
        statements['n_tokens_cumsum_previous'] = np.pad(statements['n_tokens_cumsum'], (1, 0))[:-1]
        statements = statements[statements['n_tokens_cumsum_previous'] < self.max_seq_length]
        return statements
