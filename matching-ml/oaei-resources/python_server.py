from flask import Flask
from flask import request
import gensim

# default boilerplate code
app = Flask(__name__)

# set of active gensim models
active_models = {}


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

    if model_path in active_models:
        model = active_models[model_path]
    else:
        model = gensim.models.Word2Vec.load(model_path)
        active_models[model_path] = model

    return str(concept in model.wv.vocab)


@app.route('/get-similarity', methods=['GET'])
def get_similarity_given_model():
    concept_1 = request.headers.get('concept_1')
    concept_2 = request.headers.get('concept_2')
    model_path = request.headers.get('model_path')
    if concept_1 is None or concept_2 is None or model_path is None:
        message = "ERROR! concept_1 and/or concept_2 and/or model_path not found in header. " \
                  "Similarity cannot be calculated."
        print(message)
        return message
    if model_path in active_models:
        model = active_models[model_path]
    else:
        model = gensim.models.Word2Vec.load(model_path)
        active_models[model_path] = model
    if concept_1 not in model.wv.vocab:
        message = "ERROR! concept_1 not in the vocabulary."
        print(message)
        return message
    if concept_2 not in model.wv.vocab:
        message = "ERROR! concept_2 not in the vocabulary."
        print(message)
        return message
    similarity = model.wv.similarity(concept_1, concept_2)
    return str(similarity)


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
