package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter learns and applies a classifier given a training sample and an existing alignment.
 */
public class MachineLearningScikitFilter extends MatcherYAAAJena implements Filter {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MachineLearningScikitFilter.class);
    
    /**
     * Generator for training data. If relation is equivalence, then this is the positive class. 
     * All other relations are the negative class.
     */
    private MatcherYAAAJena trainingGenerator;
    
    /**
     * Which additional confidences should be used to train the classifier.
     */
    private List<String> confidenceNames;
    
    /**
     * Number of cross validation to execute.
     */
    private int crossValidationNumber;
    
    /**
     * Number of jobs to execute in parallel.
     */
    private int numberOfParallelJobs;
    

    public MachineLearningScikitFilter() {
        this(new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                return inputAlignment;
            }
        });
    }
    
    public MachineLearningScikitFilter(Alignment trainingAlignment) {
        this(new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                return trainingAlignment;
            }
        });
    }
    
    public MachineLearningScikitFilter(Alignment trainingAlignment, int crossValidationNumber, int numberOfParallelJobs) {
        this(new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                return trainingAlignment;
            }
        }, null, crossValidationNumber, numberOfParallelJobs);
    }
    
    public MachineLearningScikitFilter(MatcherYAAAJena trainingGenerator) {
        this(trainingGenerator, null);
    }    

    public MachineLearningScikitFilter(MatcherYAAAJena trainingGenerator, List<String> confidenceNames) {
        this(trainingGenerator, confidenceNames, 5, 1);
    }
    
    public MachineLearningScikitFilter(MatcherYAAAJena trainingGenerator, int crossValidationNumber, int numberOfParallelJobs) {
        this(trainingGenerator, null, crossValidationNumber, numberOfParallelJobs);
    }

    /**
     * Constructor
     * @param trainingGenerator generator for training data.
     * @param confidenceNames confidence names to use.
     * @param crossValidationNumber Number of cross validation to execute.
     * @param numberOfParallelJobs Number of jobs to execute in parallel.
     */
    public MachineLearningScikitFilter(MatcherYAAAJena trainingGenerator, List<String> confidenceNames, int crossValidationNumber, int numberOfParallelJobs) {
        this.trainingGenerator = trainingGenerator;
        this.confidenceNames = confidenceNames;
        this.crossValidationNumber = crossValidationNumber;
        this.numberOfParallelJobs = numberOfParallelJobs;
    }

    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        Alignment trainingAlignment = trainingGenerator.match(source, target, inputAlignment, properties);
        return trainAndApplyMLModel(trainingAlignment, inputAlignment, this.confidenceNames, this.crossValidationNumber, this.numberOfParallelJobs);
    }
    
    
    /**
     * Trains a machine learning model in python and applies it to the predictAlignment to filter it.
     * @param trainAlignment Correspondences with an EQUIVALENCE relation are treated as positives. All other relations are treated as negatives.
     * @param predictAlignment the alignment to filter
     * @param confidenceNames the confidence names of the alignment to use (leave empty to use all additional confidences from trainAlignment.
     * @param crossValidationNumber the number of folds when doing a cross validation.
     * @param numberOfParallelJobs number of parallel jobs.
     * @return the filtered alignment
     */
    public static Alignment trainAndApplyMLModel(Alignment trainAlignment, Alignment predictAlignment, List<String> confidenceNames, int crossValidationNumber, int numberOfParallelJobs){
        if(confidenceNames == null || confidenceNames.isEmpty())
            confidenceNames = new ArrayList(trainAlignment.getDistinctCorrespondenceConfidenceKeys());
        if(confidenceNames.isEmpty()){
            LOGGER.warn("No attributes available for learning. Return unfiltered alignment.");
            return predictAlignment;
        }
        try{
            File trainingFile = File.createTempFile("trainingsFile", ".csv");
            writeDataset(new ArrayList(trainAlignment), trainingFile, true, confidenceNames);

            File testFile = File.createTempFile("testFile", ".csv");
            List<Correspondence> testAlignment = new ArrayList(predictAlignment); // make order explicit
            writeDataset(testAlignment, testFile, false, confidenceNames);

            List<Integer> predictions = PythonServer.getInstance().learnAndApplyMLModel(trainingFile, testFile, crossValidationNumber, numberOfParallelJobs);

            trainingFile.delete();
            testFile.delete();
            
            return filterAlignment(predictAlignment, testAlignment, predictions);
        } catch (Exception ex) {
            LOGGER.error("learnAndApplyMLModel failed. Return unfiltered alignment.", ex);
            return predictAlignment;
        }        
    }

    
    /**
     * Trains a machine learning model in python based on the given alignment and then stores the best model in a file.
     * @param alignment Correspondences with an EQUIVALENCE relation are treated as positives. All other relations are treated as negatives.
     * @param modelFile the file to store the best model.
     * @param confidenceNames the confidence names of the alignment to use (leave empty to use all additional confidences from trainAlignment.
     * @param crossValidationNumber the number of folds when doing a cross validation.
     * @param numberOfParallelJobs number of parallel jobs.
     * @return the confidences names which are used (can be directly used as input for confidenceNames in applyStoredMLModel)
     */
    public static List<String> trainAndStoreMLModel(Alignment alignment, File modelFile, List<String> confidenceNames, int crossValidationNumber, int numberOfParallelJobs){
        if(confidenceNames == null || confidenceNames.isEmpty())
            confidenceNames = new ArrayList(alignment.getDistinctCorrespondenceConfidenceKeys());
        if(confidenceNames.isEmpty()){
            LOGGER.warn("No attributes available for learning. Did not create any model file.");
            return confidenceNames;
        }        
        try{
            File trainingFile = File.createTempFile("trainingsFile", ".csv");
            writeDataset(new ArrayList(alignment), trainingFile, true, confidenceNames);
            PythonServer.getInstance().trainAndStoreMLModel(trainingFile, modelFile, crossValidationNumber, numberOfParallelJobs);
            trainingFile.delete();
        } catch (Exception ex) {
            LOGGER.error("trainAndStoreMLModel failed. Did not create any model file.", ex);
        }
        return confidenceNames;
    }
    
    /**
     * Load a machine learning model from a file (trained/generated with trainAndStoreMLModel) and apply it to the alignment which is then filtered.
     * @param modelFile the file to load the ML model.
     * @param predictAlignment the alignment which should be filtered.
     * @param confidenceNames the confidence names of the alignment to use (have to be the same as in training - order has to be the same).
     * @return the filtered alignment.
     */
    public static Alignment applyStoredMLModel(File modelFile, Alignment predictAlignment, List<String> confidenceNames){
        if(modelFile.exists() == false){
            LOGGER.warn("Model file does not exist. Return unfiltered alignment.");
            return predictAlignment;
        }
        if(confidenceNames.isEmpty()){
            
        }
        try{
            File predictFile = new File("testFile.csv");
            List<Correspondence> predictAlignmentOrdered = new ArrayList(predictAlignment); // make order explicit
            writeDataset(predictAlignmentOrdered, predictFile, false, confidenceNames);

            List<Integer> predictions = PythonServer.getInstance().applyStoredMLModel(modelFile, predictFile);

            predictFile.delete();

            return filterAlignment(predictAlignment, predictAlignmentOrdered, predictions);
        } catch (Exception ex) {
            LOGGER.error("applyStoredMLModel failed. Return unfiltered alignment.", ex);
            return predictAlignment;
        }
    }
        
    
    
    private static Alignment filterAlignment(Alignment fullAlignment, List<Correspondence> orderedAlignment, List<Integer> predictions){
        if(orderedAlignment.size() != predictions.size()){
            LOGGER.warn("Size of correspondences and predictions do not have the same size. Return unfiltered alignment.");
            return fullAlignment;
        }
        Alignment filteredAlignment = new Alignment(fullAlignment, false);
        for(int i=0; i < predictions.size(); i++){
            if(predictions.get(i) == 1){//positive class
                filteredAlignment.add(orderedAlignment.get(i));
            }
        }
        return filteredAlignment;
    }
    

    /**
     * Writes the given alignment to a file.
     * @param alignment Dataset to write. Correspondences with an EQUIVALENCE relation are treated as positives.
     *                  All other relations are treated as negatives.
     * @param file File to write.
     * @param includeTarget If true, the label (0 for negatives, 1 for positives) will be persisted.
     * @throws IOException Exception in case of problems while writing.
     */
    private static void writeDataset(List<Correspondence> alignment, File file, boolean includeTarget, List<String> confidenceNames) throws IOException{
        try(CSVPrinter csvPrinter = CSVFormat.DEFAULT.print(file, StandardCharsets.UTF_8)){            
            List<String> header = new ArrayList<>();
            header.addAll(confidenceNames);
            if(includeTarget)
                header.add("target");
            csvPrinter.printRecord(header);
            int positive = 0;
            int negative = 0;
            for(Correspondence c : alignment){
                List<Object> record = new ArrayList(confidenceNames.size());
                for(String confidenceName : confidenceNames){
                    record.add(c.getAdditionalConfidenceOrDefault(confidenceName, 0.0));                    
                }
                if(includeTarget){
                    //positive label is "1" by default in scikit learn
                    //https://scikit-learn.org/stable/modules/model_evaluation.html#from-binary-to-multiclass-and-multilabel
                    if(c.getRelation() == CorrespondenceRelation.EQUIVALENCE){
                        record.add(1); //positive
                        positive++;
                    } else {
                        record.add(0); //negative
                        negative++;
                    }
                }
                csvPrinter.printRecord(record);
            }
            if(includeTarget){
                LOGGER.info("Created training file with {} positive and {} negative examples ({} attribute(s)).", positive, negative, confidenceNames.size());
            } else{
                LOGGER.info("Created predict file with {} examples ({} attribute(s)).", alignment.size(), confidenceNames.size());
            }
        }
    }
    
}
