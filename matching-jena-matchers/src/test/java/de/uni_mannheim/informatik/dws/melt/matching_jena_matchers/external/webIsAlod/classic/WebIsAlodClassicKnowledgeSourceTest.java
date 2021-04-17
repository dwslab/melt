package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.classic;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.WebIsAlodSPARQLservice;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.xl.WebIsAlodXLKnowledgeSource;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test requires a working internet connection and is dependent on the version of the data set.
 * This Test tests class {@link WebIsAlodClassicKnowledgeSource}
 *
 */
class WebIsAlodClassicKnowledgeSourceTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(WebIsAlodClassicKnowledgeSourceTest.class);

    @BeforeAll
    @AfterAll
    static void setupAndTearDown() {
        deletePersistenceDirectory();
    }

    /**
     * Delete the persistence directory.
     */
    private static void deletePersistenceDirectory() {
        WebIsAlodSPARQLservice.closeAllServices();
        PersistenceService.getService().closePersistenceService();
        File result = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if (result != null && result.exists() && result.isDirectory()) {
            try {
                FileUtils.deleteDirectory(result);
            } catch (IOException e) {
                LOGGER.error("Failed to remove persistence directory.");
            }
        }
    }

    /**
     * Not an actual test but can be used for quick experiments.
     */
    @Test
    void synonymyPlayground(){
        // TODO not (yet) implemented
        /**
        WebIsAlodClassicKnowledgeSource dictionary = new WebIsAlodClassicKnowledgeSource(0.5);
        String term = "European Union";
        System.out.println("Synonyms for '" + term + "'");

        for(String s: dictionary.getSynonymsLexical(dictionary.getLinker().linkToSingleConcept(term))){
            System.out.println(s);
        }
         **/
    }

    @Test
    void isSynonymous() {

        //----------------------------------------------------------
        // Test 1:
        // rolex watch is a watch: 0.4453
        // watch is a rolex watch: 0.3723
        //----------------------------------------------------------

        // no boundary
        WebIsAlodClassicKnowledgeSource dictionary = new WebIsAlodClassicKnowledgeSource();
        WebIsAlodClassicLinker linker = (WebIsAlodClassicLinker) dictionary.getLinker();
        assertTrue(dictionary.isSynonymous(linker.linkToSingleConcept("watch"), linker.linkToSingleConcept("rolex watch")));

        // 0.3
        dictionary = new WebIsAlodClassicKnowledgeSource(0.3);
        assertTrue(dictionary.isSynonymous(linker.linkToSingleConcept("watch"), linker.linkToSingleConcept("rolex watch")));

        // 0.5
        dictionary = new WebIsAlodClassicKnowledgeSource(0.5);
        assertFalse(dictionary.isSynonymous(linker.linkToSingleConcept("watch"), linker.linkToSingleConcept("rolex watch")));


        //----------------------------------------------------------
        // Test 2:
        // option contract is a contract: 0.6998
        // contract is a option contract:
        //----------------------------------------------------------

        dictionary = new WebIsAlodClassicKnowledgeSource(0.5);
        assertTrue(dictionary.isSynonymous(linker.linkToSingleConcept("option_contract"), linker.linkToSingleConcept("contract")));
        // re test for buffer
        assertTrue(dictionary.isSynonymous(linker.linkToSingleConcept("option_contract"), linker.linkToSingleConcept("contract")));


        dictionary = new WebIsAlodClassicKnowledgeSource(0.8);
        assertFalse(dictionary.isSynonymous(linker.linkToSingleConcept("option_contract"), linker.linkToSingleConcept("contract")));
        // re test for buffer
        assertFalse(dictionary.isSynonymous(linker.linkToSingleConcept("option_contract"), linker.linkToSingleConcept("contract")));


        // clean up
        dictionary.close();
    }


    @Test
    void isStrongFormSynonymous() {

        //----------------------------------------------------------
        // Test 1:
        // rolex watch is a watch: 0.4453
        // watch is a rolex watch: 0.3723
        //----------------------------------------------------------

        // no boundary
        WebIsAlodClassicKnowledgeSource dictionary = new WebIsAlodClassicKnowledgeSource();
        WebIsAlodClassicLinker linker = (WebIsAlodClassicLinker) dictionary.getLinker();
        String watch = linker.linkToSingleConcept("watch");
        String rolexWatch = linker.linkToSingleConcept("rolex watch");

        assertTrue(dictionary.isStrongFormSynonymous(watch, rolexWatch));

        // 0.3
        dictionary = new WebIsAlodClassicKnowledgeSource(0.3);
        assertTrue(dictionary.isStrongFormSynonymous(watch, rolexWatch));

        // 0.5
        dictionary = new WebIsAlodClassicKnowledgeSource(0.5);
        assertFalse(dictionary.isStrongFormSynonymous(watch, rolexWatch));


        //----------------------------------------------------------
        // Test 2:
        // option contract is a contract: 0.6998
        // contract is a option contract:
        //----------------------------------------------------------

        String optionContract = linker.linkToSingleConcept("option_contract");
        String contract = linker.linkToSingleConcept("contract");

        dictionary = new WebIsAlodClassicKnowledgeSource(0.5);
        assertTrue(dictionary.isStrongFormSynonymous(optionContract, contract));
        // re test for buffer
        assertTrue(dictionary.isStrongFormSynonymous(optionContract, contract));


        dictionary = new WebIsAlodClassicKnowledgeSource(0.8);
        assertFalse(dictionary.isStrongFormSynonymous(optionContract, contract));
        // re test for buffer
        assertFalse(dictionary.isStrongFormSynonymous(optionContract, contract));

        // clean up
        dictionary.close();
    }

    @Test
    void isHypernym(){
        WebIsAlodClassicKnowledgeSource dictionary = new WebIsAlodClassicKnowledgeSource();
        WebIsAlodClassicLinker linker = (WebIsAlodClassicLinker) dictionary.getLinker();
        String watch = linker.linkToSingleConcept("watch");
        String rolexWatch = linker.linkToSingleConcept("rolex watch");
        assertTrue(dictionary.isHypernym(watch, rolexWatch));

        // again just to test the buffer
        assertTrue(dictionary.isHypernym(watch, rolexWatch));
    }

    @Test
    void isHypernymous(){
        //-------------------------------------------
        // Test 1: no boundary
        //-------------------------------------------
        WebIsAlodClassicKnowledgeSource dictionary = new WebIsAlodClassicKnowledgeSource();
        WebIsAlodClassicLinker linker = (WebIsAlodClassicLinker) dictionary.getLinker();
        String watch = linker.linkToSingleConcept("watch");
        String rolexWatch = linker.linkToSingleConcept("rolex watch");

        assertTrue(dictionary.isHypernymous(watch, rolexWatch));
        assertTrue(dictionary.isHypernymous(rolexWatch, watch));

        // for buffer
        assertTrue(dictionary.isHypernymous(watch, rolexWatch));
        assertTrue(dictionary.isHypernymous(rolexWatch, watch));


        //-------------------------------------------
        // Test 2: with boundary
        //-------------------------------------------
        dictionary = new WebIsAlodClassicKnowledgeSource(0.3);
        assertTrue(dictionary.isHypernymous(watch, rolexWatch));

        // for buffer
        assertTrue(dictionary.isHypernymous(watch, rolexWatch));

        dictionary = new WebIsAlodClassicKnowledgeSource(0.4);
        assertFalse(dictionary.isHypernymous(watch, rolexWatch));

        // for buffer
        assertFalse(dictionary.isHypernymous(watch, rolexWatch));
    }

    @Test
    void setGetMinimumConfidence(){
        WebIsAlodClassicKnowledgeSource source = new WebIsAlodClassicKnowledgeSource();

        // ok case
        source.setMinimumConfidence(0.5);
        assertEquals(0.5, source.getMinimumConfidence());

        // error case
        source.setMinimumConfidence(1.5);
        assertNotEquals(1.5, source.getMinimumConfidence());
    }

    @Test
    void isSynonymousOrHypernymyous(){

        //-------------------------------------------
        // Test 1: no boundary
        //-------------------------------------------
        WebIsAlodClassicKnowledgeSource dictionary = new WebIsAlodClassicKnowledgeSource();
        WebIsAlodClassicLinker linker = (WebIsAlodClassicLinker) dictionary.getLinker();
        String watch = linker.linkToSingleConcept("watch");
        String rolexWatch = linker.linkToSingleConcept("rolex watch");

        assertTrue(dictionary.isSynonymousOrHypernymous(watch, rolexWatch));
        assertTrue(dictionary.isSynonymousOrHypernymous(rolexWatch, watch));

        // for buffer
        assertTrue(dictionary.isSynonymousOrHypernymous(watch, rolexWatch));
        assertTrue(dictionary.isSynonymousOrHypernymous(rolexWatch, watch));


        //-------------------------------------------
        // Test 2: with boundary
        //-------------------------------------------
        dictionary = new WebIsAlodClassicKnowledgeSource(0.3);
        assertTrue(dictionary.isSynonymousOrHypernymous(watch, rolexWatch));

        // for buffer
        assertTrue(dictionary.isSynonymousOrHypernymous(watch, rolexWatch));

        dictionary = new WebIsAlodClassicKnowledgeSource(0.4);
        assertFalse(dictionary.isSynonymousOrHypernymous(watch, rolexWatch));

        // for buffer
        assertFalse(dictionary.isSynonymousOrHypernymous(watch, rolexWatch));
    }

    @Test
    void getLinker(){
        WebIsAlodClassicKnowledgeSource classic = new WebIsAlodClassicKnowledgeSource();
        assertNotNull(classic.getLinker());
    }

}