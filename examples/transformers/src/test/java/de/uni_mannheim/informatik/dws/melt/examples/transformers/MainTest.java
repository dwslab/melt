package de.uni_mannheim.informatik.dws.melt.examples.transformers;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MainTest {


    /**
     * Simple unit test for the parameters.
     */
    @Test
    void testTracks() {
        try {
            Main.main(new String[]{"-tracks", "anatomy", "conference"});
            assertEquals(2, Main.tracks.size());
        } catch (Exception e){
            Assertions.fail(e);
        }
    }

    /**
     * Simple unit test for the parameters.
     */
    @Test
    void testTransformerModels() {
        try {
            Main.main(new String[]{"--transformermodels", "A", "B"});
            assertEquals(2, Main.transformerModels.length);
        } catch (Exception e){
            Assertions.fail(e);
        }
    }

}