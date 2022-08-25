package de.uni_mannheim.informatik.dws.melt.matching_ml.kgvec2go;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

@Disabled("Disabled due to unavailable service.")
class KGvec2goClientTest {

    @Test
    void getVector() {
        // ---------
        //  DBpedia
        // ---------

        // request existing entity
        Double[] result = KGvec2goClient.getInstance().getVector("Europe", KGvec2goDatasets.DBPEDIA);
        assertNotNull(result);

        // request again for buffer test
        result = KGvec2goClient.getInstance().getVector("Europe", KGvec2goDatasets.DBPEDIA);
        assertNotNull(result);

        // request non-existing entity
        result = KGvec2goClient.getInstance().getVector("AABBCCDDEEFF", KGvec2goDatasets.DBPEDIA);
        assertNull(result);

        // request again for buffer test
        result = KGvec2goClient.getInstance().getVector("AABBCCDDEEFF", KGvec2goDatasets.DBPEDIA);
        assertNull(result);

        // erroneous requests
        assertNull(KGvec2goClient.getInstance().getVector(null, null));
        assertNull(KGvec2goClient.getInstance().getVector("car", null));
        assertNull(KGvec2goClient.getInstance().getVector(null, KGvec2goDatasets.DBPEDIA));
        assertNull(KGvec2goClient.getInstance().getVector("", KGvec2goDatasets.DBPEDIA));
        assertNull(KGvec2goClient.getInstance().getVector(" ", KGvec2goDatasets.DBPEDIA));


        // ---------
        //   ALOD
        // ---------

        // request existing entity
        result = KGvec2goClient.getInstance().getVector("Germany", KGvec2goDatasets.ALOD);
        assertNotNull(result);

        result = KGvec2goClient.getInstance().getVector("germany", KGvec2goDatasets.ALOD);
        assertNotNull(result);

        result = KGvec2goClient.getInstance().getVector("european union", KGvec2goDatasets.ALOD);
        assertNotNull(result);

        // request non-existing entity
        result = KGvec2goClient.getInstance().getVector("AABBCCDDEEFF", KGvec2goDatasets.ALOD);
        assertNull(result);

        // request again for buffer test
        result = KGvec2goClient.getInstance().getVector("AABBCCDDEEFF", KGvec2goDatasets.ALOD);
        assertNull(result);


        // -----------
        //   WordNet
        // -----------

        // request existing entity
        result = KGvec2goClient.getInstance().getVector("Europe", KGvec2goDatasets.WORDNET);
        assertNotNull(result);

        // request again for buffer test
        result = KGvec2goClient.getInstance().getVector("Europe", KGvec2goDatasets.WORDNET);
        assertNotNull(result);

        // request non-existing entity
        result = KGvec2goClient.getInstance().getVector("AABBCCDDEEFF", KGvec2goDatasets.WORDNET);
        assertNull(result);

        // request again for buffer test
        result = KGvec2goClient.getInstance().getVector("AABBCCDDEEFF", KGvec2goDatasets.WORDNET);
        assertNull(result);


        // -------------
        //   Wiktionary
        // -------------

        // request existing entity
        result = KGvec2goClient.getInstance().getVector("Europe", KGvec2goDatasets.WIKTIONARY);
        assertNotNull(result);

        // request again for buffer test
        result = KGvec2goClient.getInstance().getVector("Europe", KGvec2goDatasets.WIKTIONARY);
        assertNotNull(result);

        // request non-existing entity
        result = KGvec2goClient.getInstance().getVector("AABBCCDDEEFF", KGvec2goDatasets.WIKTIONARY);
        assertNull(result);

        // request again for buffer test
        result = KGvec2goClient.getInstance().getVector("AABBCCDDEEFF", KGvec2goDatasets.WIKTIONARY);
        assertNull(result);
    }


