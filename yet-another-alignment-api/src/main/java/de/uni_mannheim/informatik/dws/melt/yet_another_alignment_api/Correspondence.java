package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Correspondence contains a relation that holds between two elements from two different ontologies.
 * This is also known as "Mapping Cell" or "Cell".
 * It is uniquely identified by entityOne, entityTwo and relation.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class Correspondence {
    private static final Logger LOGGER = LoggerFactory.getLogger(Correspondence.class);
    
    protected String entityOne;
    protected String entityTwo;
    protected double confidence;
    protected CorrespondenceRelation relation;
    protected String identifier;
    protected Map<String, Object> extensions;
        
    
    /**
     * Constructor
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @param relation The relation that holds between the two entities.
     * @param identifier The unique identifier for the mapping.
     * @param extensions extensions in the form of a map
     */
    public Correspondence(String entityOne, String entityTwo, double confidence, CorrespondenceRelation relation, Map<String, Object> extensions, String identifier) {
        this.entityOne = entityOne;
        this.entityTwo = entityTwo;
        this.confidence = confidence;
        this.relation = relation;
        this.extensions = extensions;
        this.identifier = identifier;
    }
    
    /**
     * Constructor
     * The identifier is set to null by default.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @param relation The relation that holds between the two entities.
     * @param extensions extensions in key1, value1, key2, value2, ... format
     */
    public Correspondence(String entityOne, String entityTwo, double confidence, CorrespondenceRelation relation, Object... extensions) {
        this(entityOne, entityTwo, confidence, relation, parseExtensions(extensions), null);
    }
    
     /**
     * Constructor
     * The identifier is set to null by default.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @param relation The relation that holds between the two entities.
     * @param extensions extensions as a map of key to value (both strings)
     */
    public Correspondence(String entityOne, String entityTwo, double confidence, CorrespondenceRelation relation, Map<String, Object> extensions) {
        this(entityOne, entityTwo, confidence, relation, extensions, null);
    }
    
    /**
     * Constructor
     * The identifier is set to null by default.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @param extensions extensions in key1, value1, key2, value2, ... format
     */
    public Correspondence(String entityOne, String entityTwo, double confidence, Object... extensions) {
        this(entityOne, entityTwo, confidence, CorrespondenceRelation.EQUIVALENCE, parseExtensions(extensions));
    }


    /**
     * Constructor
     * The identifier is set to null by default.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @param relation The relation that holds between the two entities.
     */
    public Correspondence(String entityOne, String entityTwo, double confidence, CorrespondenceRelation relation) {
        this(entityOne, entityTwo, confidence, relation, new HashMap<>(), null);
    }

    /**
     * Constructor
     * The relation that holds between the two entities is assumed to be {@link CorrespondenceRelation#EQUIVALENCE}.
     * The identifier is set to null by default.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     */
    public Correspondence(String entityOne, String entityTwo, double confidence) {
        this(entityOne, entityTwo, confidence, CorrespondenceRelation.EQUIVALENCE);
    }

    /**
     * Constructor
     * The identifier is set to null by default. The confidence is assumed to be 1.0.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param relation The relation that holds between the two entities.
     */
    public Correspondence(String entityOne, String entityTwo, CorrespondenceRelation relation) {
        this(entityOne, entityTwo, 1.0, relation);
    }

    /**
     * Constructor
     * The relation that holds between the two entities is assumed to be {@link CorrespondenceRelation#EQUIVALENCE}.
     * The identifier is set to null by default. The confidence is assumed to be 1.0.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     */
    public Correspondence(String entityOne, String entityTwo) {
        this(entityOne, entityTwo, 1.0, CorrespondenceRelation.EQUIVALENCE);
    }

    /**
     * Constructor for empty correspondence initialization by {@link AlignmentParser}.
     */
    Correspondence() {
        this("", "", 1.0);
    }
    
    private static Map<String,Object> parseExtensions(Object[] arr){
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < arr.length; i+=2) {
            if(i+1 >= arr.length){
                LOGGER.error("Uneven number of extension arguments. Exepect are Key1, Value1, Key2, Value2, ....->Discard last extension");
                break;
            }
            map.put(arr[i].toString(), arr[i + 1]);
        }
        return map;
    }

    /**
     * This method returns a NEW correspondence where the source and target are reversed but not the relation.
     * @return New reversed correspondence.
     */
    public Correspondence reverseWithoutRelationChange(){
        return new Correspondence(entityTwo, entityOne, confidence, relation, extensions, identifier);
    }
    
    /**
     * This method returns a NEW correspondence with a reversed relation.
     * For example {@link CorrespondenceRelation#SUBSUME} will be reversed to {@link CorrespondenceRelation#SUBSUMED}.
     * If only the source and target should be reversed, use {@link #reverseWithoutRelationChange()} method.
     * @return New reversed correspondence.
     */
    public Correspondence reverse(){
        return new Correspondence(entityTwo, entityOne, confidence, relation.reverse(), extensions, identifier);
    }

    /**
     * Obtain the value of an extension.
     * @param extensionUri The URI identifying the extension.
     * @return The value of the extension as String, null if there is no value.
     */
    public Object getExtensionValue(String extensionUri){
        if(extensions == null) return null;
        return extensions.get(extensionUri);
    }
    
    /**
     * Obtain the value of an extension as string.
     * @param extensionUri The URI identifying the extension.
     * @return The value of the extension as String, null if there is no value.
     */
    public String getExtensionValueAsString(String extensionUri){
        if(extensions == null) return null;
        return extensions.get(extensionUri).toString();
    }


    /**
     * Obtain the value of an extension.
     * @param extensionUri The URI identifying the extension.
     * @param <T> Extension value type.
     * @return The value of the extension as String, null if there is no value.
     */
    public <T> T getExtensionValueCasted(String extensionUri){
        if(extensions == null) return null;
        return (T) extensions.get(extensionUri);
    }

    /**
     * Set the value for an extension.
     * Possible keys are defined in class DefaultExtensions.
     * @param extensionUri The URI identifying the extension. Possible keys are defined in class DefaultExtensions
     * @param extensionValue The value of the extension to be set.
     */
    public void addExtensionValue(String extensionUri, Object extensionValue){
        if(extensions == null) extensions = new HashMap<>();
        extensions.put(extensionUri, extensionValue);
    }
    

    /**
     * Adds an additional confidence based on a specific class.
     * @param matcherClass the class of the matcher
     * @param confidence the additional confidence
     */
    public void addAdditionalConfidence(Class matcherClass, double confidence) {
        addAdditionalConfidence(matcherClass.getSimpleName(), confidence);
    }
    
    /**
     * Adds an additional confidence associated with any specific string value.
     * This should be used if one class puts multiple confidence values. 
     * @param key the key which should be associated with the confidence
     * @param confidence the additional confidence
     */
    public void addAdditionalConfidence(String key, double confidence) {
        addExtensionValue(
                DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + key + DefaultExtensions.MeltExtensions.ADDITIONAL_CONFIDENCE_SUFFIX,
                confidence);
    }

    /**
     * Get a confidence given the name of the matcher.
     * @param matcherClass Class of the matcher.
     * @return The confidence if found.
     */
    public Double getAdditionalConfidence(Class matcherClass) { 
        return getAdditionalConfidence(matcherClass.getSimpleName());
    }
    
    public Double getAdditionalConfidence(String key) { 
        return (Double)this.extensions.get(
                DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + 
                key + 
                DefaultExtensions.MeltExtensions.ADDITIONAL_CONFIDENCE_SUFFIX
        ); 
    }
    
    public double getAdditionalConfidenceOrDefault(String key, double defaultValue) { 
        return (double)this.extensions.getOrDefault(
                DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + 
                key + 
                DefaultExtensions.MeltExtensions.ADDITIONAL_CONFIDENCE_SUFFIX,
                defaultValue
        ); 
    }
    
    private static final Pattern CONFIDENCE_KEY_PATTERN = Pattern.compile(
            DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + 
            "(.*)" + 
            DefaultExtensions.MeltExtensions.ADDITIONAL_CONFIDENCE_SUFFIX
    );


    /**
     * Returns all added confidences (but not all extension values).
     * @return All confidences that were added using e.g. {@link Correspondence#addAdditionalConfidence(String, double)}.
     */
    public Map<String,Double> getAdditionalConfidences() { 
        Map<String,Double> confidences = new HashMap<>();
        for(Entry<String, Object> e : this.extensions.entrySet()){
            Matcher m = CONFIDENCE_KEY_PATTERN.matcher(e.getKey());
            if(m.find()){
                confidences.put(m.group(1), (Double) e.getValue());
            }
        }
        return confidences;
    }
    

    /**
     * Adds an additional explanation 
     * @param matcherClass the class of the matcher
     * @param explanation the explanation for a correspondence
     */
    public void addAdditionalExplanation(Class matcherClass, String explanation) {
        addAdditionalExplanation(matcherClass.getSimpleName(), explanation);
    }
    
    /**
     * Adds an additional explanation 
     * @param key the key which should be associated with the explanation
     * @param explanation the explanation for a correspondence
     */
    public void addAdditionalExplanation(String key, String explanation) {
        addExtensionValue(
                DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + key + DefaultExtensions.MeltExtensions.ADDITIONAL_EXPLANATION_SUFFIX, 
                explanation);
    }
    
    public String getAdditionalExplanation(Class matcherClass) { 
        return getAdditionalExplanation(matcherClass.getSimpleName());
    }
    
    public String getAdditionalExplanation(String key) { 
        return (String)this.extensions.get(
                DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + 
                key + 
                DefaultExtensions.MeltExtensions.ADDITIONAL_EXPLANATION_SUFFIX
        ); 
    }
    
    private static final Pattern EXPLANATION_KEY_PATTERN = Pattern.compile(
            DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + 
            "(.*)" + 
            DefaultExtensions.MeltExtensions.ADDITIONAL_EXPLANATION_SUFFIX
    );
    public Map<String,String> getAdditionalExplanations() { 
        Map<String,String> explanations = new HashMap<>();
        for(Entry<String, Object> e : this.extensions.entrySet()){
            Matcher m = EXPLANATION_KEY_PATTERN.matcher(e.getKey());
            if(m.find()){
                explanations.put(m.group(1), (String) e.getValue());
            }
        }
        return explanations;
    }
    

    public Map<String, Object> getExtensions() { return this.extensions; }

    public String getEntityOne() {
        return entityOne;
    }

    public void setEntityOne(String entityOne) {
        this.entityOne = entityOne;
    }

    public String getEntityTwo() {
        return entityTwo;
    }

    public void setEntityTwo(String entityTwo) {
        this.entityTwo = entityTwo;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public CorrespondenceRelation getRelation() {
        return relation;
    }

    public void setRelation(CorrespondenceRelation relation) {
        this.relation = relation;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.entityOne);
        hash = 71 * hash + Objects.hashCode(this.entityTwo);
        hash = 71 * hash + Objects.hashCode(this.relation);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Correspondence other = (Correspondence) obj;
        if (!Objects.equals(this.entityOne, other.entityOne)) {
            return false;
        }
        if (!Objects.equals(this.entityTwo, other.entityTwo)) {
            return false;
        }
        if (this.relation != other.relation) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "<" + entityOne + "," + entityTwo + "," + confidence + "," + relation + ">";
    }
        
    public String toStringWithExtensions() {
        return "<" + entityOne + "," + entityTwo + "," + confidence + "," + relation + "," + this.extensions.toString() + ">";
    }
    
    public static Comparator<Correspondence> comparingByConfidence() {
        return (Comparator<Correspondence>) (c1, c2) -> Double.compare(c1.confidence, c2.confidence);
    }
    
    public static final Attribute<Correspondence, String> SOURCE = new SimpleAttribute<Correspondence, String>("source") {
        @Override
        public String getValue(Correspondence c, QueryOptions queryOptions) { return c.entityOne; }
    };
    
    public static final Attribute<Correspondence, String> TARGET = new SimpleAttribute<Correspondence, String>("target") {
        @Override
        public String getValue(Correspondence c, QueryOptions queryOptions) { return c.entityTwo; }
    };
    
    public static final Attribute<Correspondence, CorrespondenceRelation> RELATION = new SimpleAttribute<Correspondence, CorrespondenceRelation>("relation") {
        @Override
        public CorrespondenceRelation getValue(Correspondence c, QueryOptions queryOptions) { return c.relation; }
    };
    
    public static final Attribute<Correspondence, Double> CONFIDENCE = new SimpleAttribute<Correspondence, Double>("confidence") {
        @Override
        public Double getValue(Correspondence c, QueryOptions queryOptions) { return c.getConfidence(); }
    };
}
