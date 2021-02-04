package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BabelNetEmbeddingLinkerTest {


    @Test
    void normalize() {
        assertEquals("Petit_Bersac", BabelNetEmbeddingLinker.normalizeStatic("bn:Petit-Bersac_n_EN"));
        assertEquals("Europe", BabelNetEmbeddingLinker.normalizeStatic("Europe"));
    }
}