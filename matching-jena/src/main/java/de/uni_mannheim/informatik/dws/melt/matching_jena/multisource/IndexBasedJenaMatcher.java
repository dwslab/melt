package de.uni_mannheim.informatik.dws.melt.matching_jena.multisource;

import org.apache.jena.graph.Triple;

/**
 * This is a very special interface for matcher which have an index for the source / left ontology.
 * This index can be urther used if the matcher is applied in a multisource setting.
 * E.g. a multisource matching task consists of A, B, and C.
 * The one to one matcher get A and B. It indexes ontology A to be queried with B.
 * In the next matching task the union of A and B is matched against C.
 * The index of the matcher which contains A only needs to be updated with some triples of B.
 * This is done via method .....
 * If the index should be removed and a new matching task is started, the clearIndex method is called.
 */
public interface IndexBasedJenaMatcher {
    
    void clearIndex();
    
    void updateSourceIndex(Triple triple);
    
}
