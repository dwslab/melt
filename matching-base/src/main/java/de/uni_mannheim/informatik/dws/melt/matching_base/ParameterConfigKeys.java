package de.uni_mannheim.informatik.dws.melt.matching_base;

/**
 * List all the keys (URLs) which can be used as matching parameters.
 * Matchers itself can also define keys (which are just strings).
 */
public class ParameterConfigKeys {
    
    /**
     * Boolean value (true/false) if matching classes is required (true) or not (false)
     */
    public static final String MATCHING_CLASSES = "http://oaei.ontologymatching.org/matchingClasses";
    
    /**
     * Boolean value (true/false) if matching data properties is required (true) or not (false)
     */
    public static final String MATCHING_DATA_PROPERTIES = "http://oaei.ontologymatching.org/matchingDataProperties";
    
    /**
     * Boolean value (true/false) if matching object properties is required (true) or not (false)
     */
    public static final String MATCHING_OBJECT_PROPERTIES = "http://oaei.ontologymatching.org/matchingObjectProperties";
    
    /**
     * Boolean value (true/false) if matching rdf properties is required (true) or not (false)
     */
    public static final String MATCHING_RDF_PROPERTIES = "http://oaei.ontologymatching.org/matchingRDFProperties";
    
    /**
     * Boolean value (true/false) if matching instances is required (true) or not (false)
     */
    public static final String MATCHING_INSTANCES = "http://oaei.ontologymatching.org/matchingInstances";
    
    /**
     * List of URIs (String) which represent classes. All instances of these classes should
     * be matched (allowlist)
     */
    public static final String MATCHING_INSTANCE_TYPES = "http://oaei.ontologymatching.org/matchingInstanceTypes";
    
    /**
     * List of URIs (String) which represent classes. All instances of these classes should
     * not be matched (blocklist)
     */
    public static final String NON_MATCHING_INSTANCE_TYPES = "http://oaei.ontologymatching.org/nonMatchingInstanceTypes";

}
