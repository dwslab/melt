import pandas as pd
import pytest
from sentence_transformers import SentenceTransformer

from kbert.KBertSentenceTransformer import KBertSentenceTransformer, add_statement_texts
from kbert.constants import RESOURCES_DIR
from matching_ml.python_server_melt import load_file


# @pytest.mark.skip
def test_encode_kbert():
    # Given
    source_dir = RESOURCES_DIR / 'kbert' / 'raw' / 'all_targets'
    model = KBertSentenceTransformer('paraphrase-albert-small-v2', pooling_mode='mean_target',
                                     index_files=[source_dir / 'index_corpus.csv'])
    corpus_file_name = str(source_dir / 'corpus.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    embeddings = model.encode(corpus, batch_size=32, convert_to_tensor=True)


@pytest.mark.skip
def test_sentence_transformer():
    # Given
    model = SentenceTransformer('paraphrase-albert-small-v2')
    corpus_file_name = str(RESOURCES_DIR / 'kbert' / 'raw' / 'all_targets' / 'queries.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    embeddings = model.encode(corpus, batch_size=32, convert_to_tensor=True)


@pytest.mark.skip
def test_sentence_transformer_queries():
    # Given
    model = SentenceTransformer('paraphrase-albert-small-v2')
    corpus_file_name = str(RESOURCES_DIR / 'queries_kbert.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    embeddings = model.encode(corpus, batch_size=32, convert_to_tensor=True)
    assert False


def test_tokenize_long_description():
    # Given
    root_dir = RESOURCES_DIR / 'kbert' / 'raw' / 'all_targets'
    model = KBertSentenceTransformer('paraphrase-albert-small-v2', [root_dir / 'index_queries.csv'])
    corpus_file_name = str(root_dir / 'queries.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    features = model.tokenize([corpus[2456]])
    # Then
    features_1d = ['input_ids', 'position_ids', 'token_type_ids']
    feature_2d = 'attention_mask'
    assert all(k in features.keys() for k in features_1d + [feature_2d])
    assert all(features[k].numpy().shape == (1, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (1, model.max_seq_length, model.max_seq_length)


def test_tokenize():
    # Given
    source_dir = RESOURCES_DIR / 'kbert' / 'raw' / 'all_targets'
    model = KBertSentenceTransformer('paraphrase-albert-small-v2', [source_dir / f'index_{src}.csv' for src in ['queries', 'corpus']], sampling_mode='random')
    corpus_file_name = str(source_dir / 'queries.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    features = model.tokenize(corpus)
    # Then
    features_1d = ['input_ids', 'position_ids', 'token_type_ids']
    feature_2d = 'attention_mask'
    assert all(k in features.keys() for k in features_1d + [feature_2d])
    assert all(features[k].numpy().shape == (2, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (2, model.max_seq_length, model.max_seq_length)


def test_tokenize_subject_and_object_statements():
    # Given
    root_dir = RESOURCES_DIR / 'kbert' / 'raw' / 'all_targets'
    model = KBertSentenceTransformer('paraphrase-albert-small-v2', [root_dir / 'index_queries.csv'])
    corpus_file_name = str(root_dir / 'queries.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    features = model.tokenize([corpus[28]])
    # Then
    features_1d = ['input_ids', 'position_ids', 'token_type_ids']
    feature_2d = 'attention_mask'
    assert all(k in features.keys() for k in features_1d + [feature_2d])
    assert all(features[k].numpy().shape == (1, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (1, model.max_seq_length, model.max_seq_length)


def test_tokenize_subject_statements():
    # Given
    root_dir = RESOURCES_DIR / 'kbert' / 'raw' / 'all_targets'
    model = KBertSentenceTransformer('paraphrase-albert-small-v2', [root_dir / 'index_queries.csv'])
    corpus_file_name = str(root_dir / 'queries.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    features = model.tokenize([corpus[80]])
    # Then
    features_1d = ['input_ids', 'position_ids', 'token_type_ids']
    feature_2d = 'attention_mask'
    assert all(k in features.keys() for k in features_1d + [feature_2d])
    assert all(features[k].numpy().shape == (1, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (1, model.max_seq_length, model.max_seq_length)


def test_tokenize_very_short():
    # Given
    root_dir = RESOURCES_DIR / 'kbert' / 'raw' / 'all_targets'
    model = KBertSentenceTransformer('paraphrase-albert-small-v2', [root_dir / 'index_corpus.csv'])
    corpus_file_name = str(root_dir / 'corpus.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    features = model.tokenize([corpus[25]])
    # Then
    features_1d = ['input_ids', 'position_ids', 'token_type_ids']
    feature_2d = 'attention_mask'
    assert all(k in features.keys() for k in features_1d + [feature_2d])
    assert all(features[k].numpy().shape == (1, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (1, model.max_seq_length, model.max_seq_length)


def test_tokenize_many_targets():
    # Given
    root = RESOURCES_DIR / 'kbert' / 'raw' / 'all_targets'
    model = KBertSentenceTransformer('paraphrase-albert-small-v2', [root / 'index_queries.csv'])
    corpus_file_name = str(root / 'queries.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    features = model.tokenize([corpus[2001]])
    # Then
    features_1d = ['input_ids', 'position_ids', 'token_type_ids']
    feature_2d = 'attention_mask'
    assert all(k in features.keys() for k in features_1d + [feature_2d])
    assert all(features[k].numpy().shape == (1, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (1, model.max_seq_length, model.max_seq_length)


def test_get_statement_texts():
    # Given
    statements = pd.DataFrame({'p': ['follows', 'precedes'], 'n': ['object', 'subject'], 'r': ['o', 's']})
    # When
    statements = add_statement_texts(statements)
    # Then
    assert statements.at[0, 'text'] == 'follows object'
    assert statements.at[1, 'text'] == 'subject precedes'
