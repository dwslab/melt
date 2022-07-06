from flask import Flask, request, jsonify
from gensim import corpora, models, similarities, matutils
from scipy import linalg
from scipy.special import softmax
import csv
import json
import numpy as np
import logging
import os
import sys
import pkg_resources
from pkg_resources import DistributionNotFound
import pathlib
import tempfile
import re
from datetime import datetime
from collections import defaultdict

logging.basicConfig(
    handlers=[
        logging.FileHandler(__file__ + ".log", "a+", "utf-8"),
        logging.StreamHandler(sys.stdout),
    ],
    # format="PythonServer: %(asctime)s %(levelname)s:%(message)s",
    format="%(asctime)s %(levelname)-5s ExternalPythonProcess     - %(message)s",
    level=logging.INFO,
)
logging.addLevelName(logging.WARNING, "WARN")
logging.addLevelName(logging.CRITICAL, "FATAL")

# default boilerplate code
app = Flask(__name__)

# set of active gensim models (learning/relearning possible)
active_models = {}

# set of active gensim vector files (just consumption)
active_vectors = {}


@app.route("/melt_ml.html")
def display_server_status():
    """Can be used to check whether the server is running. Also works in a Web browser.

    Returns
    -------
    str
        A message indicating that the server is running.
    """
    return "MELT ML Server running. Ready to accept requests."


@app.route("/check-requirements", methods=["GET"])
def check_requirements() -> str:
    """Can be used to check whether the server is fully functional.

    Returns
    -------
    str
        A message listing installed and potentially missing requirements.
    """
    requirements_file = request.headers.get("requirements_file")
    app.logger.info(f"received requirements file path: {requirements_file}")
    with pathlib.Path(requirements_file).open() as requirements_txt:
        requirements = pkg_resources.parse_requirements(requirements_txt)
        ok_requirements = []
        missing_requirements = []
        for requirement in requirements:
            requirement = str(requirement)
            app.logger.info(f"Checking {requirement}")
            try:
                pkg_resources.require(requirement)
                ok_requirements.append(requirement)
            except Exception as error:
                missing_requirements.append(requirement)
        message = "Dependency Check"
        if len(ok_requirements) > 0:
            message += "\nInstalled Requirements:"
            for r in ok_requirements:
                message += "\n\t" + r
        if len(missing_requirements) > 0:
            message += "\nMissing Requirements:"
            for r in missing_requirements:
                message += "\n\t" + r
        else:
            message += "\n=> Everything is installed. You are good to go!"
        app.logger.info(message)
        return message


class MySentences(object):
    """Data structure to iterate over the lines of a file in a memory-friendly way. The files can be gzipped."""

    def __init__(self, file_or_directory_path):
        """Constructor

        Parameters
        ----------
        file_or_directory_path : str
            The path to the file containing the walks or the path to the file which contains multiple walk files.
        """
        self.file_or_directory_path = file_or_directory_path

    def __iter__(self):
        try:
            if os.path.isdir(self.file_or_directory_path):
                app.logger.info("Directory detected.")
                for file_name in os.listdir(self.file_or_directory_path):
                    app.logger.info("Processing file: " + file_name)
                    if file_name[-2:] in "gz":
                        app.logger.info("Gzip file detected! Using gzip.open().")
                        for line in gzip.open(
                            os.path.join(self.file_or_directory_path, file_name),
                            mode="rt",
                            encoding="utf-8",
                        ):
                            line = line.rstrip("\n")
                            words = line.split(" ")
                            yield words
                    else:
                        for line in open(
                            os.path.join(self.file_or_directory_path, file_name),
                            mode="rt",
                            encoding="utf-8",
                        ):
                            line = line.rstrip("\n")
                            words = line.split(" ")
                            yield words
            else:
                app.logger.info("Processing file: " + self.file_or_directory_path)
                if self.file_or_directory_path[-2:] in "gz":
                    app.logger.info("Gzip file detected! Using gzip.open().")
                    for line in gzip.open(
                        self.file_or_directory_path, mode="rt", encoding="utf-8"
                    ):
                        line = line.rstrip("\n")
                        words = line.split(" ")
                        yield words
                else:
                    for line in open(
                        self.file_or_directory_path, mode="rt", encoding="utf-8"
                    ):
                        line = line.rstrip("\n")
                        words = line.split(" ")
                        yield words
        except Exception:
            app.logger.error("Failed reading file:")
            app.logger.error(self.file_or_directory_path)
            app.logger.exception("Stack Trace:")


@app.route("/get-vocabulary-size", methods=["GET"])
def get_vocab_size():
    model_path = request.headers.get("model_path")
    vector_path = request.headers.get("vector_path")
    vectors = get_vectors(model_path, vector_path)
    return str(len(vectors.key_to_index))


@app.route("/train-word2vec", methods=["GET"])
def train_word_2_vec():
    """Method to train a word2vec model given one file to be used for training. Parameters are expected in the request
    header.

    Returns
    -------
        boolean
        'True' as string if operation was successful, else 'False' (as string).
    """
    try:
        model_path = request.headers.get("model_path")  # where the model will be stored
        vector_path = request.headers.get(
            "vector_path"
        )  # where the vector file will be stored
        file_path = request.headers.get("file_path")
        vector_dimension = request.headers.get("vector_dimension")
        number_of_threads = request.headers.get("number_of_threads")
        window_size = request.headers.get("window_size")
        iterations = request.headers.get("iterations")
        negatives = request.headers.get("negatives")
        cbow_or_sg = request.headers.get("cbow_or_sg")
        min_count = request.headers.get("min_count")
        sample = request.headers.get("sample")
        epochs = request.headers.get("epochs")

        sentences = MySentences(file_path)
        app.logger.info("Sentences object initialized.")

        if cbow_or_sg == "sg":
            model = models.Word2Vec(
                min_count=int(min_count),
                sample=float(sample),
                vector_size=int(vector_dimension),
                workers=int(number_of_threads),
                window=int(window_size),
                sg=1,
                negative=int(negatives),
                epochs=int(iterations),
            )
        else:
            model = models.Word2Vec(
                min_count=int(min_count),
                sample=float(sample),
                vector_size=int(vector_dimension),
                workers=int(number_of_threads),
                window=int(window_size),
                sg=0,
                cbow_mean=1,
                alpha=0.05,
                negative=int(negatives),
                epochs=int(iterations),
            )

        app.logger.info("Model object initialized. Building Vocabulary...")
        model.build_vocab(corpus_iterable=sentences)
        app.logger.info("Vocabulary built. Training now...")
        model.train(
            corpus_iterable=sentences, total_examples=model.corpus_count, epochs=int(epochs)
        )
        app.logger.info("Model trained.")

        model.save(model_path)
        model.wv.save(vector_path)

        active_models[os.path.realpath(model_path)] = model
        active_vectors[os.path.realpath(vector_path)] = model.wv

        return "True"

    except Exception as exception:
        app.logger.exception("An exception occurred.")
        return "False"


@app.route("/is-in-vocabulary", methods=["GET"])
def is_in_vocabulary():
    """Check whether there is a vector for the given concept.

    Returns
    -------
        boolean
        True if concept in model vocabulary, else False.
    """
    concept = request.headers.get("concept")
    model_path = request.headers.get("model_path")
    vector_path = request.headers.get("vector_path")
    vectors = get_vectors(model_path, vector_path)
    return str(concept in vectors.key_to_index)


@app.route("/get-vocabulary-terms", methods=["GET"])
def get_vocabulary_terms():
    model_path = request.headers.get("model_path")
    vector_path = request.headers.get("vector_path")
    vectors = get_vectors(model_path, vector_path)
    result = ""
    for word in vectors.key_to_index:
        result += word + "\n"
    return result


def get_vectors(model_path=None, vector_path=None):
    """Will return the gensim vectors given model_path and vector_path where only one variable is filled.
    The Java backend makes sure that the correct variable of the both is filled. This method also handles the
    caching of models and vectors.

    Returns
    -------
        gensim vectors for further operations.
    """
    if vector_path is None:
        if model_path in active_models:
            # app.logger.info("Found model in cache.")
            model = active_models[model_path]
            vectors = model.wv
        else:
            model = models.Word2Vec.load(model_path)
            active_models[model_path] = model
            vectors = model.wv
    elif vector_path in active_vectors:
        # app.logger.info("Found vector file in cache.")
        vectors = active_vectors[vector_path]
    else:
        vectors = models.KeyedVectors.load(vector_path, mmap="r")
        active_vectors[vector_path] = vectors
    return vectors


