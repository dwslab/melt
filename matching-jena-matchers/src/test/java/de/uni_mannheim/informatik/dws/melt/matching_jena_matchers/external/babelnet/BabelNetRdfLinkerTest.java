package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BabelNetRdfLinkerTest {


    private static BabelNetRdfLinker linker;

    @BeforeAll
    static void prepare(){
        String tdbPath = TestOperations.getKeyFromConfigFiles("babelnetTdbDirectory");
        if(tdbPath == null){
            fail("babelnetTdbDirectory not found in local_config.properties file.");
        }
        linker = new BabelNetRdfLinker(tdbPath);
    }

    @Test
    void linkToSingleConcept() {
        String catLink = linker.linkToSingleConcept("cat");
        assertNotNull(catLink);
        catLink = linker.linkToSingleConcept("CAT");
        assertNotNull(catLink);
        catLink = linker.linkToSingleConcept("Cat");
        assertNotNull(catLink);

        String diseaseLink = linker.linkToSingleConcept("inflammatory disease");
        assertNotNull(diseaseLink);
        diseaseLink = linker.linkToSingleConcept("Inflammatory disease");
        assertNotNull(diseaseLink);

        // check special characters
        assertNotNull(linker.linkToSingleConcept("plié"));
        assertNotNull(linker.linkToSingleConcept("e-mail"));

        // check verbs
        assertNotNull(linker.linkToSingleConcept("captivate"));

        // check adjectives
        String adjLink = linker.linkToSingleConcept("toilsome");
        assertNotNull(adjLink);
        //System.out.println(adjLink);

        // check adverbs
        assertNotNull(linker.linkToSingleConcept("tardily"));

        // check negative case
        assertNull(linker.linkToSingleConcept(null));
        assertNull(linker.linkToSingleConcept("DOES NOT EXIST! XZY"));
    }

    @Test
    void encode(){
        String encoded = linker.encode("plié");
        assertNotEquals("plié", encoded);
    }

    @Test
    void getNameOfLinker() {
        assertNotNull(linker.getNameOfLinker());
    }

    @Test
    void setNameOfLinker() {
        assertNotNull(linker.getNameOfLinker());
        linker.setNameOfLinker("My Linker");
        assertEquals("My Linker", linker.getNameOfLinker());
    }
}