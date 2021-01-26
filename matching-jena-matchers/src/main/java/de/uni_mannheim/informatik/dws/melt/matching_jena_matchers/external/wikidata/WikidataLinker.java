package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.LeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.MaxGramLeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.StringModifier;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.TokenizeConcatSpaceCapitalizeModifier;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.TokenizeConcatSpaceLowercaseModifier;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.TokenizeConcatSpaceModifier;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;


/**
 * This linker links strings to Wikidata concepts.
 * Artificial links are introduced here starting with {@link WikidataLinker#multiConceptPrefix}.
 * The refer to a bag of links. All methods can work with URIs and with those multi-concept links!
 *
 * The {@link WikidataLinker#linkToSingleConcept(String)} method, for example, will return a multi label link.
 * In order to obtain the <em>actual</em> Wikidata URIs, use method {@link WikidataLinker#getLinks(String)}.
 */
public class WikidataLinker implements LabelToConceptLinker {

    /**
     * Default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WikidataLinker.class);

    /**
     * The list of operations that is performed to find a concept in the dictionary.
     */
    LinkedList<StringModifier> stringModificationSequence;

    /**
     * The public SPARQL endpoint.
     */
    private static final String endpointUrl = "https://query.wikidata.org/bigdata/namespace/wdq/sparql/";

    /**
     * Linker name
     */
    private String linkerName = "WikidataLinker";

    /**
     * Universal prefix for multi concepts.
     */
    public static final String multiConceptPrefix = "#ML_";

    /**
     * Typically, one label refers to multiple wikidata concepts.
     * Hence, they are summarized in this data structure with the multiconcept as key.
     * A multi-concept must start with the {@link WikidataLinker#multiConceptPrefix}.
     * This data structure is static in order to ensure one store is used even if two linkers are set up by accident.
     * The data structure is also used as cache.
     */
    private static HashMap<String, HashSet<String>> multiLinkStore = new HashMap<>();

    /**
     * Constructor
     */
    public WikidataLinker(){
        stringModificationSequence = new LinkedList<>();
        stringModificationSequence.add(new TokenizeConcatSpaceModifier());
        stringModificationSequence.add(new TokenizeConcatSpaceCapitalizeModifier());
        stringModificationSequence.add(new TokenizeConcatSpaceLowercaseModifier());
    }

    /**
     * Given a multiConceptLink, this method will return the individual links.
     * @param multiConceptLink The lookup link.
     * @return Individual links, empty set if there are none.
     */
    public HashSet<String> getLinks(String multiConceptLink){
        HashSet<String> result = new HashSet<>();
        if(!multiConceptLink.startsWith(multiConceptPrefix)){
            LOGGER.warn("The given link does not start with a prefix. Return null.");
            return result;
        }
        if(multiLinkStore.containsKey(multiConceptLink)){
            return multiLinkStore.get(multiConceptLink);
        }
        return result;
    }

    /**
     * Given a set of links where the links can be multi concept links or direct links, a set of only direct links
     * is returned.
     * @param multipleLinks Set with multiple links. Multi concept links can be mixed with direct links.
     * @return A set with only direct links.
     */
    public HashSet<String> getLinks(HashSet<String> multipleLinks){
        HashSet<String> result = new HashSet<>();
        for(String link : multipleLinks){
            if(link.startsWith(multiConceptPrefix)){
                result.addAll(getLinks(link));
            } else {
                result.add(link);
            }
        }
        return result;
    }

