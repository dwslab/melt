package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external;

import java.util.Set;

/**
 * A multi concept linker may map one link to multiple concepts in the background knowledge graph.
 */
public interface MultiConceptLinker {

    /**
     * Given a link, multiple identifiers may be returned.
     * @param multiConceptLink The link.
     * @return Multiple concepts (typically URIs).
     */
    Set<String> getUris(String multiConceptLink);

    /**
     * Determine whether the link at hand is a multi-concept link.
     * @param link Link to be checked.
     * @return True if multi-concept link, else false.
     */
    boolean isMultiConceptLink(String link);
}
