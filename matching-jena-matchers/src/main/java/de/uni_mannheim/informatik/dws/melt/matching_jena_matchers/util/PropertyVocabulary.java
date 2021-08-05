package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Define some properties which are used with a similar semantic.
 */
public class PropertyVocabulary {
    
    
    
    public static final Set<Property> LABEL_LIKE_PROPERTIES = new HashSet<>(Arrays.asList(
        ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
        ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"),
        ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#altLabel"),
        ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#hiddenLabel"),
        
        ResourceFactory.createProperty("http://schema.org/name"),
        ResourceFactory.createProperty("http://schema.org/alternateName"),
        ResourceFactory.createProperty("http://schema.org/additionalName")
    ));
    
    public static final Set<String> LABEL_NAMES = new HashSet<>(Arrays.asList("label", "name"));
    
    public static final Set<Property> COMMENT_LIKE_PROPERTIES = new HashSet<>(Arrays.asList(
        ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#comment"),
        ResourceFactory.createProperty("http://schema.org/comment"),
        
        ResourceFactory.createProperty("http://purl.org/dc/terms/description"),
        ResourceFactory.createProperty("http://purl.org/dc/elements/1.1/description"),
        ResourceFactory.createProperty("http://schema.org/description"),
        ResourceFactory.createProperty("http://dbpedia.org/ontology/description"),

        ResourceFactory.createProperty("http://purl.org/dc/terms/abstract"),
        ResourceFactory.createProperty("http://dbpedia.org/ontology/abstract")
    ));
    
    public static final Set<String> COMMENT_NAMES = new HashSet<>(Arrays.asList("comment", "description", "abstract"));
    
    
    public static final Set<Property> DESCRIPTIVE_PROPERTIES = getDescriptiveProperties();
    private static Set<Property> getDescriptiveProperties(){
        HashSet<Property> set = new HashSet<>();
        set.addAll(LABEL_LIKE_PROPERTIES);
        set.addAll(COMMENT_LIKE_PROPERTIES);
        return set;
    }
    
    /**
     * Check if the property has 'label' as fragment
     * @param p the property
     * @return true if the property has 'label' as fragment
     */
    public static boolean hasPropertyLabelFragment(Property p){
        String uri = p.getURI();
        if(uri == null)
            return false;
        return LABEL_NAMES.contains(URIUtil.getUriFragment(uri).toLowerCase(Locale.ENGLISH));
    }
    
    /**
     * Check if the property has 'label' as fragment
     * @param p the property
     * @return true if the property has 'label' as fragment
     */
    public static boolean hasPropertyCommentFragment(Property p){
        String uri = p.getURI();
        if(uri == null)
            return false;
        return COMMENT_NAMES.contains(URIUtil.getUriFragment(uri).toLowerCase(Locale.ENGLISH));
    }
}
