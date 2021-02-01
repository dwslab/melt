package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings.LabelToConceptLinkerEmbeddings;

import java.io.File;

/**
 * Linker for WebIsALOD Embeddings
 */
public class WebIsAlodEmbeddingLinker extends LabelToConceptLinkerEmbeddings {

    private String nameOfLinker = "WebIsAlodEmbeddingLinker";

    /**
     * Constructor
     * @param entityFilePath Entity file path.
     */
    public WebIsAlodEmbeddingLinker(String entityFilePath){
        super(entityFilePath);
    }

    /**
     * Constructor.
     * @param entityFile Entity file.
     */
    public WebIsAlodEmbeddingLinker(File entityFile){
        super(entityFile.getAbsolutePath());
    }

    @Override
    public String normalize(String stringToBeNormalized) {
        String result = stringToBeNormalized.trim().replaceAll(" ", "_");
        result = result.trim();
        return result;
    }

    @Override
    public String getNameOfLinker() {
        return this.nameOfLinker;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        this.nameOfLinker = nameOfLinker;
    }
}
