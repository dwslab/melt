package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external;

import java.util.Set;

/**
 * General interface for all label-to-concept linkers.
 */
public interface LabelToConceptLinker {

    /**
     * Queries for a concept and returns a link that represents an entity in the background knowledge source such as the
     * {@link SemanticWordRelationDictionary}. Note that the link may not always be something intuitive such as a URI but
     * may also be an artificial identifier that is understood by the corresponding background knowledge source.
     *
     * @param labelToBeLinked The label which shall be linked to a single concept.
     * @return Concept or null if no link could be found.
     */
    String linkToSingleConcept(String labelToBeLinked);

    /**
     * This method tries to link {@code labelToBeLinked} to one concept if possible.
     * If it fails, it will try to link it to multiple concepts.
     *
     * @param labelToBeLinked The label which shall be linked.
     * @return One or multiple linked concepts in a set. Null if it could not fully link the label.
     */
    Set<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked);

    /**
     * Get instance specific name of the linker.
     *
     * @return Name as String.
     */
    String getNameOfLinker();

    /**
     * Set instance specific name of the linker.
     * @param nameOfLinker Name to be set.
     */
    void setNameOfLinker(String nameOfLinker);

}
