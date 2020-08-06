from flask import Flask, request, jsonify
from gensim import corpora, models, similarities, matutils
from scipy import linalg
import csv
import numpy as np
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
    """Data structure to iterate over the lines of a file in a memory-friendly way. The files can be gzipped.
    """

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
                logging.info("Directory detected.")
                for file_name in os.listdir(self.file_or_directory_path):
                    logging.info("Processing file: " + file_name)
                    if file_name[-2:] in "gz":
                        logging.info("Gzip file detected! Using gzip.open().")
                        for line in gzip.open(os.path.join(self.file_or_directory_path, file_name), mode='rt', encoding="utf-8"):
                            line = line.rstrip('\n')
                            words = line.split(" ")
                            yield words
                    else:
                        for line in open(os.path.join(self.file_or_directory_path, file_name), mode='rt', encoding="utf-8"):
                            line = line.rstrip('\n')
                            words = line.split(" ")
                            yield words
            else:
                logging.info("Processing file: " + self.file_or_directory_path)
                if self.file_or_directory_path[-2:] in "gz":
                    logging.info("Gzip file detected! Using gzip.open().")
                    for line in gzip.open(self.file_or_directory_path, mode='rt', encoding="utf-8"):
                        line = line.rstrip('\n')
                        words = line.split(" ")
                        yield words
                else:
                    for line in open(self.file_or_directory_path, mode='rt', encoding="utf-8"):
                        line = line.rstrip('\n')
                        words = line.split(" ")
                        yield words
        except Exception:
            logging.error("Failed reading file:")
            logging.error(self.file_or_directory_path)
            logging.exception("Stack Trace:")

@app.route('/get-vocabulary-size', methods=['GET'])
def get_vocab_size():
    model_path = request.headers.get('model_path')
    vector_path = request.headers.get('vector_path')
    vectors = get_vectors(model_path, vector_path)
    return str(len(vectors.vocab))

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
        min_count = request.headers.get('min_count')
        sample = request.headers.get('sample')

        sentences = MySentences(file_path)
        logging.info("Sentences object initialized.")

        if cbow_or_sg == 'sg':
            model = models.Word2Vec(min_count=int(min_count), sample=float(sample), size=int(vector_dimension), workers=int(number_of_threads), window=int(window_size), sg=1, negative=int(negatives), iter=int(iterations))
        else:
            model = models.Word2Vec(min_count=int(min_count), sample=float(sample), size=int(vector_dimension), workers=int(number_of_threads), window=int(window_size), sg=0, cbow_mean=1, alpha = 0.05, negative=int(negatives), iter=int(iterations))

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
            # logging.info("Found model in cache.")
            model = active_models[model_path]
            vectors = model.wv
        else:
            model = models.Word2Vec.load(model_path)
            active_models[model_path] = model
            vectors = model.wv
    elif vector_path in active_vectors:
        # logging.info("Found vector file in cache.")
        vectors = active_vectors[vector_path]
    else:
        vectors = models.KeyedVectors.load(vector_path, mmap='r')
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

# Doc2vec models

class Doc2VecCsvCorpus(object):

    def __init__(self, file_path):
        self.file_path = file_path

    def __iter__(self):
        with open(self.file_path, encoding='utf-8') as csvfile:
            readCSV = csv.reader(csvfile, delimiter=',')
            for i, row in enumerate(readCSV):
                yield models.doc2vec.TaggedDocument(row[1].split(), [row[0]])


@app.route('/train-doc2vec-model', methods=['GET'])
def train_doc2vec_model():
    input_file_path = request.headers.get('input_file_path')
    model_path = request.headers.get('model_path')
    
    vector_dimension = request.headers.get('vector_dimension')
    min_count = request.headers.get('min_count')
    number_of_threads = request.headers.get('number_of_threads')
    window_size = request.headers.get('window_size')
    iterations = request.headers.get('iterations')
    negatives = request.headers.get('negatives')
    cbow_or_sg = request.headers.get('cbow_or_sg')
    
    corpus = Doc2VecCsvCorpus(input_file_path)
    dm = 1 if cbow_or_sg == 'sg' else 0
    
    #train the model:
    model = models.doc2vec.Doc2Vec(documents=corpus, dm=dm, min_count=int(min_count), vector_size=int(vector_dimension), workers=int(number_of_threads), window=int(window_size), negative=int(negatives), epochs=int(iterations))

    #model.save(model_path + '.model')
    active_models[model_path] = model
    return "True"

