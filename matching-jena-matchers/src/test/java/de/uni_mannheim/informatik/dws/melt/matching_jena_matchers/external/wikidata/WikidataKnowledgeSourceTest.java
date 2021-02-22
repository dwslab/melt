package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.Language;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import it.uniroma1.lcl.jlt.util.Files;
import org.javatuples.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WikidataKnowledgeSourceTest {


    /**
     * Not an actual test but can be used for quick experiments.
     */
    @Test
    void synonymyPlayground(){
        String term = "option";
        System.out.println("Synonyms for '" + term + "'");
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        for(String s: wikidata.getSynonymsLexical(wikidata.getLinker().linkToSingleConcept(term))){
            System.out.println(s);
        }
    }

    @AfterAll
    @BeforeAll
    static void deleteBuffers(){
        PersistenceService.getService().closePersistenceService();
        File buffer = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if(buffer.exists() && buffer.isDirectory()) {
            Files.deleteDirectory(buffer);
        }
    }

    @Test
    void isInDictionary() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        assertTrue(wikidata.isInDictionary("derivative"));
        assertTrue(wikidata.isInDictionary("Munich"));
        assertFalse(wikidata.isInDictionary("adsfasdfasdfasdfadsfasdfadsfasd"));

        // try with language codes
        assertTrue(wikidata.isInDictionary("derivative", Language.ENGLISH));
        assertFalse(wikidata.isInDictionary("adsfasdfasdfasdfadsfasdfadsfasd", Language.ENGLISH));
        assertFalse(wikidata.isInDictionary("Privates Gymnasium Sank Paulusheim", Language.ENGLISH));
        assertTrue(wikidata.isInDictionary("St. Paulusheim", Language.GERMAN));
        assertTrue(wikidata.isInDictionary("Privatgymnasium Sankt Paulusheim", Language.GERMAN));
    }

    @Test
    void isStrongFormSynonymous(){
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        wikidata.setDiskBufferEnabled(false);
        WikidataLinker linker = new WikidataLinker();
        String link1 = linker.linkToSingleConcept("Jan Portisch");
        String link2 = linker.linkToSingleConcept("Jan Philipp Portisch");
        assertTrue(wikidata.isStrongFormSynonymous(link1, link2));
    }

    @Test
    void getSynonyms() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        wikidata.setDiskBufferEnabled(false);
        HashSet<String> result1 = wikidata.getSynonyms(wikidata.getLinker().linkToSingleConcept("financial services"), Language.ENGLISH); // Q837171
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("FS industry"));

        // re-run to trigger buffer
        result1 = wikidata.getSynonyms(wikidata.getLinker().linkToSingleConcept("financial services"), Language.ENGLISH); // Q837171
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("FS industry"));

        // try in another language
        HashSet<String> result2 = wikidata.getSynonyms(wikidata.getLinker().linkToSingleConcept("financial services"), Language.GERMAN); // Q837171
        assertTrue(result2.size() > 0);
        assertTrue(result2.contains("Finanzbranche"));
    }

    @Test
    void getHypernymsLexical() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        wikidata.setDiskBufferEnabled(false);
        HashSet<String> result1 = wikidata.getHypernymsLexical(wikidata.getLinker().linkToSingleConcept("financial services"));
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("business service")); // label of Q25351891
        assertTrue(result1.contains("business services")); // alternative label of Q25351891

        // re-test to check buffer functionality
        result1 = wikidata.getHypernymsLexical(wikidata.getLinker().linkToSingleConcept("financial services"));
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("business service")); // label of Q25351891
        assertTrue(result1.contains("business services")); // alternative label of Q25351891

        // try in another language to check multi lingual capabilities
        HashSet<String> result2 = wikidata.getHypernymsLexical(wikidata.getLinker().linkToSingleConcept("financial services"), Language.GERMAN);
        assertTrue(result2.size() > 0);
        assertTrue(result2.contains("Unternehmensdienstleistung")); // label of Q25351891
        assertTrue(result2.contains("Unternehmensbezogene Dienstleistung")); // alternative label of Q25351891
    }

    @Test
    void getHypernyms() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        wikidata.setDiskBufferEnabled(false);
        HashSet<String> result1 = wikidata.getHypernyms(wikidata.getLinker().linkToSingleConcept("financial services"));
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("http://www.wikidata.org/entity/Q268592"));

        // repeat to use buffer
        result1 = wikidata.getHypernyms(wikidata.getLinker().linkToSingleConcept("financial services"));
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("http://www.wikidata.org/entity/Q268592"));
    }

    @Test
    void buildHypernymDepthQuery(){
        // evaluates to true if executed
        String q1 = WikidataKnowledgeSource.buildHypernymDepthQuery("http://www.wikidata.org/entity/Q1048835", "http://www.wikidata.org/entity/Q458", 1);

        // evaluates to true if executed
        String q2 = WikidataKnowledgeSource.buildHypernymDepthQuery("http://www.wikidata.org/entity/Q15642541", "http://www.wikidata.org/entity/Q458", 2);

        // evaluates to true if executed
        String q3 = WikidataKnowledgeSource.buildHypernymDepthQuery("http://www.wikidata.org/entity/Q1496967", "http://www.wikidata.org/entity/Q458", 3);

        assertNotNull(q1);
        assertNotNull(q2);
        assertNotNull(q3);
        //System.out.println(q3);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isHypernym(boolean isBufferEnabled){
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        wikidata.setDiskBufferEnabled(isBufferEnabled);
        assertEquals(isBufferEnabled, wikidata.isDiskBufferEnabled());

        // check with URIs
        // Q458 -P31-> Q1048835 -P279-> Q15642541 -P279-> Q1496967
        assertTrue(wikidata.isHypernym("http://www.wikidata.org/entity/Q1048835", "http://www.wikidata.org/entity/Q458", 1));
        assertTrue(wikidata.isHypernym("http://www.wikidata.org/entity/Q15642541", "http://www.wikidata.org/entity/Q458", 2));
        assertTrue(wikidata.isHypernym("http://www.wikidata.org/entity/Q1496967", "http://www.wikidata.org/entity/Q458", 3));

        // check with links
        String linkQ1048835 = wikidata.getLinker().linkToSingleConcept("political territorial entity");
        String linkQ458 = wikidata.getLinker().linkToSingleConcept("European Union");
        String linkQ1496967 = wikidata.getLinker().linkToSingleConcept("territorial entity");
        String linkQ15642541 = wikidata.getLinker().linkToSingleConcept("human-geographic territorial entity");
        assertTrue(wikidata.isHypernym(linkQ1048835, linkQ458, 1));
        assertTrue(wikidata.isHypernym(linkQ15642541, linkQ458, 2));
        assertTrue(wikidata.isHypernym(linkQ1496967, linkQ458, 3));

        // run again to see whether buffer works
        assertTrue(wikidata.isHypernym(linkQ1496967, linkQ458, 3));

        // combinations
        assertTrue(wikidata.isHypernym("http://www.wikidata.org/entity/Q1048835", linkQ458, 1));
        assertTrue(wikidata.isHypernym(linkQ15642541, "http://www.wikidata.org/entity/Q458", 2));
        assertTrue(wikidata.isHypernym("http://www.wikidata.org/entity/Q1496967", linkQ458, 3));

        // check false
        assertFalse(wikidata.isHypernym("http://www.wikidata.org/entity/Q15642541", "http://www.wikidata.org/entity/Q458", 1));
        assertFalse(wikidata.isHypernym("http://www.wikidata.org/entity/Q1496967", "http://www.wikidata.org/entity/Q458", 2));
        assertFalse(wikidata.isHypernym("http://www.wikidata.org/entity/Q1496967", "http://www.wikidata.org/entity/Q92929240", 3));

        // check error behavior
        assertFalse(wikidata.isHypernym(null, "http://www.wikidata.org/entity/Q15642541", 1));
        assertFalse(wikidata.isHypernym("http://www.wikidata.org/entity/Q15642541", null, 1));
        assertFalse(wikidata.isHypernym(null, null, 1));
        assertFalse(wikidata.isHypernym(null, null, -1));
        assertFalse(wikidata.isHypernym("http://www.wikidata.org/entity/Q1496967", "http://www.wikidata.org/DOES_NOT_EXIST!", 3));
    }

    @Test
    void buildHypernymyQuery(){
        String query = WikidataKnowledgeSource.buildInstanceOfSublcassOfCleanQuery("http://www.wikidata.org/entity/Q837171", 3);
        assertNotNull(query);
        //System.out.println(query);
        query = WikidataKnowledgeSource.buildInstanceOfSublcassOfCleanQuery("http://www.wikidata.org/entity/Q837171", 1);
        assertNotNull(query);
        //System.out.println(query);
    }

    @Test
    void getLabelsForLink() {
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        wikidata.setDiskBufferEnabled(false);
        assertFalse(wikidata.isDiskBufferEnabled());

        HashSet<String> result1 = wikidata.getLabelsForLink(wikidata.getLinker().linkToSingleConcept("financial services"), Language.GERMAN);
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("Finanzgewerbe"));

        // re-test to check buffer functionality
        result1 = wikidata.getLabelsForLink(wikidata.getLinker().linkToSingleConcept("financial services"), Language.GERMAN);
        assertTrue(result1.size() > 0);
        assertTrue(result1.contains("Finanzgewerbe"));

        // try in another language to check multi-language feature
        HashSet<String> result2 = wikidata.getLabelsForLink(wikidata.getLinker().linkToSingleConcept("financial services"), Language.ENGLISH);
        assertTrue(result2.size() > 0);
        assertTrue(result2.contains("financial services"));
    }

    /**
     * This test requires a working internet connection.
     * If the test fails, check whether the hypernyms of the three professors named changed on Wikidata.
     */
    @Test
    void getClosestCommonHypernym() {

        // Unit test 1
        // -----------
        // Hops: 1
        WikidataKnowledgeSource wikidata = new WikidataKnowledgeSource();
        String[] clusterMemberTerms = {"Heiko Paulheim", "Christian Bizer", "Rainer Gemulla"};
        int limitOfHops = 3;

        // Step 1: Link the concepts into the knowledge source
        ArrayList<String> links = wikidata.getConceptLinks(clusterMemberTerms);

        // Step 2: Determine common hypernym
        Pair<Set<String>, Integer> closestConcepts = wikidata.getClosestCommonHypernym(links, limitOfHops);

        assertTrue(closestConcepts.getValue0().contains("http://www.wikidata.org/entity/Q5"));
        assertEquals(1, closestConcepts.getValue1());

        // Unit test 2
        // -----------
        // Hops: 2
        String[] clusterMemberTerms2 = {"dog", "aquatic mammal"};
        limitOfHops = 3;

        // Step 1: Link the concepts into the knowledge source
        links = wikidata.getConceptLinks(clusterMemberTerms2);

        // Step 2: Determine common hypernym
        closestConcepts = wikidata.getClosestCommonHypernym(links, limitOfHops);

        assertTrue(closestConcepts.getValue0().contains("http://www.wikidata.org/entity/Q729"));
        assertEquals(3, closestConcepts.getValue1());

        // Unit test 3
        // -----------
        // Making sure that it also works with links.
        links = new ArrayList<>();
        links.add("http://www.wikidata.org/entity/Q23709849");
        links.add("http://www.wikidata.org/entity/Q17744291");

        // Determine common hypernym
        closestConcepts = wikidata.getClosestCommonHypernym(links, limitOfHops);
        assertTrue(closestConcepts.getValue0().contains("http://www.wikidata.org/entity/Q5"));
        assertEquals(1, closestConcepts.getValue1());

        // Unit test 4
        // -----------
        // Negative test.
        links = new ArrayList<>();
        links.add("http://www.wikidata.org/entity/Q23709849");
        links.add("http://www.wikidata.org/entity/Q837171");
        assertNull(wikidata.getClosestCommonHypernym(links, 2));
    }

    @Test
    void determineCommonConcepts(){
        HashMap<String, HashSet<String>> map = new HashMap<>();

        HashSet<String> set1 = new HashSet<>();
        set1.add("apple");
        set1.add("fruit");
        set1.add("car");
        map.put("A", set1);

        HashSet<String> set2 = new HashSet<>();
        set2.add("mercedes");
        set2.add("benz");
        set2.add("car");
        set2.add("fruit");
        map.put("B", set2);

        HashSet<String> set3 = new HashSet<>();
        set3.add("audi");
        set3.add("a6");
        set3.add("fruit");
        set3.add("bmw");
        set3.add("7");
        set3.add("car");
        map.put("C", set3);

        Set<String> result = WikidataKnowledgeSource.determineCommonConcepts(map);
        assertEquals(2, result.size());
        assertTrue(result.contains("car"));
        assertTrue(result.contains("fruit"));
    }
}