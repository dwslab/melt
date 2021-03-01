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
    
    /**
     * Extractor for the Conference track available at OAEI.
     */
    public static final DatasetIDExtractor CONFERENCE_TRACK_EXTRACTOR = new DatasetIDExtractorUrlPattern("http://", "#", s->s.toLowerCase());
    
    /**
     * Extractor for the Knowledge graph track available at OAEI.
     */
    public static final DatasetIDExtractor KG_TRACK_EXTRACTOR = new DatasetIDExtractorUrlPattern("http://dbkwik.webdatacommons.org/", ".", s->s.replace("-", ""));
    
    /**
     * Extractor for the Knowledge graph track available at OAEI.
     */
    public static final DatasetIDExtractor LARGE_BIO_TRACK_EXTRACTOR = new DatasetIDExtractorUrlPrefixMap(
            "http://bioontology.org/projects/ontologies/fma/fmaOwlDlComponent_2_0", "fma",
            "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl", "nci",
            "http://www.ihtsdo.org/snomed", "snomed"
    );
}