@app.route('/query-doc2vec-model-batch', methods=['POST'])
def query_doc2vec_model_batch():
    try:
        content = request.get_json()
        model = active_models.get(content['modelPath'])
        if model is None:
            return "ERROR! Model not active"
        result_list = []
        for (source, target) in content['documentIds']:
            #logging.info("processing: %s and %s", source, target)
            try:
                doc2vec_similarity = float(model.docvecs.similarity(source, target))
                result_list.append(doc2vec_similarity)
            except KeyError as e:
                result_list.append(-2.0)
        return jsonify(result_list)
    except Exception as e:
        return str(e)



# TF-IDF and LSI models

@app.route('/train-vector-space-model', methods=['GET'])
def train_vector_space_model():
    input_file_path = request.headers.get('input_file_path')
    model_path = request.headers.get('model_path')

    dictionary = __createDictionary(input_file_path)
    corpus = CsvCorpus(dictionary, input_file_path)
    tfidf = models.TfidfModel(dictionary=dictionary)
    tfidf_corpus = tfidf[corpus]

    index = similarities.Similarity('index.index', tfidf_corpus, num_features=len(dictionary))
    # index = similarities.SparseMatrixSimilarity(tfidf_corpus, num_features=len(dictionary))
    # index = similarities.MatrixSimilarity(tfidf_corpus, num_features=len(dictionary))
    active_models[model_path] = (corpus, index)
    return "True"

@app.route('/query-vector-space-model', methods=['GET'])
def query_vector_space_model():
    try:
        model_path = request.headers.get('model_path')
        document_id_one = request.headers.get('document_id_one')
        document_id_two = request.headers.get('document_id_two')  # can be None

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


@app.route('/query-vector-space-model-batch', methods=['POST'])
def query_vector_space_model_batch():
    try:
        content = request.get_json()
        
        model = active_models.get(content['modelPath'])
        if model is None:
            return "ERROR! Model not active"
        (corpus, index) = model
        
        result_list = []
        for (source, target) in content['documentIds']:
            #logging.info("processing: %s and %s", source, target)
            source_position = corpus.id2pos.get(source)
            target_position = corpus.id2pos.get(target)
            #logging.info("pos: %s and %s", source_position, target_position)
            if source_position is None or target_position is None:
                result_list.append(-2.0)
                continue
            
            #first variant - very slow:
            #sims = index.similarity_by_id(source_position)
            #resulting_sim = sims[target_position]
            
            #second variant with scikit learn
            # from sklearn.metrics.pairwise import cosine_similarity
            #vec_one = index.vector_by_id(source_position)
            #vec_two = index.vector_by_id(target_position)
            #resulting_sim = cosine_similarity(vec_one, vec_two) 
            
            # third variant - best runtime
            vec_one = matutils.scipy2sparse(index.vector_by_id(corpus.id2pos.get(source)))
            vec_two = matutils.scipy2sparse(index.vector_by_id(corpus.id2pos.get(target)))
            resulting_sim = matutils.cossim(vec_one, vec_two)
            
            result_list.append(resulting_sim)
        return jsonify(result_list)
    except Exception as e:
        return str(e)

