package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.combined;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinkerEmbeddings;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.LeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.MaxGramLeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;

import java.util.HashSet;

/**
 * Linker able to combine different embedding approaches.
 * Note that the linker will not actually create a link but, instead, will return the label if it can be linked by
 * at least one of the given linkers.
 */
public class LabelToConceptLinkerCombinedEmbeddings implements LabelToConceptLinker {

    private String nameOfLinker = "CombinedConceptLinkerEmbeddings";

    /**
     * The linkers to be used.
     */
    private LabelToConceptLinkerEmbeddings[] linkers;

    /**
     * Constructor
     * @param linkers The individual linkers that shall be used.
     */
    public LabelToConceptLinkerCombinedEmbeddings(LabelToConceptLinkerEmbeddings... linkers){
        this.linkers = linkers;
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        for(LabelToConceptLinkerEmbeddings linker : linkers){
            if(linker.linkToSingleConcept(labelToBeLinked) != null){
                return labelToBeLinked;
            }
        }
        return null;
    }

    /**
     * Splits the labelToBeLinked in ngrams up to infinite size and tries to link components.
     * This corresponds to a MAXGRAM_LEFT_TO_RIGHT_TOKENIZER or NGRAM_LEFT_TO_RIGHT_TOKENIZER OneToManyLinkingStrategy.
     *
     * @param labelToBeLinked The label that shall be linked.
     * @return A set of concept URIs that were found.
     */
    private HashSet<String> linkLabelToTokensLeftToRight(String labelToBeLinked){
        LeftToRightTokenizer tokenizer;
        String[] tokens = StringOperations.tokenizeBestGuess(labelToBeLinked);
        tokenizer = new MaxGramLeftToRightTokenizer(tokens, " ");
        HashSet<String> result = new HashSet<>();
        String resultingConcept = "";
        String token = tokenizer.getInitialToken();
        while(token != null){
            resultingConcept = linkToSingleConcept(token);
            if(resultingConcept == null || resultingConcept.length() == 0){
                token = tokenizer.getNextTokenNotSuccessful();
            } else {
                result.add(resultingConcept);
                token = tokenizer.getNextTokenSuccessful();
            }
        }
        return result;
    }

    @Override
    public HashSet<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        HashSet<String> result = linkLabelToTokensLeftToRight(labelToBeLinked);
        int possibleConceptParts = StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(labelToBeLinked)).length;
        int actualConceptParts = 0;
        for(String s : result) {
            actualConceptParts = actualConceptParts + StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(s)).length;
        }
        if(possibleConceptParts <= actualConceptParts) {
            return result;
        }
        return null;
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
