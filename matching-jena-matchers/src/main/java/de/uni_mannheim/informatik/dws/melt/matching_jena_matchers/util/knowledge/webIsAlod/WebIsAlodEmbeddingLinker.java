package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.webIsAlod;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.LabelToConceptLinkerEmbeddings;

/**
 * Linker for WebIsALOD Embeddings
 */
public class WebIsAlodEmbeddingLinker extends LabelToConceptLinkerEmbeddings {

    private String nameOfLinker = "WebIsAlodEmbeddingLinker";

    /**
     * Constructor
     * @param entityFilePath Empty file path.
     */
    public WebIsAlodEmbeddingLinker(String entityFilePath){
        super(entityFilePath);
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
