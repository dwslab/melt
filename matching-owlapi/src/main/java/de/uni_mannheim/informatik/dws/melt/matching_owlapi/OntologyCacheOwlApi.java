package de.uni_mannheim.informatik.dws.melt.matching_owlapi;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache for ontologies for the OWL Api.
 */
public class OntologyCacheOwlApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyCacheOwlApi.class);

    /**
     * The internal cache for ontologies that is dependent on the OntModelSpec.
     */
    private static Map<String, OWLOntology> ontologyCache = new HashMap<>();
    
    private static OWLOntologyManager man = OWLManager.createOWLOntologyManager();

    /**
     * This flag indicates whether the cache is to be used (i.e., ontologies are held in memory).
     */
    private static boolean isDeactivatedCache = false;

    /**
     * Returns the OntModel for the given uri using a cache if indicated to do so.
     * @param uri The URI of the ontology that shall be cached.
     * @param useCache Indicates whether the cache shall be used. If set to false, ontologies will not be held in memory but re-read every time time.
     * @return OntModel reference.
     */
    public static OWLOntology get(String uri, boolean useCache) {
        if (useCache) {
            OWLOntology model = ontologyCache.get(uri);            
            if (model == null) {
                // model not found in cache → read, put it there and return
                LOGGER.info("Reading model into cache (" + uri + ")");
                model = readOWLOntology(uri);
                if(!isDeactivatedCache) {
                    ontologyCache.put(uri, model);
                }
                return model;                
            } else {
                LOGGER.info("Returning model from cache.");
                return model;
            }
        } else {
            // → do not use cache
            // plain vanilla case: read ontology and return
            return readOWLOntology(uri);
        }
    }
    private static OWLOntology readOWLOntology(String uri){
        try {
            return man.loadOntologyFromOntologyDocument(IRI.create(uri));
        } catch (OWLOntologyCreationException ex) {
            LOGGER.warn("Cannot read OWLOntology of URI " + uri + ". Returning empty ontology.", ex);
            try {
                return man.createOntology();
            } catch (OWLOntologyCreationException ex1) {
                LOGGER.warn("Cannot create empty ontology. Should not happen...", ex1);
                return null;
            }
        }        
    }
    
    public static OWLOntology get(URL url, boolean useCache) {
        return get(url.toString(), useCache);
    }
  
    public static OWLOntology get(String uri){
        return get(uri, true);
    }
    
    public static OWLOntology get(URL url){
        return get(url, true);
    }

    public boolean isDeactivatedCache() {
        return isDeactivatedCache;
    }

    /**
     * Empties the cache.
     */
    public static void emptyCache() {
        ontologyCache = new HashMap<>();
    }

    /**
     * Deactivating the cache will also clear the cache.
     * If an ontology is requested twice it is ready every time from disk.
     * @param deactivatedCache true if cache is to be deactivated, else false.
     */
    public void setDeactivatedCache(boolean deactivatedCache) {
        if(deactivatedCache){
            this.emptyCache();
        }
        isDeactivatedCache = deactivatedCache;
    }
}
