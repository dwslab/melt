package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import java.io.File;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base class for all Transformers fine tuners.
 * It just contains some variables and getter and setters.
 */
public abstract class TransformersBaseFineTuner extends TransformersBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersBaseFineTuner.class);
    
    protected static final String NEWLINE = System.getProperty("line.separator");
    
    protected File resultingModelLocation;
    protected File trainingFile;
    
    protected boolean additionallySwitchSourceTarget;

    /**
     * Run the training of a NLP transformer.
     * @param extractor used to extract text from a given resource. This is the text which represents a resource.
     * @param initialModelName the initial model name for fine tuning which can be downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>). This value can be also changed by {@link #setModelName(java.lang.String) }.
     * @param resultingModelLocation the final location where the fine-tuned model should be stored.
     */
    public TransformersBaseFineTuner(TextExtractorMap extractor, String initialModelName, File resultingModelLocation) {
        super(extractor, initialModelName);
        this.resultingModelLocation = resultingModelLocation;
        this.trainingFile = FileUtil.createFileWithRandomNumber("alignment_transformers_train", ".txt");
        this.additionallySwitchSourceTarget = false;
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
    public TransformersBaseFineTuner(TextExtractor extractor, String initialModelName, File resultingModelLocation) {
        this(TextExtractorMap.wrapTextExtractor(extractor), initialModelName, resultingModelLocation);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        LOGGER.info("Append text to training file: {}", this.trainingFile);
        writeTrainingFile(source, target, inputAlignment, this.trainingFile, true);
        return inputAlignment;
    }
    
    /**
     * Creates a new file and writes all correspondences as textual data to it.
     * Can also be used for creating a validation file.
     * @param source the source model
     * @param target the target model
     * @param trainingAlignment the training alignment to be written to file.
     * @return the training file.
     * @throws IOException in case the writing fails
     */
    public File createTrainingFile(OntModel source, OntModel target, Alignment trainingAlignment) throws IOException{
        File trainFile = FileUtil.createFileWithRandomNumber("alignment_transformers_train", ".txt");
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
        return writeClassificationFormat(source, target, trainingAlignment, trainFile, append);
    }
    
   
    protected int writeClassificationFormat(OntModel source, OntModel target, Alignment trainingAlignment, File trainFile, boolean append)throws IOException{
        int wrongRelationCorrespodences = 0;
        int notUsedCorrespondences = 0;
        int positiveCorrespondences = 0;
        int negativeCorrespondences = 0;
        int positiveExamples = 0;        
        int negativeExamples = 0;
        int numberOfAddedExamples = this.additionallySwitchSourceTarget ? 2 : 1;
        Map<Resource, Map<String, Set<String>>> cache = new HashMap<>();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile, append), StandardCharsets.UTF_8))){
            for(Correspondence c : trainingAlignment){
                String clazz;
                if(c.getRelation() == CorrespondenceRelation.EQUIVALENCE){
                    clazz = "1";
                    positiveCorrespondences++;
                } else if(c.getRelation() == CorrespondenceRelation.INCOMPAT){
                    clazz = "0";
                    negativeCorrespondences++;
                } else{
                    wrongRelationCorrespodences++;
                    continue;
                }
                
                int examplesForThisCorrespondence = 0;
                Map<String, Set<String>> sourceTexts = getTextualRepresentation(source.getResource(c.getEntityOne()), cache);
                Map<String, Set<String>> targetTexts = getTextualRepresentation(target.getResource(c.getEntityTwo()), cache);
                
                for(Entry<String, Set<String>> textLeftGroup : sourceTexts.entrySet()){
                    for(String textRight : targetTexts.get(textLeftGroup.getKey())){
                        for(String textLeft : textLeftGroup.getValue()){
                            examplesForThisCorrespondence += numberOfAddedExamples;
                            writer.write(StringEscapeUtils.escapeCsv(textLeft) + "," + StringEscapeUtils.escapeCsv(textRight) +  "," + clazz + NEWLINE);
                            if(this.additionallySwitchSourceTarget)
                                writer.write(StringEscapeUtils.escapeCsv(textRight) + "," + StringEscapeUtils.escapeCsv(textLeft) +  "," + clazz + NEWLINE);
                        }
                    }
                }
                if(c.getRelation() == CorrespondenceRelation.EQUIVALENCE){
                    positiveExamples += examplesForThisCorrespondence;
                } else if(c.getRelation() == CorrespondenceRelation.INCOMPAT){
                    negativeExamples += examplesForThisCorrespondence;
                }
                
                if(examplesForThisCorrespondence == 0){
                    notUsedCorrespondences++;
                }
            }
        }
        LOGGER.info("Wrote {} positive and {} negative training EXAMPLES. "
                + "The ALIGNMENT contains {} correspondences ({} positive, {} negative, {} other relations - not used). "
                + "{} correspondences are not used due to insufficient textual data.", positiveExamples, negativeExamples,
                trainingAlignment.size(), positiveCorrespondences, negativeCorrespondences, wrongRelationCorrespodences, notUsedCorrespondences);
        return positiveExamples + negativeExamples;
    }
    
    /**
     * Finetune a given model with the provided text in the csv file (three columns: first text, second text, label(0/1))
     * @param trainingFile csv file with three columns: first text, second text, label(0/1) (can be generated with {@link TransformersBaseFineTuner#createTrainingFile(OntModel, OntModel, Alignment) } )
     * @return the final location (directory) of the finetuned model (which is also given in the constructor)
     * @throws java.lang.Exception in case of any error
     */
    public abstract File finetuneModel(File trainingFile) throws Exception;
    
    /**
     * This method should only be used when appendOnlyToFile is set to true.
     * This will train a transformers model on the file generated by the match method during possibley multiple calls.
     * @return the final location (directory) of the fine-tuned model (which is also given in the constructor)
     * @throws Exception in case of any error
     */
    public File finetuneModel() throws Exception{
        if(this.trainingFile == null || !this.trainingFile.exists() || this.trainingFile.length() == 0){
            throw new IllegalArgumentException("Cannot finetune a model because no training file is generated. Did you call the match method before (e.g. in a pipeline)?");
        }
        File toReturn = finetuneModel(this.trainingFile);
        clearTrainingData();
        return toReturn;
    }
    
    /**
     * Removes the training data 
     */
    public void clearTrainingData(){
        if(this.trainingFile != null && this.trainingFile.exists() && this.trainingFile.length() != 0){
            this.trainingFile.delete();
            this.trainingFile = FileUtil.createFileWithRandomNumber("alignment_transformers_train", ".txt");
        }
    }
    
    //Getter and Setter

    /**
     * Returns the training file generated during multiple calls of the match method.
     * If the match method is not called yet, this will return a non existent file.
     * @return the training file
     */
    public File getTrainingFile() {
        return trainingFile;
    }
    
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
     * Return the boolean value if training examples are additionally changed in their order.
     * If true, the training examples not only contain e.g. A,B but also B,A
     * because positive and negative examples still hold even when the order is changed.
     * This will double the number of training examples.
     * @return true, training examples are additionally changed in their order
     */
    public boolean isAdditionallySwitchSourceTarget() {
        return additionallySwitchSourceTarget;
    }

    /**
     * If set to true, the training examples not only contain e.g. A,B but also B,A
     * because positive and negative example still hold even when the order is changed.
     * This will double the number of training examples.
     * The default is false.
     * @param additionallySwitchSourceTarget true, if source and target should be changed.
     */
    public void setAdditionallySwitchSourceTarget(boolean additionallySwitchSourceTarget) {
        this.additionallySwitchSourceTarget = additionallySwitchSourceTarget;
    }
}
