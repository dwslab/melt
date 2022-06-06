package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import java.io.File;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServerException;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This matcher uses the <a href="https://github.com/UKPLab/sentence-transformers">Sentence Transformers library</a> to build an embedding space for each resource given a textual representation of it.
 * Thus this matcher does not filter anything but generates matching candidates based on the text.
 */
public class SentenceTransformersFineTuner extends TransformersBaseFineTuner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceTransformersFineTuner.class);
    
    private static final String NEWLINE = System.getProperty("line.separator");
    
    /**
     * A number between zero and one which represents the  proportion of the data to include in the test split.
     */
    private float testSize;
    
    private int trainBatchSize;
    private int testBatchSize;
    
    private int numberOfEpochs;
    
    private SentenceTransformersLoss loss;
    
    //private boolean additionallySwitchNegativesForTripletLoss;
    
    /**
     * Run the training of a NLP sentence transformers.
     * @param extractor used to extract text from a given resource. This is the text which represents a resource.
     * @param initialModelName the initial model name for fine tuning which can be downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>). This value can be also changed by {@link #setModelName(java.lang.String) }.
     * @param resultingModelLocation the final location where the fine-tuned model should be stored.
     */
    public SentenceTransformersFineTuner(TextExtractorMap extractor, String initialModelName, File resultingModelLocation) {
        super(extractor, initialModelName, resultingModelLocation);
        this.testSize = 0.33f;
        this.trainBatchSize = 64;
        this.testBatchSize = 32;
        this.numberOfEpochs = 5;
        this.loss = SentenceTransformersLoss.CosineSimilarityLoss;
    }
    
    /**
     * Run the training of a NLP sentence transformers.
     * @param extractor used to extract text from a given resource. This is the text which represents a resource.
     * @param initialModelName the initial model name for fine tuning which can be downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>). This value can be also changed by {@link #setModelName(java.lang.String) }.
     * @param resultingModelLocation the final location where the fine-tuned model should be stored.
     */
    public SentenceTransformersFineTuner(TextExtractor extractor, String initialModelName, File resultingModelLocation) {
        this(TextExtractorMap.wrapTextExtractor(extractor), initialModelName, resultingModelLocation);
    }
    

    @Override
    public File finetuneModel(File trainingFile) throws PythonServerException {        
        PythonServer.getInstance().sentenceTransformersFineTuning(this, trainingFile, null);
        return this.resultingModelLocation;
    }
    

    /**
     * Run the training on the training file, but evaluate the best model on the validationFile.
     * The model will be stored at <code>resultingModelLocation</code> given in the constructor.
     * @param trainingFile the training file to use
     * (can be generated with {@link TransformersBaseFineTuner#createTrainingFile(OntModel, OntModel, Alignment) }
     * @param validationFile the validation file to use 
     * (can be generated with {@link TransformersBaseFineTuner#createTrainingFile(OntModel, OntModel, Alignment) }
     * @return the best score of the validation (using the file or train test split)
     * @throws PythonServerException in case of some error during the learning
     */
    public float finetuneModel(File trainingFile, File validationFile) throws PythonServerException {
        return PythonServer.getInstance().sentenceTransformersFineTuning(this, trainingFile, validationFile);
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
    @Override
    public int writeTrainingFile(OntModel source, OntModel target, Alignment trainingAlignment, File trainFile, boolean append) throws IOException{
        switch (this.loss) {
            case CosineSimilarityLoss:
                return writeClassificationFormat(source, target, trainingAlignment, trainFile, append);
            case MultipleNegativesRankingLoss:
                return writeTripletFormat(source, target, trainingAlignment, trainFile, append);
            default:
                throw new IOException("Loss is not recognized");
        }
    }
    
    private int writeTripletFormat(OntModel source, OntModel target, Alignment trainingAlignment, File trainFile, boolean append) throws IOException{
        int positiveCorrespondencesNotUsed = 0;
        int positiveCorrespondences = 0;
        int examples = 0;
        Map<Resource,Map<String, Set<String>>> cache = new HashMap<>();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile, append), StandardCharsets.UTF_8))){
            
            for(Correspondence posCorrespondence : trainingAlignment.getCorrespondencesRelation(CorrespondenceRelation.EQUIVALENCE)){
                positiveCorrespondences++;
                int examplesForThisPositiveCorrespondence = 0;
                Resource posOne = source.getResource(posCorrespondence.getEntityOne());
                Resource posTwo = target.getResource(posCorrespondence.getEntityTwo());
                for(Correspondence neg : trainingAlignment.getCorrespondencesSourceRelation(posCorrespondence.getEntityOne(), CorrespondenceRelation.INCOMPAT)){
                    Resource hardNegative = target.getResource(neg.getEntityTwo());
                    examplesForThisPositiveCorrespondence += writeOneTriplet(posOne, posTwo, hardNegative, cache, writer);
                    if(this.additionallySwitchSourceTarget){//if(this.additionallySwitchNegativesForTripletLoss){
                        examplesForThisPositiveCorrespondence += writeOneTriplet(posTwo, posOne, hardNegative, cache, writer);
                    }
                }
                if(this.additionallySwitchSourceTarget){
                    for(Correspondence neg : trainingAlignment.getCorrespondencesTargetRelation(posCorrespondence.getEntityTwo(), CorrespondenceRelation.INCOMPAT)){
                        Resource hardNegative = target.getResource(neg.getEntityOne());
                        examplesForThisPositiveCorrespondence += writeOneTriplet(posTwo, posOne, hardNegative, cache, writer);
                        //if(this.additionallySwitchNegativesForTripletLoss){
                            examplesForThisPositiveCorrespondence += writeOneTriplet(posOne, posTwo, hardNegative, cache, writer);
                        //}
                    }
                }
                if(examplesForThisPositiveCorrespondence == 0){
                    positiveCorrespondencesNotUsed++;
                }
                examples += examplesForThisPositiveCorrespondence;
            }
        }
        LOGGER.info("Wrote {} triplet training EXAMPLES. "
                + "The initial ALIGNMENT contains {} positive correspondences. "
                + "{} of those correspondences are not used due to insufficient textual data or"
                + " non existent negatives (the negatives should use the INCOMPAT relation)."
                ,examples, positiveCorrespondences, positiveCorrespondencesNotUsed);
        return examples;
    }
    
    private int writeOneTriplet(Resource anchor, Resource positive, Resource hardNegative, Map<Resource,Map<String, Set<String>>> cache, Writer writer) throws IOException{
        int writtenExamples = 0;
        Map<String, Set<String>> anchorGroups = getTextualRepresentation(anchor, cache);
        Map<String, Set<String>> positiveGroups = getTextualRepresentation(positive, cache);
        Map<String, Set<String>> hardNegativeGroups = getTextualRepresentation(hardNegative, cache);
        
        for(Entry<String, Set<String>> anchorGroup : anchorGroups.entrySet()){
            for(String positiveText : positiveGroups.get(anchorGroup.getKey())){
                for(String hardNegativeText : hardNegativeGroups.get(anchorGroup.getKey())){
                    for(String anchorText : anchorGroup.getValue()){
                        writtenExamples++;
                        writer.write(StringEscapeUtils.escapeCsv(anchorText) + "," + 
                                StringEscapeUtils.escapeCsv(positiveText) +  "," + 
                                StringEscapeUtils.escapeCsv(hardNegativeText) + NEWLINE);
                        
                    }
                }
            }
        }
        return writtenExamples;
    }
    
    /**
     * This class does not allow setting training argumnets. Everything is determined by attributes.
     * @param trainingArguments training arguments
     */
    @Override
    public void setTrainingArguments(TransformersTrainerArguments trainingArguments) {
        throw new IllegalArgumentException("Training arguments are not used in SentenceTransformersFineTuner.");
    }

    /**
     * This class only allows to set this value to false.
     * @param usingTensorflow should be set to false-
     */
    @Override
    public void setUsingTensorflow(boolean usingTensorflow) {
        if(usingTensorflow){
            throw new IllegalArgumentException("SentenceTransformersFineTuner only work with Pytorch. Do not set usingTensorflow to true.");
        }
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

    public int getTrainBatchSize() {
        return trainBatchSize;
    }

    public void setTrainBatchSize(int trainBatchSize) {
        this.trainBatchSize = trainBatchSize;
    }

    public int getTestBatchSize() {
        return testBatchSize;
    }

    public void setTestBatchSize(int testBatchSize) {
        this.testBatchSize = testBatchSize;
    }    

    public int getNumberOfEpochs() {
        return numberOfEpochs;
    }

    public void setNumberOfEpochs(int numberOfEpochs) {
        this.numberOfEpochs = numberOfEpochs;
    }

    public SentenceTransformersLoss getLoss() {
        return loss;
    }

    public void setLoss(SentenceTransformersLoss loss) {
        this.loss = loss;
    }
}
