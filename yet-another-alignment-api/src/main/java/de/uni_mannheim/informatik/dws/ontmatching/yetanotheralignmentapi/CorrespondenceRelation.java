package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for the relations used in a {@link Correspondence} such as "equivalence"/"=".
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public enum CorrespondenceRelation {
    SUBSUMED("<", "fr.inrialpes.exmo.align.impl.rel.SubsumedRelation"),
    SUBSUME(">", "fr.inrialpes.exmo.align.impl.rel.SubsumeRelation"),
    NON_TRANSITIVE_IMPLICATION("~>", "fr.inrialpes.exmo.align.impl.rel.NonTransitiveImplicationRelation"),
    INSTANCE_OF("InstanceOf", "fr.inrialpes.exmo.align.impl.rel.InstanceOfRelation"),
    INCOMPAT("%", "fr.inrialpes.exmo.align.impl.rel.IncompatRelation"),
    HAS_INSTANCE("HasInstance", "fr.inrialpes.exmo.align.impl.rel.HasInstanceRelation"),
    EQUIVALENCE("=", "fr.inrialpes.exmo.align.impl.rel.EquivRelation"),
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
