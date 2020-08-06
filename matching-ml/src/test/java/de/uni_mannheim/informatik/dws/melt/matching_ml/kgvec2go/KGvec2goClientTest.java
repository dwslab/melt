package de.uni_mannheim.informatik.dws.melt.matching_ml.kgvec2go;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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


        // ---------
        //   ALOD
        // ---------

        // request existing entity
        result = KGvec2goClient.getInstance().getVector("Germany", KGvec2goDatasets.ALOD);
        assertNotNull(result);
        result = KGvec2goClient.getInstance().getVector("germany", KGvec2goDatasets.ALOD);
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
}