@app.route("/get-similarity", methods=["GET"])
def get_similarity_given_model():

    concept_1 = request.headers.get("concept_1")
    concept_2 = request.headers.get("concept_2")
    model_path = request.headers.get("model_path")
    vector_path = request.headers.get("vector_path")
    vectors = get_vectors(model_path=model_path, vector_path=vector_path)

    if vectors is None:
        app.logger.error("Could not instantiate vectors.")
        return 0.0

    if concept_1 is None or concept_2 is None:
        message = (
            "ERROR! concept_1 and/or concept_2 not found in header. "
            "Similarity cannot be calculated."
        )
        app.logger.error(message)
        return message

    if concept_1 not in vectors.key_to_index:
        message = "ERROR! concept_1 not in the vocabulary."
        app.logger.error(message)
        return message
    if concept_2 not in vectors.key_to_index:
        message = "ERROR! concept_2 not in the vocabulary."
        app.logger.error(message)
        return message
    similarity = vectors.similarity(concept_1, concept_2)
    return str(similarity)


@app.route("/get-vector", methods=["GET"])
def get_vector_given_model():
    concept = request.headers.get("concept")
    model_path = request.headers.get("model_path")
    vector_path = request.headers.get("vector_path")
    vectors = get_vectors(model_path=model_path, vector_path=vector_path)

    if vectors is None:
        app.logger.error("Could not instantiate vectors.")
        return 0.0

    if concept is None:
        message = "ERROR! concept not found in header. " "Vector cannot be retrieved."
        app.logger.error(message)
        return message

    if concept not in vectors.key_to_index:
        message = "ERROR! Concept '" + str(concept) + "' not in the vocabulary."
        app.logger.error(message)
        return message

    result = ""
    for element in vectors.word_vec(concept):
        result += " " + str(element)
    return result[1:]


# Doc2vec models


class Doc2VecCsvCorpus(object):
    def __init__(self, file_path):
        self.file_path = file_path

    def __iter__(self):
        with open(self.file_path, encoding="utf-8") as csvfile:
            readCSV = csv.reader(csvfile, delimiter=",")
            for i, row in enumerate(readCSV):
                yield models.doc2vec.TaggedDocument(row[1].split(), [row[0]])


@app.route("/train-doc2vec-model", methods=["GET"])
def train_doc2vec_model():
    input_file_path = request.headers.get("input_file_path")
    model_path = request.headers.get("model_path")

    vector_dimension = request.headers.get("vector_dimension")
    min_count = request.headers.get("min_count")
    number_of_threads = request.headers.get("number_of_threads")
    window_size = request.headers.get("window_size")
    iterations = request.headers.get("iterations")
    negatives = request.headers.get("negatives")
    cbow_or_sg = request.headers.get("cbow_or_sg")

    corpus = Doc2VecCsvCorpus(input_file_path)
    dm = 1 if cbow_or_sg == "sg" else 0

    # train the model:
    model = models.doc2vec.Doc2Vec(
        documents=corpus,
        dm=dm,
        min_count=int(min_count),
        vector_size=int(vector_dimension),
        workers=int(number_of_threads),
        window=int(window_size),
        negative=int(negatives),
        epochs=int(iterations),
    )

    # model.save(model_path + '.model')
    active_models[model_path] = model
    return "True"


@app.route("/query-doc2vec-model-batch", methods=["POST"])
def query_doc2vec_model_batch():
    try:
        content = request.get_json()
        model = active_models.get(content["modelPath"])
        if model is None:
            return "ERROR! Model not active"
        result_list = []
        for (source, target) in content["documentIds"]:
            # app.logger.info("processing: %s and %s", source, target)
            try:
                doc2vec_similarity = float(model.docvecs.similarity(source, target))
                result_list.append(doc2vec_similarity)
            except KeyError as e:
                result_list.append(-2.0)
        return jsonify(result_list)
    except Exception as e:
        return str(e)


# TF-IDF and LSI models


@app.route("/train-vector-space-model", methods=["GET"])
def train_vector_space_model():
    input_file_path = request.headers.get("input_file_path")
    model_path = request.headers.get("model_path")

    dictionary = __createDictionary(input_file_path)
    corpus = CsvCorpus(dictionary, input_file_path)
    tfidf = models.TfidfModel(dictionary=dictionary)
    tfidf_corpus = tfidf[corpus]

    index = similarities.Similarity(
        "index.index", tfidf_corpus, num_features=len(dictionary)
    )
    # index = similarities.SparseMatrixSimilarity(tfidf_corpus, num_features=len(dictionary))
    # index = similarities.MatrixSimilarity(tfidf_corpus, num_features=len(dictionary))
    active_models[model_path] = (corpus, index)
    return "True"


@app.route("/query-vector-space-model", methods=["GET"])
def query_vector_space_model():
    try:
        model_path = request.headers.get("model_path")
        document_id_one = request.headers.get("document_id_one")
        document_id_two = request.headers.get("document_id_two")  # can be None

        model = active_models.get(model_path)
        if model is None:
            return "ERROR! Model not active"
        (corpus, index) = model

        pos_one = corpus.id2pos.get(document_id_one)
        if pos_one is None:
            return "ERROR! document_id_one not in the vocabulary."
        sims = index.similarity_by_id(pos_one)

        if document_id_two is None:
            return __sims2scores(sims, corpus.pos2id, 10)
        else:
            pos_two = corpus.id2pos.get(document_id_two)
            if pos_two is None:
                return "ERROR! document_id_two not in the vocabulary."
            test = sims[pos_two]
            return str(test)
    except Exception as e:
        return str(e)


@app.route("/query-vector-space-model-batch", methods=["POST"])
def query_vector_space_model_batch():
    try:
        content = request.get_json()

        model = active_models.get(content["modelPath"])
        if model is None:
            return "ERROR! Model not active"
        (corpus, index) = model

        result_list = []
        for (source, target) in content["documentIds"]:
            # app.logger.info("processing: %s and %s", source, target)
            source_position = corpus.id2pos.get(source)
            target_position = corpus.id2pos.get(target)
            # app.logger.info("pos: %s and %s", source_position, target_position)
            if source_position is None or target_position is None:
                result_list.append(-2.0)
                continue

            # first variant - very slow:
            # sims = index.similarity_by_id(source_position)
            # resulting_sim = sims[target_position]

            # second variant with scikit learn
            # from sklearn.metrics.pairwise import cosine_similarity
            # vec_one = index.vector_by_id(source_position)
            # vec_two = index.vector_by_id(target_position)
            # resulting_sim = cosine_similarity(vec_one, vec_two)

            # third variant - best runtime
            vec_one = matutils.scipy2sparse(
                index.vector_by_id(corpus.id2pos.get(source))
            )
            vec_two = matutils.scipy2sparse(
                index.vector_by_id(corpus.id2pos.get(target))
            )
            resulting_sim = matutils.cossim(vec_one, vec_two)

            result_list.append(resulting_sim)
        return jsonify(result_list)
    except Exception as e:
        return str(e)

@app.route("/run-group-shuffle-split", methods=["POST"])
def run_shuffle_split():
    try:
        content = request.get_json()

        import numpy as np
        from sklearn.model_selection import GroupShuffleSplit

        groups = np.array(content["groups"])
        X = np.ones(shape=(len(groups), 2))
        gss = GroupShuffleSplit(n_splits=1, train_size=content["trainSize"], random_state=42)
        for train_idx, test_idx in gss.split(X, groups=groups):
            return jsonify(train_idx.tolist())
        return "True"
    except Exception as e:
        import traceback
        return "ERROR " + traceback.format_exc()

english_stopwords = {
    "has",
    "mightn",
    "me",
    "here",
    "other",
    "very",
    "but",
    "ours",
    "he",
    "his",
    "there",
    "you",
    "some",
    "don",
    "such",
    "under",
    "their",
    "themselves",
    "mustn't",
    "had",
    "shan't",
    "she's",
    "yourselves",
    "by",
    "about",
    "needn",
    "re",
    "weren't",
    "any",
    "herself",
    "don't",
    "am",
    "hadn",
    "what",
    "each",
    "weren",
    "hadn't",
    "between",
    "both",
    "in",
    "can",
    "the",
    "does",
    "too",
    "shouldn",
    "once",
    "when",
    "s",
    "it",
    "as",
    "same",
    "haven",
    "hasn't",
    "didn't",
    "wasn't",
    "on",
    "shan",
    "they",
    "of",
    "was",
    "aren't",
    "out",
    "before",
    "our",
    "aren",
    "ourselves",
    "wouldn",
    "we",
    "didn",
    "having",
    "above",
    "just",
    "below",
    "why",
    "against",
    "wouldn't",
    "were",
    "yours",
    "few",
    "m",
    "doesn",
    "my",
    "nor",
    "then",
    "you'll",
    "your",
    "isn't",
    "haven't",
    "him",
    "doesn't",
    "i",
    "wasn",
    "who",
    "will",
    "that'll",
    "if",
    "hasn",
    "been",
    "myself",
    "d",
    "where",
    "into",
    "t",
    "ain",
    "couldn't",
    "being",
    "how",
    "y",
    "which",
    "you've",
    "an",
    "or",
    "from",
    "no",
    "ma",
    "doing",
    "through",
    "all",
    "most",
    "theirs",
    "than",
    "are",
    "to",
    "while",
    "shouldn't",
    "that",
    "so",
    "and",
    "only",
    "until",
    "ve",
    "isn",
    "should",
    "her",
    "yourself",
    "have",
    "over",
    "because",
    "you'd",
    "be",
    "more",
    "a",
    "himself",
    "those",
    "these",
    "not",
    "its",
    "own",
    "for",
    "she",
    "down",
    "hers",
    "you're",
    "whom",
    "after",
    "this",
    "at",
    "do",
    "ll",
    "it's",
    "up",
    "couldn",
    "with",
    "itself",
    "again",
    "off",
    "is",
    "during",
    "further",
    "mustn",
    "won",
    "did",
    "mightn't",
    "needn't",
    "should've",
    "them",
    "now",
    "o",
    "won't",
}


