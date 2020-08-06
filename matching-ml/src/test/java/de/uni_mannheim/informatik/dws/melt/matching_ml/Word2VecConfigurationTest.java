package de.uni_mannheim.informatik.dws.melt.matching_ml;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.Word2VecType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Word2VecConfigurationTest {

    @Test
    void setNumberOfThreads() {
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.CBOW);
        configuration.setNumberOfThreads(-5);

        Word2VecConfiguration configuration2 = new Word2VecConfiguration(Word2VecType.CBOW);
        configuration2.setNumberOfThreads(40);

        assertTrue(configuration.getNumberOfThreads() > 0);
        assertEquals(40, configuration2.getNumberOfThreads());
    }

    @Test
    void setNegatives() {
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);
        configuration.setNegatives(-5);

        Word2VecConfiguration configuration2 = new Word2VecConfiguration(Word2VecType.SG);
        configuration2.setNegatives(40);

        assertTrue(configuration.getNegatives() > 0);
        assertEquals(40, configuration2.getNegatives());
    }

    @Test
    void setIterations() {
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);
        configuration.setIterations(-5);

        Word2VecConfiguration configuration2 = new Word2VecConfiguration(Word2VecType.SG);
        configuration2.setIterations(40);

        assertTrue(configuration.getIterations() > 0);
        assertEquals(40, configuration2.getIterations());
    }

    @Test
    void setWindowSize() {
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);
        configuration.setWindowSize(-5);
        assertTrue(configuration.getWindowSize() > 0);

        Word2VecConfiguration configuration2 = new Word2VecConfiguration(Word2VecType.SG);
        configuration2.setWindowSize(40);

        assertTrue(configuration.getWindowSize() > 0);
        assertEquals(40, configuration2.getWindowSize());
    }

    @Test
    void setMinCount() {
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);
        configuration.setMinCount(0);
        assertEquals(Word2VecConfiguration.MIN_COUNT_DEFAULT, configuration.getMinCount());
        configuration.setMinCount(10);
        assertEquals(10, configuration.getMinCount());
    }

    @Test
    void setVectorDimension(){
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);
        configuration.setVectorDimension(-5);
        assertEquals(Word2VecConfiguration.VECTOR_DIMENSION_DEFAULT, configuration.getVectorDimension());
        configuration.setVectorDimension(411);
        assertEquals(411, configuration.getVectorDimension());
    }

    @Test
    void setSample() {
        Word2VecConfiguration configuration = new Word2VecConfiguration(Word2VecType.SG);
        configuration.setSample(-1);
        assertEquals(Word2VecConfiguration.SAMPLE_DEFAULT, configuration.getSample(), "Expected " + Word2VecConfiguration.SAMPLE_DEFAULT + " but actual: " + configuration.getSample());
        configuration.setSample(0.00000001);
        assertEquals(0.00000001, configuration.getSample(), "Expected: 0.00000001 but actual: " + configuration.getSample());
    }
}