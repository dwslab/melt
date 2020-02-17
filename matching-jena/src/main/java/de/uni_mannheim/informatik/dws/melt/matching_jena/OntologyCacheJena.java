package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache and reader for Jena ontologies.
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class OntologyCacheJena {

    /**
     * Logger for logging.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OntologyCacheJena.class);

    /**
     * default OntModelSpec that will be used when no spec is given for the Jena implementation.
     */
    public static final OntModelSpec DEFAULT_JENA_ONT_MODEL_SPEC = OntModelSpec.OWL_DL_MEM;

    
    /**
     * The internal cache for ontologies that is dependent on the OntModelSpec.
     */
    private static Map<String, OntModel> ontologyCache = new HashMap<>();

    /**
     * This flag indicates whether the cache is to be used (i.e., ontologies are held in memory).
     */
    private static boolean isDeactivatedCache = false;

    /**
     * Returns the OntModel for the given uri using a cache if indicated to do so.
     * @param uri The URI of the ontology that shall be cached.
     * @param spec The specification of the ontology.
     * @param useCache Indicates whether the cache shall be used. If set to false, ontologies will not be held in memory but re-read every time time.
     * @return OntModel reference.
     */
    public static OntModel get(String uri, OntModelSpec spec, boolean useCache) {
        if (useCache) {
            String keyForCache = uri + "_" + spec.hashCode();
            OntModel model = ontologyCache.get(keyForCache);            
            if (model == null) {
                // model not found in cache → read, put it there and return
                LOGGER.info("Reading model into cache (" + uri + ")");
                model = readOntModel(uri, spec);
                if(!isDeactivatedCache){
                    ontologyCache.put(keyForCache, model);
                }
                return model;
            } else {
                //LOGGER.info("Returning model from cache.");
                return model;
            }
        } else {
            // → do not use cache
            // plain vanilla case: read ontology and return
            return readOntModel(uri, spec);
        }
    }

    /**
     * Read and parse an ontology.
     * @param uri URI from which shall be read.
     * @param spec Jena Ontology Model specification.
     * @return OntModel instance that was read.
     */
    private static OntModel readOntModel(String uri, OntModelSpec spec){
        OntModel model = ModelFactory.createOntologyModel(spec);
        model.read(uri);
        return model;
    }

    /**
     * Returns the OntModel for the given uri using a cache by default.
     * @param uri The URI of the ontology that shall be cached.
     * @param spec The specification of the ontology.
     * @return OntModel reference.
     */
    public static OntModel get(String uri, OntModelSpec spec) {
        return get(uri, spec, true);
    }

    /**
     * Returns the OntModel for the given uri using a cache by default.
     * @param url The URI of the ontology that shall be cached.
     * @param spec The specification of the ontology.
     * @return OntModel reference.
     */
    public static OntModel get(URL url, OntModelSpec spec) {
        return get(url.toString(), spec, true);
    }

    /**
     * Returns the OntModel for the given uri using a cache by default.
     * @param uri The URI of the ontology that shall be cached.
     * @param spec The specification of the ontology.
     * @return OntModel reference.
     */
    public static OntModel get(URI uri, OntModelSpec spec) {
        return get(uri.toString(), spec, true);
    }

    /**
     * Returns the OntModel for the given uri using a cache by default.
     * @param file The File of the ontology that shall be cached.
     * @param spec The specification of the ontology.
     * @return OntModel reference.
     */
    public static OntModel get(File file, OntModelSpec spec) {
        return get(file.toURI().toString(), spec, true);
    }

    /**
     * Returns the OntModel for the given uri using a cache by default and the default OntModelSpec.
     * @param uri The URI of the ontology that shall be cached.
     * @return OntModel reference.
     */
    public static OntModel get(String uri){
        return get(uri, DEFAULT_JENA_ONT_MODEL_SPEC, true);
    }

    /**
     * Returns the OntModel for the given uri using a cache by default and the default OntModelSpec.
     * @param uri The URI of the ontology that shall be cached.
     * @return OntModel reference.
     */
    public static OntModel get(URI uri){
        return get(uri.toString(), DEFAULT_JENA_ONT_MODEL_SPEC, true);
    }

    /**
     * Returns the OntModel for the given uri using a cache by default and the default OntModelSpec.
     * @param url The URL of the ontology that shall be cached.
     * @return OntModel reference.
     */
    public static OntModel get(URL url){
        return get(url.toString(), DEFAULT_JENA_ONT_MODEL_SPEC, true);
    }

    /**
     * Returns the OntModel for the given uri using a cache by default and the default OntModelSpec.
     * @param file The File of the ontology that shall be cached.
     * @return OntModel reference.
     */
    public static OntModel get(File file){
        return get(file.toURI().toString(), DEFAULT_JENA_ONT_MODEL_SPEC, true);
    }


    /**
     * Empties the cache.
     */
    public static void emptyCache() {
        ontologyCache = new HashMap<>();
    }

    public static boolean isDeactivatedCache() {
        return isDeactivatedCache;
    }

    /**
     * Deactivating the cache will also clear the cache.
     * If an ontology is requested twice it is ready every time from disk.
     * @param deactivatedCache true if cache is to be deactivated, else false.
     */
    public static void setDeactivatedCache(boolean deactivatedCache) {
        if(deactivatedCache){
            emptyCache();
        }
        isDeactivatedCache = deactivatedCache;
    }
}