def __createDictionary(file_path, stopwords=english_stopwords):
    with open(file_path, encoding="utf-8") as f:
        # collect statistics about all tokens
        readCSV = csv.reader(f, delimiter=",")
        dictionary = corpora.Dictionary(line[1].lower().split() for line in readCSV)
    # remove stop words and words that appear only once
    stop_ids = [
        dictionary.token2id[stopword]
        for stopword in stopwords
        if stopword in dictionary.token2id
    ]
    once_ids = [tokenid for tokenid, docfreq in dictionary.dfs.items() if docfreq == 1]
    dictionary.filter_tokens(
        stop_ids + once_ids
    )  # remove stop words and words that appear only once
    dictionary.compactify()  # remove gaps in id sequence after words that were removed
    return dictionary


def __sims2scores(sims, pos2id, topsims, eps=1e-7):
    """Convert raw similarity vector to a list of (docid, similarity) results."""
    result = []
    sims = abs(
        sims
    )
    for pos in np.argsort(sims)[::-1]:
        if pos in pos2id and sims[pos] > eps:  # ignore deleted/rewritten documents
            # convert positions of resulting docs back to ids
            result.append((pos2id[pos], sims[pos]))
            if len(result) == topsims:
                break
    return result


class CsvCorpus(object):
    def __init__(self, dictionary, file_path):
        self.dictionary = dictionary
        self.file_path = file_path
        self.id2pos = {}  # map document id (string) to index position (integer)
        self.pos2id = {}  # map index position (integer) to document id (string)

    def __iter__(self):
        with open(self.file_path, encoding="utf-8") as csvfile:
            readCSV = csv.reader(csvfile, delimiter=",")
            for i, row in enumerate(readCSV):
                if row[0] in self.id2pos:
                    app.logger.info(
                        "Document ID %s already in file - the last one is used only",
                        row[0],
                    )
                self.id2pos[row[0]] = i
                self.pos2id[i] = row[0]
                yield self.dictionary.doc2bow(row[1].lower().split())


@app.route("/write-model-as-text-file", methods=["GET"])
def write_vectors_as_text_file():
    """
    Writes all vectors of the model to a text file: one vector per line

    Returns
    -------
    boolean
        'True' as string if operation was successful, else 'False' (as string).
    """
    model_path = request.headers.get("model_path")
    vector_path = request.headers.get("vector_path")
    file_to_write = request.headers.get("file_to_write")
    entity_file = request.headers.get("entity_file")
    vectors = get_vectors(model_path=model_path, vector_path=vector_path)
    final_string = ""
    if entity_file is None:
        for concept in vectors.key_to_index:
            if concept in vectors.key_to_index:
                vector = vectors.get_vector(concept)
                final_string += concept + " "
                for element in np.nditer(vector):
                    final_string += str(element) + " "
            else:
                app.logger.info(
                    "WARN: The following concept has not been found in the vector space: "
                    + concept
                )
            final_string += "\n"
        # write final string to file
    else:
        concepts = read_concept_file(entity_file)
        for concept in concepts:
            if concept in vectors.key_to_index:
                vector = vectors.get_vector(concept)
                final_string += concept + " "
                for element in np.nditer(vector):
                    final_string += str(element) + " "
            else:
                app.logger.info(
                    "WARN: The following concept has not been found in the vector space: "
                    + concept
                )
                app.logger.info("Trying to resolve new URI.")
            final_string += "\n"
    with open(file_to_write, "w+") as f:
        f.write(final_string)
    return "True"


def read_concept_file(path_to_concept_file):
    result = []
    with open(path_to_concept_file, errors="ignore") as concept_file:
        for lemma in concept_file:
            lemma = lemma.replace("\n", "").replace("\r", "")
            result.append(lemma)
    app.logger.info("Concept file read: " + str(path_to_concept_file))
    return result


############################################
#          Align Embeddings
############################################


@app.route("/align-embeddings", methods=["POST"])
def align_embeddings():
    try:
        content = request.get_json()

        source_vectors = get_vectors(vector_path=content["vectorPathSource"])
        target_vectors = get_vectors(vector_path=content["vectorPathTarget"])
        inputAlignment = content["alignment"]
        function = content["function"]

        if function == "linear_projection":
            projected_source, projected_target = linear_projection(
                source_vectors, target_vectors, inputAlignment
            )
        elif function == "neural_net_projection":
            projected_source, projected_target = neural_net_projection(
                source_vectors, target_vectors, inputAlignment
            )
        elif function == "cca_projection":
            projected_source, projected_target = cca_projection(
                source_vectors, target_vectors, inputAlignment
            )
        elif function == "analyze":
            analyze(source_vectors, target_vectors, inputAlignment)
            return "[]"
        else:
            return "ERROR Function not available"

        results = []
        for source_uri in projected_source.key_to_index:
            most_similar_target = projected_target.most_similar(
                positive=[projected_source[source_uri]], topn=1
            )[0]
            results.append((source_uri, most_similar_target[0], most_similar_target[1]))
        return jsonify(results)
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


def __normr(arr):
    return arr / linalg.norm(arr, axis=1, ord=2, keepdims=True)


def __canoncorr(X, Y):
    # Based on sourceforge.net/p/octave/statistics/ci/release-1.4.0/tree/inst/canoncorr.m

    # sio.savemat('np_vector.mat', {'X': X, 'Y': Y})
    # additional constraint because otherwise line ' A = linalg.solve(Rx, U[:, :d]) ' does not work
    assert (
        X.shape[0] > X.shape[1] and Y.shape[0] > Y.shape[1]
    ), "Vector dimension must be greater than trainings lexicon - maybe decrease vector size."

    k = X.shape[0]
    m = X.shape[1]
    n = Y.shape[1]
    d = min(m, n)

    assert X.shape[0] == Y.shape[0]  # both array should have same number of rows

    X = X - X.mean(axis=0, keepdims=True)  # center X = remove mean
    Y = Y - Y.mean(axis=0, keepdims=True)  # center Y = remove mean

    Qx, Rx = linalg.qr(X, mode="economic")
    Qy, Ry = linalg.qr(Y, mode="economic")

    U, S, V = linalg.svd(
        Qx.T.dot(Qy), full_matrices=False
    )  # full_matrices=False should correspind to svd(...,0)   #, lapack_driver='gesvd'
    V = V.T  # because svd returns transposed V (called Vh)

    A = linalg.solve(Rx, U[:, :d])
    B = linalg.solve(Ry, V[:, :d])

    f = np.sqrt(k - 1)
    A = np.multiply(A, f)
    B = np.multiply(B, f)

    return A, B


def __project_embeddings_to_lexicon_subset(
    word_vector_source, word_vector_target, lexicon
):
    source_subset_vectors = []
    target_subset_vectors = []
    for lang_source_word, lang_target_word in lexicon:
        if (
            lang_source_word not in word_vector_source
            or lang_target_word not in word_vector_target
        ):
            continue
        source_subset_vectors.append(word_vector_source[lang_source_word])
        target_subset_vectors.append(word_vector_target[lang_target_word])
    return np.array(source_subset_vectors), np.array(target_subset_vectors)


def __create_keyed_vector(old_keyed_vector, new_matrix):
    vector_size = new_matrix.shape[1]
    keyed_vector = models.KeyedVectors(vector_size)
    keyed_vector.vector_size = vector_size
    keyed_vector.vocab = old_keyed_vector.vocab
    keyed_vector.index2word = old_keyed_vector.index2word
    keyed_vector.vectors = new_matrix
    assert (len(old_keyed_vector.vocab), vector_size) == keyed_vector.vectors.shape
    return keyed_vector


