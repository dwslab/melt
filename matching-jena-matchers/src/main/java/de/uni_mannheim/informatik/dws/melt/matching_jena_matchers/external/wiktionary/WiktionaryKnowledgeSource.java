package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices;
import org.apache.jena.query.*;
import org.apache.jena.tdb.TDBFactory;
import org.mapdb.BTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.*;

/**
 * Class utilizing DBnary, a SPARQL endpoint for Wiktionary.
 * Alternatively, TDB1 can be used as offline storage.
 */
public class WiktionaryKnowledgeSource extends SemanticWordRelationDictionary {


    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WiktionaryKnowledgeSource.class);

    /**
     * Service responsible for disk buffers.
     */
    private PersistenceService persistenceService;

    /**
     * Buffer for synonyms.
     */
    private ConcurrentMap<String, HashSet<String>> synonymyBuffer;

    /**
     * Buffer for hypernymy.
     */
    private ConcurrentMap<String, HashSet<String>> hypernymyBuffer;

    /**
     * Buffer for ask queries.
     */
    private ConcurrentMap<String, Boolean> askBuffer;

    private ConcurrentMap<String, HashSet<String>> translationBuffer;

    private ConcurrentMap<String, HashSet<String>> translationOfBuffer;

    /**
     * The TDB dataset into which the dbnary data set was loaded.
     */
    private Dataset tdbDataset;

    /**
     * The public SPARQL endpoint.
     */
    private static final String ENDPOINT_URL = "http://kaiko.getalp.org/sparql";

    /**
     * True if a tdb source shall be used rather than an on-line SPARQL endpoint.
     */
    private boolean isUseTdb = false;

    /**
     * True if buffers shall be written to disk.
     */
    private boolean isDiskBufferEnabled;

    /**
     * The linker that links input strings to terms.
     */
    private WiktionaryLinker linker;

    /**
     * Constructor for Wiktionary online (SPARQL endpoint) access.
     * By default, a disk-buffer is enabled.
     */
    public WiktionaryKnowledgeSource(){
        this(true);
    }

    /**
     * Constructor
     * @param isDiskBufferEnabled True if buffers shall be written to disk.
     */
    public WiktionaryKnowledgeSource(boolean isDiskBufferEnabled){
        isUseTdb = false;
        this.isDiskBufferEnabled = isDiskBufferEnabled;
        initialize();
    }

    /**
     * Constructor for DBnary TDB access.
     *
     * @param tdbDirectoryPath Path to the Wiktionary <a href="https://jena.apache.org/documentation/tdb/index.html">TDB</a>
     *                         directory.
     */
    public WiktionaryKnowledgeSource(String tdbDirectoryPath) {
        // convenience checks for stable code
        if(tdbDirectoryPath == null){
            LOGGER.error("The provided tdbDirectoryPath for Wiktionary is null. - ABORTING PROGRAM");
            return;
        }
        File tdbDirectoryFile = new File(tdbDirectoryPath);
        if (!tdbDirectoryFile.exists()) {
            LOGGER.error("tdbDirectoryPath does not exist. - ABORTING PROGRAM");
            return;
        }
        if (!tdbDirectoryFile.isDirectory()) {
            LOGGER.error("tdbDirectoryPath is not a directory. - ABORTING PROGRAM");
            return;
        }
        this.isUseTdb = true;

        // dataset and model creation
        //tdbDataset = TDB2Factory.connectDataset(tdbDirectoryPath);
        tdbDataset = TDBFactory.createDataset(tdbDirectoryPath);
        tdbDataset.begin(ReadWrite.READ);
        this.isDiskBufferEnabled = true;
        initialize();
    }

    /**
     * Helper functions for constructor-independent actions.
     */
    private void initialize(){
        if(isDiskBufferEnabled){
            this.persistenceService = PersistenceService.getService();
            this.synonymyBuffer = persistenceService.getMapDatabase(WIKTIONARY_SYNONYMY_BUFFER);
            this.hypernymyBuffer = persistenceService.getMapDatabase(WIKTIONARY_HYPERNYMY_BUFFER);
            this.askBuffer = persistenceService.getMapDatabase(WIKTIONARY_ASK_BUFFER);
            this.translationBuffer = persistenceService.getMapDatabase(WIKTIONARY_TRANSLATION_BUFFER);
            this.translationOfBuffer = persistenceService.getMapDatabase(WIKTIONARY_TRANSLATION_OF_BUFFER);
        } else {
            this.synonymyBuffer = new ConcurrentHashMap<>();
            this.hypernymyBuffer = new ConcurrentHashMap<>();
            this.askBuffer = new ConcurrentHashMap<>();
            this.translationBuffer = new ConcurrentHashMap<>();
            this.translationOfBuffer = new ConcurrentHashMap<>();
        }
        linker = new WiktionaryLinker(this);
    }

    /**
     * De-constructor; call before ending the program.
     */
    public void close() {
        commitAll();
        if(tdbDataset != null) {
            tdbDataset.end();
            tdbDataset.close();
        }
        LOGGER.info("DBnary TDB dataset closed.");
    }

    public boolean isInDictionary(String word) {
        return this.isInDictionary(word, Language.ENGLISH);
    }

    /**
     * Language dependent query for existence in the dbnary dictionary.
     * Note that case-sensitivity applies ( (Katze, deu) can be found whereas (katze, deu) will not return any results ).
     *
     * @param word     The word to be looked for.
     * @param language The language of the word.
     * @return boolean indicating whether the word exists in the dictionary in the corresponding language.
     */
    public boolean isInDictionary(String word, Language language) {
        if(word == null || language == null){
            return false;
        }
        word = encodeWord(word);
        String key = "in_dict_" + word + "_" + language.toSparqlChar2();
        if(askBuffer.containsKey(key)){
            return askBuffer.get(key);
        }
        String queryString =
                "PREFIX lexvo: <http://lexvo.org/id/iso639-3/>\r\n" +
                        "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\r\n" +
                        "ASK {  <http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> ?p ?o . }";
        boolean result = false;

        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution;
            if (isUseTdb) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
            }
            result = SparqlServices.safeAsk(queryExecution);
            queryExecution.close();
        } catch (Exception e){
            // logging actual error is disabled
            LOGGER.warn("An error occurred while trying to look up: '" + word + "'. Returning false.");
        }
        askBuffer.put(key, result);
        commit(WIKTIONARY_ASK_BUFFER);
        return result;
    }

    /**
     * Checks for synonymy by determining whether link1 is contained in the set of synonymous words of link2 or
     * vice versa.
     * @param link1 Word 1
     * @param link2 Word 2
     * @return True if the given words are synonymous, else false.
     */
    public boolean isStrongFormSynonymous(String link1, String link2){
        if(link1 == null || link2 == null) {
            return false;
        }

        Set<String> synonyms1 = getSynonymsEncoded(link1);
        Set<String> synonyms2 = getSynonymsEncoded(link2);

        if(synonyms1 == null && synonyms2 == null){
            // only if both are null b/c one concept might not have synonyms but still be a synonym of the other concept
            return false;
        }
        if(synonyms1 == null) {
            synonyms1 = new HashSet<>();
        }
        if(synonyms2 == null) {
            synonyms2 = new HashSet<>();
        }

        synonyms1.add(link1);
        synonyms2.add(link2);

        // remove empty strings to avoid false positives
        synonyms1.remove("");
        synonyms2.remove("");

        if(synonyms1.contains(link2)) return true;
        return synonyms2.contains(link1);
    }

    public Set<String> getSynonymsEncoded(String linkedConcept){
        Set<String> result = getSynonymsLexical(linkedConcept);
        if(result == null){
            return null;
        }
        return result.stream().map(WiktionaryKnowledgeSource::encodeWord).collect(Collectors.toSet());
    }

    @Override
    public Set<String> getSynonymsLexical(String linkedConcept) {
        if (linkedConcept != null) {
            HashSet<String> result = getSynonyms(linkedConcept, Language.ENGLISH);
            if (result.size() == 0) {
                return null;
            } else return result;
        } else {
            return null;
        }
    }

    /**
     * Retrieves the synonyms of a particular word in a particular language.
     *
     * @param word     Word for which the synonyms shall be retrieved.
     * @param language Language of the word.
     * @return Set of synonyms.
     */
    public HashSet<String> getSynonyms(String word, Language language) {
        word = encodeWord(word);
        if (synonymyBuffer.containsKey(word + "_" + language.toWiktionaryChar3())) {
            return synonymyBuffer.get(word + "_" + language.toWiktionaryChar3());
        }
        HashSet<String> result = new HashSet<>();
        String queryString =
                "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\r\n" +
                        "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>\r\n" +
                        "select distinct ?synonym WHERE {\r\n" +
                        "\r\n" +
                        "{" +
                        // synonyms of described concepts
                        "select distinct ?synonym where {\r\n" +
                        "<http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> <http://kaiko.getalp.org/dbnary#describes> ?descriptionConcepts .\r\n" +
                        "?descriptionConcepts dbnary:synonym ?synonym .\r\n" +
                        "}" +
                        "}\r\n" +
                        "UNION\r\n" +
                        // and now synonyms of senses
                        "{\r\n" +
                        "select distinct ?synonym where {\r\n" +
                        "<http://kaiko.getalp.org/dbnary/" + language.toWiktionaryChar3() + "/" + word + "> <http://kaiko.getalp.org/dbnary#describes> ?descriptionConcepts .\r\n" +
                        "?descriptionConcepts ontolex:sense ?sense .\r\n" +
                        "?sense dbnary:synonym ?synonym .\r\n" +
                        "}\r\n" +
                        "}\r\n" +
                        "}";
        //System.out.println(queryString);
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution;
            if (isUseTdb) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
            }
            ResultSet queryResult = queryExecution.execSelect();
            while (queryResult.hasNext()) {
                result.add(getLemmaFromURI(queryResult.next().getResource("synonym").toString()));
            }
            queryExecution.close();
        } catch (Exception e){
            LOGGER.warn("Problem with query getSynonyms for word: '" + word + "'.");
        }
        synonymyBuffer.put(word + "_" + language.toWiktionaryChar3(), result);
        commit(WIKTIONARY_SYNONYMY_BUFFER);
        return result;
    }

    /**
     * Given a resource URI, this method will transform it to a lemma.
     *
     * @param uri Resource URI to be transformed.
     * @return Lemma.
     */
    private static String getLemmaFromURI(String uri) {
        return uri.substring(35, uri.length()).replace("_", " ");
    }

    /**
     * Encodes words so that they can be looked up in the wiktionary dictionary.
     *
     * @param word Word to be encoded.
     * @return encoded word
     */
    static String encodeWord(String word) {
        // we cannot use the Java default encoder due to some ideosyncratic encodings
        word = word.trim();
        word = word.replace("%", "%25");
        word = word.replace(" ", "_");
        word = word.replace(".", "%2E");
        word = word.replace("^", "%5E");
        word = word.replace("<", "%3C");
        word = word.replace(">", "%3E");
        return word;
    }

    /**
     * Obtain hypernyms for the given concept. The assumed language is English.
     *
     * @param linkedConcept The linked concept for which hypernyms shall be retrieved.
     * @return A set of hypernyms.
     */
    @Override
    public HashSet<String> getHypernyms(String linkedConcept) {
        return this.getHypernyms(linkedConcept, Language.ENGLISH);
    }

    /**
     * Obtain hypernyms for the given concept.
     *
     * @param linkedConcept The linked concept for which hypernyms shall be retrieved.
     * @param language      The desired language of the hypernyms.
     * @return A set of hypernyms.
     */
    public HashSet<String> getHypernyms(String linkedConcept, Language language) {
        HashSet<String> result = new HashSet<>();
        if (linkedConcept == null) return result;
        linkedConcept = encodeWord(linkedConcept);
        String key = linkedConcept + "_" + language.toSparqlChar2();
        if (hypernymyBuffer.containsKey(key)) {
            return hypernymyBuffer.get(key);
        }
        String queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX dbnarylan: <http://kaiko.getalp.org/dbnary/eng/>\n" +
                "SELECT distinct ?hypernym {\n" +
                "{select ?hypernym where {\n" +
                "dbnarylan:" + linkedConcept + " dbnary:hypernym ?hypernym.\n" +
                "?hypernym rdf:type dbnary:Page .}}\n" +
                "UNION\n" +
                "{select ?hypernym where {\n" +
                "?hs dbnary:hyponym dbnarylan:" + linkedConcept + " .\n" +
                "?hypernym dbnary:describes ?hs .\n" +
                "?hypernym rdf:type dbnary:Page .}}\n" +
                "UNION\n" +
                "{select ?hypernym where {\n" +
                "dbnarylan:" + linkedConcept + " dbnary:describes ?dc .\n" +
                "?dc dbnary:hypernym ?hypernym.\n" +
                "?hypernym rdf:type dbnary:Page .\n" +
                "}}}";
        try {
            Query query = QueryFactory.create(queryString);

            QueryExecution queryExecution;
            if(isUseTdb) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
            }
            ResultSet queryResult = queryExecution.execSelect();
            while (queryResult.hasNext()) {
                result.add(getLemmaFromURI(queryResult.next().getResource("hypernym").toString()));
            }
            queryExecution.close();
        } catch (QueryParseException qpe) {
            LOGGER.warn("Failed to build getHypernyms query for concept '" + linkedConcept + "'");
        }
        hypernymyBuffer.put(key, result);
        commit(WIKTIONARY_HYPERNYMY_BUFFER);
        return result;
    }

    /**
     * Obtain the translations for the linked concept.
     * @param linkedConcept The concept that was linked.
     * @param sourceLanguage Language of the linked concept.
     * @param targetLanguage Language to which the concept shall be translated.
     * @return The result is not a linked concept but instead a word.
     */
    public HashSet<String> getTranslation(String linkedConcept, Language sourceLanguage, Language targetLanguage) {
        String key = linkedConcept + "_" + sourceLanguage + "_" + targetLanguage;
        if(translationBuffer.containsKey(key)){
            return translationBuffer.get(key);
        }
        HashSet<String> result = new HashSet<>();
        String queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                "select distinct ?tp where\n" +
                "{\n" +
                "<http://kaiko.getalp.org/dbnary/" + sourceLanguage.toWiktionaryChar3() + "/" + linkedConcept + "> dbnary:describes ?le .\n" +
                "?t dbnary:isTranslationOf ?le .\n" +
                "?t dbnary:targetLanguage <http://lexvo.org/id/iso639-3/" + targetLanguage.toWiktionaryChar3() + "> .\n" +
                "?t dbnary:writtenForm ?tp .\n" +
                "}";
        try {
            //System.out.println(queryString);
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution;
            if(isUseTdb) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, queryString);
            }
            ResultSet queryResult = queryExecution.execSelect();
            while (queryResult.hasNext()) {
                result.add(queryResult.next().getLiteral("tp").getString());
            }

            queryExecution.close();
            translationBuffer.put(key, result);
            return result;
        } catch (Exception e){
            LOGGER.error("Could not execute getTranslation query for concept " + linkedConcept + " (" + sourceLanguage + " to " + targetLanguage + ")");
            LOGGER.error("Problematic Query:\n" + queryString);
            translationBuffer.put(key, new HashSet<>());
            return null;
        }
    }

    /**
     * Given a translation, find concepts which state that the given translation is their translation.
     * @param translation The translation.
     * @return A set of concepts of which {@code translation} is the given translation.
     */
    public HashSet<String> getTranslationOf(String translation, Language languageOfTranslation){

        // buffer lookup
        String key = translation + "_" + languageOfTranslation;
        if(translationOfBuffer.containsKey(key)){
            return translationOfBuffer.get(key);
        }

        HashSet<String> result = new HashSet<>();
        String queryString = "";
        if(languageOfTranslation != Language.CHINESE) {
            queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                    "select distinct ?c where\n" +
                    "{\n" +
                    "?c dbnary:describes ?le .\n" +
                    "?t dbnary:isTranslationOf ?le .\n" +
                    "?t dbnary:targetLanguage <http://lexvo.org/id/iso639-3/" + languageOfTranslation.toWiktionaryChar3() + "> .\n" +
                    "?t dbnary:writtenForm \"" + translation + "\"@" + languageOfTranslation.toWiktionaryLanguageTag() + " .\n" +
                    "}";
        } else {
            // special case: Chinese
            queryString = "PREFIX dbnary: <http://kaiko.getalp.org/dbnary#>\n" +
                    "select distinct ?c where\n" +
                    "{\n" +
                    "{\n" +
                    "select distinct ?c where\n" +
                    "{\n" +
                    "?c dbnary:describes ?le .\n" +
                    "?t dbnary:isTranslationOf ?le .\n" +
                    "?t dbnary:targetLanguage <http://lexvo.org/id/iso639-3/yue> .\n" +
                    "?t dbnary:writtenForm \"" + translation + "\"@yue .\n" +
                    "}\n" +
                    "}\n" +
                    "UNION\n"+
                    "{\n" +
                    "select ?c where\n" +
                    "{\n" +
                    "?c dbnary:describes ?le .\n" +
                    "?t dbnary:isTranslationOf ?le .\n" +
                    "?t dbnary:targetLanguage <http://lexvo.org/id/iso639-3/cmn> .\n" +
                    "?t dbnary:writtenForm \"" + translation + "\"@cmn .\n" +
                    "}\n" +
                    "}\n" +
                    "}\n";
        }
        try {
            //System.out.println(queryString);
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution;
            if(isUseTdb) {
                queryExecution = QueryExecutionFactory.create(query, tdbDataset);
            } else {
                queryExecution = QueryExecutionFactory.sparqlService(ENDPOINT_URL, queryString);
            }
            ResultSet queryResult = queryExecution.execSelect();
            while (queryResult.hasNext()) {
                result.add(queryResult.next().getResource("c").getURI());
            }
            translationOfBuffer.put(key, result);
            return result;
        } catch (Exception e){
            LOGGER.error("Could not execute getTranslationOf query for concept " + translation + " (" + languageOfTranslation + ")", e);
            LOGGER.error("Problematic Query:\n" + queryString);
            translationOfBuffer.put(key, new HashSet<>());
            return null;
        }
    }

    /**
     * Checks whether the two words are translation of the same word (this mechanism uses another language as common
     * denominator).
     * @param word_1 Word 1 (does not have to be linked).
     * @param language_1 Language 1.
     * @param word_2 Word 2 (does not have to be linked).
     * @param language_2 Language 2.
     * @return True, if a translation can be derived; else false.
     */
    public boolean isTranslationDerived(String word_1, Language language_1, String word_2, Language language_2){
        if(word_1 == null || word_2 == null || language_1 == null || language_2 == null) return false;
        Set<String> concepts_1 = getTranslationOf(word_1, language_1);
        Set<String> concepts_2 = getTranslationOf(word_2, language_2);
        if(concepts_1 == null || concepts_2 == null) return false;
        if(concepts_1.size() == 0 || concepts_2.size() == 0) return false;
        int expected = concepts_1.size() + concepts_2.size();
        Set<String> merge = new HashSet<>();
        merge.addAll(concepts_1);
        merge.addAll(concepts_2);
        return !(expected == merge.size());
    }

    /**
     * Checks whether linkedConceptToBeTranslated can be translated to linkedConcept_2.
     * Note that BOTH concepts have to be linked.
     *
     * @param linkedConceptToBeTranslated Linked concept
     * @param language_1                  Language of linkedConceptToBeTranslated.
     * @param linkedConcept_2             Linked concept
     * @param language_2                  Language of linkedConcept_2.
     * @return True if translation from linkedConceptToBeTranslated to linkedConcept_2 possible, else false.
     */
    public boolean isTranslationLinked(String linkedConceptToBeTranslated, Language language_1, String linkedConcept_2, Language language_2) {

        // developer note: the only way that works offline is ENG -> ANY_LANGUAGE - else more needs to be downloaded
        /*
        if (!language_1.toWiktionaryChar3().equals("eng") && !language_2.toWiktionaryChar3().equals("eng")) {
            LOGGER.error("Currently only English translations are supported.");
            return false;
        }
        */

        HashSet<String> translations_1 = getTranslation(linkedConceptToBeTranslated, language_1, language_2);
        for (String translated : translations_1) {
            translated = normalizeForTranslations(translated);
            linkedConcept_2 = normalizeForTranslations(linkedConcept_2);
            if (translated.equals(linkedConcept_2)) return true;
        }

        // try reverse lookup
        HashSet<String> translations_2 = getTranslation(linkedConcept_2, language_2, language_1);
        for (String translated : translations_2) {
            translated = normalizeForTranslations(translated);
            linkedConceptToBeTranslated = normalizeForTranslations(linkedConceptToBeTranslated);
            if (translated.equals(linkedConceptToBeTranslated)) return true;
        }
        return false;
    }

    /**
     * Checks whether linkedConceptToBeTranslated can be translated to non linked concept 2.
     * Note that the first concept has to be linked.
     *
     * @param linkedConceptToBeTranslated The linked concept.
     * @param language_1                  Language of linkedConceptToBeTranslated.
     * @param nonlinkedConcept_2          Concept not linked (just a string).
     * @param language_2                  Language of linkedConcept_2.
     * @return True if translation from linkedConceptToBeTranslated to linkedConcept_2 possible, else false.
     */
    public boolean isTranslationNonLinked(String linkedConceptToBeTranslated, Language language_1, String nonlinkedConcept_2, Language language_2) {

        // developer note: the only way that works offline is ENG -> ANY_LANGUAGE - else more needs to be downloaded
        if (!language_1.toWiktionaryChar3().equals("eng") && !language_2.toWiktionaryChar3().equals("eng")) {
            LOGGER.error("Currently only English translations are supported.");
            return false;
        }

        HashSet<String> translations_1 = getTranslation(linkedConceptToBeTranslated, language_1, language_2);
        for (String translated : translations_1) {
            translated = normalizeForTranslations(translated);
            nonlinkedConcept_2 = normalizeForTranslations(nonlinkedConcept_2);
            if (translated.equals(nonlinkedConcept_2)) return true;
        }

        // try reverse lookup
        String linkedConcept_2 = linker.linkToSingleConcept(nonlinkedConcept_2, language_2);
        if(linkedConcept_2 != null) {
            HashSet<String> translations_2 = getTranslation(linkedConcept_2, language_2, language_1);
            for (String translated : translations_2) {
                translated = normalizeForTranslations(translated);
                linkedConceptToBeTranslated = normalizeForTranslations(linkedConceptToBeTranslated);
                if (translated.equals(linkedConceptToBeTranslated)) return true;
            }
        }
        return false;
    }

    /**
     * Looks for translations of the given string. The translations are non-aggressively normalized (lower-case etc.)
     * and returned.
     *
     * @param linkedConcept The linked concept for which translations shall be obtained.
     * @param sourceLanguage Source language.
     * @param targetLanguage Target language.
     * @return The result is not a linked concept but instead a word that was normalized.
     */
    public HashSet<String> getNormalizedTranslations(String linkedConcept, Language sourceLanguage, Language targetLanguage){
        HashSet<String> result = new HashSet<>();
        HashSet<String> nonNormalized = getTranslation(linkedConcept, sourceLanguage, targetLanguage);
        if(nonNormalized == null) return null;
        for(String s : nonNormalized){
            result.add(normalizeForTranslations(s));
        }
        return result;
    }

    public boolean isUseTdb() {
        return isUseTdb;
    }

    /**
     * Normalization Function for translations.
     *
     * @param setToBeNormalized Set whose strings shall be normalized.
     * @return HashSet with Normalized Strings.
     */
    public static HashSet<String> normalizeForTranslations(HashSet<String> setToBeNormalized) {
        HashSet<String> result = new HashSet<>();
        for (String s : setToBeNormalized) {
            result.add(normalizeForTranslations(s));
        }
        return result;
    }

    public static String normalizeForTranslations(String stringToBeNormalized) {
        String result = stringToBeNormalized.toLowerCase();
        result = result.trim();
        result = result.replace(" ", "_");
        result = result.replace("-", "_");
        return result;
    }

    /**
     * Commit persistence.
     * @param persistence The persistence that is to be commited.
     */
    private void commit(PersistenceService.PreconfiguredPersistences persistence){
        if(persistence == null || persistenceService == null){
            return;
        }
        switch (persistence){
            case WIKTIONARY_SYNONYMY_BUFFER:
                persistenceService.commit(WIKTIONARY_SYNONYMY_BUFFER);
                return;
            case WIKTIONARY_HYPERNYMY_BUFFER:
                persistenceService.commit(WIKTIONARY_HYPERNYMY_BUFFER);
                return;
            case WIKTIONARY_ASK_BUFFER:
                persistenceService.commit(WIKTIONARY_ASK_BUFFER);
        }
    }

    /**
     * Commit data changes if active.
     */
    private void commitAll(){
        if(isDiskBufferEnabled && persistenceService != null){
            persistenceService.commit(WIKTIONARY_SYNONYMY_BUFFER);
            persistenceService.commit(WIKTIONARY_HYPERNYMY_BUFFER);
            persistenceService.commit(WIKTIONARY_ASK_BUFFER);
        }
    }

    @Override
    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName() {
        return "Wiktionary";
    }
}
