from flask import Flask
from flask import request
from gensim.models import KeyedVectors
import gensim
import logging


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
            model = active_models[model_path]
            vectors = model.wv
        else:
            model = gensim.models.Word2Vec.load(model_path)
            active_models[model_path] = model
            vectors = model.wv
    elif vector_path in active_vectors:
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
        return message

    if concept_1 not in vectors.vocab:
        message = "ERROR! concept_1 not in the vocabulary."
        print(message)
        return message
    if concept_2 not in vectors.vocab:
        message = "ERROR! concept_2 not in the vocabulary."
        print(message)
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
        message = "ERROR! concept not in the vocabulary."
        logging.error(message)
        print(message)
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