def analyze(word_vector_src, word_vector_tgt, lexicon):
    from sklearn.decomposition import PCA
    import matplotlib.pyplot as plt

    matrix_src, matrix_tgt = __project_embeddings_to_lexicon_subset(
        word_vector_src, word_vector_tgt, lexicon
    )
    matrix_diff = matrix_src - matrix_tgt

    diff_vector_list = []
    for src, dst in lexicon:
        if src in word_vector_src and dst in word_vector_tgt:
            diff_vector_list.append(word_vector_src[src] - word_vector_tgt[dst])
    t = np.array(diff_vector_list)
    app.logger.info(matrix_diff)
    app.logger.info(t)

    principalComponents = PCA(n_components=2).fit_transform(matrix_diff)

    plt.scatter(principalComponents[:, 0], principalComponents[:, 1])
    for i in range(principalComponents.shape[0]):
        # get text after last slash or hashtag in source uri
        source_fragment = lexicon[i][0].rsplit("/", 1)[-1].rsplit("#", 1)[-1]
        plt.annotate(
            source_fragment, (principalComponents[i, 0], principalComponents[i, 1])
        )
    plt.savefig("analyze.png")


def linear_projection(word_vector_src, word_vector_tgt, lexicon):
    matrix_src, matrix_tgt = __project_embeddings_to_lexicon_subset(
        word_vector_src, word_vector_tgt, lexicon
    )
    if matrix_src.size == 0 or matrix_tgt.size == 0:
        raise Exception(
            "The embeddings do not contain enough vectors for the input alignment."
        )

    x_mpi = linalg.pinv(matrix_src)  # Moore Penrose Pseudoinverse
    w = np.dot(x_mpi, matrix_tgt)  # linear map matrix W

    source_projected = __create_keyed_vector(
        word_vector_src, np.dot(word_vector_src.vectors, w)
    )
    return source_projected, word_vector_tgt


def neural_net_projection(word_vector_src, word_vector_tgt, lexicon):
    from keras.models import Sequential
    from keras.layers import Dense, Dropout
    from keras.optimizers import SGD
    from keras import losses

    matrix_src, matrix_tgt = __project_embeddings_to_lexicon_subset(
        word_vector_src, word_vector_tgt, lexicon
    )
    if matrix_src.size == 0 or matrix_tgt.size == 0:
        raise Exception(
            "The embeddings do not contain enough vector for the input alignment."
        )

    model = Sequential()
    model.add(
        Dense(
            word_vector_src.vector_size,
            input_dim=word_vector_src.vector_size,
            activation="relu",
        )
    )
    model.add(Dropout(0.5))
    model.add(
        Dense(
            word_vector_src.vector_size,
            input_dim=word_vector_src.vector_size,
            activation="relu",
        )
    )
    model.add(Dropout(0.5))
    model.add(
        Dense(
            word_vector_src.vector_size,
            input_dim=word_vector_src.vector_size,
            activation="relu",
        )
    )

    sgd = SGD(lr=0.01, decay=1e-6, momentum=0.9, nesterov=True)
    model.compile(
        loss=losses.mean_squared_error,
        optimizer=sgd,
        metrics=[losses.mean_squared_error],
    )

    model.fit(matrix_src, matrix_tgt, epochs=2000, batch_size=128)

    source_projected = model.predict(word_vector_src.vectors)
    source_projected_keyed_vector = __create_keyed_vector(
        word_vector_src, source_projected
    )
    return source_projected_keyed_vector, word_vector_tgt


def cca_projection(
    word_vector_source, word_vector_target, lexicon, top_correlation_ratio=0.5
):
    word_vector_source.init_sims(replace=True)
    word_vector_target.init_sims(replace=True)

    source_subset, target_subset = __project_embeddings_to_lexicon_subset(
        word_vector_source, word_vector_target, lexicon
    )
    if source_subset.size == 0 or target_subset.size == 0:
        raise Exception(
            "the embeddings do not contain enough vector for the input alignment"
        )

    A, B = __canoncorr(target_subset, source_subset)

    amount_A = int(np.ceil(top_correlation_ratio * A.shape[1]))
    U = (
        word_vector_target.vectors
        - word_vector_target.vectors.mean(axis=0, keepdims=True)
    ).dot(A[:, 0:amount_A])
    U = __normr(U)
    projected_target_vectors = __create_keyed_vector(word_vector_target, U)

    amount_B = int(np.ceil(top_correlation_ratio * B.shape[1]))
    V = (
        word_vector_source.vectors
        - word_vector_source.vectors.mean(axis=0, keepdims=True)
    ).dot(B[:, 0:amount_B])
    V = __normr(V)
    projected_source_vectors = __create_keyed_vector(word_vector_source, V)

    return projected_source_vectors, projected_target_vectors


############################################
#          Machine LEARNING
############################################


@app.route("/machine-learning", methods=["GET"])
def machine_learning() -> str:
    import pandas as pd

    try:
        cv = int(request.headers.get("cv"))
        n_jobs = int(request.headers.get("jobs"))
        train_df = pd.read_csv(request.headers.get("trainingsFile"))
        model = run_grid_search(train_df, cv, n_jobs)

        X_test = pd.read_csv(request.headers.get("predictFile"))
        y_predict = model.predict(X_test)
        return jsonify(y_predict.tolist())
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


@app.route("/ml-train-and-store-model", methods=["GET"])
def ml_train_and_store_model():
    import pandas as pd

    try:
        cv = int(request.headers.get("cv"))
        n_jobs = int(request.headers.get("jobs"))
        train_df = pd.read_csv(request.headers.get("trainingsFile"))
        model = run_grid_search(train_df, cv, n_jobs)
        from joblib import dump

        dump(model, request.headers.get("modelFile"))
        return "True"
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


# cache for machine learning models
ml_cache = {}


@app.route("/ml-load-and-apply-model", methods=["GET"])
def ml_load_and_apply_model():
    import pandas as pd

    try:
        model_file = request.headers.get("modelFile")
        if model_file in ml_cache:
            model = ml_cache[model_file]
        else:
            from joblib import load

            model = load(model_file)
            ml_cache[model_file] = model

        X_test = pd.read_csv(request.headers.get("predictFile"))
        y_predict = model.predict(X_test)
        return jsonify(y_predict.tolist())
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


def run_grid_search(df_train, cv, n_jobs):
    try:
        from sklearn import preprocessing, svm, tree
        from sklearn.naive_bayes import GaussianNB
        from sklearn.model_selection import GridSearchCV
        from sklearn.pipeline import Pipeline
        from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
        from sklearn.neural_network import MLPClassifier
        import math

        y_train = df_train["target"]
        X_train = df_train.drop(columns=["target"])

        number_of_classes = len(np.unique(y_train))
        number_of_attributes = int(X_train.shape[1])
        default_layer_size = int(
            round(((number_of_attributes + number_of_classes) / 2) + 1)
        )
        sqrt_layer_size = int(
            round(math.sqrt(number_of_attributes + number_of_classes))
        )

        random_state = 42

        params_grid = [
            {
                "estimator": [svm.SVC()],
                "estimator__random_state": [random_state],
                "estimator__C": [
                    2 ** -5,
                    2 ** -3,
                    2 ** -1,
                    2 ** 1,
                    2 ** 3,
                    2 ** 5,
                    2 ** 7,
                    2 ** 9,
                    2 ** 11,
                    2 ** 13,
                    2 ** 15,
                ],
                "estimator__gamma": [
                    2 ** -15,
                    2 ** -13,
                    2 ** -11,
                    2 ** -9,
                    2 ** -7,
                    2 ** -5,
                    2 ** -3,
                    2 ** -1,
                    2 ** 1,
                    2 ** 3,
                ],
                "scaler": [None, preprocessing.MinMaxScaler()],
            },
            {
                "estimator": [GaussianNB()],
                "scaler": [None, preprocessing.MinMaxScaler()],
            },
            {
                "estimator": [tree.DecisionTreeClassifier()],
                "estimator__random_state": [random_state],
                "estimator__max_depth": list(range(1, 20, 1)),
                "estimator__min_samples_leaf": list(range(1, 20, 1)),
                "scaler": [None, preprocessing.MinMaxScaler()],
            },
            {
                "estimator": [RandomForestClassifier()],
                "estimator__random_state": [random_state],
                "estimator__n_estimators": list(range(1, 100, 10)),
                "estimator__min_samples_leaf": list(range(1, 10, 1)),
                "scaler": [None, preprocessing.MinMaxScaler()],
            },
            {
                "estimator": [GradientBoostingClassifier()],
                "estimator__random_state": [random_state],
                "estimator__n_estimators": list(range(1, 102, 20)),
                "estimator__max_depth": list(range(1, 22, 5)),
                "scaler": [None, preprocessing.MinMaxScaler()],
            },
            {
                "estimator": [MLPClassifier()],
                "estimator__random_state": [random_state],
                "estimator__solver": ["lbfgs"],
                "estimator__hidden_layer_sizes": [
                    (default_layer_size),
                    (default_layer_size, sqrt_layer_size),
                    (sqrt_layer_size),
                ],
                "scaler": [None, preprocessing.MinMaxScaler()],
            },
        ]

        app.logger.info("Run grid search with cv: %s and jobs: %s" % (cv, n_jobs))
        grid = GridSearchCV(
            Pipeline(
                [("scaler", preprocessing.MaxAbsScaler()), ("estimator", svm.SVC())]
            ),
            param_grid=params_grid,
            scoring="f1",
            cv=cv,
            n_jobs=n_jobs,
            refit=True,
            verbose=1,
        )
        grid.fit(X_train, y_train)

        app.logger.info("cross validation: best f1 score: %s", grid.best_score_)
        app.logger.info("cross validation: chosen model: %s", grid.best_params_)

        return grid.best_estimator_
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


