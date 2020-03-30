package de.uni_mannheim.informatik.dws.melt.matching_ml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Word2VecConfigurationTest {

    @Test
    void setNumberOfThreads() {
        Word2VecConfiguration configuration = Word2VecConfiguration.CBOW;
        configuration.setNumberOfThreads(-5);

        Word2VecConfiguration configuration2 = Word2VecConfiguration.CBOW;
        configuration2.setNumberOfThreads(40);

        assertTrue(configuration.getNumberOfThreads() > 0);
        assertEquals(40, configuration2.getNumberOfThreads());
    }

    @Test
    void setNegatives() {
        Word2VecConfiguration configuration = Word2VecConfiguration.SG;
        configuration.setNegatives(-5);

        Word2VecConfiguration configuration2 = Word2VecConfiguration.SG;
        configuration2.setNegatives(40);

        assertTrue(configuration.getNegatives() > 0);
        assertEquals(40, configuration2.getNegatives());
    }

    @Test
    void setIterations() {
        Word2VecConfiguration configuration = Word2VecConfiguration.SG;
        configuration.setIterations(-5);

        Word2VecConfiguration configuration2 = Word2VecConfiguration.SG;
        configuration2.setIterations(40);

        assertTrue(configuration.getIterations() > 0);
        assertEquals(40, configuration2.getIterations());
    }

    @Test
    void setWindowSize() {
        Word2VecConfiguration configuration = Word2VecConfiguration.SG;
        configuration.setWindowSize(-5);
        assertTrue(configuration.getWindowSize() > 0);

        Word2VecConfiguration configuration2 = Word2VecConfiguration.SG;
        configuration2.setWindowSize(40);

        assertTrue(configuration.getWindowSize() > 0);
        assertEquals(40, configuration2.getWindowSize());
    }

    @Test
    void setMinCount() {
        Word2VecConfiguration configuration = Word2VecConfiguration.SG;
        configuration.setMinCount(0);
        assertTrue(configuration.getMinCount() > 0);
        configuration.setMinCount(10);
        assertEquals(10, configuration.getMinCount());
    }
}