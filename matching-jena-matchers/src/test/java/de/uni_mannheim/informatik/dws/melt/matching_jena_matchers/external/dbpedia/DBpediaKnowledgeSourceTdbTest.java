package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.BackgroundMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.ImplementedBackgroundMatchingStrategies;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.MatcherConcurrencyTesting;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.javatuples.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
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
    static void prepare() {
        deletePersistenceDirectory();
        String tdbpath = getKeyFromConfigFiles("dbpediaTdbDirectory");
        if (tdbpath == null) {
            fail("wiktionaryTdbDirectory not found in local_config.properties file.");
        }
        dbpedia = new DBpediaKnowledgeSource(tdbpath);
        linker = new DBpediaLinker(dbpedia);
    }

    @AfterAll
    static void destruct() {
        dbpedia.close();
        deletePersistenceDirectory();
    }

    /**
     * Not an actual test but can be used for quick experiments.
     */
    @Test
    void synonymyPlayground(){
        String term = "European Union";
        System.out.println("Synonyms for '" + term + "'");
        for(String s: dbpedia.getSynonymsLexical(dbpedia.getLinker().linkToSingleConcept(term))){
            System.out.println(s);
        }
    }

    @Test
    void concurrencyConstructor() {
        try {
            String tdbPath = getKeyFromConfigFiles("dbpediaTdbDirectory");
            if (tdbPath == null) {
                fail("wiktionaryTdbDirectory not found in local_config.properties file.");
            }
            DBpediaKnowledgeSource dbpedia2 = new DBpediaKnowledgeSource(tdbPath);
            assertNotNull(dbpedia2);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    @Disabled
    /**
     * In an ideal world, this test would work.
     * TODO: There seems to be a deadlock issue here, so TDB matchers should not be used concurrently.
     */
    void concurrentMatching() {
        String tdbpath = getKeyFromConfigFiles("dbpediaTdbDirectory");
        if (tdbpath == null) {
            fail("wiktionaryTdbDirectory not found in local_config.properties file.");
        }
        DBpediaKnowledgeSource dbpedia1 = new DBpediaKnowledgeSource(tdbpath);
        DBpediaKnowledgeSource dbpedia2 = new DBpediaKnowledgeSource(tdbpath);

        BackgroundMatcher backgroundMatcher1 = new BackgroundMatcher(dbpedia1,
                ImplementedBackgroundMatchingStrategies.SYNONYMY);
        BackgroundMatcher backgroundMatcher2 = new BackgroundMatcher(dbpedia2,
                ImplementedBackgroundMatchingStrategies.SYNONYMY);
        Pair<Alignment, Alignment> alignments = MatcherConcurrencyTesting.concurrencyMatching(backgroundMatcher1,
                backgroundMatcher2);
        assertEquals(alignments.getValue0(), alignments.getValue1());
    }

    @Test
    void getName() {
        assertNotNull(dbpedia.getName());
    }

    @Test
    void getSynonymsLexical() {
        LabelToConceptLinker linker = dbpedia.getLinker();
        Set<String> result = dbpedia.getSynonymsLexical(linker.linkToSingleConcept("SAP"));
        assertTrue(result.contains("SAP SE"));
    }

    @Test
    void isHypernymous(){
        dbpedia.setExcludedHypernyms(new HashSet<>());
        LabelToConceptLinker linker = dbpedia.getLinker();
        assertTrue(dbpedia.isHypernymous(linker.linkToSingleConcept("SAP SE"),
                linker.linkToSingleConcept("organisation")));
        assertFalse(dbpedia.isHypernymous(linker.linkToSingleConcept("SAP SE"),
                linker.linkToSingleConcept("cat")));
    }

    @Test
    void isStrongFormSynonymous(){
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
    void getHypernyms() {
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
    void getLinker() {
        LabelToConceptLinker linker = dbpedia.getLinker();
        assertNotNull(linker);
    }
}
