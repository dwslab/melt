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
    
    public static List<Integer> USEFULL_VECTOR_DIMENSIONS = Arrays.asList(50,100,200,500);
    public static List<Integer> USEFULL_ITERATIONS = Arrays.asList(5,10,20,40);
    
    /**
     * Size of the vector. Default: 200.
     */
    private Word2VecTyp type = Word2VecTyp.CBOW;
    
    /**
     * Size of the vector. Default: 200.
     */
    private int vectorDimension = 200;

    /**
     * The size of the window during the word2vec training. Default: 5.
     */
    private int windowSize = 5;

    /**
     * Iterations during the word2vec training. Default 5.
     */
    private int iterations = 5;

    /**
     * The number of negatives during the word2vec training. Default 5.
     */
    private int negatives = 5;

    /**
     * The minimum count for the word2vec training. Default: 1.
     */
    private int minCount = 1;

    /**
     * The number of threads to be used for the computation.
     */
    private int numberOfThreads = Runtime.getRuntime().availableProcessors();

    
    public Word2VecConfiguration(){}
    
    public Word2VecConfiguration(Word2VecTyp type){
        this.type = type;
    }
    
    public Word2VecConfiguration(Word2VecTyp type, int vectorDimension){
        this.type = type;
        this.vectorDimension = vectorDimension;
    }
    
    public Word2VecConfiguration(Word2VecTyp type, int vectorDimension, int iterations){
        this.type = type;
        this.vectorDimension = vectorDimension;
        this.iterations = iterations;
    }
    
    
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
            LOGGER.warn("The number of negatives must be greater than 1. Using default: 5.");
            negatives = 5;
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

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        if(minCount < 1){
            LOGGER.warn("The minCount must be greater than 1. Using default: 1.");
            minCount = 1;
        }
        this.minCount = minCount;
    }

    public Word2VecTyp getType() {
        return type;
    }

    public void setType(Word2VecTyp type) {
        this.type = type;
    }
}