english_stopwords = {'has', 'mightn', 'me', 'here', 'other', 'very', 'but', 'ours', 'he', 'his', 'there', 'you', 'some',
                     'don', 'such', 'under', 'their', 'themselves', "mustn't", 'had', "shan't", "she's", 'yourselves',
                     'by', 'about', 'needn', 're', "weren't", 'any', 'herself', "don't", 'am', 'hadn', 'what', 'each',
                     'weren', "hadn't", 'between', 'both', 'in', 'can', 'the', 'does', 'too', 'shouldn', 'once', 'when',
                     's', 'it', 'as', 'same', 'haven', "hasn't", "didn't", "wasn't", 'on', 'shan', 'they', 'of', 'was',
                     "aren't", 'out', 'before', 'our', 'aren', 'ourselves', 'wouldn', 'we', 'didn', 'having', 'above',
                     'just', 'below', 'why', 'against', "wouldn't", 'were', 'yours', 'few', 'm', 'doesn', 'my', 'nor',
                     'then', "you'll", 'your', "isn't", "haven't", 'him', "doesn't", 'i', 'wasn', 'who', 'will',
                     "that'll", 'if', 'hasn', 'been', 'myself', 'd', 'where', 'into', 't', 'ain', "couldn't", 'being',
                     'how', 'y', 'which', "you've", 'an', 'or', 'from', 'no', 'ma', 'doing', 'through', 'all', 'most',
                     'theirs', 'than', 'are', 'to', 'while', "shouldn't", 'that', 'so', 'and', 'only', 'until', 've',
                     'isn', 'should', 'her', 'yourself', 'have', 'over', 'because', "you'd", 'be', 'more', 'a',
                     'himself', 'those', 'these', 'not', 'its', 'own', 'for', 'she', 'down', 'hers', "you're", 'whom',
                     'after', 'this', 'at', 'do', 'll', "it's", 'up', 'couldn', 'with', 'itself', 'again', 'off', 'is',
                     'during', 'further', 'mustn', 'won', 'did', "mightn't", "needn't", "should've", 'them', 'now', 'o',
                     "won't"}

def __createDictionary(file_path, stopwords=english_stopwords):
    with open(file_path, encoding='utf-8') as f:
        # collect statistics about all tokens
        readCSV = csv.reader(f, delimiter=',')
        dictionary = corpora.Dictionary(line[1].lower().split() for line in readCSV)
    # remove stop words and words that appear only once
    stop_ids = [dictionary.token2id[stopword] for stopword in stopwords if stopword in dictionary.token2id]
    once_ids = [tokenid for tokenid, docfreq in dictionary.dfs.items() if docfreq == 1]
    dictionary.filter_tokens(stop_ids + once_ids)  # remove stop words and words that appear only once
    dictionary.compactify()  # remove gaps in id sequence after words that were removed
    return dictionary


def __sims2scores(sims, pos2id, topsims, eps=1e-7):
    """Convert raw similarity vector to a list of (docid, similarity) results."""
    result = []
    sims = abs(sims)  # TODO or maybe clip? are opposite vectors "similar" or "dissimilar"?!
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
        with open(self.file_path, encoding='utf-8') as csvfile:
            readCSV = csv.reader(csvfile, delimiter=',')
            for i, row in enumerate(readCSV):
                if row[0] in self.id2pos:
                    logging.info("Document ID %s already in file - the last one is used only", row[0])
                self.id2pos[row[0]] = i
                self.pos2id[i] = row[0]
                yield self.dictionary.doc2bow(row[1].lower().split())


@app.route('/write-model-as-text-file', methods=['GET'])
def write_vectors_as_text_file():
    """
    Writes all vectors of the model to a text file: one vector per line

    Returns
    -------
    boolean
        'True' as string if operation was successful, else 'False' (as string).
    """
    model_path = request.headers.get('model_path')
    vector_path = request.headers.get("vector_path")
    file_to_write = request.headers.get("file_to_write")
    entity_file = request.headers.get("entity_file")
    vectors = get_vectors(model_path=model_path, vector_path=vector_path)
    final_string = ""
    if entity_file is None:
        for concept in vectors.vocab:
            if concept in vectors.vocab:
                vector = vectors.get_vector(concept)
                final_string += concept + " "
                for element in np.nditer(vector):
                    final_string += str(element) + " "
            else:
                logging.info("WARN: The following concept has not been found in the vector space: " + concept)
            final_string += "\n"
        # write final string to file
    else:
        concepts = read_concept_file(entity_file)
        for concept in concepts:
            if concept in vectors.vocab:
                vector = vectors.get_vector(concept)
                final_string += concept + " "
                for element in np.nditer(vector):
                    final_string += str(element) + " "
            else:
                logging.info("WARN: The following concept has not been found in the vector space: " + concept)
                logging.info("Trying to resolve new URI.")
            final_string += "\n"
    with open(file_to_write, "w+") as f:
        f.write(final_string)
    return "True"

