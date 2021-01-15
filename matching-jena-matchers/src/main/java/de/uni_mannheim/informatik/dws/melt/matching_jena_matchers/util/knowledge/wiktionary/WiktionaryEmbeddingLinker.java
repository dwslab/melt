package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.wiktionary;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.LabelToConceptLinkerEmbeddings;

public class WiktionaryEmbeddingLinker extends LabelToConceptLinkerEmbeddings {

    private String nameOfLinker = "WiktionaryEmbeddingLinker";

    public WiktionaryEmbeddingLinker(String filePathToEntityFile) {
        super(filePathToEntityFile);
    }

    @Override
    public String normalize(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return null;
        stringToBeNormalized = stringToBeNormalized.replaceAll(" ", "");
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