@app.route("/run-openea", methods=["GET"])
def run_openea():
    try:
        from openea.modules.args.args_hander import load_args
        from openea.modules.load.kgs import read_kgs_from_folder
        from openea.models.trans import TransD, TransE, TransH, TransR
        from openea.models.semantic import DistMult, HolE, SimplE, RotatE
        from openea.models.neural import ConvE, ProjE
        from openea.approaches import (
            AlignE,
            BootEA,
            JAPE,
            Attr2Vec,
            MTransE,
            IPTransE,
            GCN_Align,
            AttrE,
            IMUSE,
            SEA,
            MultiKE,
            RSN4EA,
            GMNN,
            KDCoE,
            RDGCN,
            BootEA_RotatE,
            BootEA_TransH,
            AliNet,
        )
        from openea.models.basic_model import BasicModel

        models = {
            "TransD": TransD,
            "TransE": TransE,
            "TransH": TransH,
            "TransR": TransR,  # openea.models.trans
            "DistMult": DistMult,
            "HolE": HolE,
            "SimplE": SimplE,
            "RotatE": RotatE,  # openea.models.semantic
            "ConvE": ConvE,
            "ProjE": ProjE,  # openea.models.neural
            "AlignE": AlignE,
            "BootEA": BootEA,
            "JAPE": JAPE,
            "Attr2Vec": Attr2Vec,  # openea.approaches
            "MTransE": MTransE,
            "IPTransE": IPTransE,
            "GCN_Align": GCN_Align,
            "AttrE": AttrE,
            "IMUSE": IMUSE,
            "SEA": SEA,
            "MultiKE": MultiKE,
            "RSN4EA": RSN4EA,
            "GMNN": GMNN,
            "KDCoE": KDCoE,
            "RDGCN": RDGCN,
            "BootEA_RotatE": BootEA_RotatE,
            "BootEA_TransH": BootEA_TransH,
            "AliNet": AliNet,
            "BasicModel": BasicModel,
        }  # openea.models.basic_model

        args = load_args(request.headers.get("argumentFile"))
        kgs = read_kgs_from_folder(
            args.training_data,
            args.dataset_division,
            args.alignment_module,
            args.ordered,
            remove_unlinked=False,
        )
        model = models[args.embedding_module]()
        model.set_args(args)
        model.set_kgs(kgs)
        model.init()
        model.run()

        model.out_folder = args.output  # do not use folder hierarchy with datetime
        model.predict(
            top_k=getattr(args, "predict_top_k", None),
            min_sim_value=getattr(args, "predict_min_sim_value", None),
            output_file_name="topk.tsv",
        )
        # model.test()
        if "save" in request.headers:
            model.save()
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


############################################
# Transformers section with helper functions
############################################


def transformers_create_dataset(
    using_tensorflow, tokenizer, left_sentences, right_sentences, labels=None
):
    tensor_type = "tf" if using_tensorflow else "pt"
    # padding (padding=True) is not applied here because the tokenizer is given to the trainer
    # which does the padding for each batch (more efficient)
    encodings = tokenizer(
        left_sentences,
        right_sentences,
        return_tensors=tensor_type,
        padding=True,
        truncation="longest_first",
    )

    if using_tensorflow:
        import tensorflow as tf

        if labels:
            return tf.data.Dataset.from_tensor_slices((dict(encodings), labels))
        else:
            return tf.data.Dataset.from_tensor_slices(dict(encodings))
    else:
        import torch

        if labels:

            class MyDatasetWithLabels(torch.utils.data.Dataset):
                def __init__(self, encodings, labels):
                    self.encodings = encodings
                    self.labels = labels

                def __getitem__(self, idx):
                    item = {
                        key: val[idx].detach().clone()
                        for key, val in self.encodings.items()
                    }
                    item["labels"] = torch.tensor(self.labels[idx])
                    return item

                def __len__(self):
                    return len(self.labels)

            return MyDatasetWithLabels(encodings, labels)
        else:

            class MyDataset(torch.utils.data.Dataset):
                def __init__(self, encodings):
                    self.encodings = encodings

                def __getitem__(self, idx):
                    item = {
                        key: val[idx].detach().clone()
                        for key, val in self.encodings.items()
                    }
                    return item

                def __len__(self):
                    return len(self.encodings.input_ids)

            return MyDataset(encodings)


def transformers_read_file(file_path, with_labels):
    data_left = []
    data_right = []
    labels = []
    with open(file_path, encoding="utf-8") as csvfile:
        readCSV = csv.reader(csvfile, delimiter=",")
        for row in readCSV:
            data_left.append(row[0])
            data_right.append(row[1])
            if with_labels:
                labels.append(int(row[2]))
    return data_left, data_right, labels


def transformers_get_training_arguments(
    using_tensorflow, initial_parameters, user_parameters, melt_parameters
):
    import dataclasses

    if using_tensorflow:
        from transformers import TFTrainingArguments

        allowed_arguments = set(
            [field.name for field in dataclasses.fields(TFTrainingArguments)]
        )
    else:
        from transformers import TrainingArguments

        allowed_arguments = set(
            [field.name for field in dataclasses.fields(TrainingArguments)]
        )

    training_arguments = dict(initial_parameters)
    training_arguments.update(user_parameters)
    training_arguments.update(melt_parameters)

    not_available = training_arguments.keys() - allowed_arguments
    if len(not_available) > 0:
        app.logger.warning(
            "The following attributes are not set as training arguments because "
            + "they do not exist in the currently installed version of transformer: "
            + str(not_available)
        )
        for key_not_avail in not_available:
            del training_arguments[key_not_avail]
    if using_tensorflow:
        training_args = TFTrainingArguments(**training_arguments)
    else:
        training_args = TrainingArguments(**training_arguments)
    return training_args


def transformers_search_folder_with_highest_count(root_folder, count_regex):
    highest_step = 0
    highest_step_folder = ""
    for item in os.listdir(root_folder):

        item_path = os.path.join(root_folder, item)
        if os.path.isdir(item_path):
            checkpoint_search = re.search(count_regex, item)
            if checkpoint_search:
                checkpoint_step = int(checkpoint_search.group(1))
                if highest_step <= checkpoint_step:
                    highest_step = checkpoint_step
                    highest_step_folder = item_path
    return highest_step_folder


def transformers_init(request_headers):
    if "cuda-visible-devices" in request_headers:
        os.environ["CUDA_VISIBLE_DEVICES"] = request_headers["cuda-visible-devices"]

    if "transformers-cache" in request_headers:
        os.environ["TRANSFORMERS_CACHE"] = request_headers["transformers-cache"]


# needs to be at the top level because only top level function can be pickled
def multi_process_wrapper_function(queue, func, argument):
    queue.put(func(argument))


def run_function_multi_process(request, func):
    multi_processing = request.headers["multi-processing"]
    if multi_processing == "no_multi_process":
        return func(dict(request.headers.items(lower=True)))
    else:
        import multiprocessing as mp

        ctx = (
            mp.get_context()
            if multi_processing == "default_multi_process"
            else mp.get_context(multi_processing)
        )
        queue = ctx.Queue()
        process = ctx.Process(
            target=multi_process_wrapper_function,
            args=(
                queue,
                func,
                dict(request.headers.items(lower=True)),
            ),
        )
        process.start()
        my_result = queue.get()
        process.join()
        return my_result


