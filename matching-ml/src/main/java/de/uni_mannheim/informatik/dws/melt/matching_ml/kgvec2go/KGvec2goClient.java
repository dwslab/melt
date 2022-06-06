package de.uni_mannheim.informatik.dws.melt.matching_ml.kgvec2go;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Calls the <a href="http://kgvec2go.org/">KGvec2go</a> service.
 */
public class KGvec2goClient {

    /**
     * Default logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(KGvec2goClient.class);

    /**
     * Protocol for requests.
     */
    public final static String PROTOCOL = "http";

    /**
     * Address of server url.
     */
    public final static String DOMAIN = "kgvec2go.org";

    /**
     * The URL that shall be used to perform the requests.
     */
    public final static String SERVER_URL = PROTOCOL + "://" + DOMAIN;

    /**
     * Local vector cache.
     */
    private static HashMap<String, Double[]> vectorCache;

    /**
     * Client to communicate with the server.
     */
    private static CloseableHttpClient httpClient;

    /**
     * Indicates whether the server has been shut down.
     * Initial state: shutDown.
     */
    private static boolean isShutDown = true;

    /**
     * Instance (singleton pattern).
     */
    private static KGvec2goClient instance;

    /**
     * Central gson instance to parse JSON.
     */
    private static Gson gson;

    /**
     * Get the instance.
     *
     * @return Client instance.
     */
    public synchronized static KGvec2goClient getInstance() {
        if (instance == null) instance = new KGvec2goClient();
        if (isShutDown) instance.startServer();
        return instance;
    }

    /**
     * Private constructor for singleton pattern.
     */
    private KGvec2goClient(){
    }

    /**
     * Start the server.
     * @return True in case of success, else false.
     */
    private boolean startServer(){
        isShutDown=false;
        vectorCache = new HashMap<>();
        httpClient = HttpClients.createDefault();
        gson = new Gson();
        return true;
    }

    /**
     * Shut down the server.
     */
    public void shutDown(){
        isShutDown = true;
        instance = null;
        try {
            if (httpClient != null)
                httpClient.close();
        } catch (IOException e) {
            LOGGER.error("Could not close client.", e);
        }
    }

