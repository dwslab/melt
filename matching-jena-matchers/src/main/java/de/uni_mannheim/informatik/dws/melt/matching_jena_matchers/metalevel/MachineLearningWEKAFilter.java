package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

/**
 * Non functional code.
 */
public class MachineLearningWEKAFilter extends MatcherYAAAJena {
    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MachineLearningWEKAFilter.class);
    
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
    
    
    
    
    public MachineLearningWEKAFilter() {
        this(new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                return inputAlignment;
            }
        });
    }
    
    public MachineLearningWEKAFilter(Alignment trainingAlignment) {
        this(new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                return trainingAlignment;
            }
        });
    }
    
    public MachineLearningWEKAFilter(Alignment trainingAlignment, int crossValidationNumber, int numberOfParallelJobs) {
        this(new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                return trainingAlignment;
            }
        }, null, crossValidationNumber, numberOfParallelJobs);
    }
    
    public MachineLearningWEKAFilter(MatcherYAAAJena trainingGenerator) {
        this(trainingGenerator, null);
    }    

    public MachineLearningWEKAFilter(MatcherYAAAJena trainingGenerator, List<String> confidenceNames) {
        this(trainingGenerator, confidenceNames, 5, 1);
    }
    
    public MachineLearningWEKAFilter(MatcherYAAAJena trainingGenerator, int crossValidationNumber, int numberOfParallelJobs) {
        this(trainingGenerator, null, crossValidationNumber, numberOfParallelJobs);
    }

    /**
     * Constructor
     * @param trainingGenerator generator for training data.
     * @param confidenceNames confidence names to use.
     * @param crossValidationNumber Number of cross validation to execute.
     * @param numberOfParallelJobs Number of jobs to execute in parallel.
     */
    public MachineLearningWEKAFilter(MatcherYAAAJena trainingGenerator, List<String> confidenceNames, int crossValidationNumber, int numberOfParallelJobs) {
        this.trainingGenerator = trainingGenerator;
        this.confidenceNames = confidenceNames;
        this.crossValidationNumber = crossValidationNumber;
        this.numberOfParallelJobs = numberOfParallelJobs;
    }

    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        //Alignment trainingAlignment = trainingGenerator.match(source, target, inputAlignment, properties);
        //File model = trainModel(trainingAlignment);
        //return applyModel(model, inputAlignment);
        return inputAlignment;
    }
    
    
    public File trainModel(Alignment trainingAlignment) throws Exception{
        
        Instances trainingInstances = getTrainingInstances(trainingAlignment);
        
        //10 fold cross validation
        
        int folds = 10;
        int seed = 1324;
        Random rand = new Random(seed);
        Instances randData = new Instances(trainingInstances);
        randData.randomize(rand);
        randData.stratify(folds);
        
        Classifier cls = new J48();
        
        // perform cross-validation
        Evaluation eval = new Evaluation(randData);
        for (int n = 0; n < folds; n++) {
          Instances train = randData.trainCV(folds, n, rand);
          Instances test = randData.testCV(folds, n);
          // the above code is used by the StratifiedRemoveFolds filter, the
          // code below by the Explorer/Experimenter:
          // Instances train = randData.trainCV(folds, n, rand);

          // build and evaluate classifier
          Classifier clsCopy = AbstractClassifier.makeCopy(cls);
          clsCopy.buildClassifier(train);
          eval.evaluateModel(clsCopy, test);
        }
        
        //eval.crossValidateModel(cls, randData, folds, rand);

        
        return null;
    }
    
    public Alignment applyModel(File model, Alignment alignment){
        List<Correspondence> predictAlignmentOrdered = new ArrayList(alignment); // make order explicit
        Instances wekaTestInstances = getTestInstances(predictAlignmentOrdered);

        //TODO: load model from file and predict wekaTestInstances
        
        
        //set confidence of correspondence which is the confidence of the model
        List<Double> predictions = Arrays.asList();
        
        if(predictAlignmentOrdered.size() != predictions.size()){
            LOGGER.warn("Size of correspondences and predictions do not have the same size. Return unfiltered alignment.");
            return alignment;
        }
        Alignment filteredAlignment = new Alignment(alignment, false);
        for(int i=0; i < predictions.size(); i++){
            /*
            if(predictions.get(i) == 1){//positive class
                filteredAlignment.add(orderedAlignment.get(i));
            }
            */
        }        
        
        return null;
    }
    
    /**
     * Generates the weka instances which can be used for training a model .
     * @param alignment Dataset to write. Correspondences with an EQUIVALENCE relation are treated as positives.
     *                  All other relations are treated as negatives.
     * @return the weka instances object
     */
    public Instances getTrainingInstances(Alignment alignment){
        if(confidenceNames == null || confidenceNames.isEmpty()){
            confidenceNames = new ArrayList(alignment.getDistinctCorrespondenceConfidenceKeys());
            LOGGER.info("Confidence named used for traning the model, are all set to all available confidences in the training alignment.");
        }        
        if(confidenceNames.isEmpty()){
            LOGGER.warn("No confidences are available for learning. Returning empty dataset.");
            return new Instances("Empy", new ArrayList(), 0);
        }
        
        // 1. set up attributes
        ArrayList<Attribute> attributes = new ArrayList<>();
        for(String confidenceName : confidenceNames){
            attributes.add(new Attribute(confidenceName));
        }
        attributes.add(new Attribute("Class", Arrays.asList("negative", "positive")));
        
        // 2. create Instances object
        Instances dataset = new Instances("Train" , attributes, alignment.size());
        dataset.setClassIndex(confidenceNames.size());
        
        // 3. fill with data
        int positive = 0;
        int negative = 0;

        for(Correspondence c : alignment){
            double[] vals = new double[confidenceNames.size() + 1];
            for (int i = 0; i < confidenceNames.size(); i++) {
                vals[i] = c.getAdditionalConfidence(confidenceNames.get(i));
            }
            if(c.getRelation() == CorrespondenceRelation.EQUIVALENCE){
                vals[confidenceNames.size()] = 1.0; // index position - "positive"
                positive++;
            }else{
                vals[confidenceNames.size()] = 0.0;// index position - "negative"
                negative++;
            }
            dataset.add(new DenseInstance(1.0, vals));
        }
        LOGGER.info("Created training set with {} positive and {} negative examples ({} attribute(s)).", positive, negative, confidenceNames.size());

        return dataset;
    }
    
    /**
     * Generates the weka instances which can be used for predicting unseen examples.
     * @param alignment the correspondences which should be predicted.
     * @return the weka instances object
     */
    public Instances getTestInstances(Collection<Correspondence> alignment){
        if((confidenceNames == null || confidenceNames.isEmpty())){
            LOGGER.warn("No confidences are available for learning. Returning empty dataset.");
            return new Instances("Empy", new ArrayList(), 0);
        }
        
        // 1. set up attributes
        ArrayList<Attribute> attributes = new ArrayList<>();
        for(String confidenceName : confidenceNames){
            attributes.add(new Attribute(confidenceName));
        }
        
        // 2. create Instances object
        Instances dataset = new Instances("Test", attributes, alignment.size());
        for(Correspondence c : alignment){
            double[] vals = new double[confidenceNames.size()];
            for (int i = 0; i < confidenceNames.size(); i++) {
                vals[i] = c.getAdditionalConfidence(confidenceNames.get(i));
            }
            dataset.add(new DenseInstance(1.0, vals));
        }
        LOGGER.info("Created test set with {} examples ({} attribute(s)).", alignment.size(), confidenceNames.size());
        return dataset;
    }
    
    
       
    /**
     * Helper method to write an arff formatted file.
     * @param data the instances to write to file
     * @param file the file object
     */
    public void writeArffFile(Instances data, File file){
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))){
            // header
            writer.write(new Instances(data, 0).toString());
            writer.newLine();
            // rows
            for (int i = 0; i < data.numInstances(); i++) {
              writer.write(data.instance(i).toString());
              writer.newLine();
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not write Arff file.", ex);
        }
    }
    
    
    /*
    visualize decison tree
        // train classifier
        J48 cls = new J48();
        cls.buildClassifier(trainingInstances);

        // display classifier
        final javax.swing.JFrame jf = new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
        jf.setSize(500,400);
        jf.getContentPane().setLayout(new BorderLayout());
        TreeVisualizer tv = new TreeVisualizer(null, cls.graph(),new PlaceNode2());
        jf.getContentPane().add(tv, BorderLayout.CENTER);
        jf.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent e) {
            jf.dispose();
          }
        });
        jf.setVisible(true);
        tv.fitToScreen();
        */
    
    
    /*
    private List<Classifier> getClassifiers(){
        List<Classifier> classifiers = new ArrayList();
        
        LibSVM svm = new LibSVM();
        for()
        svm.setOptions(options);
        
        //for()
        //LibSVM
        //classifiers.add(e)
    }
    
    
    
    class WekaModel{
        
        private Classifier cls;
        private String[] hyperparamter;

        public WekaModel(Classifier cls, String[] hyperparamter) {
            this.cls = cls;
            this.hyperparamter = hyperparamter;
        }
        public WekaModel(Classifier cls, String hyperparamter) {
            this.cls = cls;
            this.hyperparamter = Utils.splitOptions(hyperparamter);
        }        
        
        public Classifier getClassifier(){
            
            //Utils.forName(Classifier.class, "J48", "-U")
            //cls.se
            
            if ((cls instanceof OptionHandler)) {
              ((OptionHandler) cls).setOptions(hyperparamter);
            }
        }
    }
*/

    public List<String> getConfidenceNames() {
        return confidenceNames;
    }
    
}
