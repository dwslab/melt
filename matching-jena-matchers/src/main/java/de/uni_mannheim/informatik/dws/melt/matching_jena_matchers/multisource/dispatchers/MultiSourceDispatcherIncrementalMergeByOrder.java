package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 * This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 * The order how they are merged is defined by subclasses.
 */
public class MultiSourceDispatcherIncrementalMergeByOrder extends MultiSourceDispatcherIncrementalMerge{
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourceDispatcherIncrementalMergeByOrder.class);
    
    /**
     * the comparator which compares the models and bring them into an order.
     * This order is used dureing merging.
     * It is final because it should not change when using the cache of the superclass.
     */
    private final Comparator<? super ModelAndIndex> comparator;
    
    /**
     * Constructor which requires the one to one matcher as well as the comparator which define es merging order.
     * Some comparators are already defined as public static attributes in this class.
     * @param oneToOneMatcher the one to one matcher.
     * @param comparator the comparator to make the merging order of the models explicit. Some comparators are already defined as public static attributes in this class.
     */
    public MultiSourceDispatcherIncrementalMergeByOrder(Object oneToOneMatcher, Comparator<? super ModelAndIndex> comparator) {
        super(oneToOneMatcher);
        this.comparator = comparator;
    }
    
    /**
     * Constructor which requires an one to one matcher. The merging order id the identity; meaning that the exact same order a given in the list.
     * @param oneToOneMatcher the one to one matcher.
     */
    public MultiSourceDispatcherIncrementalMergeByOrder(Object oneToOneMatcher) {
        this(oneToOneMatcher, IDENTITY);
    }
    
    @Override
    public int[][] getMergeTree(List<Set<Object>> models, Object parameters){
        int numberOfModels = models.size();
        if(numberOfModels < 2){
            LOGGER.warn("Nothing to merge because number of model is less than two.");
            return new int[0][0];
        }
        
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        List<ModelAndIndex> inducedOrder = new ArrayList<>(numberOfModels);
        for(int i = 0; i < numberOfModels; i++){
            inducedOrder.add(new ModelAndIndex(models.get(i), i, p));
        }
        
        if(this.comparator != IDENTITY){
            inducedOrder.sort(this.comparator);
        }
        
        //for(ModelAndIndex i : inducedOrder){
        //    System.out.println(i.getModel(OntModel.class).size());
        //}
        
        int[][] mergeTree = new int[numberOfModels - 1][2];
        
        //merge the first two in the list
        mergeTree[0][0] = inducedOrder.get(0).getIndex();
        mergeTree[0][1] = inducedOrder.get(1).getIndex();
        for(int i = 2; i < models.size(); i++){
            //merge the next in the list with the merged model before
            mergeTree[i - 1][0] = numberOfModels + (i - 2); // the merged model before
            mergeTree[i - 1][1] = inducedOrder.get(i).getIndex();
        }
        
        //JFrame f = new JFrame();
        //f.setSize(new Dimension(1000, 1000));
        //f.setLocationRelativeTo(null);
        //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //f.getContentPane().add(new Dendrogram(clusters.getTree(), clusters.getHeight()).canvas().panel());
        //f.setVisible(true);
        return mergeTree;
    }
    
    
    /**
     * Sorted by the number of triples in a model. This means small models/ontologies/knowledge graphs will be merged first.
     */
    public static final Comparator<ModelAndIndex> MODEL_SIZE_ASCENDING = new Comparator<ModelAndIndex>() {
        @Override
        public int compare(ModelAndIndex left, ModelAndIndex right) {
            Model leftModel = left.getModel(Model.class);
            Model rightModel = right.getModel(Model.class);
            if(leftModel == null || rightModel == null){
                LOGGER.warn("Could not compare models because transformation does not work");
                return 0;
            }
            return Long.compare(leftModel.size(), rightModel.size());
        }
    };
    
    /**
     * Sorted by the number of triples in a model. This means large models/ontologies/knowledge graphs will be merged first.
     */
    public static final Comparator<ModelAndIndex> MODEL_SIZE_DECENDING = MODEL_SIZE_ASCENDING.reversed();
    
    
    public static final Comparator<ModelAndIndex> IDENTITY = Comparator.comparing(ModelAndIndex::getIndex);
    
    public static final Comparator<ModelAndIndex> REVERSED = IDENTITY.reversed();
    
    /**
     * Sorted by the number of classes in a model. This means models/ontologies/knowledge graphs with small number of classes will be merged first.
     */
    public static final Comparator<ModelAndIndex> AMOUNT_OF_CLASSES_ASCENDING = new Comparator<ModelAndIndex>() {
        @Override
        public int compare(ModelAndIndex left, ModelAndIndex right) {
            OntModel leftModel = left.getModel(OntModel.class);
            OntModel rightModel = right.getModel(OntModel.class);
            if(leftModel == null || rightModel == null){
                LOGGER.warn("Could not compare models because transformation does not work");
                return 0;
            }
            return Long.compare(iteratorSize(leftModel.listClasses()), iteratorSize(rightModel.listClasses()));
        }
    };
    
    /**
     * Sorted by the number of classes in a model. This means models/ontologies/knowledge graphs with large number of classes will be merged first.
     */
    public static final Comparator<ModelAndIndex> AMOUNT_OF_CLASSES_DECENDING = AMOUNT_OF_CLASSES_ASCENDING.reversed();
    
    
    /**
     * Sorted by the number of instances in a model. This means models/ontologies/knowledge graphs with small number of instances will be merged first.
     */
    public static final Comparator<ModelAndIndex> AMOUNT_OF_INSTANCES_ASCENDING = new Comparator<ModelAndIndex>() {
        @Override
        public int compare(ModelAndIndex left, ModelAndIndex right) {
            OntModel leftModel = left.getModel(OntModel.class);
            OntModel rightModel = right.getModel(OntModel.class);
            if(leftModel == null || rightModel == null){
                LOGGER.warn("Could not compare models because transformation does not work");
                return 0;
            }
            return Long.compare(iteratorSize(leftModel.listIndividuals()), iteratorSize(rightModel.listIndividuals()));
        }
    };
    
    /**
     * Sorted by the number of instances in a model. This means models/ontologies/knowledge graphs with large number of instances will be merged first.
     */
    public static final Comparator<ModelAndIndex> AMOUNT_OF_INSTANCES_DECENDING = AMOUNT_OF_INSTANCES_ASCENDING.reversed();
    
    
    /**
     * Sorted by the number of unique subjects (of a triple /statement) in a model. Smaller gets merged first.
     */
    public static final Comparator<ModelAndIndex> UNIQUE_SUBJECTS_ASCENDING = new Comparator<ModelAndIndex>() {
        @Override
        public int compare(ModelAndIndex left, ModelAndIndex right) {
            OntModel leftModel = left.getModel(OntModel.class);
            OntModel rightModel = right.getModel(OntModel.class);
            if(leftModel == null || rightModel == null){
                LOGGER.warn("Could not compare models because transformation does not work");
                return 0;
            }
            
            return Long.compare(iteratorSize(leftModel.listSubjects()), iteratorSize(rightModel.listSubjects()));
        }
    };
    
    /**
     * Sorted by the number of unique subjects (of a triple /statement) in a model. Larger gets merged first.
     */
    public static final Comparator<ModelAndIndex> UNIQUE_SUBJECTS_DECENDING = UNIQUE_SUBJECTS_ASCENDING.reversed();
    
    private static long iteratorSize(Iterator<?> i){
        long count = 0;
        while(i.hasNext()){
            i.next();
            count++;
        }
        return count;
    }
}