package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;

public class WikidataKnowledgeSource extends SemanticWordRelationDictionary {

    /**
     * Buffer for repeated synonymy requests.
     */
    HashMap<String, HashSet<String>> synonymyBuffer = new HashMap<>();

    /**
     * Buffer for repeated hypernymy requests.
     */
    HashMap<String, HashSet<String>> hypernymyBuffer = new HashMap<>();

    /**
     * Linker
     */
    private WikidataLinker linker = new WikidataLinker();

    /**
     * The public SPARQL endpoint.
     */
    private static final String endpointUrl = "https://query.wikidata.org/bigdata/namespace/wdq/sparql/";

    /**
     * Default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WikidataKnowledgeSource.class);

    /**
     * Name of the instance.
     */
    private String knowledgeSourceName = "WikidataKnowledgeSource";

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
     * @param word The word to be used for the concept lookup.
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
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query);
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
        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query);
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
        if(synonymyBuffer.containsKey(key)){
            return synonymyBuffer.get(key);
        }

        HashSet<String> result = new HashSet<>();
        if (linkedConcept.startsWith(WikidataLinker.multiConceptPrefix)) {
            HashSet<String> individualLinks = this.linker.getLinks(linkedConcept);
            for (String individualLink : individualLinks) {
                result.addAll(getSynonyms(individualLink, language));
            }
        } else {
            String queryString = "SELECT ?l WHERE { <" + linkedConcept + "> <http://www.w3.org/2004/02/skos/core#altLabel> ?l . FILTER(LANG(?l) = '" + language.toSparqlChar2() + "') }";
            //System.out.println(queryString);
            Query query = QueryFactory.create(queryString);
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query);
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
     * This will return the hypernyms as String.
     *
     * @param linkedConcept The linked concept for which hypernyms shall be retrieved. The linked concept is a URI.
     * @return The found hypernyms as links (URIs). If it is planned to immediately use the lexical representation
     *  use {@link WikidataKnowledgeSource#getHypernymsLexical(String, Language)}.
     */
    @Override
    public HashSet<String> getHypernyms(String linkedConcept) {
        HashSet<String> result = new HashSet<>();
        if (linkedConcept.startsWith(WikidataLinker.multiConceptPrefix)) {
            HashSet<String> individualLinks = this.linker.getLinks(linkedConcept);
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
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query);
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



    public HashSet<String> getHypernymsLexical(String linkedConcept){
        return getHypernymsLexical(linkedConcept, Language.ENGLISH);
    }


    /**
     * Uses wdt:P31 (instance of) as well as wdt:P279 (subclass of).
     * @param linkedConcept The concept that has already been linked (URI).
     * @param language Language of the strings.
     * @return A set.
     */
    public HashSet<String> getHypernymsLexical(String linkedConcept, Language language) {
        String key = linkedConcept + "_" + language.toSparqlChar2();
        HashSet<String> result = new HashSet<>();
        if (linkedConcept.startsWith(this.linker.multiConceptPrefix)) {
            HashSet<String> individualLinks = this.linker.getLinks(linkedConcept);
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
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query);
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
     * @param linkedConcept The URI for which labels shall be obtained.
     * @return A set of labels.
     */
    public HashSet<String> getLabelsForLink(String linkedConcept){
        return getLabelsForLink(linkedConcept, Language.ENGLISH);
    }

    /**
     * Given a linked concept, retrieve all labels (rdfs:label, skos:altLabel).
     * @param linkedConcept The link to the concept (URI).
     * @param language Desired language for the labels.
     * @return Set of labels, all in the specified language.
     */
    public HashSet<String> getLabelsForLink(String linkedConcept, Language language){
        HashSet<String> result = new HashSet<>();
        if (linkedConcept.startsWith(this.linker.multiConceptPrefix)) {
            HashSet<String> individualLinks = this.linker.getLinks(linkedConcept);
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
            QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpointUrl, query);
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
}
