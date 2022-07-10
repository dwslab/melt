package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersBase;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTunerHpSearch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersFineTuner;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersBaseFineTuner;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert.KBertSentenceTransformersMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * A client class to communicate with python libraries such as <a href="https://radimrehurek.com/gensim/">gensim</a>.
 * This class follows a singleton pattern.
 * Communication is performed through HTTP requests.
 * In case you need a different python environment or python executable, create a file in directory python_server
 * named {@code python_command.txt} and write your absolute path of the python executable in that file.
 */
public class PythonServer {


    /**
     * Default logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(PythonServer.class);

    /**
     * Default resources directory (where the python files will be copied to by default) and where the resources
     * are read from within the JAR.
     */
    private static final String DEFAULT_RESOURCES_DIRECTORY = "./melt-resources/";

    /**
     * ObjectMapper from jackson to generate JSON.
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    public static final String PYTHON_DIRECTORY_NAME = "matching_ml";

    /**
     * Constructor
     */
    private PythonServer() {
        // do nothing; do not start the server (yet)
        serverUrl = "http://127.0.0.1:" + port;
    }

    /**
     * The URL that shall be used to perform the requests.
     */
    private static String serverUrl = "http://127.0.0.1:41193";

    /**
     * Indicator whether vectors shall be cached. This means that vectors are cached locally and similarities are
     * calculated in Java to avoid many cross-language calls. Disable in cases of infrequent calls or if memory
     * availability is limited.
     */
    private boolean isVectorCaching = true;

    /**
     * Indicates whether the server has been shut down.
     * Initial state: shutDown.
     */
    private static boolean isShutDown = true;

    /**
     * Local vector cache.
     */
    private HashMap<String, Double[]> vectorCache;

    /**
     * Indicates whether the shutdown hook has been initialized.
     * This flag is required in order to have only one hook despite multiple re-initializations.
     */
    private boolean isHookStarted = false;

    /**
     * The directory where the python files will be copied to.
     */
    private File resourcesDirectory = new File(DEFAULT_RESOURCES_DIRECTORY);

    /**
     * Developer note: Do not change the default port since other applications rely on it (e.g. the python tests).
     * Rather user {@link PythonServer#setPort(int)} if you need to change the port in certain cases.
     */
    private static final int DEFAULT_PORT = 41193;

    /**
     * The port that shall be used.
     */
    private static int port = DEFAULT_PORT;

    /**
     * In case someone wants to configure the python command programmatically.
     * Precedence always has the external file.
     */
    private static String pythonCommandBackup = null;

    /**
     * If set to true, all python files (e.g. python server melt and requirements.txt file) will be overridden with
     * every execution.
     * Set it to false for testing and debugging new features in python server.
     */
    private static boolean overridePythonFiles = true;

    /************************************
     * Transformer section
     ***********************************/

    /**
     * Run a hyperparameter fine tuning.
     * @param hpsearch the hyper parameter search model to use
     * @param trainingFile path to csv file with three columns (text left, text right, label 1/0).
     * @throws PythonServerException in case something goes wrong.
     */
    public void transformersFineTuningHpSearch(TransformersFineTunerHpSearch hpsearch, File trainingFile) throws PythonServerException{
        HttpGet request = new HttpGet(serverUrl + "/transformers-finetuning-hp-search");
        transformersUpdateBaseRequest(hpsearch, request);
        transformersFineTunerUpdateBaseRequest(hpsearch, trainingFile, request);

        request.addHeader("number-of-trials", Integer.toString(hpsearch.getNumberOfTrials()));
        request.addHeader("test-size", Float.toString(hpsearch.getTestSize()));
        request.addHeader("optimizing-metric", hpsearch.getOptimizingMetric().toString());        
        request.addHeader("hp-space", hpsearch.getHpSpace().toJsonString());
        request.addHeader("hp-mutations", hpsearch.getHpMutations().toJsonString());
        
        runRequest(request);
    }
    
    /**
     * Finetune a transformers model with the given parameters and write this model to a given folder.
     * @param fineTuner the finetuner to use
     * @param trainingFile path to csv file with three columns (text left, text right, label 1/0).
     * @throws PythonServerException in case something goes wrong.
     */
    public void transformersFineTuning(TransformersFineTuner fineTuner, File trainingFile) throws PythonServerException{
        HttpGet request = new HttpGet(serverUrl + "/transformers-finetuning");
        transformersUpdateBaseRequest(fineTuner, request);
        transformersFineTunerUpdateBaseRequest(fineTuner, trainingFile, request);
        runRequest(request);
    }

