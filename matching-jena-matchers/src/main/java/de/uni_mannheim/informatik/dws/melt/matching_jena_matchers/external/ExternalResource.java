package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external;

/**
 * An external resource carries a name, has a linker, and can be asked
 * whether it holds a representation for a specified word.
 */
public interface ExternalResource {


    /**
     * Returns the linker instance for this particular resource.
     * @return The specific linker used to link words to concepts.
     */
    LabelToConceptLinker getLinker();

    /**
     * Obtain the name of the resource.
     * @return Name of the resource.
     */
    String getName();
}
