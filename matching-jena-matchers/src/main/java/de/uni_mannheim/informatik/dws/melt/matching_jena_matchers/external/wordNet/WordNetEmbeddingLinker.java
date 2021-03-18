package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings.LabelToConceptLinkerEmbeddings;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.StringModifier;

import java.io.File;


/**
 * For this linker, you need (1) a WordNet embedding, (2) a text file of the vocabulary.
 * You can obtain both as follows:
 * <ol>
 * <li>Download dataset:
 * <a href="http://wordnet-rdf.princeton.edu/static/wordnet.nt.gz">http://wordnet-rdf.princeton.edu/static/wordnet.nt.gz</a></li>
 * <li>Train embedding with <a href="https://github.com/dwslab/jRDF2Vec">jRDF2Vec</a></li>
 * <li>Generate vocabulary text file (option -generateVocabularyFile in jRDF2Vec).</li>
 * </ol>
 */
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

    /**
     * Constructor
     * @param entityFile The file containing the entities that are available in the vector
     *                   space. The file must be UTF-8 encoded and must contain one entity per line.
     */
    public WordNetEmbeddingLinker(File entityFile){
        super(entityFile);
    }

    @Override
    public String normalize(String stringToBeNormalized) {
        //stringToBeNormalized = stringToBeNormalized.replaceAll("wn-lemma:.*#", "");
        stringToBeNormalized = stringToBeNormalized.replaceAll("http://wordnet-rdf\\.princeton\\.edu/rdf/lemma/.*#", "");
        stringToBeNormalized = stringToBeNormalized.replaceAll(" ", "_");
        return stringToBeNormalized;
    }

    /**
     * Returned are noun vectors (if they exist), then verb, then s, then r.
     * @param labelToBeLinked The label that is to be linked.
     * @return Link.
     */
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
