package de.uni_mannheim.informatik.dws.melt.matching_base.multisource;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Extracts the dataset id given a map of URL prefixes and corresponding dataset ID.
 * The prefix is case dependent and have to be a the start of the url (be sure to include e.g. http://).
 */
public class DatasetIDExtractorUrlPrefixMap implements DatasetIDExtractor {

    private Map<String, String> prefixMap;

    /**
     * Needs the map of prefix to datasetid
     * @param prefixMap map of prefix to dataset id.
     */
    public DatasetIDExtractorUrlPrefixMap(Map<String, String> prefixMap) {
        this.prefixMap = prefixMap;
    }
    
    /**
     * Needs the prefix and dataset id in the given order. Meaning: prefix1, id1, prefix2, id2 etc.
     * Throws IllegalArgumentException in case the number of arguments is odd.
     * @param prefixesAndIds prefix and dataset ids in an order
     */
    public DatasetIDExtractorUrlPrefixMap(String... prefixesAndIds) {
        this.prefixMap = new HashMap<>();
        if (prefixesAndIds.length % 2 != 0)
            throw new IllegalArgumentException("Uneven number of prefixesAndIds arguments.");
        for (int i = 0; i < prefixesAndIds.length; i+=2) {
            this.prefixMap.put(prefixesAndIds[i], prefixesAndIds[i + 1]);
        }
    }           
    
    
    @Override
    public String getDatasetID(String uri) {
        //can be improved by a Trie like from apache collections4 but this would mean more dependencies.
        //https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/Trie.html
        for(Entry<String, String> entry : prefixMap.entrySet()){
            if(uri.startsWith(entry.getKey()))
                return entry.getValue();
        }
        return DatasetIDHelper.getHost(uri); // "default" - better get host of uri which often represents the dataset
    }
}
