package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServerException;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to fine-tune a transformer model based on a generated dataset.
 * In every call to the match method, the training data will be generated and appended to a temporary file.
 * When you call the {@link TransformersFineTuner#finetuneModel() } method, then a model is fine-tuned and the
 * training file is deleted.
 */
public class TransformersFineTuner extends TransformersBaseFineTuner implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersFineTuner.class);
    
    //memory issue with transformers library
    //https://github.com/huggingface/transformers/issues/6753
    //https://github.com/huggingface/transformers/issues/1742
    
    protected boolean adjustMaxBatchSize;

    /**
     * Run the training of a NLP transformer.
     * @param extractor used to extract text from a given resource. This is the text which represents a resource.
     * @param initialModelName the initial model name for fine tuning which can be downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>). This value can be also changed by {@link #setModelName(java.lang.String) }.
     * @param resultingModelLocation the final location where the fine-tuned model should be stored.
     */
    public TransformersFineTuner(TextExtractor extractor, String initialModelName, File resultingModelLocation) {
        super(extractor, initialModelName, resultingModelLocation);
    }
    
    
    @Override
    public File finetuneModel(File trainingFile) throws Exception{
        if(this.adjustMaxBatchSize){
            int maxBatchSize = getMaximumPerDeviceTrainBatchSize(trainingFile);
            this.trainingArguments.addParameter("per_device_train_batch_size", maxBatchSize);
        }
        PythonServer.getInstance().transformersFineTuning(this, trainingFile);
        return this.resultingModelLocation;
    }
    
    /**
     * This functions tries to execute the training with one step to check which maximum {@code per_device_train_batch_size } is possible.It will start with 2 and checks only powers of 2.
     * It uses the data collected by running this fine tuner on testcases.
     * If you have a file (comma separated) then you can use {@link #getMaximumPerDeviceTrainBatchSize(java.io.File) }.
     * @return the maximum {@code per_device_train_batch_size } with the current configuration
     */
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
            
            File tmpTrainingFile = FileUtil.createFileWithRandomNumber("alignment_transformers_find_max_batch_size", ".txt");
            try{
                if(this.copyCSVLines(trainingFile, tmpTrainingFile, batchSize) == false){
                    int batchSizeWhichWorks = batchSize / 2;
                    LOGGER.info("File contains too few lines to further increase batch size. Thus use now {}", batchSizeWhichWorks);
                }
                this.trainingArguments = new TransformersTrainerArguments(backupArguments);
                this.trainingArguments.addParameter("per_device_train_batch_size", batchSize);
                this.trainingArguments.addParameter("save_at_end", false);
                this.trainingArguments.addParameter("max_steps", 1);
                finetuneModel(tmpTrainingFile);
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
    
    private String getCudaVisibleDevicesButOnlyOneGPU(){
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
     * This will add (potencially multiple) training parameters to the current {@link TransformersBase#trainingArguments trainingArguments}
     * to make the training faster. Thus do not change the trainingArguments object afterwards 
     * (with {@link TransformersBase#setTrainingArguments(de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersTrainerArguments) setTrainingArguments} ).
     * What you can do is to add more training arguments via getTrainingArguments.addParameter (as long as you do not modify any parameters added by this method).
     * The following parameters are set:
     * <ul>
     *  <li>fp16</li>   
     * </ul>
     * @see <a href="https://huggingface.co/transformers/performance.html">Transformers performance page on huggingface</a>
     */
    public void addTrainingParameterToMakeTrainingFaster(){
        //https://huggingface.co/transformers/performance.html
        //https://huggingface.co/transformers/performance.html
        this.trainingArguments.addParameter("fp16", true);
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
