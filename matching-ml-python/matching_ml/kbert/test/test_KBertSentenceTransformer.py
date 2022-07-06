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
