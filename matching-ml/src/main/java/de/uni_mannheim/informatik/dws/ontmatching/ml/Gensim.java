package de.uni_mannheim.informatik.dws.ontmatching.ml;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A client class to communicate with python gensim library.
 * Singleton pattern.
 * Communication is performed through HTTP requests.
 */
public class Gensim {

    /**
     * Default logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(Gensim.class);

    /**
     * Contructor
     */
    private Gensim(){
        startServer();
    }

    /**
     * The URL that shall be used to perform the requests.
     */
    private String serverUrl = "http://127.0.0.1:41193";

    /**
     * Ge the similarity given 2 concepts and a gensim model.
     * @param concept1 First concept.
     * @param concept2 Second concept.
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *                          order to be recognized as vector file.
     * @return -1.0 in case of failure, else similarity.
     */
    public double getSimilarity(String concept1, String concept2, String modelOrVectorPath){
        HttpGet request = new HttpGet(serverUrl + "/get-similarity");
        request.addHeader("concept_1", concept1);
        request.addHeader("concept_2", concept2);
        addModelToRequest(request, modelOrVectorPath);

        try (CloseableHttpResponse response = httpClient.execute(request)){
            HttpEntity entity = response.getEntity();
            if (entity == null){
                LOGGER.error("No server response.");
                return -1.0;
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")){
                    LOGGER.error(resultString);
                } else return Double.parseDouble(resultString);
            }
        } catch (IOException ioe){
            LOGGER.error("Problem with http request.", ioe);
        }
        return -1.0;
    }


    /**
     * Returns true when the concept can be found in the vocabulary of the model.
     * @param concept The concept/URI that shall be looked up.
     * @param modelOrVectorPath The path to the model or vector file. Note that the vector file MUST end with .kv in
     *                          order to be recognized as vector file.
     * @return True if exists, else false.
     */
    public boolean isInVocabulary(String concept, String modelOrVectorPath){
        HttpGet request = new HttpGet(serverUrl + "/is-in-vocabulary");
        request.addHeader("concept", concept);
        addModelToRequest(request, modelOrVectorPath);

        try (CloseableHttpResponse response = httpClient.execute(request)){
            HttpEntity entity = response.getEntity();
            if (entity == null){
                LOGGER.error("No server response.");
                return false;
            } else {
                String resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")){
                    LOGGER.error(resultString);
                } else return Boolean.parseBoolean(resultString);
            }
        } catch (IOException ioe){
            LOGGER.error("Problem with http request.", ioe);
        }
        return false;
    }


    /**
     * Given a path to a model or vector file, this method determines whether it is a model or a vector file and
     * adds the corresponding parameter to the request.
     * @param request The request to which the model/vector file shall be added to.
     * @param modelOrVectorPath The path to the model/vector file.
     */
    private void addModelToRequest(HttpGet request, String modelOrVectorPath){
        if(modelOrVectorPath.endsWith(".kv")){
            request.addHeader("vector_path", getCanonicalPath(modelOrVectorPath));
        } else request.addHeader("model_path", getCanonicalPath(modelOrVectorPath));
    }

    /**
     * Obtain the canonical model path.
     * @param filePath The path to the gensim model or gensim vector file.
     * @return The canonical model path as String.
     */
    private String getCanonicalPath(String filePath){
        File modelFile = new File(filePath);
        if(!modelFile.exists() || modelFile.isDirectory()){
            LOGGER.error("ERROR: The specified model path does not exist or is a directory.");
            return filePath;
        }
        try {
            return modelFile.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.error("Could not derive canoncial model path.", e);
            return filePath;
        }
    }

    /**
     * A quick technical demo. If the service works, it will print "Hello {@code name}".
     * @param name The name that shall be printed.
     */
    private void printHello(String name){
        HttpGet request = new HttpGet(serverUrl + "/hello");
        request.addHeader("name", name);
        try (CloseableHttpResponse response = httpClient.execute(request)){
            HttpEntity entity = response.getEntity();
            if (entity != null) System.out.println(EntityUtils.toString(entity));
        } catch (IOException ioe){
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
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * Get the instance.
     * @return Gensim instance.
     */
    public static Gensim getInstance(){
        if(instance == null) instance = new Gensim();
        return instance;
    }

    /**
     * Shut down the service.
      */
    public void shutDown(){
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.error("Could not close client.", e);
        }
        if(serverProcess == null)
            return;
        if(serverProcess.isAlive()){
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
    private Process serverProcess;


    /**
     * Initializes the server.
     */
    private void startServer(){
        String canonicalPath;
        File serverFile = new File("oaei-resources/python_server.py");
        try {
            if(!serverFile.exists()){
                LOGGER.error("Server File does not exist. Cannot start server. ABORTING.");
                return;
            }
            canonicalPath = serverFile.getCanonicalPath();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Server File does not exist. Cannot start server. ABORTING.");
            return;
        }
        List<String> command = Arrays.asList("python", canonicalPath);
        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            pb.inheritIO();
            this.serverProcess = pb.start();
            for(int i = 0; i < 3; i++){
                HttpGet request = new HttpGet(serverUrl + "/melt_ml.html");
                CloseableHttpClient httpClient = HttpClients.createDefault();
                try (CloseableHttpResponse response = httpClient.execute(request)){
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        LOGGER.info("Server is running.");
                        break;
                    }
                } catch (HttpHostConnectException hce){
                    LOGGER.info("Server is not yet running. Waiting 5 seconds.");
                    TimeUnit.SECONDS.sleep(5);
                } catch (IOException ioe){
                    LOGGER.error("Problem with http request.", ioe);
                }
                httpClient.close();
            }
        } catch (IOException ex) {
            LOGGER.error("Could not start python server.", ex);
        } catch (InterruptedException e) {
            LOGGER.error("Could not wait for python server.", e);
        }
    }
}
