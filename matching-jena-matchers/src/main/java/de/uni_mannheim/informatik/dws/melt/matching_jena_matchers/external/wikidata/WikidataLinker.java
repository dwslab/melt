package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.LeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.MaxGramLeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.*;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.WIKIDATA_LABEL_LINK_BUFFER;


/**
 * This linker links strings to Wikidata concepts.
 * Artificial links are introduced here starting with {@link WikidataLinker#MULTI_CONCEPT_PREFIX}.
 * The refer to a bag of links. All methods can work with URIs and with those multi-concept links!
 *
 * The {@link WikidataLinker#linkToSingleConcept(String)} method, for example, will return a multi label link.
 * In order to obtain the <em>actual</em> Wikidata URIs, use method {@link WikidataLinker#getUris(String)}.
 */
public class WikidataLinker implements LabelToConceptLinker {


    /**
     * Default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WikidataLinker.class);

    /**
     * The list of operations that is performed to find a concept in the dictionary.
     * Only used if {@link WikidataLinker#isRunAllStringModifications} is false.
     */
    LinkedList<StringModifier> stringModificationSequence;

    /**
     * A set of string operations that are all performed.
     * Only used if {@link WikidataLinker#isRunAllStringModifications} is false.
     */
    Set<StringModifier> stringModificationSet = new HashSet<>();

    /**
     * The public SPARQL endpoint.
     */
    private static final String ENDPOINT_URL = "https://query.wikidata.org/bigdata/namespace/wdq/sparql/";

    /**
     * Linker name
     */
    private String linkerName = "WikidataLinker";

    /**
     * Universal prefix for multi concepts.
     */
    public static final String MULTI_CONCEPT_PREFIX = "#ML_";

    /**
     * Service responsible for disk buffers.
     */
    PersistenceService persistenceService;

    /**
     * If true, all string modifications are performed to gain a high concept coverage.
     * This is by default true. If false, this may result to more precise results with a lower coverage.
     * Performance-wise: true will trigger only one query per linking operation, false may trigger many queries.
     *
     */
    private boolean isRunAllStringModifications = true;

    /**
     * If the disk-buffer is disabled, no buffers are read/written from/to the disk.
     * Default: true.
     */
    private boolean isDiskBufferEnabled = true;

    /**
     * Typically, one label refers to multiple wikidata concepts.
     * Hence, they are summarized in this data structure with the multi-concept as key.
     * A multi-concept must start with the {@link WikidataLinker#MULTI_CONCEPT_PREFIX}.
     * The data structure is also used as cache.
     */
    private static ConcurrentMap<String, Set<String>> multiLinkStore;

    /**
     * Constructor
     */
    public WikidataLinker(){
        this(true);
    }

    /**
     * Constructor
     *
     * @param isDiskBufferEnabled True if the disk buffer shall be enabled.
     */
    public WikidataLinker(boolean isDiskBufferEnabled){
        this.isDiskBufferEnabled = isDiskBufferEnabled;
        initializeBuffers();
        stringModificationSequence = new LinkedList<>();
        stringModificationSequence.add(new TokenizeConcatSpaceModifier());
        stringModificationSequence.add(new TokenizeConcatSpaceCapitalizeModifier());
        stringModificationSequence.add(new TokenizeConcatSpaceLowercaseModifier());
        stringModificationSequence.add(new TokenizeConcatSpaceModifierDropPlural());
        stringModificationSequence.add(new TokenizeConcatSpaceLowercaseModifierDropPlural());

        stringModificationSet.add(new TokenizeConcatSpaceModifier());
        stringModificationSet.add(new TokenizeConcatSpaceCapitalizeModifier());
        stringModificationSet.add(new TokenizeConcatSpaceLowercaseModifier());
        stringModificationSet.add(new TokenizeConcatSpaceModifierDropPlural());
        stringModificationSet.add(new TokenizeConcatSpaceLowercaseModifierDropPlural());
        // additions:
        stringModificationSet.add(new TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifier());
        stringModificationSet.add(new TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifierDropPlural());
    }

