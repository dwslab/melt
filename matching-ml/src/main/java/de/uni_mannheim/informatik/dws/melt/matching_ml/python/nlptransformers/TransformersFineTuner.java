package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to finetune a transformer model based on a generated dataset.
 * It comes in two fashions. First, it can be used to generate a fine-tuned model with every call of the match method.
 * The input alignment of the match method is used to generate a training dataset.
 * Thus, the input alignment should contain positive correspondences (with an equivalence relation) and negative
 * correspondences (with another relation than equivalence).
 * 
 * As a second fashion, this matcher can be used to only generate the training file (by possibly multiple calls to the match method).
 * Thus, within the match method only the training file is written. After all calls to the match method, one can call
 * the {@link #finetuneModel() } function to train the model on the whole dataset.
 * 
 * In both cases, the fine-tuned model as well as the tokenizer is written to the specified directory.
 */
public class TransformersFineTuner extends TransformersBase implements Filter {


    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersFineTuner.class);
    private static final String NEWLINE = System.getProperty("line.separator");
    
    protected File resultingModelLocation;
    protected File trainingFile;

    //memory issue with transformers library
    //https://github.com/huggingface/transformers/issues/6753
    //https://github.com/huggingface/transformers/issues/1742
    
    /**
     * Run the training of a NLP transformer.
     * @param extractor used to extract text from a given resource. This is the text which represents a resource.
     * @param initialModelName the initial model name for fine tuning which can be downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>). This value can be also changed by {@link #setModelName(java.lang.String) }.
     * @param resultingModelLocation the final location where the finetuned model should be stored.
     */
    public TransformersFineTuner(TextExtractor extractor, String initialModelName, File resultingModelLocation) {
        super(extractor, initialModelName);
        
        this.resultingModelLocation = resultingModelLocation;
        this.trainingFile = null;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        LOGGER.info("Write text to training file");
        if(this.trainingFile != null){
            //append to this training file:
            writeTrainingFile(source, target, inputAlignment, this.trainingFile, true);
        }else{
            //write to new file and directly train a model
            File trainFile = createTrainingFile(source, target, inputAlignment);
            if(trainFile == null)
                return inputAlignment;
            try{
                finetuneModel(trainFile);
            } finally{
                trainFile.delete();
            }
        }
        return inputAlignment;
    }
    
    /**
     * Creates a new file and writes all correspondences as textual data to it.
     * It returns null if no correspondences could be written.
     * @param source the source model
     * @param target the target model
     * @param trainingAlignment the training alignment to be written to file.
     * @return the training file.
     * @throws IOException in case the writing fails
     */
    public File createTrainingFile(OntModel source, OntModel target, Alignment trainingAlignment) throws IOException{
        File trainFile = FileUtil.createFileWithRandomNumber(this.tmpDir, "alignment_transformers_train", ".txt");
        int writtenCorrespondences = writeTrainingFile(source, target, trainingAlignment, trainFile, false);
        if(writtenCorrespondences == 0){
            LOGGER.warn("No training file is created because no correspondences have enough text.");
            trainFile.delete();
            return null;
        }
        return trainFile;
    }
    
    /**
     * Writes the correspondences to a file (append or not can be chosen by a parameter).
     * @param source the source model
     * @param target the target model
     * @param trainingAlignment the training alignment to be written to file.
     * @param trainFile the file to write all texts
     * @param append true if all content should be appended to the file.
     * @return how many correspondences were written to the file.
     * @throws IOException in case the writing fails
     */
    public int writeTrainingFile(OntModel source, OntModel target, Alignment trainingAlignment, File trainFile, boolean append) throws IOException{
        int notUsed = 0;
        int positive = 0;
        int negative = 0;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile, append), "UTF-8"))){
            for(Correspondence c : trainingAlignment){
                String left = getTextFromResource(source.getResource(c.getEntityOne()));
                String right = getTextFromResource(target.getResource(c.getEntityTwo()));
                if(StringUtils.isBlank(left) || StringUtils.isBlank(right)){
                    notUsed++;
                    continue; // do not use examples where one or the other text is zero
                }
                String clazz;
                if(c.getRelation() == CorrespondenceRelation.EQUIVALENCE){
                    clazz = "1";
                    positive++;
                }else{
                    clazz = "0";
                    negative++;
                }
                writer.write(StringEscapeUtils.escapeCsv(left) + "," + StringEscapeUtils.escapeCsv(right)+  "," + clazz + NEWLINE);
            }
        }
        LOGGER.info("Wrote {} training examples. {} positive, {} negative, {} not used due to insufficient textual data.", positive+negative, positive, negative, notUsed);
        return positive+negative;
    }
    
    private String getTextFromResource(Resource r){
        StringBuilder sb = new StringBuilder();
        for(String text : this.extractor.extract(r)){
            sb.append(text.trim()).append(" ");
        }
        return sb.toString().trim();
    }
    
    /**
     * Finetune a given model with the provided text in the csv file (three columns: first text, second text, label(0/1))
     * @param trainingFile csv file with three columns: first text, second text, label(0/1)
     * @throws java.lang.Exception in case of any error
     */
    public void finetuneModel(File trainingFile) throws Exception{
        PythonServer.getInstance().transformersFineTuning(this, trainingFile);
    }
    
    /**
     * This method should only be used when appendOnlyToFile is set to true.
     * This will train a transformers model on the file generated by the match method during possibley multiple calls.
     * @throws Exception in case of any error
     */
    public void finetuneModel() throws Exception{
        if(this.trainingFile == null){
            throw new IllegalArgumentException("Cannot finetune a model if appendOnlyToFile is set to false.");
        }
        finetuneModel(this.trainingFile);
    }
    
       /*
    private String process(String s){
        return s.replace("\n", " ").replace("\r", " ").replace("WtXmlEmptyTag", " ").trim().replaceAll(" +", " ");
    }
*/
    
    //Getter and Setter
    
    /**
     * Returns the final location where the finetuned model should be stored
     * @return the location for the trained model.
     */
    public File getResultingModelLocation() {
        return resultingModelLocation;
    }

    /**
     * Sets the final location where the finetuned model should be stored.
     * @param resultingModelLocation the model location as a file.
     */
    public void setResultingModelLocation(File resultingModelLocation) {
        this.resultingModelLocation = resultingModelLocation;
    }
    
    /**
     * Returns true, if this matcher only appends the training data to a file and do not train any transformer model.
     * This is useful if the training data should be generated by multiple runs of the match method.
     * In the match method, only the training data is generated.
     * After all calls of the match method, call the {@link #finetuneModel() } method to finally train the model.
     * @return true if during matching only the training data is generated and no model is trained.
     */
    public boolean isAppendOnlyToFile(){
        return this.trainingFile != null;
    }
    
    /**
     * Sets the value if this matcher should only append the training data to a file and do not train any transformer model.
     * This is useful if the training data should be generated by multiple runs of the match method.
     * In the match method, only the training data is generated.
     * After all calls of the match method, call the {@link #finetuneModel() } method to finally train the model.
     * @param appendOnlyToFile true if during matching only the training data is generated and no model is trained.
     *       If a model should be trained with every call to the match method set it to false (which is the default).
     */
    public void setAppendOnlyToFile(boolean appendOnlyToFile){
        if(appendOnlyToFile == true && this.trainingFile == null){
            this.trainingFile = FileUtil.createFileWithRandomNumber(tmpDir, "alignment_transformers_train", ".txt");
        }else if(appendOnlyToFile == false && this.trainingFile != null){
            this.trainingFile = null;
        }
    } 
}
