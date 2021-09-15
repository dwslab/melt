package de.uni_mannheim.informatik.dws.melt.multisourceexperiment.analysis;

import de.uni_mannheim.informatik.dws.melt.matching_base.MeltUtil;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.ClusterLinkage;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.ModelAndIndex;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherIncrementalMergeByClusterText;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherIncrementalMergeByOrder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class evaluates which comparison order actually makes a difference for each of the tracks.
 * This results in less runs which needs to be executed.
 */
public class AnalyzeModelOrderDifference {
    static{ System.setProperty("log4j.skipJansi", "false");}
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeModelOrderDifference.class);
    
    public static void main(String[] args) throws MalformedURLException{
        MeltUtil.logWelcomeMessage();
        //need to be set for large bio track
        TrackRepository.Largebio.unlimitEntityExpansion();

        //Track.setCacheFolder(new File("/work-ceph/shertlin/oaei_track_cache"));
        
        analyzeOrderDifference(TrackRepository.Knowledgegraph.V3.getDistinctOntologies());
        //[MODEL_SIZE_DECENDING]
        //[AMOUNT_OF_INSTANCES_DECENDING, UNIQUE_SUBJECTS_DECENDING]
        //[AMOUNT_OF_CLASSES_DECENDING]
        analyzeOrderDifference(TrackRepository.Conference.V1.getDistinctOntologies());
        //[AMOUNT_OF_CLASSES_DECENDING]
        //[AMOUNT_OF_INSTANCES_DECENDING]
        //[MODEL_SIZE_DECENDING, UNIQUE_SUBJECTS_DECENDING]
        //analyzeOrderDifference(TrackRepository.Largebio.V2016.ONLY_WHOLE.getDistinctOntologies());
        //[AMOUNT_OF_INSTANCES_DECENDING]
        //[AMOUNT_OF_CLASSES_DECENDING, MODEL_SIZE_DECENDING, UNIQUE_SUBJECTS_DECENDING]
        
        analyzeClusterSimDifference(TrackRepository.Knowledgegraph.V3.getDistinctOntologies());
        //[COMPLETE, AVERAGE]
        //[SINGLE]        
        analyzeClusterSimDifference(TrackRepository.Conference.V1.getDistinctOntologies());
        //[SINGLE]
        //[AVERAGE]
        //[COMPLETE]
        //analyzeClusterSimDifference(TrackRepository.Largebio.V2016.ONLY_WHOLE.getDistinctOntologies());
        //[COMPLETE, SINGLE, AVERAGE]
        
        //LOGGER.info("conference size: {}", TrackRepository.Conference.V1_ALL_TESTCASES.getDistinctOntologies().size());
        //analyzeOrderDifference(TrackRepository.Conference.V1_ALL_TESTCASES.getDistinctOntologies());
        //[AMOUNT_OF_INSTANCES_DECENDING]
        //[AMOUNT_OF_CLASSES_DECENDING]
        //[UNIQUE_SUBJECTS_DECENDING]
        //[MODEL_SIZE_DECENDING]
        //analyzeClusterSimDifference(TrackRepository.Conference.V1_ALL_TESTCASES.getDistinctOntologies());
        //[AVERAGE]
        //[SINGLE]
        //[COMPLETE]
        
        //List<URL> l = TrackRepository.Knowledgegraph.V3.getDistinctOntologies();
        //for(File f : new File("kgAdd").listFiles()){
        //    l.add(f.toURI().toURL());
        //}
        //analyzeOrderDifference(l);
        //analyzeClusterSimDifference(l);        
    }
    
    public static void analyzeOrderDifference(List<URL> ontologies){
        List<Set<Object>> models = new ArrayList<>();
        for(URL url : ontologies)
            models.add(new HashSet<>(Arrays.asList(url)));
        Map<String, Comparator<ModelAndIndex>> matchOrders = new HashMap<>();
        matchOrders.put("AMOUNT_OF_CLASSES_DECENDING", MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_CLASSES_DECENDING);
        matchOrders.put("UNIQUE_SUBJECTS_DECENDING", MultiSourceDispatcherIncrementalMergeByOrder.UNIQUE_SUBJECTS_DECENDING);
        matchOrders.put("MODEL_SIZE_DECENDING", MultiSourceDispatcherIncrementalMergeByOrder.MODEL_SIZE_DECENDING);
        matchOrders.put("AMOUNT_OF_INSTANCES_DECENDING", MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_INSTANCES_DECENDING);
        
        Map<IntArray, Set<String>> sameOrders = new HashMap<>();
        for(Map.Entry<String, Comparator<ModelAndIndex>> entry : matchOrders.entrySet()){
            int[][] order = new MultiSourceDispatcherIncrementalMergeByOrder(null, entry.getValue()).getMergeTree(models, new Properties());
            sameOrders.computeIfAbsent(new IntArray(order), __->new HashSet<>()).add(entry.getKey());
        }
        
        LOGGER.info("Same orders:");        
        for(Set<String> s : sameOrders.values()){
            LOGGER.info(s.toString());
        }
    }
    
    public static void analyzeClusterSimDifference(List<URL> ontologies){
        List<Set<Object>> models = new ArrayList<>();
        for(URL url : ontologies)
            models.add(new HashSet<>(Arrays.asList(url)));

        Map<IntArray, Set<String>> sameOrders = new HashMap<>();
        for(ClusterLinkage linkage : Arrays.asList(ClusterLinkage.SINGLE, ClusterLinkage.AVERAGE, ClusterLinkage.COMPLETE)){
            int[][] order = new MultiSourceDispatcherIncrementalMergeByClusterText(null, linkage).getMergeTree(models, new Properties());
            sameOrders.computeIfAbsent(new IntArray(order), __->new HashSet<>()).add(linkage.name());
        }
        
        LOGGER.info("Same orders:");        
        for(Set<String> s : sameOrders.values()){
            LOGGER.info(s.toString());
        }
    }
    
    
    public static Map<String, Comparator<ModelAndIndex>> getAllOrders(){
        Map<String, Comparator<ModelAndIndex>> orders = new HashMap<>();
        orders.put("ClassesDescending", MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_CLASSES_DECENDING);
        orders.put("ClassesAscending", MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_CLASSES_ASCENDING);
        
        orders.put("SubjectsDescending", MultiSourceDispatcherIncrementalMergeByOrder.UNIQUE_SUBJECTS_DECENDING);
        orders.put("SubjectsAscending", MultiSourceDispatcherIncrementalMergeByOrder.UNIQUE_SUBJECTS_ASCENDING);
        
        orders.put("ModelSizeDescending", MultiSourceDispatcherIncrementalMergeByOrder.MODEL_SIZE_DECENDING);
        orders.put("ModelSizeAscending", MultiSourceDispatcherIncrementalMergeByOrder.MODEL_SIZE_ASCENDING);
        
        orders.put("InstancesDescending", MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_INSTANCES_DECENDING);
        orders.put("InstancesAscending", MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_INSTANCES_ASCENDING);
        return orders;
    }
    
    public static Map<String, Comparator<ModelAndIndex>> getOrders(Track track){
        Map<String, Comparator<ModelAndIndex>> orders = getAllOrders();
        if(track == TrackRepository.Knowledgegraph.V3 || track == TrackRepository.Conference.V1){
            orders.remove("SubjectsDescending");
            orders.remove("SubjectsAscending");
        }else if(track == TrackRepository.Largebio.V2016.ONLY_WHOLE){
            orders.remove("SubjectsDescending");
            orders.remove("SubjectsAscending");
            
            orders.remove("ModelSizeDescending");
            orders.remove("ModelSizeAscending");
        }else if(track == TrackRepository.Conference.V1_ALL_TESTCASES){
            //do not remove anything - all is different.
        }else{
            throw new UnsupportedOperationException();
        }
        return orders;
    }
    
    public static Set<ClusterLinkage> getAllClusterLinkages(){
        return new HashSet<>(Arrays.asList(ClusterLinkage.SINGLE, ClusterLinkage.AVERAGE, ClusterLinkage.COMPLETE));
    }
    public static Set<ClusterLinkage> getClusterLinkages(Track track){
        if(track == TrackRepository.Knowledgegraph.V3){
            return new HashSet<>(Arrays.asList(ClusterLinkage.SINGLE, ClusterLinkage.AVERAGE)); // ClusterLinkage.COMPLETE is same as average
        }else if(track == TrackRepository.Conference.V1 || track == TrackRepository.Conference.V1_ALL_TESTCASES){
            return new HashSet<>(Arrays.asList(ClusterLinkage.SINGLE, ClusterLinkage.AVERAGE, ClusterLinkage.COMPLETE));
        }else if(track == TrackRepository.Largebio.V2016.ONLY_WHOLE){
            return new HashSet<>(Arrays.asList(ClusterLinkage.SINGLE)); //only one is necessary
        }else{
            throw new UnsupportedOperationException();
        }
    }
}

class IntArray {
    private int[][] array;

    public IntArray(int[][] array) {
        this.array = array;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Arrays.deepHashCode(this.array);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntArray other = (IntArray) obj;
        if (!Arrays.deepEquals(this.array, other.array)) {
            return false;
        }
        return true;
    }
}
