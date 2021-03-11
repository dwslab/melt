package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.LeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.MaxGramLeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Links concepts to BabelNet.
 */
public class BabelNetLinker implements LabelToConceptLinker {


    private static Logger LOGGER = LoggerFactory.getLogger(BabelNetLinker.class);

    /**
     * Default name of the linker.
     */
    private String nameOfLinker = "BabelNetLinker";

    /**
     * KnowledgeSource used for lookups.
     */
    private BabelNetKnowledgeSource dictionary;

    /**
     * Buffer for {@link BabelNetLinker#linkToSingleConcept(String)} Method.
     */
    private ConcurrentMap<String, String> singleConceptBuffer;


    /**
     * Buffer for {@link BabelNetLinker#linkToPotentiallyMultipleConcepts(String)} Method.
     */
    private ConcurrentMap<String, Set<String>> multipleConceptBuffer;

    /**
     * Managing buffers.
     */
    private PersistenceService persistenceService;

    /**
     * You cannot add null to a concurrent HashMap.
     * Therefore, a null character is added rather than null.
     */
    private static final String NULL_CHARACTER = "*null*";

    /**
     * Constructor
     * @param dictionary BabelNetKnowledgeSource instance that is to be used.
     */
    public BabelNetLinker(BabelNetKnowledgeSource dictionary){
        this.dictionary = dictionary;
        initializeBuffers();
    }

    /**
     * Initializes local database
     */
    private void initializeBuffers(){
        persistenceService = PersistenceService.getService();
        this.singleConceptBuffer = persistenceService.getMapDatabase(PersistenceService.PreconfiguredPersistences.BABELNET_SINGLE_CONCEPT_BUFFER);
        this.multipleConceptBuffer = persistenceService.getMapDatabase(PersistenceService.PreconfiguredPersistences.BABELNET_MULTI_CONCEPT_BUFFER);
    }


    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        if (labelToBeLinked == null || labelToBeLinked.equals("")) return null;

        String key = labelToBeLinked;
        try {
            if (singleConceptBuffer.containsKey(key)) {
                String link = singleConceptBuffer.get(key);
                if (link.equals(NULL_CHARACTER)) {
                    return null;
                } else return link;
            }
        } catch ( Exception e){
            LOGGER.error("Problem during lookup", e);
        }

        // try simple approach
        String lowerCaseLabel = normalizeForBabelnetLookupWithoutTokenization(labelToBeLinked);
        if(dictionary.isInDictionary(lowerCaseLabel)){
            singleConceptBuffer.put(key, lowerCaseLabel);
            return lowerCaseLabel;
        }

        // try normalized approach
        labelToBeLinked = normalizeForBabelnetLookupWithTokenization(labelToBeLinked);
        if(dictionary.isInDictionary(labelToBeLinked)){
            singleConceptBuffer.put(key, labelToBeLinked);
            return labelToBeLinked;
        }

        // not found
        singleConceptBuffer.put(key, NULL_CHARACTER);
        return null;
    }

    @Override
    public Set<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        // buffer
        if(multipleConceptBuffer.containsKey(labelToBeLinked)){
            Set<String> links =  multipleConceptBuffer.get(labelToBeLinked);
            if(links.size() == 0){
                return null;
            } else return links;
        }

        Set<String> result = linkLabelToTokensLeftToRight(labelToBeLinked);
        int possibleConceptParts = StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(labelToBeLinked)).length;

        int actualConceptParts = 0;
        for(String s : result) {
            actualConceptParts = actualConceptParts + StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(s)).length;
        }

        // TODO: for now: only 100% results
        if(possibleConceptParts <= actualConceptParts) {
            multipleConceptBuffer.put(labelToBeLinked, result);
            return result;
        }

        multipleConceptBuffer.put(labelToBeLinked, new HashSet<>());
        return null;
    }

    /**
     * Splits the labelToBeLinked in ngrams up to infinite size and tries to link components.
     * This corresponds to a MAXGRAM_LEFT_TO_RIGHT_TOKENIZER or NGRAM_LEFT_TO_RIGHT_TOKENIZER OneToManyLinkingStrategy.
     * @param labelToBeLinked The label that shall be linked.
     * @return A set of concept URIs that were found.
     */
    private Set<String> linkLabelToTokensLeftToRight(String labelToBeLinked){
        StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(labelToBeLinked));
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

    /**
     * Normalize for BabelNet lookup, i.e., lowercasing and camel-case resolution.
     * Dev-Remark: The BabelNet library utilizes space-separation.
     * @param lookupString The string that shall be normalized for lookup.
     * @return Space-separated lookup word.
     */
    public static String normalizeForBabelnetLookupWithTokenization(String lookupString){
        lookupString = lookupString.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", " "); // convert camelCase to under_score_case
        lookupString = lookupString.replace("_", " ");
        lookupString = lookupString.replaceAll("( ){1,}", " "); // make sure there are no double-spaces
        lookupString = lookupString.toLowerCase();
        return lookupString;
    }

    /**
     * Normalize for BabelNet lookup, i.e., lowercasing and camel-case resolution.
     * Dev-Remark: The BabelNet library utilizes space-separation.
     * @param lookupString The string that shall be normalized for lookup.
     * @return Space-separated lookup word.
     */
    public static String normalizeForBabelnetLookupWithoutTokenization(String lookupString){
        lookupString = lookupString.replace("_", " ");
        lookupString = lookupString.replaceAll("( ){1,}", " "); // make sure there are no double-spaces
        lookupString = lookupString.toLowerCase();
        return lookupString;
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
