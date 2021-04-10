package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations.deletePersistenceDirectory;
import static org.junit.jupiter.api.Assertions.*;

class DBpediaKnowledgeSourceTest {


    @BeforeAll
    @AfterAll
    static void setupAndTearDown() {
        deletePersistenceDirectory();
    }

    @Test
    void checkEndpointIsLive(){
        String sparqlQueryString = "ASK {?a ?b ?c}";
        assertTrue(SparqlServices.safeAsk(sparqlQueryString, DBpediaKnowledgeSource.getEndpointUrl()));
    }

    @Test
    void getName(){
        DBpediaKnowledgeSource dBpediaKnowledgeSource = new DBpediaKnowledgeSource();
        assertNotNull(dBpediaKnowledgeSource.getName());
    }

    @Test
    void isStrongFormSynonymous(){
        DBpediaKnowledgeSource dbpedia = new DBpediaKnowledgeSource();
        LabelToConceptLinker linker = dbpedia.getLinker();
        assertTrue(dbpedia.isStrongFormSynonymous(linker.linkToSingleConcept("SAP"),
                linker.linkToSingleConcept("SAP SE")));
        assertTrue(dbpedia.isStrongFormSynonymous(linker.linkToSingleConcept("swap"),
                linker.linkToSingleConcept("swap (finance)")));
        assertFalse(dbpedia.isStrongFormSynonymous(linker.linkToSingleConcept("SAP"),
                linker.linkToSingleConcept("car")));
        assertFalse(dbpedia.isStrongFormSynonymous(null, linker.linkToSingleConcept("car")));
    }

    @Test
    void getSynonymsLexical(){
        DBpediaKnowledgeSource dbpedia = new DBpediaKnowledgeSource();
        LabelToConceptLinker linker = dbpedia.getLinker();
        Set<String> result = dbpedia.getSynonymsLexical(linker.linkToSingleConcept("SAP"));
        assertTrue(result.contains("SAP SE"));
    }

    @Test
    void isHypernymous(){
        DBpediaKnowledgeSource dbpedia = new DBpediaKnowledgeSource();
        dbpedia.setExcludedHypernyms(new HashSet<>());
        LabelToConceptLinker linker = dbpedia.getLinker();
        assertTrue(dbpedia.isHypernymous(linker.linkToSingleConcept("SAP SE"),
                linker.linkToSingleConcept("Societas Europaea")));
        assertFalse(dbpedia.isHypernymous(linker.linkToSingleConcept("SAP SE"),
                linker.linkToSingleConcept("cat")));
    }

    @Test
    void getHypernyms(){
        DBpediaKnowledgeSource dbpedia = new DBpediaKnowledgeSource();
        dbpedia.setExcludedHypernyms(new HashSet<>());
        LabelToConceptLinker linker = dbpedia.getLinker();
        Set<String> result = dbpedia.getHypernyms(linker.linkToSingleConcept("SAP"));
        assertTrue(result.contains("http://dbpedia.org/ontology/Company"));
        assertTrue(result.contains("http://www.w3.org/2002/07/owl#Thing"));
        dbpedia.getExcludedHypernyms().add("http://www.w3.org/2002/07/owl#Thing");
        result = dbpedia.getHypernyms(linker.linkToSingleConcept("SAP"));
        assertTrue(result.contains("http://dbpedia.org/ontology/Company"));
        assertFalse(result.contains("http://www.w3.org/2002/07/owl#Thing"));
    }

    @Test
    void getSynonymsLexicalQuery(){
        DBpediaKnowledgeSource dbpedia = new DBpediaKnowledgeSource();
        LabelToConceptLinker linker = dbpedia.getLinker();
        String query = dbpedia.getSynonymsLexicalQuery(linker.linkToSingleConcept("SAP"));
        assertNotNull(query);
    }

    @Test
    void getLinker(){
        DBpediaKnowledgeSource dbpedia = new DBpediaKnowledgeSource();
        LabelToConceptLinker linker = dbpedia.getLinker();
        assertNotNull(linker);
    }

    @Test
    void getEndpointUrl() {
        assertNotNull(DBpediaKnowledgeSource.getEndpointUrl());
    }
}