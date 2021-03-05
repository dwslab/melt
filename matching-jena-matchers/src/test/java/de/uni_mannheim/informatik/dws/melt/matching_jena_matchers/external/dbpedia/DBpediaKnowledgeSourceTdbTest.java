package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations.deletePersistenceDirectory;
import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations.getKeyFromConfigFiles;
import static org.junit.jupiter.api.Assertions.*;

public class DBpediaKnowledgeSourceTdbTest {


    private static DBpediaKnowledgeSource dbpedia;
    private static DBpediaLinker linker;

    @BeforeAll
    public static void prepare() {
        deletePersistenceDirectory();
        String tdbpath = getKeyFromConfigFiles("dbpediaTdbDirectory");
        if(tdbpath == null){
            fail("wiktionaryTdbDirectory not found in local_config.properties file.");
        }
        dbpedia = new DBpediaKnowledgeSource(tdbpath);
        linker = new DBpediaLinker(dbpedia);
    }

    @AfterAll
    public static void destruct() {
        dbpedia.close();
        deletePersistenceDirectory();
    }

    @Test
    void getName(){
        assertNotNull(dbpedia.getName());
    }

    @Test
    void getSynonymsLexical(){
        LabelToConceptLinker linker = dbpedia.getLinker();
        Set<String> result = dbpedia.getSynonymsLexical(linker.linkToSingleConcept("SAP"));
        assertTrue(result.contains("SAP SE"));
    }

    @Test
    void getHypernyms(){
        dbpedia.setExcludedHypernyms(new HashSet<>());
        Set<String> result = dbpedia.getHypernyms(linker.linkToSingleConcept("SAP"));
        assertTrue(result.contains("http://dbpedia.org/ontology/Organisation"));
        assertTrue(result.contains("http://www.w3.org/2002/07/owl#Thing"));
        dbpedia.getExcludedHypernyms().add("http://www.w3.org/2002/07/owl#Thing");
        result = dbpedia.getHypernyms(linker.linkToSingleConcept("SAP"));
        assertTrue(result.contains("http://dbpedia.org/ontology/Organisation"));
        assertFalse(result.contains("http://www.w3.org/2002/07/owl#Thing"));
    }

    @Test
    void getLinker(){
        LabelToConceptLinker linker = dbpedia.getLinker();
        assertNotNull(linker);
    }
}
