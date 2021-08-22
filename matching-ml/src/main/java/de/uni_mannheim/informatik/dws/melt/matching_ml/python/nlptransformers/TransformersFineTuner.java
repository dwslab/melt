package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServerException;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to fine-tune a transformer model based on a generated dataset.
 * In every call to the match method, the training data will be generated and appended to a temporary file.
 * When you call the {@link TransformersFineTuner#finetuneModel() } method, then a model is fine-tuned and the
 * training file is deleted.
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
     * @param resultingModelLocation the final location where the fine-tuned model should be stored.
     * @param tmpDir Sets the tmp directory used by the matcher. In this folder the file with all texts from the knowledge graph are stored.
     */
    public TransformersFineTuner(TextExtractor extractor, String initialModelName, File resultingModelLocation, File tmpDir) {
        super(extractor, initialModelName);
        this.tmpDir = tmpDir;
        this.resultingModelLocation = resultingModelLocation;
        this.trainingFile = FileUtil.createFileWithRandomNumber(this.tmpDir, "alignment_transformers_train", ".txt");
    }
    
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
        this(extractor, initialModelName, resultingModelLocation, FileUtil.SYSTEM_TMP_FOLDER);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        LOGGER.info("Append text to training file: {}", this.trainingFile);
        writeTrainingFile(source, target, inputAlignment, this.trainingFile, true);
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
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile, append), StandardCharsets.UTF_8))){
            if(this.multipleTextsToMultipleExamples){
                for(Correspondence c : trainingAlignment){
                    for(String textLeft : this.extractor.extract(source.getResource(c.getEntityOne()))){
                        if(StringUtils.isBlank(textLeft)){
                            continue;
                        }
                        for(String textRight : this.extractor.extract(target.getResource(c.getEntityTwo()))){
                            if(StringUtils.isBlank(textRight)){
                                continue;
                            }
                            String clazz;
                            if(c.getRelation() == CorrespondenceRelation.EQUIVALENCE){
                                clazz = "1";
                                positive++;
                            }else{
                                clazz = "0";
                                negative++;
                            }
                            writer.write(StringEscapeUtils.escapeCsv(textLeft) + "," + StringEscapeUtils.escapeCsv(textRight) +  "," + clazz + NEWLINE);
                        }
                    }
                }
                LOGGER.info("Wrote {} training examples. {} positive, {} negative (number of unused is to determined).", positive+negative, positive, negative);
            }else{
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
                LOGGER.info("Wrote {} training examples. {} positive, {} negative, {} not used due to insufficient textual data.", positive+negative, positive, negative, notUsed);
            }
        }
        
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
     * @return the final location (directory) of the finetuned model (which is also given in the constructor)
     * @throws java.lang.Exception in case of any error
     */
    public File finetuneModel(File trainingFile) throws Exception{
        PythonServer.getInstance().transformersFineTuning(this, trainingFile);
        return this.resultingModelLocation;
    }
    
    /**
     * This method should only be used when appendOnlyToFile is set to true.
     * This will train a transformers model on the file generated by the match method during possibley multiple calls.
     * @return the final location (directory) of the fine-tuned model (which is also given in the constructor)
     * @throws Exception in case of any error
     */
    public File finetuneModel() throws Exception{
        if(this.trainingFile == null || !this.trainingFile.exists() || this.trainingFile.length() == 0){
            throw new IllegalArgumentException("Cannot finetune a model if because no training file is generated. Did you call the match method before (e.g. in a pipeline)?");
        }
        File toReturn = finetuneModel(this.trainingFile);
        this.trainingFile.delete();
        return toReturn;
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
}
