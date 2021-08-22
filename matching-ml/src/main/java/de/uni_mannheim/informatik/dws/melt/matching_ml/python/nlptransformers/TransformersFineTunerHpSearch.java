package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServerException;
import java.io.File;
import java.io.IOException;
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
            int maxBatchSize = getMaximumPerDeviceTrainBatchSize(trainingFile);
            List<Object> list = new ArrayList<>();
            if(maxBatchSize < 4){
                int i = 1;
                while(i <= maxBatchSize){
                    list.add(i);
                    i *= 2;
                }
            }else if(maxBatchSize < 8){
                int i = 2;
                while(i <= maxBatchSize){
                    list.add(i);
                    i *= 2;
                }
            }else{
                int i = 4;
                while(i <= maxBatchSize){
                    list.add(i);
                    i *= 2;
                }
            }
            LOGGER.info("Set the hyper parameter search space for \"per_device_train_batch_size\" to: {}", list);
            
            this.hpSpace.choice("per_device_train_batch_size", list);
            this.hpMutations.choice("per_device_train_batch_size", list);
        }
        PythonServer.getInstance().transformersFineTuningHpSearch(this, trainingFile);
        return this.resultingModelLocation;
    }
    
    public int getMaximumPerDeviceTrainBatchSize(){
        if(this.trainingFile == null || !this.trainingFile.exists() || this.trainingFile.length() == 0){
            throw new IllegalArgumentException("Cannot get maximum per device train batch size because no training file is generated. Did you call the match method before (e.g. in a pipeline)?");
        }
        return getMaximumPerDeviceTrainBatchSize(this.trainingFile);
    }

    /**
     * This functions tries to execute the training with one step to check which maximum {@code per_device_train_batch_size } is possible.It will start with 2 and checks only powers of 2.
     * @param trainingFile the training file to use
     * @return the maximum {@code per_device_train_batch_size } with the current configuration
     */
    public int getMaximumPerDeviceTrainBatchSize (File trainingFile){        
        //save variables for restoring afterwards
        TransformersTrainerArguments backupArguments = this.trainingArguments;
        String backupCudaString = this.cudaVisibleDevices;
                
        this.cudaVisibleDevices = getCudaVisibleDevicesButOnlyOneGPU();
        int batchSize = 4;
        while(batchSize < 8193){
            LOGGER.info("Try out batch size of {}", batchSize);
            //generate a smaller training file -> faster tokenizer
            
            File tmpTrainingFile = FileUtil.createFileWithRandomNumber(this.tmpDir, "alignment_transformers_find_max_batch_size", ".txt");
            try{
                if(this.copyCSVLines(trainingFile, tmpTrainingFile, batchSize) == false){
                    int batchSizeWhichWorks = batchSize / 2;
                    LOGGER.info("File contains too few lines to further increase batch size. Thus use now {}", batchSizeWhichWorks);
                }
                this.trainingArguments = new TransformersTrainerArguments(backupArguments);
                this.trainingArguments.addParameter("per_device_train_batch_size", batchSize);
                this.trainingArguments.addParameter("save_at_end", false);
                this.trainingArguments.addParameter("max_steps", 1);
                super.finetuneModel(tmpTrainingFile);
            }catch (PythonServerException ex) {
                //CUDA ERROR: RuntimeError: CUDA out of memory. Tried to allocate 192.00 MiB (GPU 0; 10.76 GiB total capacity; 9.54 GiB already allocated; 50.56 MiB free; 9.59 GiB reserved in total by PyTorch)
                //CPU  ERROR: RuntimeError: [enforce fail at ..\c10\core\CPUAllocator.cpp:79] data. DefaultCPUAllocator: not enough memory: you tried to allocate 50878464 bytes.
                if(ex.getMessage().contains("not enough memory") || ex.getMessage().contains("out of memory")){
                    int batchSizeWhichWorks = batchSize / 2;
                    LOGGER.info("Found memory error, thus returning batchsize of {}", batchSizeWhichWorks);
                    this.trainingArguments = backupArguments;
                    this.cudaVisibleDevices = backupCudaString;
                    return batchSizeWhichWorks;
                }else{
                    LOGGER.warn("Something went wrong in python server during getMaximumPerDeviceTrainBatchSize. Return default of 8", ex);
                    this.trainingArguments = backupArguments;
                    this.cudaVisibleDevices = backupCudaString;
                    return 8;
                }
            }catch (IOException ex) {
                LOGGER.warn("Something went wrong with io during getMaximumPerDeviceTrainBatchSize. Return default of 8", ex);
                this.trainingArguments = backupArguments;
                this.cudaVisibleDevices = backupCudaString;
                return 8;
            }catch (Exception ex) {
                LOGGER.warn("Something went wrong during getMaximumPerDeviceTrainBatchSize. Return default of 8", ex);
                this.trainingArguments = backupArguments;
                this.cudaVisibleDevices = backupCudaString;
                return 8;
            }finally{
                tmpTrainingFile.delete();
            }
            batchSize *= 2;
        }
        
        LOGGER.info("It looks like that batch sizes up to 8192 works out which is unusual. If greater batch sizes are possible the code to search max batch size needs to be changed.");
        this.trainingArguments = backupArguments;
        this.cudaVisibleDevices = backupCudaString;
        return batchSize;
    }
    
    public String getCudaVisibleDevicesButOnlyOneGPU(){
        String gpus = this.getCudaVisibleDevices();
        if(gpus == null) // this means use all available.
            return "0"; //then we select only the first one
        gpus = gpus.trim();
        if(gpus.isEmpty())
            return "0"; // same as above
        String[] array = gpus.split(",");
        return array[0]; // one element is always contained
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