def read_concept_file(path_to_concept_file):
    result = []
    with open(path_to_concept_file, errors='ignore') as concept_file:
        for lemma in concept_file:
            lemma = lemma.replace("\n", "").replace("\r", "")
            result.append(lemma)
    logging.info("Concept file read: " + str(path_to_concept_file))
    return result

############################################
#          Align Embeddings
############################################

@app.route('/align-embeddings', methods=['POST'])
def align_embeddings():
    try:
        content = request.get_json()
        
        source_vectors = get_vectors(vector_path=content['vectorPathSource'])
        target_vectors = get_vectors(vector_path=content['vectorPathTarget'])
        inputAlignment = content['alignment']
        function = content['function']
        
        if function == 'linear_projection':
            projected_source, projected_target = linear_projection(source_vectors, target_vectors, inputAlignment)
        elif function == 'neural_net_projection':
            projected_source, projected_target = neural_net_projection(source_vectors, target_vectors, inputAlignment)
        elif function == 'cca_projection':
            projected_source, projected_target = cca_projection(source_vectors, target_vectors, inputAlignment)
        elif function == 'analyze':
            analyze(source_vectors, target_vectors, inputAlignment)
            return '[]'
        else:
            return "ERROR Function not available"
        
        results = []
        for source_uri in projected_source.vocab:
            most_similar_target = projected_target.most_similar(positive=[projected_source[source_uri]], topn=1)[0]
            results.append((source_uri, most_similar_target[0], most_similar_target[1]))
        return jsonify(results)
    except Exception as e:
        import traceback
        return "ERROR " + traceback.format_exc()


def __normr(arr):
    return arr / linalg.norm(arr, axis=1, ord=2, keepdims=True)


def __canoncorr(X, Y):
    # Based on sourceforge.net/p/octave/statistics/ci/release-1.4.0/tree/inst/canoncorr.m

    #sio.savemat('np_vector.mat', {'X': X, 'Y': Y})
    #additional constraint because otherwise line ' A = linalg.solve(Rx, U[:, :d]) ' does not work
    assert (X.shape[0] > X.shape[1] and Y.shape[0] > Y.shape[1]), \
        'Vector dimension must be greater than trainings lexicon - maybe decrease vector size.'

    k = X.shape[0]
    m = X.shape[1]
    n = Y.shape[1]
    d = min(m, n)

    assert (X.shape[0] == Y.shape[0])  # both array should have same number of rows


    X = X - X.mean(axis=0, keepdims=True)  # center X = remove mean
    Y = Y - Y.mean(axis=0, keepdims=True)  # center Y = remove mean

    Qx, Rx = linalg.qr(X, mode='economic')
    Qy, Ry = linalg.qr(Y, mode='economic')

    U, S, V = linalg.svd(Qx.T.dot(Qy),
                         full_matrices=False)  # full_matrices=False should correspind to svd(...,0)   #, lapack_driver='gesvd'
    V = V.T  # because svd returns transposed V (called Vh)

    A = linalg.solve(Rx, U[:, :d])
    B = linalg.solve(Ry, V[:, :d])

    f = np.sqrt(k - 1)
    A = np.multiply(A, f)
    B = np.multiply(B, f)

    return A, B