    /**
     * Initialization of buffers.
     */
    private void initializeBuffers(){
        persistenceService = PersistenceService.getService();
        if(isDiskBufferEnabled){
            this.multiLinkStore = persistenceService.getMapDatabase(WIKIDATA_LABEL_LINK_BUFFER);
        } else {
            this.multiLinkStore = new ConcurrentHashMap<>();
        }
    }

    /**
     * Given a multiConceptLink, this method will return the individual links.
     * @param multiConceptLink The lookup link.
     * @return Individual links, empty set if there are none.
     */
    public Set<String> getUris(String multiConceptLink){
        Set<String> result = new HashSet<>();
        if(!multiConceptLink.startsWith(MULTI_CONCEPT_PREFIX)){
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
    public HashSet<String> getUris(HashSet<String> multipleLinks){
        HashSet<String> result = new HashSet<>();
        for(String link : multipleLinks){
            if(link.startsWith(MULTI_CONCEPT_PREFIX)){
                result.addAll(getUris(link));
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
     * @return Link as String (!= wikidata URI).
     */
    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        return linkToSingleConcept(labelToBeLinked, Language.ENGLISH);
    }

    /**
     * Link to one concept. Note: Technically, one link will be returned BUT this link may represent multiple concepts.
     * To retrieve those concepts, method {@link WikidataLinker#getUris(String)} is to be called.
     * @param labelToBeLinked The label which shall be used to link to a concept.
     * @param language Language of the label to be linked.
     * @return One link representing one or more concepts on Wikidata as String. The link != URI!
     */
    public String linkToSingleConcept(String labelToBeLinked, Language language) {
        if(labelToBeLinked == null || language == null || labelToBeLinked.trim().equals("")){
            return null;
        }
        if(isRunAllStringModifications) {
            return linkToSingleConceptByRunningAllModifications(labelToBeLinked, language);
        } else {
            return linkToSingleConceptGreedy(labelToBeLinked, language);
        }
    }

    /**
     * Helper method. Multiple string operations are tried out. If one wikidata concept
     * could be found, the concept is immediately returned and the process stops prematurely.
     * @param labelToBeLinked The label that shall be linked.
     * @param language The language of the label.
     * @return One link representing one or more concepts on Wikidata as String. The link != URI!
     */
    private String linkToSingleConceptGreedy(String labelToBeLinked, Language language){
        String key = MULTI_CONCEPT_PREFIX + labelToBeLinked + "_" + language.toSparqlChar2();

        // cache lookup
        if (multiLinkStore.containsKey(key)) {
            if (multiLinkStore.get(key) == null || multiLinkStore.get(key).size() == 0) return null;
            else return key;
        }

        // run modification sequence
        String modifiedConcept;
        for (StringModifier modifier : stringModificationSequence) {
            modifiedConcept = modifier.modifyString(labelToBeLinked);
            boolean isFound = false;

            // try lookup
            HashSet<String> multiLinkLinks = new HashSet<>();
            List<String> labelResult = linkWithLabel(modifiedConcept, language);
            if (labelResult.size() > 0) {
                multiLinkLinks.addAll(labelResult);
            }
            List<String> altLabelResult = linkWithAltLabel(modifiedConcept, language);
            if (altLabelResult.size() > 0) {
                multiLinkLinks.addAll(altLabelResult);
            }

            if (multiLinkLinks.size() == 0) {
                isFound = false;
            } else {
                isFound = true;
            }

            if (isFound) {
                multiLinkStore.put(key, multiLinkLinks);
                commit();
                return key;
            }
        }
        // linking not successful
        multiLinkStore.put(key, new HashSet<>());
        commit();
        return null;
    }

    /**
     * Helper method: Will perform all string modifications and collect all concepts found thereby.
     * @param labelToBeLinked The label that shall be linked.
     * @param language Language of the label.
     * @return Link as String (!= Wikidata URI)
     */
    private String linkToSingleConceptByRunningAllModifications(String labelToBeLinked, Language language){
        String key = MULTI_CONCEPT_PREFIX + labelToBeLinked + "_" + language.toSparqlChar2() + "_all_modifications";

        if(multiLinkStore.containsKey(key)){
            LOGGER.debug("Found in buffer: " + key);
            if(multiLinkStore.get(key).size() == 0){
                return null;
            } else return key;
        }

        Set<String> allModifications = new HashSet<>();
        for(StringModifier modifier : stringModificationSet){
            allModifications.add(modifier.modifyString(labelToBeLinked));
        }
        // try lookup
        Set<String> multiLinkLinks = this.linkWithMultipleLabels(allModifications, language);

        if(multiLinkLinks.size() > 0){
            multiLinkStore.put(key, multiLinkLinks);
            commit();
            return key;
        } else {
            // linking not successful
            multiLinkStore.put(key, new HashSet<>());
            commit();
            return null;
        }
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
        String token = tokenizer.getInitialToken();
        while(token != null){
            String resultingConcept = linkToSingleConcept(token, language);
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
    private List<String> linkWithLabel(String label, Language language) {
        List<String> result = new ArrayList<>();
        String queryString = "SELECT ?c WHERE { ?c <http://www.w3.org/2000/01/rdf-schema#label> \"" + label + "\"@" + language.toSparqlChar2() + " . }";
        //System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
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
     * This will check the labels as well as the alternative labels in one query.
     * @param labels A set of labels that shall be used for the linking operation.
     * @param language The language of the labels.
     * @return Set of URIs that have been found.
     */
    private Set<String> linkWithMultipleLabels(Set<String> labels, Language language){
        Set<String> result = new HashSet<>();
        String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "SELECT ?c WHERE {\n";
        boolean first = true;
        for (String label : labels){
            if(first){
                first = false;
                queryString += buildFragmentLabelAltLabel(label, language);
            } else {
                queryString += "UNION\n" + buildFragmentLabelAltLabel(label, language);
            }
        }
        queryString += "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
        ResultSet resultSet = queryExecution.execSelect();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            String uri = solution.getResource("c").getURI();
            result.add(uri);
        }
        queryExecution.close();
        return result;
    }

    private String buildFragmentLabelAltLabel(String label, Language language){
        return "{ ?c rdfs:label \"" + label + "\"@" + language.toSparqlChar2() + " . }\n" +
                "UNION\n" +
                "{ ?c skos:altLabel \"" + label + "\"@" + language.toSparqlChar2() + " . }\n";
    }

    /**
     * Link with alternative label.
     * @param label Label.
     * @param language Language.
     * @return A list of URIs in String format.
     */
    private List<String> linkWithAltLabel(String label, Language language) {
        List<String> result = new ArrayList<>();
        String queryString = "SELECT ?c WHERE { ?c <http://www.w3.org/2004/02/skos/core#altLabel> \"" + label + "\"@" + language.toSparqlChar2() + " . }";
        //System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
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

    public boolean isRunAllStringModifications() {
        return isRunAllStringModifications;
    }

    public void setRunAllStringModifications(boolean runAllStringModifications) {
        this.isRunAllStringModifications = runAllStringModifications;
    }

    public boolean isDiskBufferEnabled() {
        return isDiskBufferEnabled;
    }

    /**
     * Commit data changes if active.
     */
    private void commit(){
        if(isDiskBufferEnabled){
            persistenceService.commit(WIKIDATA_LABEL_LINK_BUFFER);
        }
    }

    public void setDiskBufferEnabled(boolean diskBufferEnabled) {
        if(diskBufferEnabled && this.isDiskBufferEnabled) return;
        if(!diskBufferEnabled && !this.isDiskBufferEnabled) return;

        // re-initialize buffers
        this.isDiskBufferEnabled = diskBufferEnabled;
        initializeBuffers();
    }
}