def inner_transformers_prediction(request_headers):
    try:
        transformers_init(request_headers)

        model_name = request_headers["model-name"]
        prediction_file_path = request_headers["prediction-file-path"]
        tmp_dir = request_headers["tmp-dir"]
        using_tensorflow = request_headers["using-tf"].lower() == "true"
        change_class = request_headers["change-class"].lower() == "true"
        training_arguments = json.loads(request_headers["training-arguments"])

        from transformers import AutoTokenizer

        tokenizer = AutoTokenizer.from_pretrained(model_name)

        app.logger.info("Prepare transformers dataset and tokenize")
        data_left, data_right, _ = transformers_read_file(prediction_file_path, False)
        assert len(data_left) == len(data_right)
        predict_dataset = transformers_create_dataset(
            using_tensorflow, tokenizer, data_left, data_right
        )
        app.logger.info("Transformers dataset contains %s rows.", len(data_left))

        with tempfile.TemporaryDirectory(dir=tmp_dir) as tmpdirname:
            initial_arguments = {
                "report_to": "none",
                #'disable_tqdm' : True,
            }
            fixed_arguments = {
                "output_dir": os.path.join(tmpdirname, "trainer_output_dir")
            }
            training_args = transformers_get_training_arguments(
                using_tensorflow, initial_arguments, training_arguments, fixed_arguments
            )

            app.logger.info("Loading transformers model")
            if using_tensorflow:
                import tensorflow as tf

                app.logger.info(
                    "Num gpu avail: " + str(len(tf.config.list_physical_devices("GPU")))
                )
                from transformers import TFTrainer, TFAutoModelForSequenceClassification

                with training_args.strategy.scope():
                    model = TFAutoModelForSequenceClassification.from_pretrained(
                        model_name, num_labels=2
                    )

                trainer = TFTrainer(
                    model=model, tokenizer=tokenizer, args=training_args
                )
            else:
                import torch

                app.logger.info("Is gpu used: " + str(torch.cuda.is_available()))
                from transformers import Trainer, AutoModelForSequenceClassification

                model = AutoModelForSequenceClassification.from_pretrained(
                    model_name, num_labels=2
                )

                trainer = Trainer(model=model, tokenizer=tokenizer, args=training_args)

            app.logger.info("Run prediction")
            pred_out = trainer.predict(predict_dataset)
            app.logger.info(pred_out.metrics)
        class_index = 0 if change_class else 1
        # sigmoid: scores = 1 / (1 + np.exp(-pred_out.predictions, axis=1[:, class_index]))
        # compute softmax to get class probabilities (scores between 0 and 1)
        scores = softmax(pred_out.predictions, axis=1)[:, class_index]
        return scores.tolist()
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


@app.route("/transformers-prediction", methods=["GET"])
def transformers_prediction():
    result = run_function_multi_process(request, inner_transformers_prediction)
    if isinstance(result, str):
        return result
    else:
        return jsonify(result)


def inner_transformers_finetuning(request_headers):
    try:
        transformers_init(request_headers)

        initial_model_name = request_headers["model-name"]
        resulting_model_location = request_headers["resulting-model-location"]
        tmp_dir = request_headers["tmp-dir"]
        training_file = request_headers["training-file"]
        using_tensorflow = request_headers["using-tf"].lower() == "true"
        training_arguments = json.loads(request_headers["training-arguments"])

        save_at_end = training_arguments.get("save_at_end", True)
        training_arguments.pop("save_at_end", None)  # delete if existent

        weight_of_positive_class = training_arguments.get("weight_of_positive_class", -1.0)
        training_arguments.pop("weight_of_positive_class", None)  # delete if existent

        from transformers import AutoTokenizer

        tokenizer = AutoTokenizer.from_pretrained(initial_model_name)

        app.logger.info("Prepare transformers dataset and tokenize")
        data_left, data_right, labels = transformers_read_file(training_file, True)
        assert len(data_left) == len(data_right) == len(labels)
        training_dataset = transformers_create_dataset(
            using_tensorflow, tokenizer, data_left, data_right, labels
        )
        app.logger.info(
            "Transformers dataset contains %s examples.", len(training_dataset)
        )

        with tempfile.TemporaryDirectory(dir=tmp_dir) as tmpdirname:
            initial_arguments = {
                "report_to": "none",
                # 'disable_tqdm' : True,
            }

            fixed_arguments = {
                "output_dir": os.path.join(tmpdirname, "trainer_output_dir"),
                "save_strategy": "no",
            }

            training_args = transformers_get_training_arguments(
                using_tensorflow, initial_arguments, training_arguments, fixed_arguments
            )

            app.logger.info("Loading transformers model")
            if using_tensorflow:
                import tensorflow as tf

                app.logger.info(
                    "Using Tensorflow. Num GPU available: " + str(len(tf.config.list_physical_devices("GPU")))
                )
                from transformers import TFTrainer, TFAutoModelForSequenceClassification

                with training_args.strategy.scope():
                    model = TFAutoModelForSequenceClassification.from_pretrained(
                        initial_model_name, num_labels=2
                    )

                trainer = TFTrainer(
                    model=model,
                    tokenizer=tokenizer,
                    train_dataset=training_dataset,
                    args=training_args,
                )
            else:
                import torch

                app.logger.info("Using pytorch. GPU used: " + str(torch.cuda.is_available()))
                from transformers import Trainer, AutoModelForSequenceClassification

                model = AutoModelForSequenceClassification.from_pretrained(
                    initial_model_name, num_labels=2
                )

                # tokenizer is added to the trainer because only in this case the tokenizer will be saved along the model to be reused.
                trainer = Trainer(
                    model=model,
                    tokenizer=tokenizer,
                    train_dataset=training_dataset,
                    args=training_args,
                )
                
                if weight_of_positive_class >= 0.0:
                    # calculate class weights
                    if weight_of_positive_class > 1.0:
                        import numpy as np
                        from sklearn.utils.class_weight import compute_class_weight
                        unique_labels = np.unique(labels)
                        if len(unique_labels) <= 1:
                            class_weights = [0.5, 0.5] # only one label available -> default to [0.5, 0.5]
                        else:
                            class_weights = compute_class_weight('balanced', classes=unique_labels, y=labels)                        
                    else:
                        class_weights = [ 1.0 - weight_of_positive_class, weight_of_positive_class]
                    app.logger.info("Using class weights: " + str(class_weights))
                    class WeightedLossTrainer(Trainer):
    
                        def set_melt_weight(self, melt_weight_arg):
                            self.melt_weight = torch.FloatTensor(melt_weight_arg).to(device=self.args.device)
                            

                        def compute_loss(self, model, inputs, return_outputs=False):
                            labels = inputs.get("labels")
                            outputs = model(**inputs)
                            logits = outputs.get('logits')
                            loss_fct = torch.nn.CrossEntropyLoss(weight=self.melt_weight)
                            loss = loss_fct(logits.view(-1, self.model.config.num_labels), labels.view(-1))
                            return (loss, outputs) if return_outputs else loss

                    trainer = WeightedLossTrainer(
                        model=model,
                        tokenizer=tokenizer,
                        train_dataset=training_dataset,
                        args=training_args,
                    )
                    trainer.set_melt_weight(class_weights)


            app.logger.info("Run training")
            trainer.train()

            if save_at_end:
                app.logger.info("Save model")
                trainer.save_model(resulting_model_location)
        return "True"
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


@app.route("/transformers-finetuning", methods=["GET"])
def transformers_finetuning():
    result = run_function_multi_process(request, inner_transformers_finetuning)
    if isinstance(result, str):
        return result
    else:
        return jsonify(result)


