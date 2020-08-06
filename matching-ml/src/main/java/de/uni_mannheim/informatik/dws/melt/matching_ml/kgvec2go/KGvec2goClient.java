package de.uni_mannheim.informatik.dws.melt.matching_ml.kgvec2go;


import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
     * The URL that shall be used to perform the requests.
     */
    private final static String SERVER_URL = "http://kgvec2go.org";

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
     * Instance (singleton pattern.
     */
    private static KGvec2goClient instance;

    /**
     * Central gson instance to parse JSON.
     */
    private static Gson gson;

    /**
     * Get the instance.
     *
     * @return Gensim instance.
     */
    public static KGvec2goClient getInstance() {
        if (instance == null) instance = new KGvec2goClient();
        if (isShutDown) instance.startServer();
        return instance;
    }

    /**
     * Private constructor for singleton pattern
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
    private void shutDown(){
        isShutDown = true;
        instance = null;
        try {
            if (httpClient != null)
                httpClient.close();
        } catch (IOException e) {
            LOGGER.error("Could not close client.", e);
        }
    }


    public Double[] getVector(String word, KGvec2goDatasets dataset){
        // check buffer
        if(isInBuffer(word, dataset)){
            return getFromBuffer(word, dataset);
        }

        // perform web request
        String requestUrl = SERVER_URL + "/rest/get-vector/" + dataset.toString() + "/" + word;

        String resultString = null;
        HttpGet request = new HttpGet(requestUrl);
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

        if(resultString != null){
            if(dataset != KGvec2goDatasets.WORDNET) {
                KGvec2goVectorResponseEntity vre = gson.fromJson(resultString, KGvec2goVectorResponseEntity.class);
                writeToBuffer(word, dataset, vre.vector);
                return vre.vector;
            } else {
                KGvec2goVectorResponseEntityArray vre = gson.fromJson(resultString, KGvec2goVectorResponseEntityArray.class);
                if(vre == null || vre.result == null || vre.result.length == 0){
                    writeToBuffer(word, dataset, null);
                    return null;
                }
                writeToBuffer(word, dataset, vre.result[0].vector);
                return vre.result[0].vector;
            }
        }
        writeToBuffer(word, dataset, null);
        return null;
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

}
