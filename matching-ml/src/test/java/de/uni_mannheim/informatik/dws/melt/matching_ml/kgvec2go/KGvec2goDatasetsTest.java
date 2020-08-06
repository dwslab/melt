package de.uni_mannheim.informatik.dws.melt.matching_ml.kgvec2go;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KGvec2goDatasetsTest {

    @Test
    public void isValidString(){
        assertFalse(KGvec2goDatasets.isValidString(""));
        assertFalse(KGvec2goDatasets.isValidString(null));
        assertFalse(KGvec2goDatasets.isValidString("null"));
        assertTrue(KGvec2goDatasets.isValidString("dbpedia"));
        assertTrue(KGvec2goDatasets.isValidString("alod"));
        assertTrue(KGvec2goDatasets.isValidString("wordnet"));
        assertTrue(KGvec2goDatasets.isValidString("wiktionary"));
    }

}