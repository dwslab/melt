package de.uni_mannheim.informatik.dws.melt.matching_ml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

/**
 * A client class to communicate with python <a href="https://radimrehurek.com/gensim/">gensim</a> library.
 * Singleton pattern.
 * Communication is performed through HTTP requests.
 * In case you need a different python environment or python executable, create a file in directory python_server
 * named {@code python_command.txt} and write your absolute path of the python executable in that file.
 */
public class Gensim {

    /**
     * Default logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(Gensim.class);
    
    /**
     * Default resources directory (where the python files will be copied to by default) and where the resources
     * are read from within the JAR.
     */
    private static final String DEFAULT_RESOURCES_DIRECTORY = "./melt-resources/";

    /**
     * Objectmapper from jackson to generate JSON.
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    

    /**
     * Constructor
     */
    private Gensim() {
        // do nothing; do not start the server (yet)
    }

    /**
     * The URL that shall be used to perform the requests.
     */
    private String serverUrl = "http://127.0.0.1:41193";

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
     * This flag is required in order to have only one hook despite multiple reinitializations.
     */
    private boolean isHookStarted = false;
    
    /**
     * The directory where the python files will be copied to.
     */
    private File resourcesDirectory = new File(DEFAULT_RESOURCES_DIRECTORY);

    
    /************************************
     * Vector space model
     ***********************************/
    
