package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enumeration for the relations used in a {@link Correspondence} such as "equivalence"/"=".
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public enum CorrespondenceRelation {
    /**
     * Equivalence relation, when two concepts are exactly the same (has the same intension/semantic).
     * Represents an OWL equivalence relation. Use the following symbol for textual representation: =.
     */
    EQUIVALENCE("=", "fr.inrialpes.exmo.align.impl.rel.EquivRelation", 
            "http://www.w3.org/2004/02/skos/core#exactMatch",
            "http://www.w3.org/2002/07/owl#equivalentClass",
            "http://www.w3.org/2002/07/owl#equivalentProperty",
            "http://www.w3.org/2002/07/owl#sameAs"),
    
    /**
     * Represents a subsume / superclass of relation. Entity one (source/left) is "greater" than entity two (target/right).
     * The source/left element is the super-class. The target/right element is the sub-class.
     * Example: Person -subsume-&gt; Student.
     * This is the inverse relation of {@link #SUBSUMED}.
     * Use the following symbol for textual representation: &gt;
     */
    SUBSUME(">", "fr.inrialpes.exmo.align.impl.rel.SubsumeRelation", 
            "http://www.w3.org/2004/02/skos/core#narrowMatch", "http://www.w3.org/2004/02/skos/core#narrower"),
    
    /**
     * Represents a subsumed/rdfs:subClassOf relation. Entity one (source/left) is "smaller" than entity two (target/right).
     * The source/left element is the sub-class. The target/right element is the super-class.
     * Example: Student -subsumed-&gt; Person.
     * This is the inverse relation of {@link #SUBSUME}.
     * Use the following symbol for textual representation: &lt;
     */
    SUBSUMED("<", "fr.inrialpes.exmo.align.impl.rel.SubsumedRelation", 
            "http://www.w3.org/2004/02/skos/core#broadMatch", "http://www.w3.org/2004/02/skos/core#broader",
            "http://www.w3.org/2000/01/rdf-schema#subClassOf"),
    
    /**
     * Non transitive implication relation (see work of C-OWL and others).
     * Use the following symbol for textual representation: ~&gt;
     */
    NON_TRANSITIVE_IMPLICATION("~>", "fr.inrialpes.exmo.align.impl.rel.NonTransitiveImplicationRelation",
        "http://melt.dws.uni-mannheim.de/relation#NonTransitiveImplicationRelation"),
    
    /**
     * The relation between an instance and class.
     * Entity one (source/left) is the instance and entity two (target/right) is the class.
     * This is the inverse relation of {@link #INSTANCE_OF}.
     * Use the following symbol for textual representation: InstanceOf
     */
    INSTANCE_OF("InstanceOf", "fr.inrialpes.exmo.align.impl.rel.InstanceOfRelation", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
    
    /**
     * Has instance relation which connects a class (source/left) to its instance (target/right).
     * This is the inverse relation of {@link #INSTANCE_OF}.
     * Use the following symbol for textual representation: HasInstance
     */
    HAS_INSTANCE("HasInstance", "fr.inrialpes.exmo.align.impl.rel.HasInstanceRelation",
        "http://melt.dws.uni-mannheim.de/relation#HasInstanceRelation"),
    
    /**
     * Part of relation which connects two classes.
     * The source/left class is a part of the target/right class.
     * The source class is the part class whereas target is the whole class.
     * Example: tire -partof-&gt; car
     * This is the inverse relation of {@link #HAS_A}.
     * Use the following symbol for textual representation: PartOf
     */
    PART_OF("PartOf", "fr.inrialpes.exmo.align.impl.rel.PartOf", 
            "http://purl.org/dc/terms/isPartOf"),
    
    /**
     * Has a relation which connects two classes.
     * The target/right class is a part of the source/left class.
     * The source class is the whole class whereas target is the part class.
     * Example: car -has a-&gt; tire 
     * This is the inverse relation of {@link #PART_OF}.
     * Use the following symbol for textual representation: HasA
     */
    HAS_A("HasA", "fr.inrialpes.exmo.align.impl.rel.HasA", 
            "http://purl.org/dc/terms/hasPart"),
    
    /**
     * Related relation which connects two classes/instances.
     * This relation is highly underspecified.
     * The source/left class is somehow related to the target/right class.
     * Use the following symbol for textual representation: Related
     */
    RELATED("Related", "fr.inrialpes.exmo.align.impl.rel.Related",  //artificial class name
            "http://www.w3.org/2004/02/skos/core#relatedMatch", "http://www.w3.org/2004/02/skos/core#related"),
    
    /**
     * Close match relation meaning that two concepts are close to each other and can be used in <b>some</b> cases interchangeably.
     * Use the following symbol for textual representation: Close
     */
    CLOSE("Close", "fr.inrialpes.exmo.align.impl.rel.Close",//artificial class name
        "http://www.w3.org/2004/02/skos/core#closeMatch"),
    
    /**
     * Incompatiple relation meaning that two concepts should not be matched.
     * Use the following symbol for textual representation: %
     */
    INCOMPAT("%", "fr.inrialpes.exmo.align.impl.rel.IncompatRelation",
        "http://melt.dws.uni-mannheim.de/relation#IncompatRelation"),
    /**
     * Unkown relation which is used when something goes wrong or a reverse relation is not available.
     * Use the following symbol for textual representation: ?
     */
    UNKNOWN("?", "fr.inrialpes.exmo.align.impl.rel.Unknown",
        "http://melt.dws.uni-mannheim.de/relation#Unknown"); //artificial class name

    private String prettyLabel;
    private String className;
    private List<String> rdfRepresentations;

    /**
     * Constructor
     * @param prettyLabel Human-readable, pretty label.
     * @param className Name of implementing class.
     */
    private CorrespondenceRelation(String prettyLabel, String className, String... rdfRepresentations) {
        this.prettyLabel = prettyLabel;
        this.className = className;
        this.rdfRepresentations = Arrays.asList(rdfRepresentations);
    }

    /**
     * Reverses the current relation. If the relation is symmetric, the same relation will be returned.
     * @return Reversed correspondence relation.
     */
    public CorrespondenceRelation reverse() {
        switch (this) {
            case EQUIVALENCE:
                return EQUIVALENCE;
            case SUBSUME:
                return SUBSUMED;
            case SUBSUMED:
                return SUBSUME;
            case NON_TRANSITIVE_IMPLICATION:
                return UNKNOWN;
            case INSTANCE_OF:
                return HAS_INSTANCE;
            case HAS_INSTANCE:
                return INSTANCE_OF;
            case PART_OF:
                return HAS_A;
            case HAS_A:
                return PART_OF;
            case RELATED:
                return RELATED;
            case CLOSE:
                return CLOSE;
            case INCOMPAT:
                return INCOMPAT;
            case UNKNOWN:
                return UNKNOWN;
            default:
                return UNKNOWN;
        }
    }

    /**
     * Maps a string representatin to a CorrespondenceRelation.
     */
    private static Map<String, CorrespondenceRelation> mapping = generateMapping();

    /**
     * Generates the {@link CorrespondenceRelation#mapping}.
     * @return mapping
     */
    private static Map<String, CorrespondenceRelation> generateMapping() {
        Map<String, String> prefix = new HashMap<>();
        prefix.put("http://www.w3.org/2002/07/owl#", "owl:");
        prefix.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
        prefix.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
        prefix.put("http://purl.org/dc/terms/", "dcterms:");
        prefix.put("http://www.w3.org/2004/02/skos/core#", "skos:");
        
        StringJoiner textualPattern = new StringJoiner("|");
        for(Entry<String, String> replacement : prefix.entrySet()){
            textualPattern.add(Pattern.quote(replacement.getKey()));
        }
        Pattern pattern = Pattern.compile("(" + textualPattern.toString() + ")");
        
        Map<String, CorrespondenceRelation> rel = new HashMap<>();
        for (CorrespondenceRelation r : CorrespondenceRelation.values()) {
            rel.put(r.prettyLabel.toLowerCase(Locale.ENGLISH), r);
            rel.put(r.className.toLowerCase(Locale.ENGLISH), r);
            if(r.rdfRepresentations!=null){
                for(String s : r.rdfRepresentations){
                    rel.put(s.toLowerCase(Locale.ENGLISH), r);
                    
                    Matcher matcher = pattern.matcher(s);
                    StringBuffer sb  = new StringBuffer();
                    while (matcher.find()) {
                        matcher.appendReplacement(sb, prefix.get(matcher.group(1)));
                    }
                    matcher.appendTail(sb);
                    
                    rel.put(sb.toString().toLowerCase(Locale.ENGLISH), r);
                }
            }
        }
        return rel;
    }

    /**
     * Get a CorrespondenceRelation using a String representation (a.k.a. pretty label).
     *
     * @param label String representation fo the relation.
     * @return CorrespondenceRelation if found - otherwise return {@link CorrespondenceRelation#UNKNOWN UNKNOWN}.
     */
    public static CorrespondenceRelation parse(String label) {
        if(label == null)
            return UNKNOWN;
        return mapping.getOrDefault(label.toLowerCase(Locale.ENGLISH), UNKNOWN);
    }

    public String getPrettyLabel() {
        return prettyLabel;
    }

    public String getClassName() {
        return className;
    }

    public List<String> getRdfRepresentations() {
        return rdfRepresentations;
    }
    
    /**
     * Return the most representative RDF relation or null if no relation is available.
     * Favors skos relations which are mainly used in SSSOM format.
     * @return most representative RDF relation or null if no relation is available.
     */
    public String getRdfRepresentation() {
        if(this.rdfRepresentations == null)
            return null;
        if(this.rdfRepresentations.isEmpty())
            return null;
        return this.rdfRepresentations.get(0);
    }
    
    /**
     * Returns the pretty printed label. This is used when serializing the mapping.
     * @return the pretty printed label
     */
    @Override
    public String toString() {
        return prettyLabel;
    }
}
