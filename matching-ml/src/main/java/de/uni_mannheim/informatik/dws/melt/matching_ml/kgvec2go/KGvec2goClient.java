package de.uni_mannheim.informatik.dws.melt.matching_ml.kgvec2go;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    public static Double[] getVector(String word, KGvec2goDatasets dataset){
        // check buffer
        Double[] result = getFromBuffer(word, dataset);
        if(result != null) return result;

        // perform web request
        String requestUrl = SERVER_URL + "/rest/get-vector/" + dataset.toString() + "/" + word;

        // TODO write to buffer

        return result;
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
