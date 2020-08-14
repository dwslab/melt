package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MachineLearningScikitFilter extends MatcherYAAAJena {

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
        if(confidenceNames == null || confidenceNames.isEmpty())
            confidenceNames = getConfidenceKeys(trainingAlignment);
        if(confidenceNames.isEmpty()){
            LOGGER.warn("No attributes available for learning. Return unfiltered alignment.");
            return inputAlignment;
        }
        File trainingFile = new File("trainingsFile.csv");
        writeDataset(new ArrayList(trainingAlignment), trainingFile, true);
        
        File testFile = new File("testFile.csv");
        List<Correspondence> testAlignment = new ArrayList(inputAlignment); // make order explicit
        writeDataset(testAlignment, testFile, false);

        List<Integer> prediction = PythonServer.getInstance().learnAndApplyMLModel(trainingFile, testFile, this.crossValidationNumber, this.numberOfParallelJobs);
        
        PythonServer.shutDown();
        trainingFile.delete();
        testFile.delete();
        
        Alignment filteredAlignment = new Alignment(inputAlignment, false);
        if(testAlignment.size() != prediction.size()){
            LOGGER.warn("Size of prediction from scikit learn and test alignemnt do not have the same size. Return unfiltered alignment.");
            return inputAlignment;
        }
        for(int i=0; i < prediction.size(); i++){
            if(prediction.get(i) == 1){
                filteredAlignment.add(testAlignment.get(i));
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
    public void writeDataset(List<Correspondence> alignment, File file, boolean includeTarget) throws IOException{
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
            if(includeTarget)
                LOGGER.info("Created training set with {} positive and {} negative examples ({} attribute(s)).", positive, negative, confidenceNames.size());
        }
    }

    /**
     * Obtain all keys of the additional confidence.
     * @param alignment The alignment from which the additional keys shall be extracted.
     * @return A list of keys that can be used to obtain the set confidences.
     */
    private List<String> getConfidenceKeys(Alignment alignment){
        Set<String> keySet = new HashSet();
        for(Correspondence c : alignment){
            keySet.addAll(c.getAdditionalConfidences().keySet());
        }
        return new ArrayList<>(keySet);
    }
}
