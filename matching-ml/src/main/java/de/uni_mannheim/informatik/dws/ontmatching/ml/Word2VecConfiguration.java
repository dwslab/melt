package de.uni_mannheim.informatik.dws.ontmatching.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The configuration for the word2vec calculation.
 */
public enum Word2VecConfiguration {
    CBOW, SG;

    /**
     * Default logger.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(Word2VecConfiguration.class);

    /**
     * Size of the vector. Default: 200.
     */
    private int vectorDimension = 200;

    /**
     * The size of the window during the word2vec training. Default: 5.
     */
    private int windowSize = 5;

    /**
     * Iterations during the word2vec training.
     */
    private int iterations = 5;

    /**
     * The number of negatives during the word2vec training.
     */
    private int negatives = 25;

    /**
     * The number of threads to be used for the computation.
     */
    private int numberOfThreads = Runtime.getRuntime().availableProcessors();

    public int getNumberOfThreads(){
        return this.numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads){
        if(numberOfThreads < 1){
            LOGGER.warn("The number of threads must be greater than 0. Using default: All available processors.");
            numberOfThreads = Runtime.getRuntime().availableProcessors();
        }
        this.numberOfThreads = numberOfThreads;
    }

    public int getNegatives(){
        return this.negatives;
    }

    public void setNegatives(int negatives){
        if(negatives < 1){
            LOGGER.warn("The number of negatives must be greater than 1. Using default: 25.");
            negatives = 25;
        }
        this.negatives = negatives;
    }

    public int getIterations(){
        return this.iterations;
    }

    public void setIterations(int iterations){
        if(iterations < 1){
            LOGGER.warn("The number of iterations must be greater than 1. Using default: 5.");
            iterations = 5;
        }
        this.iterations = iterations;
    }

    public int getWindowSize(){
        return this.windowSize;
    }

    public void setWindowSize(int windowSize){
        if(windowSize < 2){
            LOGGER.warn("The window size must be greater than 2. Using default: 5.");
            windowSize = 5;
        }
        this.windowSize = windowSize;
    }

    public int getVectorDimension(){
        return this.vectorDimension;
    }

    public void setVectorDimension(int vectorDimension) {
        if(vectorDimension < 1){
            LOGGER.warn("The vector dimension must be greater than 1. Using default vector size (200).");
            vectorDimension = 200;
        }
        this.vectorDimension = vectorDimension;
    }

    @Override
    public String toString(){
        switch (this){
            case CBOW:
                return "cbow";
            case SG:
                return "sg";
            default:
                // this code part is never reached
                return "UNDEFINED";
        }
    }
}