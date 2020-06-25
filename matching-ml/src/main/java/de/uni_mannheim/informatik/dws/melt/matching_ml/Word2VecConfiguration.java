package de.uni_mannheim.informatik.dws.melt.matching_ml;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The configuration for the word2vec calculation.
 */
public class Word2VecConfiguration {

    /**
     * Default logger.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(Word2VecConfiguration.class);

    /**
     * Model type.
     * Default type: SG.
     */
    private Word2VecType type = Word2VecType.SG;

    /**
     * Size of the vector. Default: 200.
     */
    private int vectorDimension = VECTOR_DIMENSION_DEFAULT;

    /**
     * Default value for parameter {@link Word2VecConfiguration#vectorDimension}.
     */
    public static final int VECTOR_DIMENSION_DEFAULT = 200;

    /**
     * The size of the window during the word2vec training. Default: 5.
     */
    private int windowSize = WINDOW_SIZE_DEFAULT;

    /**
     * Default value for parameter {@link Word2VecConfiguration#windowSize}.
     */
    public static final int WINDOW_SIZE_DEFAULT = 5;

    /**
     * Iterations during the word2vec training.
     */
    private int iterations = ITERATIONS_DEFAULT;

    /**
     * Default value for parameter {@link Word2VecConfiguration#iterations}.
     */
    public static final int ITERATIONS_DEFAULT = 5;

    /**
     * The number of negatives during the word2vec training. Default 5.
     */
    private int negatives = NEGATIVES_DEFAULT;

    /**
     * Default value for parameter {@link Word2VecConfiguration#negatives}.
     */
    public static final int NEGATIVES_DEFAULT = 5;

    /**
     * The minimum count for the word2vec training.
     */
    private int minCount = MIN_COUNT_DEFAULT;

    /**
     * Default for parameter {@link Word2VecConfiguration#minCount}
     */
    public static final int MIN_COUNT_DEFAULT = 1;

    /**
     * The number of threads to be used for the computation.
     */
    private int numberOfThreads = Runtime.getRuntime().availableProcessors();

    /**
     * Documentation of parameter from the gensim documentation: "The threshold for configuring which higher-frequency
     * words are randomly downsampled, useful range is (0, 1e-5)."
     */
    private double sample = SAMPLE_DEFAULT;

    /**
     * Default for {@link Word2VecConfiguration#sample} parameter.
     */
    public static final double SAMPLE_DEFAULT =  0.001;

    /**
     * Default Constructor.
     * Many parameters are assumed such as training type SG.
     */
    public Word2VecConfiguration(){}

    /**
     * Constructor
     * @param type Training type (SG/CBOW).
     */
    public Word2VecConfiguration(Word2VecType type){
        setType(type);
    }

    /**
     * Constructor
     * @param type Training type (SG/CBOW).
     * @param vectorDimension ize of the vectors (number of elements).
     */
    public Word2VecConfiguration(Word2VecType type, int vectorDimension){
        setType(type);
        this.vectorDimension = vectorDimension;
    }

    /**
     * Constructor
     * @param type Training type (SG/CBOW).
     * @param vectorDimension Size of the vectors (number of elements).
     * @param iterations aka epochs
     */
    public Word2VecConfiguration(Word2VecType type, int vectorDimension, int iterations){
        setType(type);
        this.vectorDimension = vectorDimension;
        this.iterations = iterations;
    }


    public int getNumberOfThreads(){
        return this.numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads){
        if(numberOfThreads < 1){
            LOGGER.warn("The number of threads must be greater than 0. Using default: All available processors.");
            this.numberOfThreads = Runtime.getRuntime().availableProcessors();
        } else this.numberOfThreads = numberOfThreads;
    }

    public int getNegatives(){
        return this.negatives;
    }

    public void setNegatives(int negatives){
        if(negatives < 1){
            LOGGER.warn("The number of negatives must be greater than 1. Using default: 5.");
            this.negatives = 5;
        } else this.negatives = negatives;
    }

    public int getIterations(){
        return this.iterations;
    }

    public void setIterations(int iterations){
        if(iterations < 1){
            LOGGER.warn("The number of iterations must be greater than 1. Using default: 5.");
            this.iterations = 5;
        } else this.iterations = iterations;
    }

    public int getWindowSize(){
        return this.windowSize;
    }

    public void setWindowSize(int windowSize){
        if(windowSize < 2){
            LOGGER.warn("The window size must be greater than 2. Using default: 5.");
            this.windowSize = 5;
        } else this.windowSize = windowSize;
    }

    public int getVectorDimension(){
        return this.vectorDimension;
    }

    public void setVectorDimension(int vectorDimension) {
        if(vectorDimension < 1){
            LOGGER.warn("The vector dimension must be greater than 1. Using default vector size (200).");
            this.vectorDimension = 200;
        } else this.vectorDimension = vectorDimension;
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        if(minCount < 1){
            LOGGER.warn("The minCount must be greater than 1. Using default: 1.");
            this.minCount = 1;
        } else this.minCount = minCount;
    }

    public Word2VecType getType() {
        return type;
    }

    public void setType(Word2VecType type) {
        this.type = type;
    }

    public double getSample() {
        return sample;
    }

    public void setSample(double sample) {
        if(sample < 0.0) {
            LOGGER.warn("The sample must be greater than 0 or equal 0. Using default: 0.");
            this.sample = 0.001;
        } else this.sample = sample;
    }
}