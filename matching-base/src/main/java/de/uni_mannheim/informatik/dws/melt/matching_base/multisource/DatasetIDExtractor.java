package de.uni_mannheim.informatik.dws.melt.matching_base.multisource;

/**
 * Extracts from an URI the corresponding source / dataset identifier (which needs to be included in the URI like a specific domain etc).
 */
public interface DatasetIDExtractor {    
    /**
     * Extracts from an URI the corresponding source / dataset identifier (which needs to be included in the URI like a specific domain etc).
     * @param uri the uri which should contain any dataset specific component which is extracted.
     * @return the dataset identifier as string
     */
    String getDatasetID(String uri);
}
