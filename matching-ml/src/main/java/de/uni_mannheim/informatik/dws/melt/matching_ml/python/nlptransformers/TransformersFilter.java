package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This filter extracts the corresponding text for a resource (with the specified and customizable extractor) given all correspondences in the input alignment.
 * The texts of the two resources are fed into the specified transformer model and the prediction is added in form of a confidence to the correspondence.
 * No filtering is applied in this class.
 */
public class TransformersFilter extends TransformersBase implements Filter {


    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersFilter.class);
    private static final String NEWLINE = System.getProperty("line.separator");
    
    protected boolean changeClass;
    protected BatchSizeOptimization batchSizeOptimization;

    /**
     * Constructor with all required parameters and default values for optional parameters (can be changed by setters).
     * It uses the systems default tmp dir to store the files with texts generated from the knowledge graphs.
     * Pytorch is used instead of tensorflow and all visible GPUs are used for prediction.
     * @param extractor the extractor to select which text for each resource should be used.
     * @param modelName the model name which can be a model id (a hosted model on huggingface.co) or a path to a directory containing a model and tokenizer
     * (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     * see first parameter pretrained_model_name_or_path of the from_pretrained
     * function in huggingface library</a>). In case of a path, it should be absolute. 
     * The path can be generated by e.g. {@link FileUtil#getCanonicalPathIfPossible(java.io.File) }
     */
    public TransformersFilter(TextExtractor extractor, String modelName) {
        super(extractor, modelName);
        this.changeClass = false;
        this.batchSizeOptimization = BatchSizeOptimization.NONE;
    }
    
    /**
     * Constructor with all required parameters and default values for optional parameters (can be changed by setters).
     * It uses the systems default tmp dir to store the files with texts generated from the knowledge graphs.
     * Pytorch is used instead of tensorflow and all visible GPUs are used for prediction.
     * @param extractor the extractor to select which text for each resource should be used.
     * @param modelName the model name which can be a model id (a hosted model on huggingface.co) or a path to a directory containing a model and tokenizer
     * (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     * see first parameter pretrained_model_name_or_path of the from_pretrained
     * function in huggingface library</a>). In case of a path, it should be absolute. 
     * The path can be generated by e.g. {@link FileUtil#getCanonicalPathIfPossible(java.io.File) }
     */
    public TransformersFilter(TextExtractorMap extractor, String modelName) {
        super(extractor, modelName);
        this.changeClass = false;
        this.batchSizeOptimization = BatchSizeOptimization.NONE;
    }

    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        File inputFile = FileUtil.createFileWithRandomNumber("alignment_transformers_predict", ".txt");
        Map<Correspondence, List<Integer>> map;
        try{
            map = createPredictionFile(source, target, inputAlignment, inputFile, false);
        }catch (IOException ex) {
            LOGGER.warn("Could not write text to prediction file. Return unmodified input alignment.", ex);
            inputFile.delete();
            return inputAlignment;
        }
        try {
            if(map.isEmpty()){
                LOGGER.warn("No correspondences have enough text to be processed (the input alignment has {} " +
                        "correspondences) - the input alignment is returned unchanged.", inputAlignment.size());
                return inputAlignment;
            }
            
            LOGGER.info("Run prediction");
            List<Double> confidenceList = predictConfidences(inputFile);
            LOGGER.info("Finished prediction");

            for(Entry<Correspondence, List<Integer>> correspondenceToLineNumber : map.entrySet()){
                double max = 0.0;
                for(Integer lineNumber : correspondenceToLineNumber.getValue()){
                    Double conf = confidenceList.get(lineNumber);
                    if(conf == null){
                        throw new IllegalArgumentException("Could not find a confidence for a given correspondence.");
                    }
                    if(conf > max)
                        max = conf;
                }
                correspondenceToLineNumber.getKey().addAdditionalConfidence(this.getClass(), max);
            }
        } finally {
            inputFile.delete();
        }
        return inputAlignment;
    }
    


    /**
     * Create the prediction file which is a CSV file with two columns.The first column is the text from the left resource and the second column is the text from the right resource.
     * @param source The source model
     * @param target The target model
     * @param predictionAlignment the alignment to process. All correspondences which have enough text are used.
     * @param outputFile the csv file to which the output should be written to.
     * @param append if true, then the training alignment is append to the given file.
     * @return the map which maps the the correspondence to (possibly multiple) row numbers.
     * In case of multipleTextsToMultipleExamples is set to true, multiple rows can correspond to one correspondence,
     * because each text (e.g. label, comment etc) of the two resources is used as an example.
     * @throws IOException in case the writing fails.
     */
    public Map<Correspondence, List<Integer>> createPredictionFile(OntModel source, OntModel target, Alignment predictionAlignment, File outputFile, boolean append) throws IOException {
        Map<Correspondence, List<Integer>> map = new HashMap<>();
        int i = 0;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, append), StandardCharsets.UTF_8))){
            Map<Resource,Map<String, Set<String>>> cache = new HashMap<>();
            for(Correspondence c : predictionAlignment){
                c.addAdditionalConfidence(this.getClass(), 0.0d); // initialize it
                
                Map<String, Set<String>> sourceTexts = getTextualRepresentation(source.getResource(c.getEntityOne()), cache);
                Map<String, Set<String>> targetTexts = getTextualRepresentation(target.getResource(c.getEntityTwo()), cache);

                for(Entry<String, Set<String>> textLeftGroup : sourceTexts.entrySet()){
                    for(String textRight : targetTexts.get(textLeftGroup.getKey())){
                        if(StringUtils.isBlank(textRight)){
                            continue;
                        }
                        for(String textLeft : textLeftGroup.getValue()){
                            if(StringUtils.isBlank(textLeft)){
                                continue;
                            }
                            writer.write(StringEscapeUtils.escapeCsv(textLeft) + "," + StringEscapeUtils.escapeCsv(textRight) + NEWLINE);
                            map.computeIfAbsent(c, __-> new ArrayList<>()).add(i);
                            i++;
                        }
                    }
                }
            }
        }
        LOGGER.info("Wrote {} examples to prediction file {}", i, outputFile);
        return map;
    }
        
    /**
     * Run huggingface transformers library.
     * @param predictionFilePath path to csv file with two columns (text left and text right).
     * @throws Exception in case something goes wrong.
     * @return a list of confidences
     */
    public List<Double> predictConfidences(File predictionFilePath) throws Exception{
        if(this.batchSizeOptimization != BatchSizeOptimization.NONE){
            this.trainingArguments.addParameter("per_device_eval_batch_size", getMaximumPerDeviceEvalBatchSize(predictionFilePath));
        }
        return PythonServer.getInstance().transformersPrediction(this, predictionFilePath);
    }
    
    
    /**
     * This functions tries to execute the prediction with number of example equal to the tested batch size.
     * It will start with 2 and checks only powers of 2.
     * @param trainingFile the training file to use
     * @return the maximum {@code per_device_eval_batch_size }
     */
    protected int getMaximumPerDeviceEvalBatchSize(File trainingFile){
        //save variables for restoring afterwards
        TransformersTrainerArguments backupArguments = this.trainingArguments;
        String backupCudaString = this.cudaVisibleDevices;
        
        this.cudaVisibleDevices = getCudaVisibleDevicesButOnlyOneGPU();
        int batchSize = 4;
        List<String> batchExamples = getExamplesForBatchSizeOptimization(trainingFile, 8194, this.batchSizeOptimization);
        while(batchSize < 8193){
            LOGGER.info("Try out per_device_eval_batch_size of {}", batchSize);
            //generate a smaller training file -> faster tokenizer
            
            File tmpTrainingFile = FileUtil.createFileWithRandomNumber("alignment_transformers_predict_find_max_batch_size", ".txt");
            try{
                if(this.writeExamplesToFile(batchExamples, tmpTrainingFile, batchSize) == false){
                    int batchSizeWhichWorks = batchSize / 2;
                    LOGGER.info("File contains too few lines to further increase batch size. Thus use now {}", batchSizeWhichWorks);
                    return batchSizeWhichWorks;
                }
                this.trainingArguments = new TransformersTrainerArguments(backupArguments);
                this.trainingArguments.addParameter("per_device_eval_batch_size", batchSize);

                PythonServer.getInstance().transformersPrediction(this, tmpTrainingFile);
            }catch (PythonServerException ex) {
                //CUDA ERROR: RuntimeError: CUDA out of memory. Tried to allocate 192.00 MiB (GPU 0; 10.76 GiB total capacity; 9.54 GiB already allocated; 50.56 MiB free; 9.59 GiB reserved in total by PyTorch)
                //CPU  ERROR: RuntimeError: [enforce fail at ..\c10\core\CPUAllocator.cpp:79] data. DefaultCPUAllocator: not enough memory: you tried to allocate 50878464 bytes.
                if(ex.getMessage().contains("not enough memory") || ex.getMessage().contains("out of memory")){
                    int batchSizeWhichWorks = batchSize / 2;
                    if(this.batchSizeOptimization == BatchSizeOptimization.USE_LONGEST_TEXTS_PESSIMISTIC)
                        batchSizeWhichWorks = batchSize / 4;
                    LOGGER.info("Found memory error, thus returning batchsize of {}", batchSizeWhichWorks);
                    this.trainingArguments = backupArguments;
                    this.cudaVisibleDevices = backupCudaString;
                    return batchSizeWhichWorks;
                }else{
                    LOGGER.warn("Something went wrong in python server during getMaximumPerDeviceEvalBatchSize. Return default of 8", ex);
                    this.trainingArguments = backupArguments;
                    this.cudaVisibleDevices = backupCudaString;
                    return 8;
                }
            }catch (Exception ex) {
                LOGGER.warn("Something went wrong during getMaximumPerDeviceEvalBatchSize. Return default of 8", ex);
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
        return batchSize;
    }

    //setter and getter

    /**
     * Return true if the class is changed in the classification.
     * This is useful if a pretrained model predict exactly the opposite class.
     * @return true if the class is changed in the classification.
     */
    public boolean isChangeClass() {
        return changeClass;
    }

    /**
     * If set to true, the class is changed in the classification.
     * This is useful if a pretrained model predict exactly the opposite class.
     * @param changeClass true if the class should be changed in the classification.
     */
    public void setChangeClass(boolean changeClass) {
        this.changeClass = changeClass;
    }
    
    /**
     * Return true if batch size optimization is turned on.
     * @return true if batch size optimization is turned on.
     * @deprecated better use {@code getBatchSizeOptimization }
     */
    public boolean isOptimizeBatchSize() {
        return batchSizeOptimization != BatchSizeOptimization.NONE;
    }

    /**
     * Set the value if batch size should be optimized before running the prediction.
     * This should only be set to true, if the dataset is huge.
     * Otherwise the algorithm to find the largest batch size needs too much time.
     * @param optimizeBatchSize if true, optimize the batch size every time the match method is called.
     * @deprecated better use {@code setBatchSizeOptimization }
     */
    public void setOptimizeBatchSize(boolean optimizeBatchSize) {
        this.batchSizeOptimization = BatchSizeOptimization.USE_LONGEST_TEXTS;
    }

    /**
     * Returns how the batch size is optimized.
     * @return how the batch size is optimized
     */
    public BatchSizeOptimization getBatchSizeOptimization() {
        return batchSizeOptimization;
    }

    /**
     * Sets how the batch size is optimized.
     * @param batchSizeOptimization how the batch size is optimized
     */
    public void setBatchSizeOptimization(BatchSizeOptimization batchSizeOptimization) {
        this.batchSizeOptimization = batchSizeOptimization;
    }
    
    
    
    /**
     * This will enabled or disable all possible optimization to improve prediction speed.
     * Currently this includes mixed precision training and batch size optimization.
     * @param optimize true to enable
     */
    public void setOptimizeAll(boolean optimize){
        setOptimizeBatchSize(optimize);
        setOptimizeForMixedPrecisionTraining(optimize);
    }
    
    /**
     * This will return the value if all optimization techiques are enabled or diabled.
     * @return true if enabled.
     */
    public boolean isOptimizeAll(){
        return isOptimizeBatchSize() && isOptimizeForMixedPrecisionTraining();
    }
}
