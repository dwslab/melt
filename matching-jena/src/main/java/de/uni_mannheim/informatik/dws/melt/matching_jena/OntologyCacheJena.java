package de.uni_mannheim.informatik.dws.melt.matching_jena;

import de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.JenaTransformerHelper;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.jena.graph.Graph;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDFLib;
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
        return get(uri, spec, useCache, Lang.RDFXML); //RDF XML is the default in jena.
        
    }

    /**
     * Returns the OntModel for the given uri using a cache if indicated to do so.
     * @param uri The URI of the ontology that shall be cached.
     * @param spec The specification of the ontology.
     * @param useCache Indicates whether the cache shall be used. If set to false, ontologies will not be held in memory but re-read every time time.
     * @param hintlang Set the hint {@link Lang}. This is the RDF syntax used when there is no way to deduce the syntax (e.g. read from a InputStream,
     * no recognized file extension, no recognized HTTP Content-Type provided).
     * @return OntModel reference.
     */
    public static synchronized OntModel get(String uri, OntModelSpec spec, boolean useCache, Lang hintlang) {
        if (useCache) {
            String keyForCache = uri + "_" + spec.hashCode();
            OntModel model = ontologyCache.get(keyForCache);            
            if (model == null) {
                // model not found in cache → read, put it there and return
                LOGGER.info("Reading model into cache (" + uri + ")");
                model = readOntModel(uri, spec, hintlang);
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
            LOGGER.info("Reading model not cached (" + uri + ")");
            return readOntModel(uri, spec, hintlang);
        }
    }

    /**
     * Read and parse an ontology.
     * @param uri URI from which shall be read.
     * @param spec Jena Ontology Model specification.
     * @param hintLang Set the hint {@link Lang}. This is the RDF syntax used when there is no way to deduce the syntax (e.g. read from a InputStream,
     * no recognized file extension, no recognized HTTP Content-Type provided).
     * @return OntModel instance that was read.
     */
    private static OntModel readOntModel(String uri, OntModelSpec spec, Lang hintLang){
        File f = TdbUtil.getFileFromURL(uri);
        if(TdbUtil.isTDB1Dataset(f)){
            return TdbUtil.getOntModelFromTDB(f.getAbsolutePath(), spec);
        }else{
            OntModel model = ModelFactory.createOntologyModel(spec);
            //model.read(uri);
            //RDFDataMgr.read(model, uri);
            RDFParser.create()
                .source(uri)
                .base(uri)
                .errorHandler(ErrorHandlerFactory.errorHandlerWarn)
                .lang(hintLang)
                .context(null)
                .parse(StreamRDFLib.graph(model.getGraph()));
            
            return model;
        }
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
     * Adds an ont model with a specified key to the cache.
     * @param key key is usually uri + "_" + OntModelSpec.hashCode()
     * @param model the ontmodel
     */
    public static void put(String key, OntModel model) {
        ontologyCache.put(key, model);
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
    
    /*
    //methods for createing and loading TDB Model
    //https://jena.apache.org/documentation/tdb/commands.html
    //the tdbloader script can be found here:
    //https://github.com/apache/jena/tree/main/apache-jena/bat
    //https://github.com/apache/jena/tree/main/apache-jena/bin
    //which just calls java class here:
    //https://github.com/apache/jena/blob/main/jena-cmds/src/main/java/tdb/tdbloader.java
    
    private static void createTDBcache(String url, String tdblocation){
        Dataset d = TDBFactory.createDataset(tdblocation);
        GraphTDB graphTDB = (GraphTDB)d.asDatasetGraph().getDefaultGraph();
        TDBLoader.load(graphTDB,url, true);
    }
    private static OntModel loadTDBcache(String tdblocation, OntModelSpec spec){
        Dataset d = TDBFactory.createDataset(tdblocation);
        return ModelFactory.createOntologyModel(spec, d.getDefaultModel());
    }
    */
    public static int numberOfCacheEntries(){
        return ontologyCache.size();
    }
}
