package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSSOMPrefixMap {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SSSOMPrefixMap.class);
    
    private final Map<String, String> prefixMap;
    private final Map<String, String> cache;
    
    public SSSOMPrefixMap(){
        this.prefixMap = new HashMap<>();
        this.prefixMap.put("sssom", "https://w3id.org/sssom/");
        this.prefixMap.put("owl", "http://www.w3.org/2002/07/owl#");
        this.prefixMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.prefixMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        this.prefixMap.put("skos", "http://www.w3.org/2004/02/skos/core#");
        this.prefixMap.put("semapv", "https://w3id.org/semapv/vocab/");
        
        this.cache = new HashMap<>();
    }
    
    public void add(String shortForm, String longForm) {
        this.prefixMap.put(shortForm, longForm);
    }
    
    public void addAll(Map<? extends String, ? extends String> map) {
        if(map != null)
            this.prefixMap.putAll(map);
    }
    
    public String expand(String shortForm) {
        if ( shortForm.startsWith("http") ) {
            return shortForm;
        }

        String[] parts = shortForm.split(":", 2);
        if ( parts.length == 1 ) {
            return shortForm;
        }

        String prefix = prefixMap.get(parts[0]);
        if ( prefix == null ) {
            throw new IllegalArgumentException("Undeclared prefix");
        }

        return prefix + parts[1];
    }
    
    /**
     * Expands the given parameter with the prefixes stored in this prefix map.
     * The object can be a string or list of strings.
     * @param shortFormObject the object which may contains short forms
     * @return the modified value
     */
    public Object expandObject(Object shortFormObject) {
        if(shortFormObject instanceof String){
            return expand((String) shortFormObject);
        }
        if(shortFormObject instanceof List){
            List<?> oldList = (List)shortFormObject;
            List<Object> newList = new ArrayList<>(oldList.size());
            for(Object o : oldList){
                newList.add(expandObject(o));
            }
            return newList;
        }
        return shortFormObject;
    }
    
    public String shorten(String iri) {
        String shortId = cache.getOrDefault(iri, null);

        if ( shortId == null ) {
            String bestPrefix = null;
            int bestLength = 0;

            for ( String prefixName : prefixMap.keySet() ) {
                String prefix = prefixMap.get(prefixName);
                if ( iri.startsWith(prefix) && prefix.length() > bestLength ) {
                    bestPrefix = prefixName;
                    bestLength = prefix.length();
                }
            }

            if ( bestPrefix != null ) {
                shortId = bestPrefix + ":" + iri.substring(bestLength);
                cache.put(iri, shortId);
            }
        }

        return shortId != null ? shortId : iri;
    }
    
    /**
     * Shortens the given parameter with the prefixes stored in this prefix map.
     * The object can be a string or list of strings.
     * @param longFormObject the object which may contains long forms
     * @return the modified value
     */
    public Object shortenObject(Object longFormObject) {
        if(longFormObject instanceof String){
            return shorten((String) longFormObject);
        }
        if(longFormObject instanceof List){
            List<?> oldList = (List)longFormObject;
            List<Object> newList = new ArrayList<>(oldList.size());
            for(Object o : oldList){
                newList.add(shortenObject(o));
            }
            return newList;
        }
        return longFormObject;
    }

    public Map<String, String> getPrefixMap() {
        return prefixMap;
    }
    
}