    @Test
    void getSimilarity(){
        KGvec2goClient kgvec2go = KGvec2goClient.getInstance();

        // ---------
        //   ALOD
        // ---------

        assertTrue(kgvec2go.getSimilarity("germany", "europe", KGvec2goDatasets.ALOD) > kgvec2go.getSimilarity("germany", "japan", KGvec2goDatasets.ALOD));
        assertNull(kgvec2go.getSimilarity("usa", null, KGvec2goDatasets.ALOD));
        assertNull(kgvec2go.getSimilarity(null, "usa", KGvec2goDatasets.ALOD));
        assertNull(kgvec2go.getSimilarity("AAABBBCCC", "usa", KGvec2goDatasets.ALOD));

        // ----------
        //   DBpedia
        // ----------

        assertTrue(kgvec2go.getSimilarity("Germany", "Europe", KGvec2goDatasets.DBPEDIA) > kgvec2go.getSimilarity("Europe", "Japan", KGvec2goDatasets.DBPEDIA));
        assertNull(kgvec2go.getSimilarity("USA", null, KGvec2goDatasets.ALOD));
        assertNull(kgvec2go.getSimilarity(null, "USA", KGvec2goDatasets.ALOD));
        assertNull(kgvec2go.getSimilarity("AAABBBCCC", "USA", KGvec2goDatasets.ALOD));

        // ----------
        //   WordNet
        // ----------

        assertTrue(kgvec2go.getSimilarity("Germany", "Europe", KGvec2goDatasets.WORDNET) > kgvec2go.getSimilarity("Europe", "Japan", KGvec2goDatasets.WORDNET));
        assertNull(kgvec2go.getSimilarity("USA", null, KGvec2goDatasets.WORDNET));
        assertNull(kgvec2go.getSimilarity(null, "USA", KGvec2goDatasets.WORDNET));
        assertNull(kgvec2go.getSimilarity("AAABBBCCC", "USA", KGvec2goDatasets.WORDNET));


        // --------------
        //   Wiktionary
        // --------------

        assertTrue(kgvec2go.getSimilarity("Germany", "Europe", KGvec2goDatasets.WIKTIONARY) > kgvec2go.getSimilarity("Europe", "war", KGvec2goDatasets.WIKTIONARY));
        assertNull(kgvec2go.getSimilarity("USA", null, KGvec2goDatasets.WIKTIONARY));
        assertNull(kgvec2go.getSimilarity(null, "USA", KGvec2goDatasets.WIKTIONARY));
        assertNull(kgvec2go.getSimilarity("AAABBBCCC", "USA", KGvec2goDatasets.WIKTIONARY));
    }

    @Test
    void isServiceAvailable(){
        assertTrue(KGvec2goClient.getInstance().isServiceAvailable());
    }

    @Test
    void cosineSimilarity(){
        Double[] v1 = {3d, 8d, 7d, 5d, 2d, 9d};
        Double[] v2 = {10d, 8d, 6d, 6d, 4d, 5d};
        Double[] v3 = {10d, 8d};

        assertEquals(0.8639, KGvec2goClient.cosineSimilarity(v1, v2), 0.00001);
        assertThrows(ArithmeticException.class, () -> KGvec2goClient.cosineSimilarity(v1, v3));
    }

    @Test
    void getEncodedURIString(){
        assertEquals("http://kgvec2go.org/rest/get-vector/ALOD/hello%20world", KGvec2goClient.getEncodedURIString("/rest/get-vector/" + KGvec2goDatasets.ALOD.toString() + "/" + "hello world"));
    }

    @Test
    void getEncodedURI(){
        try {
            assertEquals(new URI("http://kgvec2go.org/rest/get-vector/ALOD/hello%20world"), KGvec2goClient.getEncodedURI("/rest/get-vector/" + KGvec2goDatasets.ALOD.toString() + "/" + "hello world"));
        } catch (URISyntaxException use){
            fail(use);
        }
    }

    @Test
    void shutDown(){
        KGvec2goClient.getInstance().shutDown();
    }

}