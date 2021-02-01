package de.uni_mannheim.informatik.dws.melt.matching_ml.python.openea;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OpenEAConfiguration {
    /**
     * Default Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenEAMatcher.class);
    
    /**
     * ObjectMapper from jackson to parse JSON configuration.
     */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    /**
     * Keys which are atomatically set.
     */
    private static final Set<String> UNUSED_KEYS = new HashSet<>(Arrays.asList("training_data", "output", "dataset_division"));
    
    private Map<String, Object> arguments;

    public OpenEAConfiguration() {
        this.arguments = new HashMap();
    }
    
    /**
     * Parses the json config file given in the OpenEA git repository /run/args/.
     * @param jsonConfigFile the file pointing to a json config file.
     */
    public OpenEAConfiguration(File jsonConfigFile) {
        this();
        parseJsonConfigIntern(jsonConfigFile);
    }
    
    /**
     * Parses the json config file given in the OpenEA git repository /run/args/.
     * @param stream the stream containing json configuration.
     */
    public OpenEAConfiguration(InputStream stream) {
        this();
        parseJsonConfigIntern(stream);
    }
    
    /**
     * Parses the json config file given in the OpenEA git repository /run/args/.
     * It will override properties already set.
     * @param jsonConfigFile the file pointing to a json config file.
     */
    public void parseJsonConfig(File jsonConfigFile){
        parseJsonConfigIntern(jsonConfigFile);
    }
    
    private void parseJsonConfigIntern(File jsonConfigFile){
        try {
            Map<String, Object> map = JSON_MAPPER.readValue(jsonConfigFile, new TypeReference<Map<String,Object>>(){});
            for(Entry<String, Object> entry : map.entrySet()){
                if(UNUSED_KEYS.contains(entry.getKey()) == false){
                    this.arguments.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Could not parse json config file.", ex);
        }
    }
    
    /**
     * Parses the json config stream given in the OpenEA git repository /run/args/.
     * It will override properties already set.
     * @param stream the stream containing json configuration.
     */
    public void parseJsonConfig(InputStream stream){
        parseJsonConfigIntern(stream);
    }

    private void parseJsonConfigIntern(InputStream stream){
        try {
            Map<String, Object> map = JSON_MAPPER.readValue(stream, new TypeReference<Map<String,Object>>(){});
            for(Entry<String, Object> entry : map.entrySet()){
                if(UNUSED_KEYS.contains(entry.getKey()) == false){
                    this.arguments.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Could not parse json config file.", ex);
        }
    }
    
    public Map<String, Object> getArgumentMap(){
        return this.arguments;
    }
    
    public List<String> getArgumentLine(){
        List<String> argumentList = new ArrayList<>(this.arguments.size());
        for(Entry<String, Object> entry : this.arguments.entrySet()){
            argumentList.add("--" + entry.getKey() + " " + entry.getValue());
        }
        return argumentList;
    }
    
    public String getArgumentLineString(){
        return String.join(" ", getArgumentLine());
    }
    
    public void writeArgumentsToFile(File fileToWrite) throws IOException{
        JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValue(fileToWrite, this.arguments);
    }
    
    /**
     * Adds an argument.
     * @param key the key without -- in front as is also appears in the json file.
     * @param value the value as a object. Make sure to use the right type like String, Integer, List etc
     */
    public void addArgument(String key, Object value){
        if(UNUSED_KEYS.contains(key)){
            LOGGER.warn("Do no add key {} because this can only be added trough method addFileLocations.");
            return;
        }
        this.arguments.put(key, value);
    }

    public void addFileLocations(String trainingData, String output, String datasetDivision){
        this.arguments.put("training_data", trainingData);
        this.arguments.put("output", output);
        this.arguments.put("dataset_division", datasetDivision);
    }
    
    public boolean containsKey(String key){
        return this.arguments.containsKey(key);
    }
    
    
    /***********************
     * Maybe Setter
     ***********************/
    
    /*
    public void setOpeneaEmbeddingModule(OpenEAEmbeddingModule embeddingModule){
        this.arguments.put("embedding_module", embeddingModule.name());
    }
    
    
    public void setOpeneaInit(OpenEAInit init){
        this.arguments.put("init", init.name());
    }
    
    public void setOpeneaAlignmentModule(OpenEAAlignmentModule alignmentModule){
        this.arguments.put("alignment_module", alignmentModule.name());
    }
    
    
    public enum OpenEAAlignmentModule {
    sharing,
    mapping,
    swapping
}
    
    
    public enum OpenEAEmbeddingModule {
    AliNet,
    BasicModel,
    TransE,
    TransD,
    TransH,
    TransR,
    DistMult,
    HolE,
    SimplE,
    RotatE,
    ProjE,
    ConvE,
    SEA,
    RSN4EA,
    JAPE,
    Attr2Vec,
    MTransE,
    AlignE,
    BootEA,
    GCN_Align,
    GMNN,
    KDCoE,
    RDGCN        
}
    
    public enum OpenEAInit {
    normal,
    unit,
    xavier,
    uniform
}

    */
    
}
