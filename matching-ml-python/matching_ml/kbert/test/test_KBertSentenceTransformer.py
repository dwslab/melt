import pandas as pd
import pytest
from sentence_transformers import SentenceTransformer

from kbert.KBertSentenceTransformer import KBertSentenceTransformer, get_statement_texts
from kbert.constants import RESOURCES_DIR
from matching_ml.python_server_melt import load_file


def test_encode():
    # Given
    model = KBertSentenceTransformer('paraphrase-albert-small-v2', pooling_mode='mean_target')
    corpus_file_name = str(RESOURCES_DIR / 'corpus_kbert.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    embeddings = model.encode(corpus[1859:1861], batch_size=2, convert_to_tensor=True)
    assert embeddings.numpy().shape == (2, 768)


@pytest.mark.skip
def test_sentence_transformer():
    # Given
    model = SentenceTransformer('paraphrase-albert-small-v2')
    corpus_file_name = str(RESOURCES_DIR / 'corpus_kbert.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    embeddings = model.encode(corpus, batch_size=32, convert_to_tensor=True)
    assert False


def test_tokenize_long_description():
    # Given
    model = KBertSentenceTransformer('paraphrase-albert-small-v2')
    corpus_file_name = str(RESOURCES_DIR / 'queries_kbert.csv')
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
    model = KBertSentenceTransformer('paraphrase-albert-small-v2')
    corpus_file_name = str(RESOURCES_DIR / 'corpus_kbert.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    features = model.tokenize(corpus[1859:1861])
    # Then
    features_1d = ['input_ids', 'position_ids', 'token_type_ids']
    feature_2d = 'attention_mask'
    assert all(k in features.keys() for k in features_1d + [feature_2d])
    assert all(features[k].numpy().shape == (2, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (2, model.max_seq_length, model.max_seq_length)


def test_tokenize_very_short():
    # Given
    model = KBertSentenceTransformer('paraphrase-albert-small-v2')
    corpus_file_name = str(RESOURCES_DIR / 'corpus_kbert.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    features = model.tokenize([corpus[25]])
    # Then
    features_1d = ['input_ids', 'position_ids', 'token_type_ids']
    feature_2d = 'attention_mask'
    assert all(k in features.keys() for k in features_1d + [feature_2d])
    assert all(features[k].numpy().shape == (1, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (1, model.max_seq_length, model.max_seq_length)


def test_get_statement_texts():
    # Given
    statements = pd.DataFrame({'p': ['follows', 'precedes'], 'n': ['object', 'subject'], 'role': ['o', 's']})
    # When
    statements = get_statement_texts(statements)
    # Then
    assert statements.at[0, 'text'] == 'follows object'
    assert statements.at[1, 'text'] == 'subject precedes'