    /**
     * Run a transformers model on a CSV file with two columns (text left and text right) to predict if they describe the same concept.
     * @param filter the filter
     * @param predictionFilePath path to csv file with two columns (text left and text right).
     * @throws PythonServerException in case something goes wrong.
     * @return a list of confidences
     */
    public List<Double> transformersPrediction(TransformersFilter filter, File predictionFilePath) throws PythonServerException {
        //curl http://127.0.0.1:41193/transformers-prediction -H "modelName: bert-base-cased-finetuned-mrpc" -H "usingTF: false" \
        // -H "trainingArguments: {}"  -H "tmpDir: ./my_tmp_dir" -H "cudaVisibleDevices: 0" -H "transformersCache: ./cache_transformers" \
        // -H "predictionFilePath: ./train.txt" -H "changeClass: false" -H "multiProcessing: default_multi_process" 
        HttpGet request = new HttpGet(serverUrl + "/transformers-prediction");
        transformersUpdateBaseRequest(filter, request);
        
        request.addHeader("prediction-file-path", getCanonicalPath(predictionFilePath));
        request.addHeader("change-class", Boolean.toString(filter.isChangeClass()));

        String resultString = runRequest(request);
        try {
            return JSON_MAPPER.readValue(resultString, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Double.class));
        } catch (JsonProcessingException ex) {
            throw new PythonServerException("Could not parse JSON", ex);
        }
    }
    
    /**
     * Run sentence transformers prediction.
     * @param matcher the matcher
     * @param corpusFile path to csv file with two columns (url, text representation).
     * @param queriesFile path to csv file with two columns (url, text representation).
     * @return the newly generated alignment
     * @throws PythonServerException in case something goes wrong.
     */
    public Alignment sentenceTransformersPrediction(SentenceTransformersMatcher matcher, File corpusFile, File queriesFile) throws PythonServerException{
        
        HttpGet request = new HttpGet(serverUrl + "/sentencetransformers-prediction");
        transformersUpdateBaseRequest(matcher, request);
        
        request.addHeader("corpus-file-name", getCanonicalPath(corpusFile));
        request.addHeader("queries-file-name", getCanonicalPath(queriesFile));
        
        request.addHeader("query-chunk-size", Integer.toString(matcher.getQueryChunkSize()));
        request.addHeader("corpus-chunk-size", Integer.toString(matcher.getCorpusChunkSize()));
        request.addHeader("topk", Integer.toString(matcher.getTopK()));
        request.addHeader("both-directions", Boolean.toString(matcher.isBothDirections()));
        request.addHeader("topk-per-resource", Boolean.toString(matcher.isTopkPerResource()));
        request.addHeader("kbert", Boolean.toString(matcher.getClass() == KBertSentenceTransformersMatcher.class));
        request.addHeader("pooling-mode", matcher.getPoolingMode());

        String resultString = runRequest(request);
        try {
            return parseJSON(resultString);
        } catch (Exception ex) {
            throw new PythonServerException("Could not parse JSON", ex);
        }
    }
    
    /**
     * Run fine tuning for sentence transformers.
     * @param fineTuner the matcher
     * @param trainingFile path to csv file with three columns (text left, text right, label 1/0).
     * @param validationFile the path to the validation file - can also be null to use train test split of trainings file.
     * @throws PythonServerException in case something goes wrong.
     * @return the best score of the validation (using the file or train test split).
     */
    public float sentenceTransformersFineTuning(SentenceTransformersFineTuner fineTuner, File trainingFile, File validationFile) throws PythonServerException{
        
        HttpGet request = new HttpGet(serverUrl + "/sentencetransformers-finetuning");
        transformersUpdateBaseRequest(fineTuner, request);
        transformersFineTunerUpdateBaseRequest(fineTuner, trainingFile, request);
        
        request.addHeader("loss", fineTuner.getLoss().name());
        request.addHeader("test-size", Float.toString(fineTuner.getTestSize()));
        request.addHeader("train-batch-size", Integer.toString(fineTuner.getTrainBatchSize()));
        request.addHeader("test-batch-size", Integer.toString(fineTuner.getTestBatchSize()));
        request.addHeader("num-epochs", Integer.toString(fineTuner.getNumberOfEpochs()));
        if(validationFile != null)
            request.addHeader("validation-file", getCanonicalPath(validationFile));
        
        String resultString = runRequest(request);
        try {
            return JSON_MAPPER.readValue(resultString, Float.class);
        } catch (JsonProcessingException ex) {
            throw new PythonServerException("Could not parse JSON", ex);
        }
    }
    
    private void transformersFineTunerUpdateBaseRequest(TransformersBaseFineTuner fineTuner, File trainingFile,  HttpGet request){
        request.addHeader("resulting-model-location", getCanonicalPath(fineTuner.getResultingModelLocation()));
        request.addHeader("training-file", getCanonicalPath(trainingFile));
    }
    
    private void transformersUpdateBaseRequest(TransformersBase base, HttpGet request){
        request.addHeader("model-name", base.getModelName());
        request.addHeader("using-tf", Boolean.toString(base.isUsingTensorflow()));
        request.addHeader("training-arguments", base.getTrainingArguments().toJsonString());
        request.addHeader("tmp-dir", getCanonicalPath(FileUtil.getUserTmpFolder()));
        request.addHeader("multi-processing", base.getMultiProcessing().toString());
        
        String cudaVisibleDevices = base.getCudaVisibleDevices();
        if(cudaVisibleDevices != null){
            cudaVisibleDevices = cudaVisibleDevices.trim();
            if(cudaVisibleDevices.isEmpty() == false){
                request.addHeader("cuda-visible-devices", cudaVisibleDevices);
            }
        }
        
        File transformersCache = base.getTransformersCache();
        if(transformersCache != null)
            request.addHeader("transformers-cache", getCanonicalPath(transformersCache));
    }
    
    
    
    /************************************
     * OpenEA section
     * **********************************/

    /**
     * Run the openEA library.
     *
     * @param argumentFile the argument file to use
     * @param save         saves the embeddings to files
     * @throws Exception in case something goes wrong.
     */
    public void runOpenEAModel(File argumentFile, boolean save) throws Exception {
        HttpGet request = new HttpGet(serverUrl + "/run-openea");
        request.addHeader("argumentFile", getCanonicalPath(argumentFile));
        if (save)
            request.addHeader("save", "True");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                }
            }
        }
    }

    /* ***********************************
     * learn ML model for Alignment
     * **********************************/

    /**
     * Learn a ML model for a given training file.
     * This file should be comma separated and containing a header.
     * The class attribute should be named "target".
     *
     * @param trainFile   the train file
     * @param predictFile the file to predict
     * @param cv          number of cross validations
     * @param jobs        number of parallel jobs to run
     * @return a list of double
     * @throws Exception throws exception in case of errors
     */
    public List<Integer> learnAndApplyMLModel(File trainFile, File predictFile, int cv, int jobs) throws Exception {
        HttpGet request = new HttpGet(serverUrl + "/machine-learning");
        request.addHeader("trainingsFile", getCanonicalPath(trainFile));
        request.addHeader("predictFile", getCanonicalPath(predictFile));
        request.addHeader("cv", Integer.toString(cv));
        request.addHeader("jobs", Integer.toString(jobs));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else
                    return JSON_MAPPER.readValue(resultString, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Integer.class));
            }
        }
    }

    /**
     * Learn a ML model for a given training file and stores it in the given model file.
     * The training file should be comma separated and containing a header.
     * The class attribute should be named "target".
     *
     * @param trainFile the train file
     * @param modelFile where to store the model
     * @param cv        number of cross validations
     * @param jobs      number of parallel jobs to run
     * @throws Exception throws exception in case of errors
     */
    public void trainAndStoreMLModel(File trainFile, File modelFile, int cv, int jobs) throws Exception {
        HttpGet request = new HttpGet(serverUrl + "/ml-train-and-store-model");
        request.addHeader("trainingsFile", getCanonicalPath(trainFile));
        request.addHeader("modelFile", modelFile.getAbsolutePath());
        request.addHeader("cv", Integer.toString(cv));
        request.addHeader("jobs", Integer.toString(jobs));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                }
            }
        }
    }

    /**
     * Apply a stored model to a new file (predict file).
     *
     * @param predictFile the predict file
     * @param modelFile   where to store the model
     * @return a list of integers which represents the classes
     * @throws Exception throws exception in case of errors
     */
    public List<Integer> applyStoredMLModel(File modelFile, File predictFile) throws Exception {
        HttpGet request = new HttpGet(serverUrl + "/ml-load-and-apply-model");
        request.addHeader("predictFile", getCanonicalPath(predictFile));
        request.addHeader("modelFile", getCanonicalPath(modelFile));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else
                    return JSON_MAPPER.readValue(resultString, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Integer.class));
            }
        }
    }

    /* ***********************************
     * Embedding alignment
     * **********************************/

    /**
     * Align two knowledge graph embeddings
     *
     * @param vectorPathSource the source path to a vector file
     * @param vectorPathTarget the target path to a vector file
     * @param function         function which is used to translate the embeddings
     * @param alignment        the alignment with initial mapping
     * @return alignment
     * @throws Exception in case of errors
     */
    public Alignment alignModel(String vectorPathSource, String vectorPathTarget, String function, Alignment alignment) throws Exception {
        ObjectNode root = JSON_MAPPER.createObjectNode();
        root.put("vectorPathSource", vectorPathSource);
        root.put("vectorPathTarget", vectorPathTarget);
        root.put("function", function);
        ArrayNode array = root.putArray("alignment");
        for (Correspondence c : alignment) {
            array.addArray().add(c.getEntityOne()).add(c.getEntityTwo());
        }
        String jsonContent = JSON_MAPPER.writeValueAsString(root);

        HttpPost request = new HttpPost(serverUrl + "/align-embeddings");
        request.setEntity(new StringEntity(jsonContent, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else return parseJSON(resultString);
            }
        }
    }

    private Alignment parseJSON(String resultString) throws Exception {
        JsonNode array = JSON_MAPPER.readTree(resultString);
        Alignment alignment = new Alignment();
        for (JsonNode element : array) {
            alignment.add(element.get(0).asText(), element.get(1).asText(), element.get(2).asDouble());
        }
        return alignment;
    }


    /* ***********************************
     * Vector space model
     * **********************************/

    /**
     * Method to train a vector space model. The file for the training (i.e., csv file where first column is id and second column text) has to
     * exist already.
     *
     * @param modelPath        identifier for the model (used for querying a specific model
     * @param trainingFilePath The file path to the file that shall be used for training.
     */
    public void trainVectorSpaceModel(String modelPath, String trainingFilePath) {
        HttpGet request = new HttpGet(serverUrl + "/train-vector-space-model");
        request.addHeader("input_file_path", getCanonicalPath(trainingFilePath));
        request.addHeader("model_path", modelPath);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }
    }

    /**
     * Method to query a vector space model (which has to be trained with trainVectorSpaceModel).
     *
     * @param modelPath     identifier for the model (used for querying a specific model
     * @param documentIdOne Document id for the first document
     * @param documentIdTwo Document id for the second document
     * @return The cosine similarity in the vector space between the two documents.
     * @throws Exception Thrown if there are server problems.
     */
    public double queryVectorSpaceModel(String modelPath, String documentIdOne, String documentIdTwo) throws Exception {
        HttpGet request = new HttpGet(serverUrl + "/query-vector-space-model");
        request.addHeader("model_path", modelPath);
        request.addHeader("document_id_one", documentIdOne);
        request.addHeader("document_id_two", documentIdTwo);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else return Double.parseDouble(resultString);
            }
        }
    }

    /**
     * Method to query a vector space model (which has to be trained with trainVectorSpaceModel) in a batch mode.
     *
     * @param modelPath identifier for the model (used for querying a specific model
     * @param alignment the alignment which contains the source and target uris
     * @return The cosine similarities in the vector space between the requested documents in the same order .
     * @throws Exception Thrown if there are server problems.
     */
    public List<Double> queryVectorSpaceModel(String modelPath, List<Correspondence> alignment) throws Exception {
        ObjectNode root = JSON_MAPPER.createObjectNode();
        root.put("modelPath", modelPath);
        //root.putPOJO("documentIds", documentIds);
        ArrayNode array = root.putArray("documentIds");
        for (Correspondence c : alignment) {
            array.addArray()
                    .add(c.getEntityOne())
                    .add(c.getEntityTwo());
        }
        String jsonContent = JSON_MAPPER.writeValueAsString(root);

        HttpPost request = new HttpPost(serverUrl + "/query-vector-space-model-batch");
        request.setEntity(new StringEntity(jsonContent, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else
                    return JSON_MAPPER.readValue(resultString, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Double.class));
            }
        }
    }

    /**
     * Method to query a vector space model (which has to be trained with trainVectorSpaceModel) in a batch mode.
     *
     * @param modelPath identifier for the model (used for querying a specific model
     * @param alignment the alignment which contains the source and target uris
     * @return The alignment where the confidence is updated if possible
     * @throws Exception Thrown if there are server problems.
     */
    public Alignment queryVectorSpaceModel(String modelPath, Alignment alignment) throws Exception {
        //make the order explicit
        List<Correspondence> list = new ArrayList<>();
        for (Correspondence c : alignment) {
            list.add(c);
        }
        List<Double> confidences = queryVectorSpaceModel(modelPath, list);
        if (confidences.size() != list.size())
            throw new Exception("Size of result list of confidences is not equal to initial list of correspondences.");

        Alignment a = new Alignment();
        for (int i = 0; i < list.size(); i++) {
            Correspondence c = list.get(i);
            Double conf = confidences.get(i);
            if (conf <= 1.1 && conf >= -1.1) { // between -1.0 and 1.0 (.1 because of rounding)
                a.add(c.getEntityOne(), c.getEntityTwo(), conf);
            } else {
                a.add(c.getEntityOne(), c.getEntityTwo(), c.getConfidence());
            }
        }
        return a;
    }


    /* ***********************************
     * doc2vec model
     * **********************************/

    /**
     * Method to train a doc2vec model. The file for the training (i.e., csv file where first column is id and second colum text) has to
     * exist already.
     *
     * @param modelPath        identifier for the model (used for querying a specific model
     * @param trainingFilePath The file path to the file that shall be used for training.
     * @param configuration    the configuration for the doc2vec model
     */
    public void trainDoc2VecModel(String modelPath, String trainingFilePath, Word2VecConfiguration configuration) {
        HttpGet request = new HttpGet(serverUrl + "/train-doc2vec-model");
        request.addHeader("input_file_path", getCanonicalPath(trainingFilePath));
        request.addHeader("model_path", modelPath);

        request.addHeader("vector_dimension", "" + configuration.getVectorDimension());
        request.addHeader("min_count", "" + configuration.getMinCount());
        request.addHeader("number_of_threads", "" + configuration.getNumberOfThreads());
        request.addHeader("window_size", "" + configuration.getWindowSize());
        request.addHeader("iterations", "" + configuration.getIterations());
        request.addHeader("negatives", "" + configuration.getNegatives());
        request.addHeader("cbow_or_sg", configuration.getType().toString());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }
    }

    /**
     * Method to query a doc2vec model (which has to be trained with trainDoc2VecModel) in a batch mode.
     *
     * @param modelPath identifier for the model (used for querying a specific model
     * @param alignment the alignment which contains the source and target uris
     * @return The cosine similarities in the doc2vec space between the requested documents in the same order .
     * @throws Exception Thrown if there are server problems.
     */
    public List<Double> queryDoc2VecModel(String modelPath, List<Correspondence> alignment) throws Exception {
        ObjectNode root = JSON_MAPPER.createObjectNode();
        root.put("modelPath", modelPath);
        //root.putPOJO("documentIds", documentIds);
        ArrayNode array = root.putArray("documentIds");
        for (Correspondence c : alignment) {
            array.addArray()
                    .add(c.getEntityOne())
                    .add(c.getEntityTwo());
        }
        String jsonContent = JSON_MAPPER.writeValueAsString(root);

        HttpPost request = new HttpPost(serverUrl + "/query-doc2vec-model-batch");
        request.setEntity(new StringEntity(jsonContent, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                //LOGGER.info("query-doc2vec result: {}", resultString);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else
                    return JSON_MAPPER.readValue(resultString, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Double.class));
            }
        }
    }



    /* ***********************************
     * Word2vec model
     * **********************************/

    /**
     * Method to train a word2vec model. The file for the training (i.e., file with sentences, paths etc.) has to
     * exist already.
     *
     * @param modelOrVectorPath If a vector file is desired, the file ending '.kv' is required.
     * @param trainingFilePath  The file path to the file that shall be used for training or to the directory containing the files that shall be used.
     * @param configuration     The configuration for the training operation.
     * @return True if training succeeded, else false.
     */
    public boolean trainWord2VecModel(String modelOrVectorPath, String trainingFilePath, Word2VecConfiguration configuration) {
        HttpGet request = new HttpGet(serverUrl + "/train-word2vec");
        if (modelOrVectorPath.endsWith(".kv")) {
            request.addHeader("vector_path", modelOrVectorPath);
            request.addHeader("model_path", modelOrVectorPath.substring(0, modelOrVectorPath.length() - 3));
        } else {
            request.addHeader("model_path", modelOrVectorPath);
            request.addHeader("vector_path", modelOrVectorPath + ".kv");
        }

        request.addHeader("file_path", getCanonicalPath(trainingFilePath));
        request.addHeader("vector_dimension", "" + configuration.getVectorDimension());
        request.addHeader("number_of_threads", "" + configuration.getNumberOfThreads());
        request.addHeader("window_size", "" + configuration.getWindowSize());
        request.addHeader("iterations", "" + configuration.getIterations());
        request.addHeader("negatives", "" + configuration.getNegatives());
        request.addHeader("cbow_or_sg", configuration.getType().toString());
        request.addHeader("min_count", "" + configuration.getMinCount());
        request.addHeader("sample", "" + configuration.getSample());
        request.addHeader("epochs", "" + configuration.getEpochs());

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                LOGGER.error("No server response.");
                return false;
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    LOGGER.error(resultString);
                    return false;
                } else return Boolean.parseBoolean(resultString);
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
            return false;
        }
    }

    /**
     * Ge the similarity given 2 concepts and a gensim model.
     *
     * @param concept1          First concept.
     * @param concept2          Second concept.
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *                          order to be recognized as vector file.
     * @return -1.0 in case of failure, else similarity.
     */
    public double getSimilarity(String concept1, String concept2, String modelOrVectorPath) {
        if (isVectorCaching) {
            // caching is enabled: do not use gensim library but cache vectors and calculate in java on demand
            Double[] v1 = getVector(concept1, modelOrVectorPath);
            Double[] v2 = getVector(concept2, modelOrVectorPath);
            if (v1 != null && v2 != null) {
                return cosineSimilarity(v1, v2);
            }
        } else {
            HttpGet request = new HttpGet(serverUrl + "/get-similarity");
            request.addHeader("concept_1", concept1);
            request.addHeader("concept_2", concept2);
            addModelToRequest(request, modelOrVectorPath);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    LOGGER.error("No server response.");
                    return -1.0;
                } else {
                    String resultString = EntityUtils.toString(entity);
                    if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                        LOGGER.error(resultString);
                    } else return Double.parseDouble(resultString);
                }
            } catch (IOException ioe) {
                LOGGER.error("Problem with http request.", ioe);
            }
        }
        // failure case
        return -1.0;
    }

    /**
     * Returns the vector of a concept.
     *
     * @param concept           The concept for which the vector shall be obtained.
     * @param modelOrVectorPath The model path or vector file path leading to the file to be used.
     * @return The vector for the specified concept.
     */
    public Double[] getVector(String concept, String modelOrVectorPath) {

        String v1key = concept + "-" + modelOrVectorPath;
        if (isVectorCaching) {
            if (vectorCache.containsKey(v1key)) {
                return vectorCache.get(v1key);
            }
        }

        HttpGet request = new HttpGet(serverUrl + "/get-vector");
        request.addHeader("concept", concept);
        addModelToRequest(request, modelOrVectorPath);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                LOGGER.error("No server response.");
                return null;
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    LOGGER.error(resultString);
                } else {
                    String[] tokenizedResult = resultString.split(" ");
                    Double[] result = new Double[tokenizedResult.length];
                    for (int i = 0; i < result.length; i++) {
                        try {
                            result[i] = Double.parseDouble(tokenizedResult[i]);
                        } catch (NumberFormatException nfe) {
                            LOGGER.error("Number format exception occurred on token: " + tokenizedResult[i], nfe);
                            // cannot return vector
                            if (isVectorCaching) {
                                vectorCache.put(v1key, null);
                            }
                            return null;
                        }
                    }
                    if (isVectorCaching) {
                        vectorCache.put(v1key, result);
                    }
                    return result;
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }
        if (isVectorCaching) {
            vectorCache.put(v1key, null);
        }
        return null;
    }

    /**
     * Returns true when the concept can be found in the vocabulary of the model.
     *
     * @param concept           The concept/URI that shall be looked up.
     * @param modelOrVectorPath The model or vector file. Note that the vector file MUST end with .kv in
     *                          order to be recognized as vector file.
     * @return True if exists, else false.
     */
    public boolean isInVocabulary(String concept, File modelOrVectorPath) {
        return isInVocabulary(concept, modelOrVectorPath.getAbsolutePath());
    }

    /**
     * Returns true when the concept can be found in the vocabulary of the model.
     *
     * @param concept           The concept/URI that shall be looked up.
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *                          order to be recognized as vector file.
     * @return True if exists, else false.
     */
    public boolean isInVocabulary(String concept, String modelOrVectorPath) {
        if (isVectorCaching) {
            return getVector(concept, modelOrVectorPath) != null;
        } else {
            HttpGet request = new HttpGet(serverUrl + "/is-in-vocabulary");
            request.addHeader("concept", concept);
            addModelToRequest(request, modelOrVectorPath);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    LOGGER.error("No server response.");
                    return false;
                } else {
                    String resultString = EntityUtils.toString(entity);
                    if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                        LOGGER.error(resultString);
                    } else return Boolean.parseBoolean(resultString);
                }
            } catch (IOException ioe) {
                LOGGER.error("Problem with http request.", ioe);
            }
            return false;
        }
    }

    /**
     * Returns the full vocabulary of the specified model as HashSet (e.g. for fast indexing).
     * Be aware that this operation can be very memory-consuming for very large models.
     * <p>
     * Note: If you want to just check whether a concept exists in the vocabulary, it is better to call
     * {@link PythonServer#isInVocabulary(String, String)}.Note further that you do not need to build your own
     * cache if the PythonServer has enabled vector caching (you can check this with {@link PythonServer#isVectorCaching()}.
     *
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *                          *                   order to be recognized as vector file.
     * @return Returns all vocabulary entries without vectors in a String HashSet.
     */
    public Set<String> getVocabularyTerms(String modelOrVectorPath) {
        HashSet<String> result = new HashSet<>();
        HttpGet request = new HttpGet(serverUrl + "/get-vocabulary-terms");
        addModelToRequest(request, modelOrVectorPath);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                LOGGER.error("No server response. Returning empty set.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    LOGGER.error(resultString);
                } else {
                    result.addAll(Arrays.asList(resultString.split("\\n")));
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request. Returning empty set.", ioe);
        } catch (Exception e) {
            LOGGER.error("Another exception occurred.", e);
        }
        return result;
    }

    /**
     * Writes the vocabulary of the given gensim model to a text file (UTF-8 encoded).
     *
     * @param modelOrVectorPath The model of which the vocabulary shall be obtained.
     * @param fileToWritePath   The file path of the file that shall be written.
     */
    public void writeVocabularyToFile(String modelOrVectorPath, String fileToWritePath) {
        Set<String> vocab = getVocabularyTerms(modelOrVectorPath);
        writeSetToFile(new File(fileToWritePath), vocab);
    }

    /**
     * Writes the vocabulary of the given gensim model to a text file (UTF-8 encoded).
     *
     * @param modelOrVectorPath The model of which the vocabulary shall be obtained.
     * @param fileToWrite       The file that shall be written.
     */
    public void writeVocabularyToFile(String modelOrVectorPath, File fileToWrite) {
        Set<String> vocab = getVocabularyTerms(modelOrVectorPath);
        writeSetToFile(fileToWrite, vocab);
    }

    /**
     * This method writes the content of a {@code Set<String>} to a file. The file will be UTF-8 encoded.
     *
     * @param fileToWrite File which will be created and in which the data will
     *                    be written.
     * @param setToWrite  Set whose content will be written into fileToWrite.
     * @param <T>         Type of the Set.
     */
    private static <T> void writeSetToFile(File fileToWrite, Set<T> setToWrite) {
        LOGGER.info("Start writing Set to file '" + fileToWrite.getName() + "'");
        Iterator<T> iterator = setToWrite.iterator();
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite), StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;
            while (iterator.hasNext()) {
                line = iterator.next().toString();
                if (!(line.equals("") || line.equals("\n"))) { // do not write empty lines or just line breaks
                    if (firstLine) {
                        writer.write(line);
                        firstLine = false;
                    } else {
                        writer.write("\n");
                        writer.write(line);
                    }
                }
            } // end while
            writer.flush();
            writer.close();
            LOGGER.info("Finished writing file '" + fileToWrite.getName() + "'");
        } catch (IOException e) {
            LOGGER.error("Could not write file.", e);
        }
    }

    /**
     * Given a path to a model or vector file, this method determines whether it is a model or a vector file and
     * adds the corresponding parameter to the request.
     *
     * @param request           The request to which the model/vector file shall be added to.
     * @param modelOrVectorPath The path to the model/vector file.
     */
    private void addModelToRequest(HttpGet request, String modelOrVectorPath) {
        if (modelOrVectorPath.endsWith(".kv")) {
            request.addHeader("vector_path", getCanonicalPath(modelOrVectorPath));
        } else request.addHeader("model_path", getCanonicalPath(modelOrVectorPath));
    }

    /**
     * Obtain the canonical model path.
     *
     * @param filePath The path to the gensim model or gensim vector file.
     * @return The canonical model path as String.
     */
    private String getCanonicalPath(String filePath) {
        return getCanonicalPath(new File(filePath));
    }

    /**
     * Obtain the canonical model path.
     *
     * @param file the file to get the canonical path from
     * @return The canonical path as String.
     */
    private String getCanonicalPath(File file) {
        if (!file.exists()) {
            LOGGER.warn("The specified path does not exist: {}", file);
        }
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.warn("Could not retrieve canonical path of file. Use absolute path instead");
            return file.getAbsolutePath();
        }
    }
    
    
    public List<Integer> runGroupShuffleSplit(List<Integer> groups, double trainSize) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("trainSize", trainSize);
        map.put("groups", groups);
        String jsonContent = JSON_MAPPER.writeValueAsString(map);

        HttpPost request = new HttpPost(serverUrl + "/run-group-shuffle-split");
        request.setEntity(new StringEntity(jsonContent, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else
                    return JSON_MAPPER.readValue(resultString, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Integer.class));
            }
        }
        
        
    }

    /**
     * A quick technical demo. If the service works, it will print "Hello {@code name}".
     *
     * @param name The name that shall be printed.
     */
    private void printHello(String name) {
        HttpGet request = new HttpGet(serverUrl + "/hello");
        request.addHeader("name", name);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) System.out.println(EntityUtils.toString(entity));
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }
    }
    
    private String runRequest(HttpUriRequest request) throws PythonServerException{
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new PythonServerException("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new PythonServerException(resultString);
                } else return resultString;
            }
        } catch (IOException ex) {
            throw new PythonServerException("Could not execute python server request", ex);
        }
    }

    /**
     * Instance (singleton pattern.
     */
    private static PythonServer instance;

    /**
     * Client to communicate with the server.
     */
    private static CloseableHttpClient httpClient;

    /**
     * Get the instance.
     *
     * @return Gensim instance.
     */
    public static PythonServer getInstance() {
        if (instance == null) instance = new PythonServer();
        if (isShutDown) instance.startServer();
        return instance;
    }

    /**
     * Get the instance (singleton pattern).
     *
     * @param resourcesDirectory Directory where the files shall be copied to.
     * @return Gensim Instance
     */
    public static PythonServer getInstance(File resourcesDirectory) {
        if (instance == null) instance = new PythonServer();
        instance.setResourcesDirectory(resourcesDirectory);
        if (isShutDown) instance.startServer();
        return instance;
    }

    /**
     * Checks whether all Python requirements are installed and whether the server is functional.
     *
     * @return True if the server is fully functional, else false.
     */
    public static boolean checkRequirements() {
        PythonServer.getInstance();
        HttpGet request = new HttpGet(serverUrl + "/check-requirements");
        File requirementsFile = new File(DEFAULT_RESOURCES_DIRECTORY + "requirements.txt");
        if (!requirementsFile.exists()) {
            LOGGER.error("Could not find requirements file.");
            return false;
        }
        request.addHeader("requirements_file", requirementsFile.getAbsolutePath());
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String resultMessage = EntityUtils.toString(entity);
            System.out.println(resultMessage);
            if (resultMessage.contains("good to go")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
            return false;
        }
    }

    /**
     * Shut down the service.
     */
    public static void shutDown() {
        isShutDown = true;
        instance = null;
        try {
            if (httpClient != null)
                httpClient.close();
        } catch (IOException e) {
            LOGGER.error("Could not close client.", e);
        }
        if (serverProcess == null)
            return;
        if (serverProcess.isAlive()) {
            try {
                serverProcess.destroyForcibly().waitFor();
            } catch (InterruptedException ex) {
                LOGGER.error("Interruption while forcibly terminating python server process.", ex);
            }
        }
    }

    /**
     * The python process.
     */
    private static Process serverProcess;

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param baseDirectory The base directory.
     * @param resourceName  ie.: "/SmartLibrary.dll"
     */
    private void exportResource(File baseDirectory, String resourceName) {
        File destination = new File(baseDirectory, resourceName);
        if (!PythonServer.overridePythonFiles && destination.exists())
            return;

        // there must not be an OS-specific separator - a forward slash is strictly required here (getResourceAsStream)!
        try (InputStream stream = this.getClass().getResourceAsStream("/" + resourceName)) {
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }
            int readBytes;
            byte[] buffer = new byte[4096];
            try (OutputStream resStreamOut = new FileOutputStream(destination)) {
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Could not read/write resource file: " + resourceName + " (base directory: "
                    + baseDirectory.getAbsolutePath() + ")", ex);
        }
    }

    /**
     * Initializes the server.
     *
     * @return True if successful, else false.
     */
    private boolean startServer() {
        isShutDown = false;

        File serverResourceDirectory = this.resourcesDirectory;
        serverResourceDirectory.mkdirs();

        File sourceDirectory = new File(new File(
                this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()
        ).getParentFile().getParentFile().getParentFile(), "matching-ml-python/" + PYTHON_DIRECTORY_NAME);
        File destination = new File(serverResourceDirectory, PYTHON_DIRECTORY_NAME);
        try {
            FileUtils.copyDirectory(sourceDirectory, destination);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        exportResource(serverResourceDirectory, "requirements.txt");

        httpClient = HttpClients.createDefault(); // has to be re-instantiated
        String canonicalPath;
        File serverFile = new File(serverResourceDirectory, PYTHON_DIRECTORY_NAME + "/python_server_melt.py");
        try {
            if (!serverFile.exists()) {
                LOGGER.error("Server File does not exist. Cannot start server. ABORTING. Please make sure that " +
                        "the 'python_server_melt.py' file is placed in directory '" + DEFAULT_RESOURCES_DIRECTORY + "'.");
                return false;
            }
            canonicalPath = serverFile.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.error("Server File (" + serverFile.getAbsolutePath() + ") does not exist. " +
                    "Cannot start server. ABORTING.", e);
            return false;
        }
        String pythonCommand = getPythonCommand();
        List<String> command = new ArrayList<>(Arrays.asList(pythonCommand, canonicalPath));
        command.add("" + PythonServer.getPort());
        command.add(getLogLevel());
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(System.getProperty("user.dir")));
        updateEnvironmentPath(pb.environment(), pythonCommand);
        LOGGER.info("Start PythonServer in folder {} with command {}", pb.directory().toString(), String.join(" ", command));
        LOGGER.info("If the python command is \"wrong\" for your machine (e.g. python refers to python2), place a file under ./melt-resources/python_command.txt " +
                "with your command and create the melt-resources folder if it does not exist. This has to be done in the working directory - in case of " +
                "SEALS this would be in $SEALS_HOME.");

        try {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            serverProcess = pb.start();
            final int maxTrials = 10;
            for (int i = 0; i < maxTrials; i++) {
                HttpGet request = new HttpGet(serverUrl + "/melt_ml.html");
                CloseableHttpClient httpClient = HttpClients.createDefault();
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        LOGGER.info("Server is running.");
                        break;
                    }
                } catch (HttpHostConnectException hce) {
                    LOGGER.info("Server is not yet running. Waiting 5 seconds. Trial {} / {}", i + 1, maxTrials);
                    TimeUnit.SECONDS.sleep(5);
                } catch (IOException ioe) {
                    LOGGER.error("Problem with http request.", ioe);
                }
                httpClient.close();
                if (i == maxTrials - 1) {
                    LOGGER.error("Failed to start the python server after " + maxTrials + " trials.");
                    isHookStarted = false;
                    isShutDown = true;
                    return false;
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Could not start python server.", ex);
        } catch (InterruptedException e) {
            LOGGER.error("Could not wait for python server.", e);
        }
        vectorCache = new HashMap<>();

        // now: add shutdown hook in case the JVM is terminating
        if (!isHookStarted) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("JVM shutdown detected - close python server if still open.");
                shutDown();
                LOGGER.info("Shutdown completed.");
            }));
            isHookStarted = true;
        }
        return true;
    }

    private String getLogLevel() {
        if (LOGGER.isTraceEnabled() || LOGGER.isDebugEnabled()) {
            return "DEBUG";
        } else if (LOGGER.isInfoEnabled()) {
            return "INFO";
        } else if (LOGGER.isWarnEnabled()) {
            return "WARNING";
        } else if (LOGGER.isErrorEnabled()) {
            return "ERROR";
        } else {
            return "CRITICAL"; //actually not used by slf4j
        }
    }

    /**
     * Returns the python command which is extracted from {@code file melt-resources/python_command.txt}.
     *
     * @return The python executable path.
     */
    protected String getPythonCommand() {
        Path filePath = Paths.get(this.getResourcesDirectoryPath(), "python_command.txt");
        if (Files.exists(filePath)) {
            LOGGER.info("Python command file detected.");
            try {
                String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                fileContent = fileContent.replace("\r", "").replace("\n", "")
                        .replace("{File.pathSeparator}", File.pathSeparator)
                        .replace("{File.separator}", File.separator)
                        .trim();
                return fileContent;
            } catch (IOException ex) {
                LOGGER.warn("The file which should contain the python command could not be read.", ex);
            }
        }
        if (PythonServer.pythonCommandBackup != null && !PythonServer.pythonCommandBackup.isEmpty()) {
            return PythonServer.pythonCommandBackup;
        } else {
            return "python"; //the default
        }
    }

    /**
     * Updates the environment variable PATH with additional python needed directories like env/lib/bin
     *
     * @param environment   The environment to be changed.
     * @param pythonCommand The python executable path.
     */
    protected void updateEnvironmentPath(Map<String, String> environment, String pythonCommand) {
        String path = environment.getOrDefault("PATH", "");
        String additionalPaths = getPythonAdditionalPath(pythonCommand);
        if (!additionalPaths.isEmpty()) {
            if (!path.endsWith(File.pathSeparator))
                path += File.pathSeparator;
            path += additionalPaths;
        }
        environment.put("PATH", path);
    }

    /**
     * Returns a concatenated path of directories which can be used in the PATH variable.
     * It searches based on a python executable path, all bin directories within the python dir.
     *
     * @param pythonCommand The python executable path.
     * @return a concatenated path of directories which can be used in the PATH variable.
     */
    protected String getPythonAdditionalPath(String pythonCommand) {
        File f = new File(pythonCommand).getParentFile();
        if (f == null) {
            return "";
        }
        try {
            return Files.find(f.toPath(), 6, (path, attributes) -> attributes.isDirectory() && path.getFileName().toString().equals("bin"))
                    .map(path -> path.toAbsolutePath().toString())
                    .collect(Collectors.joining(File.pathSeparator));
        } catch (IOException ex) {
            LOGGER.info("Could not add more directories in path", ex);
            return "";
        }
    }

    /**
     * Calculate The cosine similarity between two vectors.
     *
     * @param vector1 First vector.
     * @param vector2 Second vector.
     * @return Cosine similarity as double.
     */
    public static double cosineSimilarity(Double[] vector1, Double[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 = norm1 + Math.pow(vector1[i], 2);
            norm2 = norm2 + Math.pow(vector2[i], 2);
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Writes the vectors to a human-readable text file.
     *
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *                          *                          order to be recognized as vector file.
     * @param fileToWrite       The file that will be written.
     */
    public void writeModelAsTextFile(String modelOrVectorPath, String fileToWrite) {
        writeModelAsTextFile(modelOrVectorPath, fileToWrite, null);
    }

    /**
     * Writes the vectors to a human-readable text file.
     *
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *                          *                          order to be recognized as vector file.
     * @param fileToWrite       The file that will be written.
     * @param entityFile        The vocabulary that shall appear in the text file (can be null if all words shall be written).
     *                          The file must contain one word per line. The contents must be a subset of the vocabulary.
     */
    public void writeModelAsTextFile(String modelOrVectorPath, String fileToWrite, String entityFile) {
        HttpGet request = new HttpGet(serverUrl + "/write-model-as-text-file");
        addModelToRequest(request, modelOrVectorPath);
        if (entityFile != null) {
            request.addHeader("entity_file", entityFile);
        }
        request.addHeader("file_to_write", fileToWrite);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                LOGGER.error("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    LOGGER.error(resultString);
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }
    }

    public File getResourcesDirectory() {
        return resourcesDirectory;
    }

    /**
     * Sets the python command programmatically. This is used when no external file python_command.txt is found.
     *
     * @param pythonCommandBackup the python command.
     */
    public static void setPythonCommandBackup(String pythonCommandBackup) {
        PythonServer.pythonCommandBackup = pythonCommandBackup.trim();
    }

    /**
     * If set to true, all python files (e.g. python server melt and requirements.txt file) will be overridden with every execution.
     * If you want to make changes to the python server (e.g. to develop and test a feature) you can set it to false.
     * Then all modifications to these files will not be changed.
     *
     * @param overrideFiles if true, override the python server files.
     */
    public static void setOverridePythonFiles(boolean overrideFiles) {
        PythonServer.overridePythonFiles = overrideFiles;
    }

    /**
     * Get the resource directory as String.
     *
     * @return Directory as String.
     */
    public String getResourcesDirectoryPath() {
        try {
            return this.resourcesDirectory.getCanonicalPath();
        } catch (IOException ioe) {
            LOGGER.error("Could not determine canonical path for resources directory. Returning default.");
            return "./melt-resources/";
        }
    }

    /**
     * Set the directory where the python files will be copied to.
     *
     * @param resourcesDirectory Must be a directory.
     */
    public void setResourcesDirectory(File resourcesDirectory) {
        if (!resourcesDirectory.exists()) resourcesDirectory.mkdir();
        if (!resourcesDirectory.isDirectory()) {
            LOGGER.error("The specified directory is no directory. Using default: '" + DEFAULT_RESOURCES_DIRECTORY + "'");
            resourcesDirectory = new File(DEFAULT_RESOURCES_DIRECTORY);
        }

        // check if python command file exists in default resources directory
        Path pythonCommandFilePath = Paths.get(DEFAULT_RESOURCES_DIRECTORY, "python_command.txt");
        if (Files.exists(pythonCommandFilePath)) {
            LOGGER.info("Python command file detected. Trying to copy file to external resources directory.");
            try {
                FileUtils.copyFile(pythonCommandFilePath.toFile(), new File(resourcesDirectory, "python_command.txt"));
            } catch (IOException e) {
                LOGGER.error("Could not copy python command file.", e);
            }
            LOGGER.info("Python command file successfully copied to external resources directory.");
        }
        this.resourcesDirectory = resourcesDirectory;
    }

    /**
     * Returns the size of the vocabulary of the stated model/vector set.
     *
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *                          order to be recognized as vector file.
     * @return -1 in case of an error else the size of the vocabulary.
     */
    public int getVocabularySize(String modelOrVectorPath) {
        HttpGet request = new HttpGet(serverUrl + "/get-vocabulary-size");
        addModelToRequest(request, modelOrVectorPath);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                LOGGER.error("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    LOGGER.error(resultString);
                } else return Integer.parseInt(resultString);
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }
        return -1;
    }

    /**
     * If true: enabled. Else: false.
     *
     * @return True if enabled, else false.
     */
    public boolean isVectorCaching() {
        return isVectorCaching;
    }

    /**
     * If vector caching is turned on, similarities will be calculated on Java site (rather than in Python) and
     * vectors are held in memories. Turn this function on, if you plan to do many computations with the same set
     * of vectors. This will increase the performance at the cost of memory.
     *
     * @param vectorCaching True if caching shall be enabled, else false.
     */
    public void setVectorCaching(boolean vectorCaching) {
        isVectorCaching = vectorCaching;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        if (instance != null) {
            LOGGER.error("Server is already running. The port cannot be changed.");
            return;
        }
        if (port > 0) {
            PythonServer.port = port;
        } else {
            LOGGER.error("You tried to set the port to a negative number. Using default: " + DEFAULT_PORT);
            PythonServer.port = DEFAULT_PORT;
        }
        PythonServer.serverUrl = "http://127.0.0.1:" + port;
    }

    public static String getServerUrl() {
        return serverUrl;
    }
}