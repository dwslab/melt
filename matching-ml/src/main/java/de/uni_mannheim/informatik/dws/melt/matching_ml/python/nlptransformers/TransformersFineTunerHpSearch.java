package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformersFineTunerHpSearch extends TransformersFineTuner implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersFineTunerHpSearch.class);

    /**
     * the number of different hyperparamter combinations which are tried out
     */
    private int numberOfTrials;
    
    /**
     * A number between zero and one which represents the  proportion of the data to include in the test split
     */
    private float testSize;    
    
    /**
     * The metric to optimize during hyperparameter search.
     */
    private TransformersOptimizingMetric optimizingMetric;
    
    /**
     * The initial hyper parameter search space.
     * For nearly all algorithms except PBT, this is the only search space which should be defined.
     */
    private TransformersHpSearchSpace hpSpace;
    
    /**
     * These are the mutations for the hyperparameter - only applicable for PBT (population based training).
     */
    private TransformersHpSearchSpace hpMutations;
    
    private boolean adjustMaxBatchSize;
    
    
    public TransformersFineTunerHpSearch(TextExtractor extractor, String initialModelName, File resultingModelLocation) {
        super(extractor, initialModelName, resultingModelLocation);
        this.numberOfTrials = 10;
        this.testSize = 0.33f;
        this.optimizingMetric = TransformersOptimizingMetric.AUC;
        this.hpSpace = TransformersHpSearchSpace.getDefaultHpSpace();
        this.hpMutations = TransformersHpSearchSpace.getDefaultHpSpaceMutations();
    }


    /**
     * Finetune a given model with the provided text in the csv file (three columns: first text, second text, label(0/1))
     * @param trainingFile csv file with three columns: first text, second text, label(0/1)
     * @return the final location (directory) of the finetuned model (which is also given in the constructor)
     * @throws java.lang.Exception in case of any error
     */
    @Override
    public File finetuneModel(File trainingFile) throws Exception{
        if(this.adjustMaxBatchSize){
            int maxBatchSize = getMaximumPerDeviceTrainBatchSize();
            
            List<Object> list = new ArrayList<>();
            int i = 4;
            while(i <= maxBatchSize){
                list.add(i);
                i *= 2;
            }
            
            this.hpSpace.choice("per_device_train_batch_size", list);
            this.hpMutations.choice("per_device_train_batch_size", list);
        }
        PythonServer.getInstance().transformersFineTuningHpSearch(this, trainingFile);
        return this.resultingModelLocation;
    }

    /**
     * Returns the number of trials which should be executed during hyperparameter search.
     * This means how many different hyperparameter combinations should be tried out.
     * The more the better, but also takes more time.
     * @return the number of different hyperparamter combinations which are executed
     */
    public int getNumberOfTrials() {
        return numberOfTrials;
    }

    /**
     * Sets the number of trials which should be executed during hyperparameter search.
     * This means how many different hyperparameter combinations should be tried out.
     * The more the better, but also takes more time.
     * @param numberOfTrials the number of different hyperparamter combinations which are executed
     */
    public void setNumberOfTrials(int numberOfTrials) {
        this.numberOfTrials = numberOfTrials;
    }

    /**
     * Returns a number between zero and one which represents the  proportion of the data to include in the test split.
     * @return a number between zero and one which represents the  proportion of the data to include in the test split
     */
    public float getTestSize() {
        return testSize;
    }

    /**
     * Sets the number between zero and one which represents the  proportion of the data to include in the test split
     * @param testSize number between zero and one which represents the  proportion of the data to include in the test split
     */
    public void setTestSize(float testSize) {
        if(testSize < 0.0 || testSize > 1.0)
            throw new IllegalArgumentException("Test size should be between zero and one");
        this.testSize = testSize;
    }

    /**
     * Returns the metric which is optimized during hyperparameter search.
     * @return the metric which is optimized during hyperparameter search
     */
    public TransformersOptimizingMetric getOptimizingMetric() {
        return optimizingMetric;
    }

    /**
     * Sets the metric which is optimized during hyperparameter search.
     * @param optimizingMetric the metric which is optimized during hyperparameter search
     */
    public void setOptimizingMetric(TransformersOptimizingMetric optimizingMetric) {
        this.optimizingMetric = optimizingMetric;
    }

    /**
     * Returns the initial hyper parameter search space.
     * For nearly all algorithms except PBT, this is the only search space which should be defined.
     * @return the initial hyper parameter search space.
     */
    public TransformersHpSearchSpace getHpSpace() {
        return hpSpace;
    }

    /**
     * Sets the initial hyper parameter search space.
     * For nearly all algorithms except PBT, this is the only search space which should be defined.
     * @param hpSpace the initial hyper parameter search space
     */
    public void setHpSpace(TransformersHpSearchSpace hpSpace) {
        if(hpSpace == null)
            throw new IllegalArgumentException("HpSpace should not be null.");
        this.hpSpace = hpSpace;
    }

    /**
     * Returns the mutations for the hyperparameter - only applicable for PBT (population based training).
     * @return the mutations for the hyperparameter
     */
    public TransformersHpSearchSpace getHpMutations() {
        return hpMutations;
    }

    /**
     * Sets the mutations for the hyperparameter - only applicable for PBT (population based training).
     * @param hpMutations the mutations for the hyperparameter
     */
    public void setHpMutations(TransformersHpSearchSpace hpMutations) {
        if(hpMutations == null)
            throw new IllegalArgumentException("HpMutations should not be null.");
        this.hpMutations = hpMutations;
    }

    /**
     * Returns the value if max batch size is adjusted or not.
     * @return true if the batch size is modified.
     */
    public boolean isAdjustMaxBatchSize() {
        return adjustMaxBatchSize;
    }

    /**
     * If set to true, then it will set the max value of the search space for the training batch size to the maximum
     * which is possible with the current GPU/CPU.
     * @param adjustMaxBatchSize true to enable the adjustment
     */
    public void setAdjustMaxBatchSize(boolean adjustMaxBatchSize) {
        this.adjustMaxBatchSize = adjustMaxBatchSize;
    }
    
}