    /**
     * Simple test whether the server can be reached and is responding.
     * @return True if server can be reached, else false.
     */
    public boolean isServiceAvailable(){
        String requestUrl = SERVER_URL;
        String resultString;

        HttpGet request = new HttpGet(requestUrl);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                LOGGER.error("No server response.");
            } else {
                resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error") || resultString.contains("Not Found")) {
                    LOGGER.error("A connection could be established but the KGvec2go main page (" + requestUrl + ") seems not to be working:\n", resultString);
                }
            }
            if(getVector("car", KGvec2goDatasets.ALOD) == null){
                LOGGER.error("The vector REST service cannot be reached. (Tried to get vector for \"car\" for the ALDO dataset.)");
                return false;
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request. The service seems to be unavailable. Make sure that your device " +
                    "is connected to the Internet.", ioe);
            return false;
        }
        return true;
    }

    /**
     * Receive a vector in the form of a double array.
     * @param word Word for lookup.
     * @param dataset Dataset for lookup.
     * @return Null in case of failure, else vector.
     */
    public Double[] getVector(String word, KGvec2goDatasets dataset){
        // sanity check
        if(word == null || word.trim().equals("") || dataset == null) {
            return null;
        }

        // check buffer
        if(isInBuffer(word, dataset)){
            return getFromBuffer(word, dataset);
        }

        // perform web request
        String requestUrlString = getEncodedURIString("/rest/get-vector/" + dataset.toString() + "/" + word);

        String resultString = null;
        HttpGet request = new HttpGet(requestUrlString);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                LOGGER.error("No server response.");
            } else {
                resultString = EntityUtils.toString(entity);
                if (resultString.startsWith("ERROR") || resultString.contains("500 Internal Server Error")) {
                    LOGGER.error(resultString);
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Problem with http request.", ioe);
        }

        try {
            if (resultString != null) {
                if (dataset != KGvec2goDatasets.WORDNET) {
                    KGvec2goVectorResponseEntity vre = gson.fromJson(resultString, KGvec2goVectorResponseEntity.class);
                    writeToBuffer(word, dataset, vre.vector);
                    return vre.vector;
                } else {
                    KGvec2goVectorResponseEntityArray vre = gson.fromJson(resultString, KGvec2goVectorResponseEntityArray.class);
                    if (vre == null || vre.result == null || vre.result.length == 0) {
                        writeToBuffer(word, dataset, null);
                        return null;
                    }
                    writeToBuffer(word, dataset, vre.result[0].vector);
                    return vre.result[0].vector;
                }
            }
        } catch (JsonSyntaxException jse){
            LOGGER.error("Syntax exception occurred with the following result string: " + resultString + "\nThe following request URL was used:\n" + requestUrlString, jse);
            // important: continue but do not write to buffer
            return null;
        }
        writeToBuffer(word, dataset, null);
        return null;
    }


    /**
     *
     * @param word1 Lookup word 1.
     * @param word2 Lookup word 2.
     * @param datasets Dataset to be used for lookup.
     * @return Similarity.
     */
    public Double getSimilarity(String word1, String word2, KGvec2goDatasets datasets){
        return cosineSimilarity(getVector(word1, datasets), getVector(word2, datasets));
    }


    /**
     * Look in the cache whether a vector already exists for the given word in the given dataset.
     * @param word for which the vector shall be looked up.
     * @param dataset in which shall be looked.
     * @return Null if not in cache, else vector.
     */
    private static Double[] getFromBuffer(String word, KGvec2goDatasets dataset){
        // TODO normalize word
        if(vectorCache.containsKey(dataset.toString() + "_" + word)) {
            return vectorCache.get(dataset.toString() + "_" + word);
        } else return null;
    }

    /**
     * Checks whether there is an entry for the specified word and dataset in the buffer.
     * @param word Word to be looked up.
     * @param dataset Dataset to be used.
     * @return False if not in buffer, else true.
     */
    private static boolean isInBuffer(String word, KGvec2goDatasets dataset){
        // TODO normalize word
        String key = dataset.toString() + "_" + word;
        return vectorCache.containsKey(key);
    }

    /**
     * Write in the cache whether a vector already exists for the given word in the given dataset.
     * @param word for which the vector shall be looked up.
     * @param dataset in which shall be looked.
     * @param vector to be written. Null if there is no match.
     */
    private static void writeToBuffer(String word, KGvec2goDatasets dataset, Double[] vector){
        // TODO normalize word
        String key = dataset.toString() + "_" + word;
        if(vectorCache.containsKey(key)) {
            LOGGER.warn(word + " already contained in the cache for dataset " + dataset.toString() + ".\nTrigger overwrite action.");
        }
        vectorCache.put(key, vector);
    }

    /**
     * Calculates the cosine similarity between two vectors.
     * @param vector1 First vector.
     * @param vector2 Second vector.
     * @return Cosine similarity as double.
     */
    public static Double cosineSimilarity(Double[] vector1, Double[] vector2){
        if(vector1 == null || vector2 == null){
            return null;
        }
        if(vector1.length != vector2.length){
            LOGGER.error("ERROR - the vectors must be of the same dimension.");
            throw new ArithmeticException("The vectors must be of the same dimension");
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 = norm1 +  Math.pow(vector1[i], 2);
            norm2 = norm2 + Math.pow(vector2[i], 2);
        }
        return dotProduct / ( Math.sqrt(norm1) * Math.sqrt(norm2) );
    }

    public static URI getEncodedURI(String path) throws URISyntaxException{
        URI uri = null;
        uri = new URI(PROTOCOL, DOMAIN, path, null);
        return uri;
    }

    /**
     * If there is a syntax error, the given {@code uriString} is returned.
     * @param path to be appended to {@link KGvec2goClient#SERVER_URL}.
     * @return Encoded URI as String.
     */
    public static String getEncodedURIString(String path){
        try {
            URI uri = getEncodedURI(path);
            return uri.toASCIIString();
        } catch (URISyntaxException e) {
            LOGGER.error("Trying plain URI string due to URI syntax exception: ", e);
            return PROTOCOL + "://" + DOMAIN + path;
        }
    }

}
