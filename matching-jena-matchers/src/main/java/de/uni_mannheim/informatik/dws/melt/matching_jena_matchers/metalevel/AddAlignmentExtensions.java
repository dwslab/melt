package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the provided extensions to the alignment when the matcher is executed.
 */
public class AddAlignmentExtensions extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddAlignmentExtensions.class);
    
    private Map<Object, Object> extensions;
    
    public AddAlignmentExtensions(Map<Object, Object> extensions){
        this.extensions = extensions;
    }
    
    /**
     * List the Extensions as key1, value1, key2, value2 etc.
     * @param extensions extensions in key1, value1, key2, value2, ... format
     */
    public AddAlignmentExtensions(Object... extensions){
        this.extensions = parseExtensions(extensions);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(inputAlignment == null)
            inputAlignment = new Alignment();
        for(Entry<Object, Object> entry : this.extensions.entrySet()){
            inputAlignment.addExtensionValue(entry.getKey(), entry.getValue());
        }
        return inputAlignment;
    }
    
    private static Map<Object,Object> parseExtensions(Object[] arr){
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < arr.length; i+=2) {
            if(i+1 >= arr.length){
                LOGGER.error("Uneven number of extension arguments. Exepect are Key1, Value1, Key2, Value2, ....->Discard last extension");
                break;
            }
            map.put(arr[i], arr[i + 1]);
        }
        return map;
    }
}
