package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.WebIsAlodEmbeddingLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wiktionary.WiktionaryEmbeddingLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetEmbeddingLinker;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class LabelToConceptLinkerEmbeddingsTest {

    @Test
    void linkToSingleConcept() {
        String pathToWiktionaryEntityFile = loadFile("dbnary_embedding_entities.txt").getAbsolutePath();
        WiktionaryEmbeddingLinker wiktionaryLinker = new WiktionaryEmbeddingLinker(pathToWiktionaryEntityFile);

        String pathToWordnetEntityFile = loadFile("wordnet_embedding_entities.txt").getAbsolutePath();
        WordNetEmbeddingLinker wordnetLinker = new WordNetEmbeddingLinker(pathToWordnetEntityFile);

        String pathToAlodEntityFile = loadFile("alodc_embedding_entities.txt").getAbsolutePath();
        WebIsAlodEmbeddingLinker webIsAlodLinker =  new WebIsAlodEmbeddingLinker(pathToAlodEntityFile);

        // test 1: Europe
        String term = "Europe";
        String wiktionary1_link = wiktionaryLinker.linkToSingleConcept(term);
        String wordnet1_link = wordnetLinker.linkToSingleConcept(term);
        String webIsAlod1_link = webIsAlodLinker.linkToSingleConcept(term);
        assertNotNull(wiktionary1_link);
        assertNotNull(wordnet1_link);
        assertNotNull(webIsAlod1_link);
        System.out.println("Wiktionary (" + term + "): " + wiktionary1_link);
        System.out.println("Wordnet (" + term + "): " + wordnet1_link);
        System.out.println("WebIsALOD (" + term + "): " + webIsAlod1_link);

        // test 2: europe
        term = "europe";
        String wiktionary2_link = wiktionaryLinker.linkToSingleConcept(term);
        String wordnet2_link = wordnetLinker.linkToSingleConcept(term);
        String webIsAlod2_link = webIsAlodLinker.linkToSingleConcept(term);
        assertNotNull(wiktionary2_link);
        assertNotNull(wordnet2_link);
        assertNotNull(webIsAlod2_link);
        System.out.println("Wiktionary (" + term + "): " + wiktionary2_link);
        System.out.println("Wordnet (" + term + "): " + wordnet2_link);
        System.out.println("WebIsALOD (" + term + "): " + wiktionary2_link);

        // null test
        term = null;
        String wiktionary3_link = wiktionaryLinker.linkToSingleConcept(term);
        String wordnet3_link = wordnetLinker.linkToSingleConcept(term);
        String webIsAlod3_link = webIsAlodLinker.linkToSingleConcept(term);
        assertNull(wiktionary3_link);
        assertNull(wordnet3_link);
        assertNull(webIsAlod3_link);
    }

    @Test
    void linkToPotentiallyMultipleConcepts() {
        String pathToWiktionaryEntityFile = "/Users/janportisch/Documents/PhD/LREC_2020/Language_Models/wiktionary/dbnary_entities.txt";
        WiktionaryEmbeddingLinker wiktionaryLinker = new WiktionaryEmbeddingLinker(pathToWiktionaryEntityFile);

        String pathToWordnetEntityFile = "/Users/janportisch/Documents/PhD/LREC_2020/Language_Models/wordnet/wordnet_entities.txt";
        WordNetEmbeddingLinker wordnetLinker = new WordNetEmbeddingLinker(pathToWordnetEntityFile);

        String pathToAlodEntityFile = "/Users/janportisch/Documents/PhD/Language_Models_old/alod/alodc_entities.txt";
        WebIsAlodEmbeddingLinker webIsAlodLinker =  new WebIsAlodEmbeddingLinker(pathToAlodEntityFile);

        // test 1: Europe
        String term = "European Union car";
        HashSet<String> wiktionary1_links = wiktionaryLinker.linkToPotentiallyMultipleConcepts(term);
        HashSet<String> wordnet1_links = wordnetLinker.linkToPotentiallyMultipleConcepts(term);
        HashSet<String> webIsAlod1_links = webIsAlodLinker.linkToPotentiallyMultipleConcepts(term);
        assertNotNull(wiktionary1_links);
        assertNotNull(wordnet1_links);
        assertNotNull(webIsAlod1_links);
        System.out.println("\nWiktionary (" + term + "):");
        wiktionary1_links.stream().forEach(System.out::println);
        System.out.println("\nWordnet (" + term + "): ");
        wordnet1_links.stream().forEach(System.out::println);
        System.out.println("\nWebIsALOD (" + term + "): ");
        webIsAlod1_links.stream().forEach(System.out::println);
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            File result =  FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.");
            return null;
        }
    }
}