def __project_embeddings_to_lexicon_subset(word_vector_source, word_vector_target, lexicon):
    source_subset_vectors = []
    target_subset_vectors = []
    for lang_source_word, lang_target_word in lexicon:
        if lang_source_word not in word_vector_source or lang_target_word not in word_vector_target:
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
    
    matrix_src, matrix_tgt = __project_embeddings_to_lexicon_subset(word_vector_src, word_vector_tgt, lexicon)
    matrix_diff = matrix_src - matrix_tgt
    
    diff_vector_list = []
    for src, dst in lexicon:
        if src in word_vector_src and dst in word_vector_tgt:
            diff_vector_list.append(word_vector_src[src] - word_vector_tgt[dst])
    t = np.array(diff_vector_list)
    logging.info(matrix_diff)
    logging.info(t)

    principalComponents = PCA(n_components=2).fit_transform(matrix_diff)

    plt.scatter(principalComponents[:, 0], principalComponents[:, 1])
    for i in range(principalComponents.shape[0]):
        # get text after last slash or hashtag in source uri
        source_fragment = lexicon[i][0].rsplit('/',1)[-1].rsplit('#',1)[-1] 
        plt.annotate(source_fragment, (principalComponents[i, 0], principalComponents[i, 1]))
    plt.savefig('analyze.png')
    

def linear_projection(word_vector_src, word_vector_tgt, lexicon):
    matrix_src, matrix_tgt = __project_embeddings_to_lexicon_subset(word_vector_src, word_vector_tgt, lexicon)
    if matrix_src.size == 0 or matrix_tgt.size == 0:
        raise Exception('the embeddings do not contain enough vector for the input alignment')
    
    x_mpi = linalg.pinv(matrix_src)  # Moore Penrose Pseudoinverse
    w = np.dot(x_mpi, matrix_tgt)  # linear map matrix W

    source_projected = __create_keyed_vector(word_vector_src, np.dot(word_vector_src.vectors, w))
    return source_projected, word_vector_tgt


def neural_net_projection(word_vector_src, word_vector_tgt, lexicon):
    from keras.models import Sequential
    from keras.layers import Dense, Dropout
    from keras.optimizers import SGD
    from keras import losses

    matrix_src, matrix_tgt = __project_embeddings_to_lexicon_subset(word_vector_src, word_vector_tgt, lexicon)
    if matrix_src.size == 0 or matrix_tgt.size == 0:
        raise Exception('the embeddings do not contain enough vector for the input alignment')
    
    #TODO: optimze model
    model = Sequential()
    model.add(Dense(word_vector_src.vector_size, input_dim=word_vector_src.vector_size, activation='relu'))
    model.add(Dropout(0.5))
    model.add(Dense(word_vector_src.vector_size, input_dim=word_vector_src.vector_size, activation='relu'))
    model.add(Dropout(0.5))
    model.add(Dense(word_vector_src.vector_size, input_dim=word_vector_src.vector_size, activation='relu'))

    sgd = SGD(lr=0.01, decay=1e-6, momentum=0.9, nesterov=True)
    model.compile(loss=losses.mean_squared_error,
                  optimizer=sgd,
                  metrics=[losses.mean_squared_error])

    model.fit(matrix_src, matrix_tgt, epochs=2000, batch_size=128)

    source_projected = model.predict(word_vector_src.vectors)
    source_projected_keyed_vector = __create_keyed_vector(word_vector_src, source_projected)
    return source_projected_keyed_vector, word_vector_tgt


def cca_projection(word_vector_source, word_vector_target, lexicon, top_correlation_ratio = 0.5):
    word_vector_source.init_sims(replace=True)
    word_vector_target.init_sims(replace=True)

    source_subset, target_subset = __project_embeddings_to_lexicon_subset(word_vector_source, word_vector_target, lexicon)
    if source_subset.size == 0 or target_subset.size == 0:
        raise Exception('the embeddings do not contain enough vector for the input alignment')

    A, B = __canoncorr(target_subset, source_subset)

    amount_A = int(np.ceil(top_correlation_ratio * A.shape[1]))
    U = (word_vector_target.vectors - word_vector_target.vectors.mean(axis=0, keepdims=True)).dot(A[:, 0:amount_A])
    U = __normr(U)
    projected_target_vectors = __create_keyed_vector(word_vector_target, U)

    amount_B = int(np.ceil(top_correlation_ratio * B.shape[1]))
    V = (word_vector_source.vectors - word_vector_source.vectors.mean(axis=0, keepdims=True)).dot(B[:, 0:amount_B])
    V = __normr(V)
    projected_source_vectors = __create_keyed_vector(word_vector_source, V)

    return projected_source_vectors, projected_target_vectors


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
