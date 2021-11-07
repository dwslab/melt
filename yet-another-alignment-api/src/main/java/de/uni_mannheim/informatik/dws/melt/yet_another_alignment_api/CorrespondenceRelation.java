package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for the relations used in a {@link Correspondence} such as "equivalence"/"=".
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public enum CorrespondenceRelation {
    
    /**
     * Equivalence relation, when two concepts are exactly the same (has the same intension/semantic).
     * Represents an OWL equivalence relation. Use the following symbol for textual representation: =
     */
    EQUIVALENCE("=", "fr.inrialpes.exmo.align.impl.rel.EquivRelation"),  
    /**
     * Represents a subsumption relation. Entity one (source/left) is "greater" than entity two (target/right).
     * Use the following symbol for textual representation: &gt;
     */
    SUBSUME(">", "fr.inrialpes.exmo.align.impl.rel.SubsumeRelation"),
    /**
     * Represents a subsumption relation. Entity one (source/left) is "smaller" than entity two (target/right).
     * Use the following symbol for textual representation: &lt;
     */
    SUBSUMED("<", "fr.inrialpes.exmo.align.impl.rel.SubsumedRelation"),
    /**
     * Non transitive implication relation (see work of C-OWL and others).
     * Use the following symbol for textual representation: ~&gt;
     */
    NON_TRANSITIVE_IMPLICATION("~>", "fr.inrialpes.exmo.align.impl.rel.NonTransitiveImplicationRelation"),
    /**
     * The relation between an instance and class.
     * Entity one (source/left) is the instance than entity two (target/right) is the class.
     * Use the following symbol for textual representation: InstanceOf
     */
    INSTANCE_OF("InstanceOf", "fr.inrialpes.exmo.align.impl.rel.InstanceOfRelation"),
    /**
     * Has instance relation which connects a class (source/left) to its instances (target/right)
     * Use the following symbol for textual representation: HasInstance
     */
    HAS_INSTANCE("HasInstance", "fr.inrialpes.exmo.align.impl.rel.HasInstanceRelation"),
    /**
     * Incompatiple relation mening that two concepts should not be matched.
     * Use the following symbol for textual representation: %
     */
    INCOMPAT("%", "fr.inrialpes.exmo.align.impl.rel.IncompatRelation"),
    /**
     * Unkown relation which is used when something goes wrong or a reverse relation is not available.
     * Use the following symbol for textual representation: ?
     */
    UNKNOWN("?", "fr.inrialpes.exmo.align.impl.rel.Unknown"); //artificial class name

    private String prettyLabel;
    private String className;

    /**
     * Constructor
     * @param prettyLabel Human-readable, pretty label.
     * @param className Name of implementing class.
     */
    private CorrespondenceRelation(String prettyLabel, String className) {
        this.prettyLabel = prettyLabel;
        this.className = className;
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
        Map<String, CorrespondenceRelation> rel = new HashMap<>();
        for (CorrespondenceRelation r : CorrespondenceRelation.values()) {
            rel.put(r.prettyLabel, r);
            rel.put(r.className, r);
        }
        return rel;
    }

    /**
     * Get a CorrespondenceRelation using a String representation (a.k.a. pretty label).
     *
     * @param label String representation fo the relation.
     * @return CorrespondenceRelation if found.
     */
    public static CorrespondenceRelation parse(String label) {
        return mapping.getOrDefault(label, UNKNOWN);
    }


    @Override
    public String toString() {
        return prettyLabel;
    }
}
