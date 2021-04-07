package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings.LabelToConceptLinkerEmbeddings;

import java.io.File;

public class WiktionaryEmbeddingLinker extends LabelToConceptLinkerEmbeddings {


    private String nameOfLinker = "WiktionaryEmbeddingLinker";

    /**
     * Constructor
     * @param filePathToEntityFile Path to the vocabulary file (UTF-8 encoded, one concept per line).
     */
    public WiktionaryEmbeddingLinker(String filePathToEntityFile) {
        super(filePathToEntityFile);
    }

    /**
     * Constructor
     * @param entityFile Vocabulary file (UTF-8 encoded, one concept per line).
     */
    public WiktionaryEmbeddingLinker(File entityFile) {
        super(entityFile);
    }

    @Override
    public String normalize(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return null;
        stringToBeNormalized = stringToBeNormalized.replaceAll(" ", "_");
        stringToBeNormalized = stringToBeNormalized.replaceAll("^http://kaiko.getalp.org/dbnary/eng/", "");
        return stringToBeNormalized;
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
