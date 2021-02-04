package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.apache.jena.query.*;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.*;
import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices.safeAsk;

public class WikidataKnowledgeSource extends SemanticWordRelationDictionary {


    /**
     * Buffer for repeated synonymy requests.
     */
    ConcurrentMap<String, HashSet<String>> synonymyBuffer;

    /**
     * Buffer for repeated hypernymy requests.
     */
    ConcurrentMap<String, HashSet<String>> hypernymyBuffer;

    /**
     * Buffer for (expensive) ask queries.
     */
    ConcurrentMap<String, Boolean> askBuffer;

    /**
     * Linker for the Wikidata knowledge source.
     */
    private WikidataLinker linker = new WikidataLinker();

    /**
     * The public SPARQL endpoint.
     */
    private static final String ENDPOINT_URL = "https://query.wikidata.org/bigdata/namespace/wdq/sparql/";

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataKnowledgeSource.class);

    /**
     * Name of the instance.
     */
    private String knowledgeSourceName = "WikidataKnowledgeSource";

    /**
     * Service responsible for disk buffers.
     */
    private PersistenceService persistenceService;

    /**
     * If the disk-buffer is disabled, no buffers are read/written from/to the disk.
     * Default: true.
     */
    private boolean isDiskBufferEnabled = true;

    /**
     * Constructor
     */
    public WikidataKnowledgeSource(){
        this(true);
    }

    /**
     * Constructor
     *
     * @param isDiskBufferEnabled True if the buffer shall be enabled.
     */
    public WikidataKnowledgeSource(boolean isDiskBufferEnabled){
        this.isDiskBufferEnabled = isDiskBufferEnabled;
        initializeBuffers();
        if(this.linker == null){
            this.linker = new WikidataLinker();
        }
    }

    /**
     * Initialize buffers (either on-disk or memory).
     */
    private void initializeBuffers(){
        persistenceService = PersistenceService.getService();
        if(isDiskBufferEnabled){
            this.synonymyBuffer = persistenceService.getMapDatabase(WIKIDATA_SYNONYMY_BUFFER);
            this.hypernymyBuffer = persistenceService.getMapDatabase(WIKIDATA_HYPERNYMY_BUFFER);
            this.askBuffer = persistenceService.getMapDatabase(WIKIDATA_ASK_BUFFER);
        } else {
            this.synonymyBuffer = new ConcurrentHashMap<>();
            this.hypernymyBuffer = new ConcurrentHashMap<>();
            this.askBuffer = new ConcurrentHashMap<>();
        }
    }

    /**
     * Test whether the given word can be mapped (1-1) to a Wikidata concept (no smart mechanisms applied).
     * The assumed default language is English.
     *
     * @param word The word to be looked for.
     * @return True if the word can be found in the dictionary.
     */
    @Override
    public boolean isInDictionary(String word) {
        return isInDictionary(word, Language.ENGLISH);
    }

    /**
     * Test whether the given word can be mapped (1-1) to a Wikidata concept (no smart mechanisms applied).
     *
     * @param word     The word to be used for the concept lookup.
     * @param language The language of the word
     * @return True if the specified word is found in the knowledge resource.
     */
    public boolean isInDictionary(String word, Language language) {
        if (isInDictionaryWithLabelAskQuery(word, language)) return true;
        else return isInDictionaryWithAltLabelAskQuery(word, language);
    }

    /**
     * Ask query with label.
     *
     * @param word     The concept label that shall be looked up.
     * @param language The language of the label.
     * @return True, if a concept has the label as rdfs:label.
     */
    private boolean isInDictionaryWithLabelAskQuery(String word, Language language) {
        String queryString = "ASK WHERE { ?c <http://www.w3.org/2000/01/rdf-schema#label> \"" + word + "\"@" + language.toSparqlChar2() + " . }";
        //System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
        boolean result = queryExecution.execAsk();
        return result;
    }

    /**
     * Ask query with altLabel.
     *
     * @param word     The concept label that shall be looked up.
     * @param language The language of the label.
     * @return True, if a concept has the label as skos:altLabel.
     */
    private boolean isInDictionaryWithAltLabelAskQuery(String word, Language language) {
        String queryString = "ASK WHERE { ?c <http://www.w3.org/2004/02/skos/core#altLabel> \"" + word + "\"@" + language.toSparqlChar2() + " . }";
        //System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
        boolean result = queryExecution.execAsk();
        return result;
    }

    @Override
    public HashSet<String> getSynonyms(String linkedConcept) {
        return getSynonyms(linkedConcept, Language.ENGLISH);
    }

    /**
     * Language-bound synonymy retrieval.
     *
     * @param linkedConcept The linked concept for which synonyms shall be retrieved.
     * @param language      The language of the synonyms.
     * @return A set of synonyms (string).
     */
    public HashSet<String> getSynonyms(String linkedConcept, Language language) {
        String key = linkedConcept + "_" + language.toSparqlChar2();
        if (synonymyBuffer.containsKey(key)) {
            return synonymyBuffer.get(key);
        }

        HashSet<String> result = new HashSet<>();
        if (linkedConcept.startsWith(WikidataLinker.MULTI_CONCEPT_PREFIX)) {
            Set<String> individualLinks = this.linker.getUris(linkedConcept);
            for (String individualLink : individualLinks) {
                result.addAll(getSynonyms(individualLink, language));
            }
        } else {
            String queryString = "SELECT ?l WHERE { <" + linkedConcept + "> <http://www.w3.org/2004/02/skos/core#altLabel> ?l . FILTER(LANG(?l) = '" + language.toSparqlChar2() + "') }";
            //System.out.println(queryString);
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
            ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();
                result.add(solution.getLiteral("?l").getLexicalForm());
            }
        }
        synonymyBuffer.put(key, result);
        return result;
    }

    /**
     * For multiple words look for all links.
     *
     * @param conceptsToBeLinked An array of concepts that shall be linked.
     * @return A list of links that were found for the given concepts. Concepts that could not be linked are ignored.
     * If none of the given concepts can be linked, the resulting ArrayList will be empty.
     */
    public ArrayList<String> getConceptLinks(String[] conceptsToBeLinked) {

        // result data structure
        ArrayList<String> result = new ArrayList<>();

        // linking mechanism
        LabelToConceptLinker linker = getLinker();

        // link each of the given labels in variable 'result'
        for (String label : conceptsToBeLinked) {
            String link = linker.linkToSingleConcept(label);
            if (link == null) {
                LOGGER.debug("Concept '" + label + "' could not be linked into the given knowledge graph.");
            } else {
                result.add(link);
                LOGGER.debug("Concept '" + label + "' was linked to: " + link);
            }
        }
        return result;
    }

    /**
     * Determine the closest common hypernym.
     *
     * @param links       The linked concepts for which the closest common hypernym shall be found.
     * @param limitOfHops This is an expensive operation. You can limit the number of upward hops to perform.
     * @return The closest common hypernym together with the upwards-depth.
     * This is represented as pair:<br>
     * [0] Set of common concepts (String)<br>
     * [1] The depth as integer. If there is a direct hyperconcept, the depth will be equal to 1.<br>
     * If multiple candidates apply, all are returned. If there is no closest common hypernym, null will be
     * returned.
     */
    public Pair<Set<String>, Integer> getClosestCommonHypernym(List<String> links, int limitOfHops) {

        // The links for the next iteration, i.e. the concepts whose hypernyms will be looked for in the next
        //iteration.
        HashMap<String, HashSet<String>> linksForNextIteration = new HashMap<>();

        // All hypernyms
        HashMap<String, HashSet<String>> allHyperconcepts = new HashMap<>();
        int currentHops = 0;

        for (; currentHops < limitOfHops; currentHops++) {

            LOGGER.debug("\n\nIteration " + (currentHops + 1));

            for (String link : links) {

                HashSet<String> nextNextIteration = new HashSet<>();
                if (!linksForNextIteration.containsKey(link)) {
                    // there is no next lookup defined -> use root link

                    nextNextIteration = (getHypernyms(link));

                    // set links for next iteration
                    linksForNextIteration.put(link, nextNextIteration);

                    // set links all hypernyms
                    addOrPut(allHyperconcepts, link, nextNextIteration);

                    // simple logging
                    if (nextNextIteration != null && nextNextIteration.size() > 0) {
                        LOGGER.debug("\nHyperconcepts for " + link);
                        for (String s : nextNextIteration) {
                            LOGGER.debug("\t" + s);
                        }
                    }

                } else {
                    // the next lookup iteration has been defined

                    for (String nextConcept : linksForNextIteration.get(link)) {
                        nextNextIteration.addAll(getHypernyms(nextConcept));
                    }

                    // set links for next iteration
                    linksForNextIteration.put(link, nextNextIteration);

                    // set links all hypernyms
                    addOrPut(allHyperconcepts, link, nextNextIteration);

                    // just logging:
                    if (nextNextIteration.size() > 0) {
                        LOGGER.debug("\nNew hypernyms for " + link);

                        // just logging:
                        for (String s : nextNextIteration) {
                            LOGGER.debug("\t" + s);
                        }
                    }
                }
            }

            // check whether a common hypernym has been found
            Set<String> commonConcepts = determineCommonConcepts(allHyperconcepts);

            if (commonConcepts.size() > 0) {
                return new Pair<>(commonConcepts, currentHops + 1);
            }
        }
        // nothing found, return an empty set
        return null;
    }

    /**
     * Helper method.
     *
     * @param map      The map to which shall be added or put.
     * @param key      Key for the map.
     * @param setToAdd What shall be added.
     */
    private static void addOrPut(HashMap<String, HashSet<String>> map, String key, HashSet<String> setToAdd) {
        if (setToAdd == null) return;
        if (map.containsKey(key)) map.get(key).addAll(setToAdd);
        else map.put(key, setToAdd);
    }

    /**
     * Helper method. Given a map of concepts, the common concepts are determined and returned.
     * Package modifier for better testing.
     *
     * @param data The data structure in which it shall be checked whether there are common concepts.
     * @return Common concepts. Set is empty if there are non.
     */
    static Set<String> determineCommonConcepts(HashMap<String, HashSet<String>> data) {
        HashSet<String> result = new HashSet<>();
        HashSet<String> alreadyProcessed = new HashSet<>();
        for (HashMap.Entry<String, HashSet<String>> entry : data.entrySet()) {
            for (String concept : entry.getValue()) {
                if (!alreadyProcessed.contains(concept)) {
                    // now check whether the current concept is contained everywhere
                    boolean isCommonConcept = true;
                    for (HashMap.Entry<String, HashSet<String>> entry2 : data.entrySet()) {
                        if (!entry2.getValue().contains(concept)) {
                            // not a common concept -> leave for loop early
                            isCommonConcept = false;
                            break;
                        }
                    }
                    if (isCommonConcept) {
                        result.add(concept);
                    }
                    alreadyProcessed.add(concept);
                }
            }
        }
        return result;
    }

    /**
     * Query fragment, add '}' to make it usable.
     * Replace {@code <subconcept>} and {@code <superconcept>}.
     */
    private static final String IS_HYPERNYM_LEVEL_1_NO_CLOSE =
            "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
            "ASK WHERE {\n" +
                    "{ <subconcept> wdt:P31 <superconcept>. }\n" +
                    "UNION\n" +
                    "{ <subconcept> wdt:P279 <superconcept>. }\n";

    /**
     * Query fragment, add '}' to make it usable.
     * Replace {@code <subconcept>} and {@code <superconcept>}.
     */
    private static final String IS_HYPERNYM_LEVEL_2_NO_CLOSE =
            IS_HYPERNYM_LEVEL_1_NO_CLOSE +
                    "  UNION" +
                    "  {\n" +
                    "    <subconcept> wdt:P31 ?1.\n" +
                    "    ?1 wdt:P31 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P31 ?1.\n" +
                    "    ?1 wdt:P279 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P279 ?1.\n" +
                    "    ?1 wdt:P31 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P279 ?1.\n" +
                    "    ?1 wdt:P279 <superconcept>.\n" +
                    "  }\n";

    /**
     * Query fragment, add '}' to make it usable.
     * Replace {@code <subconcept>} and {@code <superconcept>}.
     */
    private static final String IS_HYPERNYM_LEVEL_3_NO_CLOSE =
            IS_HYPERNYM_LEVEL_2_NO_CLOSE +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P31 ?1.\n" +
                    "    ?1 wdt:P31 ?2.\n" +
                    "    ?2 wdt:P31 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "    {\n" +
                    "    <subconcept> wdt:P31 ?1.\n" +
                    "    ?1 wdt:P31 ?2.\n" +
                    "    ?2 wdt:P279 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P31 ?1.\n" +
                    "    ?1 wdt:P279 ?2.\n" +
                    "    ?2 wdt:P279 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P31 ?1.\n" +
                    "    ?1 wdt:P279 ?2.\n" +
                    "    ?2 wdt:P31 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P279 ?1.\n" +
                    "    ?1 wdt:P31 ?2.\n" +
                    "    ?2 wdt:P31 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P279 ?1.\n" +
                    "    ?1 wdt:P31 ?2.\n" +
                    "    ?2 wdt:P279 <superconcept>.\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P279 ?1.\n" +
                    "    ?1 wdt:P279 ?2.\n" +
                    "    ?2 wdt:P31 <superconcept>.\n" +
                    "  }\n" +
                    "    UNION\n" +
                    "  {\n" +
                    "    <subconcept> wdt:P279 ?1.\n" +
                    "    ?1 wdt:P279 ?2.\n" +
                    "    ?2 wdt:279 <superconcept>.\n" +
                    "  }";

    /**
     * Checks whether one wikidata URI is a subclass/instance of the other.
     *
     * @param superconcept URI of the superconcept.
     * @param subconcept   URI of the subconcept.
     * @param depth        The depth.
     * @return Query as String.
     */
    static String buildHypernymDepthQuery(String superconcept, String subconcept, int depth) {
        if (superconcept == null || subconcept == null) {
            LOGGER.error("The concepts cannot be null.");
            return null;
        }
        superconcept = StringOperations.addTagIfNotExists(superconcept);
        subconcept = StringOperations.addTagIfNotExists(subconcept);
        switch (depth) {
            case 1:
                return replaceConceptsAndCompleteQuery(IS_HYPERNYM_LEVEL_1_NO_CLOSE, superconcept, subconcept);
            case 2:
                return replaceConceptsAndCompleteQuery(IS_HYPERNYM_LEVEL_2_NO_CLOSE, superconcept, subconcept);
            case 3:
                return replaceConceptsAndCompleteQuery(IS_HYPERNYM_LEVEL_3_NO_CLOSE, superconcept, subconcept);
            default:
                LOGGER.error("Query not implemented for a depth of " + depth + ".\n" +
                        "Note: A depth of > 3 will contain more than 14 statements that are joined with UNION - do you " +
                        "really want this? Returning null.");
                return null;
        }
    }

    /**
     * Helper method. Only to be used in {@link WikidataKnowledgeSource#buildHypernymDepthQuery(String, String, int)}.
     *
     * @param template     Template to be used.
     * @param superConcept Super concept.
     * @param subConcept   Sub concept.
     * @return Complete query.
     */
    private static String replaceConceptsAndCompleteQuery(String template, String superConcept, String subConcept) {
        String result = template;
        result = result.replaceAll("<subconcept>", subConcept);
        result = result.replaceAll("<superconcept>", superConcept);
        return result + "}";
    }

    /**
     * Determine whether the specified superConcept is actually a superConcept given the specified subConcept.
     * @param superConcept URI or link.
     * @param subConcept URI or link.
     * @param depth The desired depth (integer in the range [1, 2, 3]).
     * @return True if it is a hypernym, else false.
     */
    public boolean isHypernym(String superConcept, String subConcept, int depth) {
        if(superConcept == null ||subConcept == null){
            LOGGER.error("The concepts cannot be null - one of them is. Returning false.");
            return false;
        }

        // check the buffer
        String key = "IS_HYPER_" + superConcept + "_" + subConcept + "_d" + depth;
        if(askBuffer.containsKey(key)){
            LOGGER.debug("Serving from buffer: " + key);
            return askBuffer.get(key);
        }

        Set<String> superUris = new HashSet<>();
        Set<String> subUris = new HashSet<>();
        boolean superIsUri = false;
        boolean subIsUri = false;
        if (superConcept.startsWith(WikidataLinker.MULTI_CONCEPT_PREFIX)) {
            Set<String> individualLinks = this.linker.getUris(superConcept);
            if (individualLinks != null) superUris.addAll(individualLinks);
        } else superIsUri = true;
        if(subConcept.startsWith(WikidataLinker.MULTI_CONCEPT_PREFIX)){
            Set<String> individualLinks = this.linker.getUris(subConcept);
            if(individualLinks != null) subUris.addAll(individualLinks);
        } else subIsUri = true;
        if( (superUris.size() == 0 && !superIsUri) || (subUris.size() == 0 && !subIsUri) ){
            askBuffer.put(key, false);
            commit();
            return false;
        }
        if(superIsUri && subIsUri){
            // we have two URIs: end of recursion
            String queryString = buildHypernymDepthQuery(superConcept, subConcept, depth);
            boolean result = safeAsk(queryString, ENDPOINT_URL);
            askBuffer.put(key, result);
            commit();
            return result;
        } else {
            // we have at least one link, we need to add the link to the URI set so that it works in mixed cases (one link and one URI)
            if(superIsUri){
                superUris.add(superConcept);
            } else if(subIsUri){
                subUris.add(subConcept);
            }

            for (String superConceptUri : superUris) {
                for (String subConceptUri : subUris) {
                    // let's recursively determine a solution
                    boolean intermediateResult = isHypernym(superConceptUri, subConceptUri, depth);
                    if (intermediateResult){
                        askBuffer.put(key, true);
                        commit();
                        return true;
                    }
                }
            }
        }
        askBuffer.put(key, false);
        commit();
        return false;
    }

    /**
     * The query obtained is so that the depth is upwards followed. There is no mixture of wdt:P31 (instance of) and
     * wdt:P279 (subclass of). That means, that only super-instances are upwards followed UNION superclasses are upwards
     * followed. The "instance-of" of a super-class cannot be found with this query!
     * <p>
     * DEV remark: This is a bit too involved for an easy-to-understand API. This is currently not used. Look at the
     * unit test to better understand what the query does.
     *
     * @param wikidataUri The wikidata URI.
     * @param depth       The desired depth.
     * @return The query as String.
     */
    static String buildInstanceOfSublcassOfCleanQuery(String wikidataUri, int depth) {
        String selectParameters = "?c1";
        for (int i = 1; i < depth; i++) {
            selectParameters += " ?c" + (i + 1);
        }

        String instanceTriples = "";
        for (int i = 1; i < depth; i++) {
            instanceTriples += "     OPTIONAL{?c" + i + " wdt:P31 " + "?c" + (i + 1) + " .}\n";
        }

        String subclassTriples = "";
        for (int i = 1; i < depth; i++) {
            subclassTriples += "     OPTIONAL{?c" + i + " wdt:P279 " + "?c" + (i + 1) + " .}\n";
        }

        return  "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                "SELECT DISTINCT " + selectParameters + " WHERE { \n" +
                "  {\n" +
                "     <" + wikidataUri + "> wdt:P31 ?c1 .\n" +
                instanceTriples +
                "}\n" +
                "UNION\n" +
                "{\n" +
                "     <" + wikidataUri + "> wdt:P279 ?c1 .\n" +
                subclassTriples +
                "}\n}";
    }

    /**
     * This will return the direct hypernyms as String.
     *
     * @param linkedConcept The linked concept for which hypernyms shall be retrieved. The linked concept is a URI.
     * @return The found hypernyms as links (URIs). If it is planned to immediately use the lexical representation
     * use {@link WikidataKnowledgeSource#getHypernymsLexical(String, Language)}. In case nothing was found,
     * an empty set will be returned.
     */
    @Override
    public HashSet<String> getHypernyms(String linkedConcept) {
        HashSet<String> result = new HashSet<>();
        if (linkedConcept.startsWith(WikidataLinker.MULTI_CONCEPT_PREFIX)) {
            Set<String> individualLinks = this.linker.getUris(linkedConcept);
            if (individualLinks == null) {
                return result;
            }
            for (String individualLink : individualLinks) {
                result.addAll(getHypernyms(individualLink));
            }
        } else {
            String queryString = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                    "SELECT DISTINCT ?c WHERE { \n" +
                    "  { <" + linkedConcept + "> wdt:P31 ?c . }\n" +
                    "  UNION\n" +
                    "  { <" + linkedConcept + "> wdt:P279 ?c . }\n" +
                    "}";
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
        }
        return result;
    }

    /**
     * Uses wdt:P31 (instance of) as well as wdt:P279 (subclass of).
     *
     * @param linkedConcept The concept that has already been linked (URI). The assumed language is English.
     * @return A set of links.
     */
    public HashSet<String> getHypernymsLexical(String linkedConcept) {
        return getHypernymsLexical(linkedConcept, Language.ENGLISH);
    }

    /**
     * Uses wdt:P31 (instance of) as well as wdt:P279 (subclass of).
     *
     * @param linkedConcept The concept that has already been linked (URI).
     * @param language      Language of the strings.
     * @return A set of links.
     */
    public HashSet<String> getHypernymsLexical(String linkedConcept, Language language) {
        String key = linkedConcept + "_" + language.toSparqlChar2();
        HashSet<String> result = new HashSet<>();
        if (linkedConcept.startsWith(WikidataLinker.MULTI_CONCEPT_PREFIX)) {
            Set<String> individualLinks = this.linker.getUris(linkedConcept);
            for (String individualLink : individualLinks) {
                result.addAll(getHypernymsLexical(individualLink, language));
            }
        } else {
            String queryString = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                    "SELECT DISTINCT ?l WHERE { \n" +
                    "  { <" + linkedConcept + "> wdt:P31 ?c . \n" +
                    "    ?c rdfs:label ?l .\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  { <" + linkedConcept + "> wdt:P31 ?c . \n" +
                    "    ?c skos:altLabel ?l .\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  { <" + linkedConcept + "> wdt:P279 ?c . \n" +
                    "    ?c rdfs:label ?l .\n" +
                    "  }\n" +
                    "  UNION\n" +
                    "  { <" + linkedConcept + "> wdt:P279 ?c . \n" +
                    "    ?c skos:altLabel ?l .\n" +
                    "  }\n" +
                    "  FILTER(LANG(?l) = '" + language.toSparqlChar2() + "')\n" +
                    "}";
            //System.out.println(queryString);
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
            ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();
                String uri = solution.getLiteral("l").getLexicalForm();
                result.add(uri);
            }
            queryExecution.close();
        }
        hypernymyBuffer.put(key, result);
        return result;
    }

    /**
     * Given a URI, obtain the written representations, i.e., the labels.
     *
     * @param linkedConcept The URI for which labels shall be obtained.
     * @return A set of labels.
     */
    public HashSet<String> getLabelsForLink(String linkedConcept) {
        return getLabelsForLink(linkedConcept, Language.ENGLISH);
    }

    /**
     * Given a linked concept, retrieve all labels (rdfs:label, skos:altLabel).
     *
     * @param linkedConcept The link to the concept (URI).
     * @param language      Desired language for the labels.
     * @return Set of labels, all in the specified language.
     */
    public HashSet<String> getLabelsForLink(String linkedConcept, Language language) {
        HashSet<String> result = new HashSet<>();
        if (linkedConcept.startsWith(WikidataLinker.MULTI_CONCEPT_PREFIX)) {
            Set<String> individualLinks = this.linker.getUris(linkedConcept);
            for (String individualLink : individualLinks) {
                result.addAll(getLabelsForLink(individualLink, language));
            }
        } else {
            String queryString = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "SELECT DISTINCT ?l WHERE { \n" +
                    "  { <" + linkedConcept + "> rdfs:label ?l .}\n" +
                    "  UNION\n" +
                    "  { <" + linkedConcept + "> skos:altLabel ?l .}\n" +
                    "  FILTER(LANG(?l) = '" + language.toSparqlChar2() + "') }";
            //System.out.println(queryString);
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
            ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();
                String lexicalForm = solution.getLiteral("l").getLexicalForm();
                result.add(lexicalForm);
            }
            queryExecution.close();
        }
        return result;
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName() {
        return this.knowledgeSourceName;
    }

    public boolean isDiskBufferEnabled() {
        return isDiskBufferEnabled;
    }


    private void commit(PersistenceService.PreconfiguredPersistences persistence){
        switch (persistence){
            case WIKIDATA_SYNONYMY_BUFFER:
                persistenceService.commit(WIKIDATA_SYNONYMY_BUFFER);
                return;
            case WIKIDATA_ASK_BUFFER:
                persistenceService.commit(WIKIDATA_ASK_BUFFER);
                return;
            case WIKIDATA_HYPERNYMY_BUFFER:
                persistenceService.commit(WIKIDATA_HYPERNYMY_BUFFER);
                return;
        }
    }

    /**
     * Commit data changes if active.
     */
    private void commit(){
        if(isDiskBufferEnabled){
            persistenceService.commit(WIKIDATA_SYNONYMY_BUFFER);
            persistenceService.commit(WIKIDATA_HYPERNYMY_BUFFER);
            persistenceService.commit(WIKIDATA_ASK_BUFFER);
        }
    }

    /**
     * Note that when you disable your buffer during runtime, the buffer will be reinitialized.
     * @param diskBufferEnabled True for enablement, else false.
     */
    public void setDiskBufferEnabled(boolean diskBufferEnabled) {

        // do nothing if already enabled and set enabled
        if(diskBufferEnabled && this.isDiskBufferEnabled && ((WikidataLinker) this.getLinker()).isDiskBufferEnabled()) return;

        // do nothing if already enabled and set enabled
        if(!diskBufferEnabled && !this.isDiskBufferEnabled && !((WikidataLinker) this.getLinker()).isDiskBufferEnabled()) return;

        // re-initialize buffers
        this.isDiskBufferEnabled = diskBufferEnabled;
        initializeBuffers();

        // also organize the linker
        ((WikidataLinker) this.getLinker()).setDiskBufferEnabled(diskBufferEnabled);
    }
}