    /**
     * Method to train a vector space model. The file for the training (i.e., csv file where first column is id and second column text) has to
     * exist already.
     * @param modelPath identifier for the model (used for querying a specific model
     * @param trainingFilePath The file path to the file that shall be used for training.
     */
    public void trainVectorSpaceModel(String modelPath, String trainingFilePath){
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
     * @param modelPath identifier for the model (used for querying a specific model
     * @param documentIdOne Document id for the first document
     * @param documentIdTwo Document id for the second document
     * @return The cosine similarity in the vector space between the two documents.
     * @throws Exception Thrown if there are server problems.
     */
    public double queryVectorSpaceModel(String modelPath, String documentIdOne, String documentIdTwo) throws Exception{
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
     * @param modelPath identifier for the model (used for querying a specific model
     * @param alignment the alignment which contains the source and target uris
     * @return The cosine similarities in the vector space between the requested documents in the same order .
     * @throws Exception Thrown if there are server problems.
     */
    public List<Double> queryVectorSpaceModel(String modelPath, List<Correspondence> alignment) throws Exception{
        ObjectNode root = JSON_MAPPER.createObjectNode();
        root.put("modelPath", modelPath);
        //root.putPOJO("documentIds", documentIds);
        ArrayNode array = root.putArray("documentIds");
        for(Correspondence c : alignment){
            array.addArray()
                    .add(c.getEntityOne())
                    .add(c.getEntityTwo());
        }       
        String jsonContent = JSON_MAPPER.writeValueAsString(root);
        
        HttpPost request = new HttpPost(serverUrl + "/query-vector-space-model-batch");
        request.setEntity(new StringEntity(jsonContent,ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else return JSON_MAPPER.readValue(resultString, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Double.class));
            }
        }
    }
    
    /**
     * Method to query a vector space model (which has to be trained with trainVectorSpaceModel) in a batch mode.
     * @param modelPath identifier for the model (used for querying a specific model
     * @param alignment the alignment which contains the source and target uris
     * @return The alignment where the confidence is updated if possible
     * @throws Exception Thrown if there are server problems.
     */
    public Alignment queryVectorSpaceModel(String modelPath, Alignment alignment) throws Exception{
        //make the order explicit
        List<Correspondence> list = new ArrayList<>();
        for(Correspondence c : alignment){
            list.add(c);
        }
        List<Double> confidences = queryVectorSpaceModel(modelPath, list);
        if(confidences.size() != list.size())
            throw new Exception("Size of result list of confidences is not equal to initial list of correspondences.");
        
        Alignment a = new Alignment();        
        for(int i=0; i < list.size(); i++){
            Correspondence c = list.get(i);
            Double conf = confidences.get(i);            
            if(conf <= 1.1 && conf >= -1.1){ // between -1.0 and 1.0 (.1 because of rounding)
                a.add(c.getEntityOne(), c.getEntityTwo(), conf);
            }else{
                a.add(c.getEntityOne(), c.getEntityTwo(), c.getConfidence());
            }
        }
        return a;
    }
    
    
    /************************************
     * doc2vec model
     ***********************************/
    
    /**
     * Method to train a doc2vec model. The file for the training (i.e., csv file where first column is id and second colum text) has to
     * exist already.
     * @param modelPath identifier for the model (used for querying a specific model
     * @param trainingFilePath The file path to the file that shall be used for training.
     * @param configuration the configuration for the doc2vec model
     */
    public void trainDoc2VecModel(String modelPath, String trainingFilePath, Word2VecConfiguration configuration){
        HttpGet request = new HttpGet(serverUrl + "/train-doc2vec-model");
        request.addHeader("input_file_path", getCanonicalPath(trainingFilePath));
        request.addHeader("model_path", modelPath);
        
        request.addHeader("vector_dimension", "" + configuration.getVectorDimension());
        request.addHeader("min_count", "" + configuration.getMinCount());
        request.addHeader("number_of_threads", "" + configuration.getNumberOfThreads());
        request.addHeader("window_size", "" + configuration.getWindowSize());
        request.addHeader("iterations", "" + configuration.getIterations());
        request.addHeader("negatives", "" + configuration.getNegatives());
        request.addHeader("cbow_or_sg", configuration.toString());
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }
    }
    
    /**
     * Method to query a doc2vec model (which has to be trained with trainDoc2VecModel) in a batch mode.
     * @param modelPath identifier for the model (used for querying a specific model
     * @param alignment the alignment which contains the source and target uris
     * @return The cosine similarities in the doc2vec space between the requested documents in the same order .
     * @throws Exception Thrown if there are server problems.
     */
    public List<Double> queryDoc2VecModel(String modelPath, List<Correspondence> alignment) throws Exception{
        ObjectNode root = JSON_MAPPER.createObjectNode();
        root.put("modelPath", modelPath);
        //root.putPOJO("documentIds", documentIds);
        ArrayNode array = root.putArray("documentIds");
        for(Correspondence c : alignment){
            array.addArray()
                    .add(c.getEntityOne())
                    .add(c.getEntityTwo());
        }
        String jsonContent = JSON_MAPPER.writeValueAsString(root);
        
        HttpPost request = new HttpPost(serverUrl + "/query-doc2vec-model-batch");
        request.setEntity(new StringEntity(jsonContent,ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No server response.");
            } else {
                String resultString = EntityUtils.toString(entity);
                //LOGGER.info("query-doc2vec result: {}", resultString);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    throw new Exception(resultString);
                } else return JSON_MAPPER.readValue(resultString, JSON_MAPPER.getTypeFactory().constructCollectionType(List.class, Double.class));
            }
        }
    }
    
    
    
    /************************************
     * Word2vec model
     ***********************************/

    /**
     * Method to train a word2vec model. The file for the training (i.e., file with sentences, paths etc.) has to
     * exist already.
     * @param modelOrVectorPath If a vector file is desired, the file ending '.kv' is required.
     * @param trainingFilePath The file path to the file that shall be used for training.
     * @param configuration The configuration for the training operation.
     * @return True if training succeeded, else false.
     */
    public boolean trainWord2VecModel(String modelOrVectorPath, String trainingFilePath, Word2VecConfiguration configuration){
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
        request.addHeader("cbow_or_sg", configuration.toString());

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
                return this.cosineSimilarity(v1, v2);
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
     * @param concept The concept for which the vector shall be obtained.
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
                            LOGGER.error("Number format exception occured on token: " + tokenizedResult[i], nfe);
                            // cannot return vector
                            if(isVectorCaching) {
                                vectorCache.put(v1key, null);
                            }
                            return null;
                        }
                    }
                    if(isVectorCaching) {
                        vectorCache.put(v1key, result);
                    }
                    return result;
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }
        if(isVectorCaching) {
            vectorCache.put(v1key, null);
        }
        return null;
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
        File modelFile = new File(filePath);
        if (!modelFile.exists() || modelFile.isDirectory()) {
            LOGGER.error("ERROR: The specified model path does not exist or is a directory.");
            return filePath;
        }
        try {
            return modelFile.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.error("Could not derive canonical model path.", e);
            return filePath;
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

    /**
     * Instance (singleton pattern.
     */
    private static Gensim instance;

    /**
     * Client to communicate with the server.
     */
    private static CloseableHttpClient httpClient;

    /**
     * Get the instance.
     *
     * @return Gensim instance.
     */
    public static Gensim getInstance() {
        if (instance == null) instance = new Gensim();
        if (isShutDown) instance.startServer();
        return instance;
    }

    /**
     * Get the instance (singleton pattern).
     * @param resourcesDirectory Directory where the files shall be copied to.
     * @return Gensim Instance
     */
    public static Gensim getInstance(File resourcesDirectory){
        if (instance == null) instance = new Gensim();
        instance.setResourcesDirectory(resourcesDirectory);
        if (isShutDown) instance.startServer();
        return instance;
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
     * @param resourceName ie.: "/SmartLibrary.dll"
     */
    private void exportResource(File baseDirectory, String resourceName) {
        try (InputStream stream = this.getClass().getResourceAsStream("/" + resourceName)){
            if(stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }
            int readBytes;
            byte[] buffer = new byte[4096];
            try (OutputStream resStreamOut = new FileOutputStream(new File(baseDirectory,resourceName))){
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
     */
    private boolean startServer() {
        isShutDown = false;
        
        File serverResourceDirectory = this.resourcesDirectory;
        serverResourceDirectory.mkdirs();

        exportResource(serverResourceDirectory, "python_server.py");
        exportResource(serverResourceDirectory, "requirements.txt");

        httpClient = HttpClients.createDefault(); // has to be re-instantiated
        String canonicalPath;
        File serverFile = new File(serverResourceDirectory, "python_server.py");
        try {
            if (!serverFile.exists()) {
                LOGGER.error("Server File does not exist. Cannot start server. ABORTING. Please make sure that " +
                        "the 'python_server.py' file is placed in directory '" + DEFAULT_RESOURCES_DIRECTORY + "'.");
                return false;
            }
            canonicalPath = serverFile.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.error("Server File (" + serverFile.getAbsolutePath() + ") does not exist. " +
                    "Cannot start server. ABORTING.", e);
            return false;
        }
        String pythonCommand = getPythonCommand();
        List<String> command = Arrays.asList(pythonCommand, canonicalPath);
        ProcessBuilder pb = new ProcessBuilder(command);
        updateEnvironmentPath(pb.environment(), pythonCommand);
        //List<String> command = Arrays.asList("python", "--version");
        //ProcessBuilder pb = new ProcessBuilder(command);
        //pb.environment().put("PATH", "{FOLDER CONTAINING PYTHON EXE}" + File.pathSeparator + pb.environment().get("PATH"));
        try {
            pb.inheritIO();
            this.serverProcess = pb.start();
            final int maxTrials = 4;
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
                    LOGGER.info("Server is not yet running. Waiting 5 seconds. Trial {} / 4", i + 1);
                    TimeUnit.SECONDS.sleep(5);
                } catch (IOException ioe) {
                    LOGGER.error("Problem with http request.", ioe);
                }
                httpClient.close();
                if (i == maxTrials -1){
                    LOGGER.error("Failed to start the gensim server after " + maxTrials + " trials.");
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
        if(!isHookStarted) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("JVM shutdown detected - close python server if still open.");
                shutDown();
                LOGGER.info("Shutdown completed.");
            }));
            isHookStarted = true;
        }
        return true;
    }

    /**
     * Returns the python command which is extracted from {@code file melt-resources/python_command.txt}.
     * @return The python executable path.
     */
    protected String getPythonCommand(){
        String pythonCommand = "python";
        Path filePath = Paths.get(this.getResourcesDirectoryPath(), "python_command.txt");
        if(Files.exists(filePath)){
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
        return pythonCommand;
    }

    /**
     * Updates the environment variable PATH with additional python needed directories like env/lib/bin
     * @param environment The environment to be changed.
     * @param pythonCommand The python executable path.
     */
    protected void updateEnvironmentPath(Map<String,String> environment, String pythonCommand){
        String path = environment.getOrDefault("PATH", "");
        String additionalPaths = getPythonAdditionalPath(pythonCommand);
        if(additionalPaths.isEmpty() == false){
            if(path.endsWith(File.pathSeparator) == false)
                path += File.pathSeparator;
            path += additionalPaths;
        }
        environment.put("PATH",  path);
    }

    /**
     * Returns a concatenated path of directories which can be used in the PATH variable.
     * It searches based on a python executable path, all bin directories within the python dir.
     * @param pythonCommand the python executable path
     * @return a concatenated path of directories which can be used in the PATH variable
     */
    protected String getPythonAdditionalPath(String pythonCommand){
        File f = new File(pythonCommand).getParentFile();
        if(f == null){
            return "";
        }
        try {
            String s = Files.find(f.toPath(), 6, (path, attributes) -> attributes.isDirectory() && path.getFileName().toString().equals("bin"))
                    .map(path->path.toAbsolutePath().toString())
                    .collect(Collectors.joining(File.pathSeparator));
            return s;
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
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *      *                          order to be recognized as vector file.
     * @param fileToWrite The file that will be written.
     */
    public void writeModelAsTextFile(String modelOrVectorPath, String fileToWrite){
        writeModelAsTextFile(modelOrVectorPath, fileToWrite, null);
    }

    /**
     * Writes the vectors to a human-readable text file.
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *      *                          order to be recognized as vector file.
     * @param fileToWrite The file that will be written.
     * @param entityFile The vocabulary that shall appear in the text file (can be null if all words shall be written).
     *                   The file must contain one word per line. The contents must be a subset of the vocabulary.
     */
    public void writeModelAsTextFile(String modelOrVectorPath, String fileToWrite, String entityFile){
        HttpGet request = new HttpGet(serverUrl + "/write-model-as-text-file");
        addModelToRequest(request, modelOrVectorPath);
        if(entityFile != null) {
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
     * Get the resource directory as String.
     * @return Directory as String.
     */
    public String getResourcesDirectoryPath() {
        try {
            return this.resourcesDirectory.getCanonicalPath();
        } catch (IOException ioe){
            LOGGER.error("Could not determine canonical path for resources directory. Returning default.");
            return "./melt-resources/";
        }
    }

    /**
     * Set the directory where the python files will be copied to.
     * @param resourcesDirectory Must be a directory.
     */
    public void setResourcesDirectory(File resourcesDirectory) {
        if(!resourcesDirectory.exists()) resourcesDirectory.mkdir();
        if(!resourcesDirectory.isDirectory()){
            LOGGER.error("The specified directory is no directory. Using default: '" + DEFAULT_RESOURCES_DIRECTORY + "'");
            resourcesDirectory = new File(DEFAULT_RESOURCES_DIRECTORY);
        }

        // check if python command file exists in default resources directory
        Path pythonCommandFilePath = Paths.get(DEFAULT_RESOURCES_DIRECTORY, "python_command.txt");
        if(Files.exists(pythonCommandFilePath)){
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
     * If true: enabled. Else: false.
     * @return True if enabled, else false.
     */
    public boolean isVectorCaching() {
        return isVectorCaching;
    }

    /**
     * If vector caching is turned on, similarities will be calculated on Java site (rather than in Python) and
     * vectors are held in memories. Turn this function on, if you plan to do many computations with the same set
     * of vectors. This will increase the performance at the cost of memory.
     * @param vectorCaching True if caching shall be enabled, else false.
     */
    public void setVectorCaching(boolean vectorCaching) {
        isVectorCaching = vectorCaching;
    }
}