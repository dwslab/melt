package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;


public class PropertySpecificStringProcessing {
    
    
    //vocabulary definition:
    private static final Model m = ModelFactory.createDefaultModel();
    private static final String meltNamespace = "http://melt.dws.informatik.uni-mannheim.de/";
    /**
     * Extracts the fragment of the url e.g. part after last slash or hashtag.
     */
    public static final Property URL_FRAGMENT = m.createProperty(meltNamespace + "fragment");
    /**
     * Extracts the local name from the URI. This wrapps the Jena method getLocalName of class Resource which maps itself to org.apache.jena.rdf.model.impl.Util.splitNamespaceXML.
     */
    public static final Property URL_LOCAL_NAME = m.createProperty(meltNamespace + "local_name");
    /**
     * This property uses all literals which are also strings e.g. all literal with datatype XSD string or dtLangString or have a language tag. 
     */
    public static final Property ALL_STRING_LITERALS = m.createProperty(meltNamespace + "all_string_literals");
    /**
     * This property uses all literals of the resource.
     */
    public static final Property ALL_LITERALS = m.createProperty(meltNamespace + "all_literals");
    
    private Set<Property> properties;
    private Function<String, Object> processing;
    private double confidence;
    /**
     * The index name, this Processing belongs to.
     * Same index name means that all objects are search in this index.
     */
    private String indexName;
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, String indexName, Set<Property> properties) {
        this.properties = properties;
        this.processing = processing;
        this.confidence = confidence;
        this.indexName = indexName;
    }

    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, String indexName, Property... property) {
        this(processing, confidence, indexName, new HashSet<>(Arrays.asList(property)));
    }
    
    public PropertySpecificStringProcessing(Function<String, Object> processing, double confidence, Property... property) {
        this(processing, confidence, UUID.randomUUID().toString(), new HashSet<>(Arrays.asList(property)));
    }

    
    public Set<Property> getProperties() {
        return properties;
    }

    public Function<String, Object> getProcessing() {
        return processing;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getIndexName() {
        return indexName;
    }
}
