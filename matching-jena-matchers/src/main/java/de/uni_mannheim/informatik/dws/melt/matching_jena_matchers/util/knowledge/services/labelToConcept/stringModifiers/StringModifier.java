package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers;


/**
 * A simple interface for classes that can modify Strings.
 * Used by LabelToConceptLinkers to define the sequence in which Strings are ecdited for querying.
 *
 * Developer information: As some modifiers call other modifiers you should not log results in those methods (rather
 * in the calling application).
 */
public interface StringModifier {

    /**
     * String modification method.
     * @param stringToBeModified The string which shall be modified.
     * @return Modified String.
     */
    String modifyString(String stringToBeModified);

    /**
     * Returns a unique name of the modifier.
     * This can be used to create unique keys for instance.
     * @return Name of the modifier.
     */
    String getName();

}