    /**
     * Will link one label to a multi-link concept.
     *
     * @param labelToBeLinked The label which shall be linked to a single concept.
     * @return Link as String.
     */
    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        return linkToSingleConcept(labelToBeLinked, Language.ENGLISH);
    }

    /**
     * Link to one concept. Note: Technically, one link will be returned BUT this link may represent multiple concepts.
     * To retrieve those concepts, method {@link WikidataLinker#getLinks(String)} is to be called.
     * @param labelToBeLinked The label which shall be used to link to a concept.
     * @param language Language of the label to be linked.
     * @return One link representing one or more concepts on Wikidata.
     */
    public String linkToSingleConcept(String labelToBeLinked, Language language) {
        if(labelToBeLinked == null || language == null || labelToBeLinked.trim().equals("")){
            return null;
        }
        String key = multiConceptPrefix + labelToBeLinked + "_" + language.toSparqlChar2();

        // cache lookup
        if(multiLinkStore.containsKey(key)){
            if (multiLinkStore.get(key) == null) return null;
            else return key;
        }

        // run modification sequence
        String modifiedConcept;
        for(StringModifier modifier : stringModificationSequence) {
            modifiedConcept = modifier.modifyString(labelToBeLinked);
            boolean isFound = false;

            // try lookup
            HashSet<String> multiLinkLinks = new HashSet<>();
            ArrayList<String> labelResult = linkWithLabel(modifiedConcept, language);
            if (labelResult.size() > 0) {
                multiLinkLinks.addAll(labelResult);
            }
            ArrayList<String> altLabelResult = linkWithAltLabel(modifiedConcept, language);
            if (altLabelResult.size() > 0) {
                multiLinkLinks.addAll(altLabelResult);
            }

            if(multiLinkLinks.size() == 0){
                isFound = false;
            } else {
                isFound = true;
            }

            if(isFound) {
                multiLinkStore.put(key, multiLinkLinks);
                return key;
            }
        }
        multiLinkStore.put(key, null);
        return null;
    }

    @Override
    public HashSet<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        return linkToPotentiallyMultipleConcepts(labelToBeLinked, Language.ENGLISH);
    }

    public HashSet<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked, Language language) {
        HashSet<String> result = linkLabelToTokensLeftToRight(labelToBeLinked, language);
        int possibleConceptParts = StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(labelToBeLinked)).length;

        int actualConceptParts = 0;
        for(String s : result) {
            actualConceptParts = actualConceptParts + StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(s)).length;
        }

        // TODO: for now: only 100% results
        if(possibleConceptParts <= actualConceptParts) {
            return result;
        }
        return null;
    }


    /**
     * Splits the labelToBeLinked in ngrams up to infinite size and tries to link components.
     * This corresponds to a MAXGRAM_LEFT_TO_RIGHT_TOKENIZER or NGRAM_LEFT_TO_RIGHT_TOKENIZER OneToManyLinkingStrategy.
     *
     * @param labelToBeLinked The label that shall be linked.
     * @param language The language of the label.
     * @return A set of concept URIs that were found.
     */
    private HashSet<String> linkLabelToTokensLeftToRight(String labelToBeLinked, Language language){
        //StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(labelToBeLinked));
        LeftToRightTokenizer tokenizer;
        String[] tokens = StringOperations.tokenizeBestGuess(labelToBeLinked);

        //tokenizer = new NgramLeftToRightTokenizer(tokens, "_", 10);
        tokenizer = new MaxGramLeftToRightTokenizer(tokens, " ");

        HashSet<String> result = new HashSet<>();
        String resultingConcept = "";
        String token = tokenizer.getInitialToken();
        while(token != null){
            resultingConcept = linkToSingleConcept(token, language);
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
     * Given a label, a set of Wikidata concepts (= URIs as String) will be returned that carry that label.
     * @param label The label to be used for the lookup.
     * @param language The language of the given label.
     * @return A list of URIs in String form.
     */
    private ArrayList<String> linkWithLabel(String label, Language language) {
        ArrayList<String> result = new ArrayList<>();
        String queryString = "SELECT ?c WHERE { ?c <http://www.w3.org/2000/01/rdf-schema#label> \"" + label + "\"@" + language.toSparqlChar2() + " . }";
        //System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query);
        ResultSet resultSet = queryExecution.execSelect();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            String uri = solution.getResource("c").getURI();
            result.add(uri);
        }
        queryExecution.close();
        return result;
    }

    /**
     * Link with alternative label.
     * @param label Label.
     * @param language Language.
     * @return A list of URIs in String format.
     */
    private ArrayList<String> linkWithAltLabel(String label, Language language) {
        ArrayList<String> result = new ArrayList<>();
        String queryString = "SELECT ?c WHERE { ?c <http://www.w3.org/2004/02/skos/core#altLabel> \"" + label + "\"@" + language.toSparqlChar2() + " . }";
        //System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query);
        ResultSet resultSet = queryExecution.execSelect();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            String uri = solution.getResource("c").getURI();
            result.add(uri);
        }
        queryExecution.close();
        return result;
    }


    @Override
    public String getNameOfLinker() {
        return this.linkerName;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        this.linkerName = nameOfLinker;
    }
}
