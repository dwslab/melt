package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.MultiConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.LeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.MaxGramLeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.*;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.apache.jena.query.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.DBPEDIA_LABEL_LINK_BUFFER;

public class DBpediaLinker implements LabelToConceptLinker, MultiConceptLinker {


    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaLinker.class);

    /**
     * The public SPARQL endpoint.
     */
    private static final String ENDPOINT_URL = "https://dbpedia.org/sparql";

    private String nameOfLinker = "DBpedia Linker";

    public static String getEndpointUrl() {
        return ENDPOINT_URL;
    }

    /**
     * Universal prefix for multi concepts.
     */
    public static final String MULTI_CONCEPT_PREFIX = "#ML_";

    /**
     * Service responsible for disk buffers.
     */
    PersistenceService persistenceService;

    /**
     * Often, one label refers to multiple concepts.
     * Hence, they are summarized in this data structure with the multi-concept as key.
     * A multi-concept must start with the {@link DBpediaLinker#MULTI_CONCEPT_PREFIX}.
     * The data structure is also used as cache.
     */
    private static ConcurrentMap<String, Set<String>> multiLinkStore;

    /**
     * True if buffer shall be written/read to/from disk to reduce the number of queries sent out.
     */
    private boolean isDiskBufferEnabled;

    /**
     * A set of string operations that are all performed.
     */
    Set<StringModifier> stringModificationSet = new HashSet<>();

    /**
     * True if a TDB source shall be used rather than an on-line SPARQL endpoint.
     */
    private boolean isUseTdb = false;

    /**
     * The TDB dataset into which the DBpedia data set was loaded.
     */
    private Dataset tdbDataset;

    /**
     * Constructor
     *
     * @param dBpediaKnowledgeSource DBpedia knowledge source (configuration of the knowledge source will will be used).
     */
    public DBpediaLinker(DBpediaKnowledgeSource dBpediaKnowledgeSource) {
        this.isDiskBufferEnabled = dBpediaKnowledgeSource.isDiskBufferEnabled();
        initializeBuffers();
        stringModificationSet.add(new TokenizeConcatSpaceModifier());
        stringModificationSet.add(new TokenizeConcatSpaceCapitalizeModifier());
        stringModificationSet.add(new TokenizeConcatSpaceLowercaseModifier());
        stringModificationSet.add(new TokenizeConcatSpaceModifierDropPlural());
        stringModificationSet.add(new TokenizeConcatSpaceLowercaseModifierDropPlural());
        stringModificationSet.add(new TokenizeConcatSpaceCapitalizeFirstLetterLowercaseRestModifier());
        stringModificationSet.add(new TokenizeConcatSpaceOnlyCapitalizeFirstLetterModifierDropPlural());
        this.isUseTdb = dBpediaKnowledgeSource.isUseTdb();
        if(this.isUseTdb){
            this.tdbDataset = dBpediaKnowledgeSource.getTdbDataset();
        }
    }

    /**
     * Given a multiConceptLink, this method will return the individual links.
     *
     * @param multiConceptLink The lookup link.
     * @return Individual links, empty set if there are none.
     */
    @NotNull
    public Set<String> getUris(String multiConceptLink) {
        Set<String> result = new HashSet<>();
        if (multiConceptLink == null) {
            return result;
        }
        if (!multiConceptLink.startsWith(MULTI_CONCEPT_PREFIX)) {
            LOGGER.warn("The given link does not start with a prefix. Returning the link");
            result.add(multiConceptLink);
            return result;
        }
        if (multiLinkStore.containsKey(multiConceptLink)) {
            return multiLinkStore.get(multiConceptLink);
        }
        return result;
    }

    @Override
    public boolean isMultiConceptLink(String link) {
        return link.startsWith(MULTI_CONCEPT_PREFIX);
    }

    /**
     * Given a set of links where the links can be multi concept links or direct links, a set of only direct links
     * is returned.
     *
     * @param multipleLinks Set with multiple links. Multi concept links can be mixed with direct links.
     * @return A set with only direct links.
     */
    public Set<String> getUris(Set<String> multipleLinks) {
        Set<String> result = new HashSet<>();
        for (String link : multipleLinks) {
            if (link.startsWith(MULTI_CONCEPT_PREFIX)) {
                result.addAll(getUris(link));
            } else {
                result.add(link);
            }
        }
        return result;
    }

    /**
     * Initialization of buffers.
     */
    private void initializeBuffers() {
        persistenceService = PersistenceService.getService();
        if (isDiskBufferEnabled) {
            multiLinkStore = persistenceService.getMapDatabase(DBPEDIA_LABEL_LINK_BUFFER);
        } else {
            multiLinkStore = new ConcurrentHashMap<>();
        }
    }

    /**
     * Link to a single concept. The assumed language is English.
     *
     * @param labelToBeLinked The label which shall be linked to a single concept.
     * @return Link. The link is not a URI.
     */
    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        return linkToSingleConcept(labelToBeLinked, Language.ENGLISH);
    }

    /**
     * Link to a single concept. The assumed language is English.
     *
     * @param labelToBeLinked The label which shall be linked to a single concept.
     * @param language        Language
     * @return Link. The link is not a URI.
     */
    public String linkToSingleConcept(String labelToBeLinked, Language language) {
        if (labelToBeLinked == null || language == null || labelToBeLinked.trim().equals("")) {
            return null;
        }
        String key = MULTI_CONCEPT_PREFIX + labelToBeLinked + "_" + language.toSparqlChar2();
        if (multiLinkStore.containsKey(key)) {
            LOGGER.debug("Found in buffer: " + key);
            if (multiLinkStore.get(key).size() == 0) {
                return null;
            } else return key;
        }
        Set<String> allModifications = new HashSet<>();
        for (StringModifier modifier : stringModificationSet) {
            allModifications.add(modifier.modifyString(labelToBeLinked));
        }

        // try lookup
        String queryString = this.getLinkerQueryString(allModifications, language);
        QueryExecution queryExecution;
        if(this.isUseTdb){
            queryExecution = QueryExecutionFactory.create(queryString, tdbDataset);
        } else {
            queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, queryString);
        }
        ResultSet resultSet = SparqlServices.safeExecution(queryExecution);
        Set<String> uris = new HashSet<>();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            String uri = solution.getResource("c").getURI();
            uris.add(uri);
        }
        queryExecution.close();

        // now we need to check disambiguations
        Set<String> disambiguations = new HashSet<>();
        for (String uri : uris) {
            disambiguations.addAll(getDisambiguationUris(uri));
        }
        uris.addAll(disambiguations);

        multiLinkStore.put(key, uris);
        commit();
        if (uris.size() > 0) {
            return key;
        } else return null;
    }

    /**
     * Commit data changes if active.
     */
    private void commit() {
        if (isDiskBufferEnabled) {
            persistenceService.commit(DBPEDIA_LABEL_LINK_BUFFER);
        }
    }

    /**
     * @param uri The URI for which a disambiguation shall be obtained.
     * @return A set of URIs. The set may be empty.
     */
    @NotNull
    public Set<String> getDisambiguationUris(String uri) {
        Set<String> result = new HashSet<>();
        if (uri == null || uri.equals("")) {
            return result;
        }
        String disambiguationQuery = "SELECT DISTINCT ?c WHERE {<" + uri + "> <http://dbpedia.org/ontology/wikiPageDisambiguates> ?c}";
        QueryExecution queryExecution;

        if(this.isUseTdb){
            queryExecution = QueryExecutionFactory.create(disambiguationQuery, tdbDataset);
        } else {
            queryExecution = QueryExecutionFactory.sparqlService(getEndpointUrl(), disambiguationQuery);
        }

        ResultSet queryResult = SparqlServices.safeExecution(queryExecution);
        while (queryResult.hasNext()) {
            QuerySolution solution = queryResult.next();
            String disambiguatedUri = solution.getResource("c").getURI();
            result.add(disambiguatedUri);
        }
        queryExecution.close();
        return result;
    }

    /**
     * SPARQL query to find links using a label and a language.
     *
     * @param concepts The concepts for which shall be looked.
     * @param language The language of the label.
     * @return SPARQL String that can be executed.
     */
    static String getLinkerQueryString(Set<String> concepts, Language language) {
        StringBuilder result = new StringBuilder();
        result.append("SELECT DISTINCT ?c WHERE {\n");
        boolean first = true;
        for (String concept : concepts) {
            if (first) {
                first = false;
            } else {
                result.append("UNION ");
            }
            result
                    .append(getPredicateQueryLine("http://www.w3.org/2000/01/rdf-schema#label", concept, language))
                    .append("UNION ")
                    .append(getPredicateQueryLine("http://xmlns.com/foaf/0.1/name", concept, language))
                    .append("UNION ")
                    .append(getPredicateQueryLine("http://dbpedia.org/property/name", concept, language))
                    .append("UNION ")
                    .append(getPredicateQueryLine("http://dbpedia.org/property/otherNames", concept, language))
                    .append("UNION ")
                    .append(getPredicateQueryLine("http://dbpedia.org/ontology/alias", concept, language));
        }
        result.append("}");
        return result.toString();
    }

    /**
     * Helper method to build a query.
     *
     * @param predicate The predicate.
     * @param concept   The concept String representation.
     * @param language  The language.
     * @return A string builder.
     */
    static StringBuilder getPredicateQueryLine(String predicate, String concept, Language language) {
        StringBuilder result = new StringBuilder();
        result
                .append("{?c <")
                .append(predicate)
                .append("> \"")
                .append(concept)
                .append("\"@")
                .append(language.toSparqlChar2())
                .append("}\n");
        return result;
    }

    @Override
    public Set<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        return linkToPotentiallyMultipleConcepts(labelToBeLinked, Language.ENGLISH);
    }

    public HashSet<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked, Language language) {
        HashSet<String> result = linkLabelToTokensLeftToRight(labelToBeLinked, language);
        int possibleConceptParts = StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(labelToBeLinked)).length;

        int actualConceptParts = 0;
        for (String s : result) {
            actualConceptParts = actualConceptParts + StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(s)).length;
        }

        // TODO: for now: only 100% results
        if (possibleConceptParts <= actualConceptParts) {
            return result;
        }
        return null;
    }

    /**
     * Splits the labelToBeLinked in ngrams up to infinite size and tries to link components.
     * This corresponds to a MAXGRAM_LEFT_TO_RIGHT_TOKENIZER or NGRAM_LEFT_TO_RIGHT_TOKENIZER OneToManyLinkingStrategy.
     *
     * @param labelToBeLinked The label that shall be linked.
     * @param language        The language of the label.
     * @return A set of concept URIs that were found.
     */
    private HashSet<String> linkLabelToTokensLeftToRight(String labelToBeLinked, Language language) {
        //StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(labelToBeLinked));
        LeftToRightTokenizer tokenizer;
        String[] tokens = StringOperations.tokenizeBestGuess(labelToBeLinked);

        //tokenizer = new NgramLeftToRightTokenizer(tokens, "_", 10);
        tokenizer = new MaxGramLeftToRightTokenizer(tokens, " ");

        HashSet<String> result = new HashSet<>();
        String token = tokenizer.getInitialToken();
        while (token != null) {
            String resultingConcept = linkToSingleConcept(token, language);
            if (resultingConcept == null || resultingConcept.length() == 0) {
                token = tokenizer.getNextTokenNotSuccessful();
            } else {
                result.add(resultingConcept);
                token = tokenizer.getNextTokenSuccessful();
            }
        }
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

    public boolean isDiskBufferEnabled() {
        return isDiskBufferEnabled;
    }

    public void setDiskBufferEnabled(boolean diskBufferEnabled) {
        if (diskBufferEnabled && this.isDiskBufferEnabled) return;
        if (!diskBufferEnabled && !this.isDiskBufferEnabled) return;

        // re-initialize buffers
        this.isDiskBufferEnabled = diskBufferEnabled;
        initializeBuffers();
    }
}
