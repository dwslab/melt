package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.wordNet;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.LabelToConceptLinkerEmbeddings;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.labelToConcept.stringModifiers.StringModifier;

public class WordNetEmbeddingLinker extends LabelToConceptLinkerEmbeddings {

    private String nameOfLinker = "WordNetEmbeddingLinker";

    /**
     * Constructor
     * @param entityFilePath The file path to the file containing the entities that are available in the vector
     *                       space. The file must be UTF-8 encoded and must contain one entity per line.
     */
    public WordNetEmbeddingLinker(String entityFilePath){
        super(entityFilePath);
    }

    @Override
    public String normalize(String stringToBeNormalized) {
        stringToBeNormalized = stringToBeNormalized.replaceAll("wn-lemma:.*#", "");
        stringToBeNormalized = stringToBeNormalized.replaceAll(" ", "_");
        return stringToBeNormalized;
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        if (labelToBeLinked == null || labelToBeLinked.equals("")) return null;

        String lookupKey = normalize(labelToBeLinked);
        String[] posRanking = {"n", "v", "s", "r"};
        for(String pos : posRanking){
            String key = lookupKey + "-" + pos;
            if(lookupMap.containsKey(key)){
                return lookupMap.get(key);
            }
        }

        // advanced lookup
        String modifiedConcept;
        for(StringModifier modifier : getStringModificationSequence()) {
            modifiedConcept = modifier.modifyString(labelToBeLinked);
            for(String pos : posRanking){
                String key = modifiedConcept + "-" + pos;
                if(lookupMap.containsKey(key)){
                    return lookupMap.get(key);
                }
            }
        }


        return null;
    }

    @Override
    public String getNameOfLinker() {
        return nameOfLinker;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        this.nameOfLinker = nameOfLinker;
    }
}