@app.route("/transformers-finetuning-hp-search", methods=["GET"])
def transformers_finetuning_hp_search():
    try:
        transformers_init(request.headers)
        os.environ["TOKENIZERS_PARALLELISM"] = "false"

        initial_model_name = request.headers["model-name"]
        resulting_model_location = request.headers["resulting-model-location"]
        tmp_dir = request.headers["tmp-dir"]
        training_file = request.headers["training-file"]
        using_tensorflow = request.headers["using-tf"].lower() == "true"
        training_arguments = json.loads(request.headers["training-arguments"])
        number_of_trials = int(request.headers["number-of-trials"])
        test_size = float(request.headers["test-size"])
        optimizing_metric = request.headers["optimizing-metric"]

        hp_space = json.loads(request.headers["hp-space"])
        hp_mutations = json.loads(request.headers["hp-mutations"])

        if optimizing_metric not in set(
            ["loss", "accuracy", "f1", "precision", "recall", "auc", "aucf1"]
        ):
            raise ValueError(
                "optimize_metric is not one of loss, accuracy, f1, precision, recall, auc, aucf1."
            )

        optimize_direction = "minimize" if optimizing_metric == "loss" else "maximize"

        from transformers import AutoTokenizer

        tokenizer = AutoTokenizer.from_pretrained(initial_model_name)

        app.logger.info("Prepare transformers dataset and tokenize")
        data_left, data_right, labels = transformers_read_file(training_file, True)
        assert len(data_left) == len(data_right) == len(labels)

        from sklearn.model_selection import train_test_split

        [
            data_left_train,
            data_left_test,
            data_right_train,
            data_right_test,
            labels_train,
            labels_test,
        ] = train_test_split(
            data_left, data_right, labels, stratify=labels, test_size=test_size
        )

        training_dataset = transformers_create_dataset(
            using_tensorflow, tokenizer, data_left_train, data_right_train, labels_train
        )
        eval_dataset = transformers_create_dataset(
            using_tensorflow, tokenizer, data_left_test, data_right_test, labels_test
        )

        app.logger.info(
            "Transformers dataset for training has %s and for eval %s examples.",
            len(training_dataset),
            len(eval_dataset),
        )

        with tempfile.TemporaryDirectory(dir=tmp_dir) as tmpdirname:
            initial_arguments = {
                "report_to": "none",
                "disable_tqdm": True,
            }

            fixed_arguments = {
                "output_dir": os.path.join(tmpdirname, "trainer_output_dir"),
                "skip_memory_metrics": True,  # see https://github.com/huggingface/transformers/issues/11249
                "save_strategy": "epoch",
                "do_eval": True,
                "evaluation_strategy": "epoch",
            }
            training_args = transformers_get_training_arguments(
                using_tensorflow, initial_arguments, training_arguments, fixed_arguments
            )

            from sklearn.metrics import (
                accuracy_score,
                roc_auc_score,
                precision_recall_fscore_support,
            )

            def compute_metrics(pred):
                labels = pred.label_ids
                preds = pred.predictions.argmax(-1)
                acc = accuracy_score(labels, preds)
                precision, recall, f1, _ = precision_recall_fscore_support(
                    labels, preds, average="binary", pos_label=1, zero_division=0
                )
                preds_proba = softmax(pred.predictions, axis=1)[:, 1]
                auc = roc_auc_score(labels, preds_proba)
                return {
                    "accuracy": acc,
                    "f1": f1,
                    "precision": precision,
                    "recall": recall,
                    "auc": auc,
                    "aucf1": auc + f1,
                }

            app.logger.info("Loading transformers model")
            if using_tensorflow:
                import tensorflow as tf

                app.logger.info(
                    "Using Tensorflow. Num GPU available: " + str(len(tf.config.list_physical_devices("GPU")))
                )
                from transformers import TFTrainer, TFAutoModelForSequenceClassification

                def model_init():
                    return TFAutoModelForSequenceClassification.from_pretrained(
                        initial_model_name, num_labels=2
                    )

                trainer = TFTrainer(
                    model_init=model_init,
                    tokenizer=tokenizer,
                    train_dataset=training_dataset,
                    eval_dataset=eval_dataset,
                    compute_metrics=compute_metrics,
                    args=training_args,
                )
            else:
                import torch

                app.logger.info("Using pytorch. GPU used: " + str(torch.cuda.is_available()))
                from transformers import Trainer, AutoModelForSequenceClassification

                def model_init():
                    return AutoModelForSequenceClassification.from_pretrained(
                        initial_model_name, num_labels=2
                    )

                # tokenizer is added to the trainer because only in this case the tokenizer will be saved along the model to be reused.
                trainer = Trainer(
                    model_init=model_init,
                    tokenizer=tokenizer,
                    train_dataset=training_dataset,
                    eval_dataset=eval_dataset,
                    compute_metrics=compute_metrics,
                    args=training_args,
                )

            # based on the following example: https://docs.ray.io/en/master/tune/examples/pbt_transformers.html
            app.logger.info("Run hyperparameter search")

            ray_local_dir = os.path.join(tmpdirname, "ray_local_dir")
            run_name = "run_" + datetime.today().strftime("%Y-%m-%d_%H-%M-%S")

            import ray

            ray.init(include_dashboard=False, ignore_reinit_error=True)
            from ray import tune
            from ray.tune.schedulers import PopulationBasedTraining

            # process search space
            def process_search_space(search_space):
                for key, value in search_space.items():
                    function_to_call = getattr(tune, value["name"], None)
                    if function_to_call is None:
                        raise ValueError(
                            "the following function name is not part of ray.tune: "
                            + str(value["name"])
                        )
                    search_space[key] = function_to_call(**value["params"])

            process_search_space(hp_space)
            process_search_space(hp_mutations)

            shorter_names = {
                "weight_decay": "w_decay",
                "learning_rate": "lr",
                "per_device_train_batch_size": "batch",
                "num_train_epochs": "epochs",
            }
            param_dict_shorter_names = {
                hp_key: shorter_names[hp_key] if hp_key in shorter_names else hp_key
                for hp_key in set(hp_space.keys()).union(hp_mutations.keys())
            }

            app.logger.info("hp_space: " + str(hp_space))
            app.logger.info("hp_mutations: " + str(hp_mutations))

            from ray.tune import CLIReporter

            class FlushingReporter(CLIReporter):
                def report(self, trials, done, *sys_info):
                    print(self._progress_str(trials, done, *sys_info), flush=True)

            reporter = FlushingReporter(
                parameter_columns=param_dict_shorter_names,
                metric_columns={
                    "objective": "objective",
                    "eval_auc": "auc",
                    "eval_f1": "f1",
                    "eval_precision": "prec",
                    "eval_recall": "rec",
                    "eval_accuracy": "acc",
                    "time_total_s": "time(s)",
                },
            )

            best_run = trainer.hyperparameter_search(
                hp_space=lambda _: hp_space,
                backend="ray",
                n_trials=number_of_trials,
                compute_objective=lambda x: x["eval_" + optimizing_metric],
                direction=optimize_direction,
                scheduler=PopulationBasedTraining(
                    time_attr="training_iteration",
                    perturbation_interval=1,
                    metric="objective",
                    mode=optimize_direction[:3],
                    hyperparam_mutations=hp_mutations,
                ),
                keep_checkpoints_num=1,
                checkpoint_score_attr="training_iteration",
                raise_on_failed_trial=False,
                resources_per_trial={"cpu": 1, "gpu": 1},
                local_dir=ray_local_dir,
                name=run_name,
                progress_reporter=reporter,
                trial_name_creator=lambda trial: trial.trial_id,
                trial_dirname_creator=lambda trial: trial.trial_id,
            )

            ray.shutdown()

            trial_root_folder = os.path.join(ray_local_dir, run_name, best_run.run_id)
            highest_outer_step_folder = transformers_search_folder_with_highest_count(
                trial_root_folder, "checkpoint_([0-9]+)"
            )

            if not highest_outer_step_folder:
                app.logger.warning(
                    "Could not find a checkpoint directory to load to best model from. Return without saving any model."
                )
                return "ERROR Could not find a checkpoint directory to load to best model from"

            highest_step_folder = transformers_search_folder_with_highest_count(
                highest_outer_step_folder, "checkpoint-([0-9]+)"
            )
            if not highest_step_folder:
                app.logger.warning(
                    "Could not find a checkpoint within the checkpoint directory but lets try the outer checkpoint directory."
                )
                highest_step_folder = highest_outer_step_folder

            app.logger.info(
                "Found best model in checkpoint folder: " + str(highest_step_folder)
            )

            if using_tensorflow:
                with training_args.strategy.scope():
                    model = TFAutoModelForSequenceClassification.from_pretrained(
                        highest_step_folder, num_labels=2
                    )
                tokenizer = AutoTokenizer.from_pretrained(highest_step_folder)
                trainer = TFTrainer(
                    model=model,
                    tokenizer=tokenizer,
                    eval_dataset=eval_dataset,
                    compute_metrics=compute_metrics,
                    args=training_args,
                )
            else:
                model = AutoModelForSequenceClassification.from_pretrained(
                    highest_step_folder, num_labels=2
                )
                tokenizer = AutoTokenizer.from_pretrained(highest_step_folder)
                trainer = Trainer(
                    model=model,
                    tokenizer=tokenizer,
                    eval_dataset=eval_dataset,
                    compute_metrics=compute_metrics,
                    args=training_args,
                )

            app.logger.info("Best model scored:")
            trainer.evaluate()

            app.logger.info("Save model")
            trainer.save_model(resulting_model_location)
        del os.environ["TOKENIZERS_PARALLELISM"]
        return "True"
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


