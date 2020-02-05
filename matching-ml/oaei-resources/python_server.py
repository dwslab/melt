from flask import Flask
from flask import request
from gensim.models import KeyedVectors
import gensim
import traceback
import logging
import os


logging.basicConfig(handlers=[logging.FileHandler(__file__ + '.log', 'w', 'utf-8')], format='%(asctime)s %(levelname)s:%(message)s', level=logging.INFO)

# default boilerplate code
app = Flask(__name__)

# set of active gensim models (learning/relearning possible)
active_models = {}

# set of active gensim vector files (just consumption)
active_vectors = {}


@app.route('/melt_ml.html')
def display_server_status():
    """Can be used to check whether the server is running. Also works in a Web browser.

    Returns
    -------
    str
        A message indicating that the server is running.
    """
    return "MELT ML Server running. Ready to accept requests."


class MySentences(object):
    """Data structure to iterate over the lines of a file in a memory-friendly way.
    """

    def __init__(self, file_name):
        self.file_name = file_name

    def __iter__(self):
        try:
            for line in open(self.file_name, mode='rt', encoding="utf-8"):
                line = line.rstrip('\n')
                words = line.split(" ")
                yield words
        except Exception:
            logging.error("Failed reading file:")
            logging.error(self.file_name)
            logging.exception("Stack Trace:")



@app.route('/train-word2vec', methods=['GET'])
def train_word_2_vec():
    """Method to train a word2vec model given one file to be used for training. Parameters are expected in the request
    header.

    Returns
    -------
        boolean
        'True' as string if operation was successful, else 'False' (as string).
    """
    try:
        model_path = request.headers.get('model_path') # where the model will be stored
        vector_path = request.headers.get('vector_path') # where the vector file will be stored
        file_path = request.headers.get('file_path')
        vector_dimension = request.headers.get('vector_dimension')
        number_of_threads = request.headers.get('number_of_threads')
        window_size = request.headers.get('window_size')
        iterations = request.headers.get('iterations')
        negatives = request.headers.get('negatives')
        cbow_or_sg = request.headers.get('cbow_or_sg')

        sentences = MySentences(file_path)
        logging.info("Sentences object initialized.")

        if cbow_or_sg == 'sg':
            model = gensim.models.Word2Vec(min_count=1, size=int(vector_dimension), workers=int(number_of_threads), window=int(window_size), sg=1, negative=int(negatives), iter=int(iterations))
        else:
            model = gensim.models.Word2Vec(min_count=1, size=int(vector_dimension), workers=int(number_of_threads), window=int(window_size), sg=0, cbow_mean=1, alpha = 0.05, negative=int(negatives), iter=int(iterations))

        logging.info("Model object initialized. Building Vocabulary...")
        model.build_vocab(sentences)
        logging.info("Vocabulary built. Training now...")
        model.train(sentences=sentences, total_examples=model.corpus_count, epochs=model.epochs)
        logging.info("Model trained.")

        model.save(model_path)
        model.wv.save(vector_path)

        active_models[os.path.realpath(model_path)] = model
        active_vectors[os.path.realpath(vector_path)] = model.wv

        return "True"

    except Exception as exception:
        logging.exception("An exception occurred.")
        return "False"



@app.route('/is-in-vocabulary', methods=['GET'])
def is_in_vocabulary():
    """Check whether there is a vector for the given concept.

    Returns
    -------
        boolean
        True if concept in model vocabulary, else False.
    """
    concept = request.headers.get('concept')
    model_path = request.headers.get('model_path')
    vector_path = request.headers.get('vector_path')
    vectors = get_vectors(model_path, vector_path)
    return str(concept in vectors.vocab)


def get_vectors(model_path, vector_path):
    """Will return the gensim vectors given model_path and vector_path where only one variable is filled.
    The Java backend makes sure that the correct variable of the both is filled. This method also handles the
    caching of models and vectors.

    Returns
    -------
        gensim vectors for further operations.
    """
    if vector_path is None:
        if model_path in active_models:
            # logging.info("Found model in cache.")
            model = active_models[model_path]
            vectors = model.wv
        else:
            model = gensim.models.Word2Vec.load(model_path)
            active_models[model_path] = model
            vectors = model.wv
    elif vector_path in active_vectors:
        # logging.info("Found vector file in cache.")
        vectors = active_vectors[vector_path]
    else:
        vectors = KeyedVectors.load(vector_path, mmap='r')
        active_vectors[vector_path] = vectors
    return vectors


@app.route('/get-similarity', methods=['GET'])
def get_similarity_given_model():

    concept_1 = request.headers.get('concept_1')
    concept_2 = request.headers.get('concept_2')
    model_path = request.headers.get('model_path')
    vector_path = request.headers.get("vector_path")
    vectors = get_vectors(model_path=model_path, vector_path=vector_path)

    if vectors is None:
        logging.error("Could not instantiate vectors.")
        return 0.0

    if concept_1 is None or concept_2 is None:
        message = "ERROR! concept_1 and/or concept_2 not found in header. " \
                  "Similarity cannot be calculated."
        print(message)
        logging.error(message)
        return message

    if concept_1 not in vectors.vocab:
        message = "ERROR! concept_1 not in the vocabulary."
        print(message)
        logging.error(message)
        return message
    if concept_2 not in vectors.vocab:
        message = "ERROR! concept_2 not in the vocabulary."
        print(message)
        logging.error(message)
        return message
    similarity = vectors.similarity(concept_1, concept_2)
    return str(similarity)


@app.route('/get-vector', methods=['GET'])
def get_vector_given_model():
    concept = request.headers.get('concept')
    model_path = request.headers.get('model_path')
    vector_path = request.headers.get("vector_path")
    vectors = get_vectors(model_path=model_path, vector_path=vector_path)

    if vectors is None:
        logging.error("Could not instantiate vectors.")
        return 0.0

    if concept is None:
        message = "ERROR! concept not found in header. " \
                  "Vector cannot be retrieved."
        print(message)
        logging.error(message)
        return message

    if concept not in vectors.vocab:
        message = "ERROR! Concept '" + str(concept) + "' not in the vocabulary."
        print(message)
        logging.error(message)
        return message

    result = ""
    for element in vectors.word_vec(concept):
        result += " " + str(element)
    return result[1:]


@app.route('/hello', methods=['GET'])
def hello_demo():
    """A demo program that will return Hello <name> when called.

    Returns
    -------
    greeting : str
        A simple greeting.
    """
    name_to_greet = request.headers.get('name')
    print(name_to_greet)
    return "Hello " + str(name_to_greet) + "!"


if __name__ == "__main__":
    app.run(debug=False, port=41193)
