from sentence_transformers import SentenceTransformer

from kbert.KBertSentenceTransformer import KBertSentenceTransformer
from kbert.constants import RESOURCES_DIR
from matching_ml.python_server_melt import load_file


def test_kbert_sentence_transformer():
    # Given
    model = KBertSentenceTransformer('paraphrase-albert-small-v2')
    corpus_file_name = str(RESOURCES_DIR / 'corpus_kbert.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    embeddings = model.encode(corpus, batch_size=2, convert_to_tensor=True)
    assert False


def test_sentence_transformer():
    # Given
    model = SentenceTransformer('paraphrase-albert-small-v2')
    corpus_file_name = str(RESOURCES_DIR / 'corpus_kbert.csv')
    corpus, corpus_pos_to_id = load_file(corpus_file_name)
    # When
    embeddings = model.encode(corpus, batch_size=32, convert_to_tensor=True)
    assert False


def test_tokenize_long_description():
    # target thymus gland makes problems
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
    # target thymus gland makes problems
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
    assert all(features[k].numpy().shape == (1, model.max_seq_length) for k in features_1d)
    assert features[feature_2d].numpy().shape == (1, model.max_seq_length, model.max_seq_length)


def test_tokenize_very_short():
    # target thymus gland makes problems
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
