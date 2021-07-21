package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import org.junit.jupiter.api.Test;


class MainTest {


    @Test
    void anatomy() {
        try {
            Main.anatomy("", "gpt2", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}