def inner_sentencetransformers_prediction(request_headers):
    try:
        transformers_init(request_headers)

        model_name = request_headers["model-name"]
        corpus_file_name = request_headers["corpus-file-name"]
        queries_file_name = request_headers["queries-file-name"]

        query_chunk_size = int(request_headers["query-chunk-size"])
        corpus_chunk_size = int(request_headers["corpus-chunk-size"])
        top_k = int(request_headers["topk"])
        both_directions = request_headers["both-directions"].lower() == "true"
        topk_per_resource = request_headers["topk-per-resource"].lower() == "true"

        from sentence_transformers import util
        import torch

        cache_folder_path = (
            request_headers["transformers-cache"]
            if "transformers-cache" in request_headers
            else None
        )

        if "kbert" in request_headers and request_headers["kbert"].lower() == "true":
            from kbert.KBertSentenceTransformer import KBertSentenceTransformer
            embedder = KBertSentenceTransformer(model_name, cache_folder=cache_folder_path)
        else:
            from sentence_transformers import SentenceTransformer
            embedder = SentenceTransformer(model_name, cache_folder=cache_folder_path)

        corpus, corpus_pos_to_id = load_file(corpus_file_name)
        queries, queries_pos_to_id = load_file(queries_file_name)

        app.logger.info(
            "loaded corpora with %s corpus documents and %s query documents. Compute now embedding.",
            len(corpus),
            len(queries),
        )

        app.logger.info("Compute corpus embedding.")
        corpus_embeddings = embedder.encode(corpus, convert_to_tensor=True)
        app.logger.info("Compute query embedding.")
        query_embeddings = embedder.encode(queries, convert_to_tensor=True)

        app.logger.info("Is gpu used: " + str(torch.cuda.is_available()))
        if torch.cuda.is_available():
            corpus_embeddings = corpus_embeddings.to("cuda")
            query_embeddings = query_embeddings.to("cuda")

        corpus_embeddings = util.normalize_embeddings(corpus_embeddings)
        query_embeddings = util.normalize_embeddings(query_embeddings)

        app.logger.info("Run semantic search with topk=%s", top_k)

        hits = util.semantic_search(
            query_embeddings,
            corpus_embeddings,
            query_chunk_size,
            corpus_chunk_size,
            top_k,
            util.dot_score,
        )

        app.logger.info("Preparing results")
        result_dict = defaultdict(set)
        for query_pos, query_hits in enumerate(hits):
            query_id = queries_pos_to_id[query_pos]
            for hit in query_hits:
                corpus_pos = hit["corpus_id"]
                corpus_id = corpus_pos_to_id[corpus_pos]
                result_dict[(corpus_id, query_id)].add(hit["score"])

        if both_directions:
            app.logger.info(
                "Run semantic search with topk=%s in other direction", top_k
            )
            hits = util.semantic_search(
                corpus_embeddings,
                query_embeddings,
                query_chunk_size,
                corpus_chunk_size,
                top_k,
                util.dot_score,
            )

            for corpus_pos, corpus_hits in enumerate(hits):
                corpus_id = corpus_pos_to_id[corpus_pos]
                for hit in corpus_hits:
                    query_pos = hit["corpus_id"]
                    query_id = queries_pos_to_id[query_pos]
                    result_dict[(corpus_id, query_id)].add(hit["score"])
        results = []
        for (left, right), scores in result_dict.items():
            results.append((left, right, max(scores)))

        if topk_per_resource == False:
            return results

        # if top k per resource is true, then further filter the alignment
        source_dict = defaultdict(set)
        target_dict = defaultdict(set)
        for correspondence in results:
            source_dict[correspondence[0]].add(correspondence)
            target_dict[correspondence[1]].add(correspondence)

        final_alignment = set()
        for alignment in source_dict.values():
            selected = sorted(alignment, key=lambda x: x[2], reverse=True)[:top_k]
            final_alignment.update(selected)
        for alignment in target_dict.values():
            selected = sorted(alignment, key=lambda x: x[2], reverse=True)[:top_k]
            final_alignment.update(selected)
        return list(final_alignment)
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


def load_file(file_path):
    mapping_pos_to_id = dict()
    corpus = []
    with open(file_path, encoding="utf-8") as csvfile:
        for i, row in enumerate(csv.reader(csvfile, delimiter=",")):
            mapping_pos_to_id[i] = row[0]
            corpus.append(row[1])
    return corpus, mapping_pos_to_id


@app.route("/sentencetransformers-prediction", methods=["GET"])
def sentencetransformers_prediction():
    result = run_function_multi_process(request, inner_sentencetransformers_prediction)
    if isinstance(result, str):
        return result
    else:
        return jsonify(result)


def inner_sentencetransformers_finetuning(request_headers):
    try:
        # https://github.com/UKPLab/sentence-transformers/issues/791#issuecomment-790402913
        transformers_init(request_headers)

        model_name = request_headers["model-name"]
        tmp_dir = request_headers["tmp-dir"]
        resulting_model_location = request_headers["resulting-model-location"]
        training_file = request_headers["training-file"]

        sentence_loss = request_headers["loss"]
        train_batch_size = int(request_headers["train-batch-size"])
        test_batch_size = int(request_headers["test-batch-size"])
        num_epochs = int(request_headers["num-epochs"])
        cache_folder_path = (
            request_headers["transformers-cache"]
            if "transformers-cache" in request_headers
            else None
        )

        from sentence_transformers import SentenceTransformer, InputExample, losses

        model = SentenceTransformer(model_name, cache_folder=cache_folder_path)

        if sentence_loss == "CosineSimilarityLoss":
            parser = lambda row: InputExample(
                texts=[row[0], row[1]], label=float(row[2])
            )
            train_loss = losses.CosineSimilarityLoss(model)
        elif sentence_loss == "MultipleNegativesRankingLoss":
            parser = lambda row: InputExample(texts=[row[0], row[1]])
            train_loss = losses.MultipleNegativesRankingLoss(model)
        elif sentence_loss == "MultipleNegativesRankingLossWithHardNegatives":
            parser = lambda row: InputExample(texts=row)
            train_loss = losses.MultipleNegativesRankingLoss(model)
        else:
            raise ValueError("the selected loss is not available")

        def read_input_examples(file_path, input_example_generator):
            input_examples = []
            with open(file_path, encoding="utf-8") as csvfile:
                for row in csv.reader(csvfile, delimiter=","):
                    input_examples.append(input_example_generator(row))
            return input_examples

        all_input_examples = read_input_examples(training_file, parser)
        if "validation-file" in request_headers:
            train_examples = all_input_examples
            validation_examples = read_input_examples(
                request_headers["validation-file"], parser
            )
            app.logger.info(
                "Use separate train and validation file: %s train and %s validation.",
                len(train_examples),
                len(validation_examples),
            )
        else:
            test_size = float(request_headers["test-size"])
            from sklearn.model_selection import train_test_split

            train_examples, validation_examples = train_test_split(
                all_input_examples,
                stratify=[i.label for i in all_input_examples],
                test_size=test_size,
            )
            app.logger.info(
                "Loaded %s examples. Do a split(validation percentage: %s): %s are training and %s are validation",
                len(all_input_examples),
                test_size,
                len(train_examples),
                len(validation_examples),
            )

        from sentence_transformers.evaluation import EmbeddingSimilarityEvaluator
        from torch.utils.data import DataLoader
        import math

        train_dataloader = DataLoader(
            train_examples, shuffle=True, batch_size=train_batch_size
        )
        evaluator = EmbeddingSimilarityEvaluator.from_input_examples(
            validation_examples, write_csv=True, batch_size=test_batch_size
        )
        warmup_steps = math.ceil(
            len(train_dataloader) * num_epochs * 0.1
        )  # 10% of train data for warm-up

        app.logger.info("Run the training now")
        model.fit(
            train_objectives=[(train_dataloader, train_loss)],
            epochs=num_epochs,
            warmup_steps=warmup_steps,
            save_best_model=True,
            output_path=resulting_model_location,
            evaluator=evaluator,
        )

        return model.best_score  # this will return a float value with the best score
    except Exception as e:
        import traceback

        return "ERROR " + traceback.format_exc()


@app.route("/sentencetransformers-finetuning", methods=["GET"])
def sentencetransformers_finetuning():
    result = run_function_multi_process(request, inner_sentencetransformers_finetuning)
    if isinstance(result, str):
        return result
    else:
        return jsonify(result)


@app.route("/hello", methods=["GET"])
def hello_demo() -> str:
    """A demo program that will return Hello <name> when called.

    Returns
    -------
    greeting : str
        A simple greeting.
    """
    name_to_greet = request.headers.get("name")
    app.logger.info(name_to_greet)
    return "Hello " + str(name_to_greet) + "!"


@app.errorhandler(Exception)
def handle_exception(e):
    """Return JSON instead of HTML for general errors."""
    return "ERROR: " + str(e), 500


@app.route("/shutdown", methods=["GET"])
def shutdown():
    request.environ.get("werkzeug.server.shutdown")()


def main():
    # threaded=False because otherwise GridSearchCV do not run in parallel
    # see https://stackoverflow.com/questions/50665837/using-flask-with-joblib
    # determine the port
    try:
        if len(sys.argv) == 2:
            logging.info("Received argument: " + sys.argv[1])
            int_port = int(sys.argv[1])
            if int_port > 0:
                port = int_port
        elif len(sys.argv) == 3:
            numeric_level = getattr(logging, sys.argv[2].upper(), None)
            if not isinstance(numeric_level, int):
                logging.info("Cannot parse log level " + sys.argv[2])
            else:
                logging.getLogger().setLevel(numeric_level)
                logging.getLogger("werkzeug").setLevel(numeric_level)
            logging.info("Received port and log level")
            int_port = int(sys.argv[1])
            if int_port > 0:
                port = int_port
        else:
            port = 41193
    except Exception as e:
        logging.info("Exception occurred. Using default port: 41193")
        port = 41193
        logging.error(e)
    logging.info(f"Starting server using port {port}")
    app.run(debug=False, port=port, threaded=False)


if __name__ == "__main__":
    main()
