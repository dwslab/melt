package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge;

import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.AnderbergHierarchicalClustering;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.CLINK;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.HierarchicalClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.PointerHierarchyRepresentationResult;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.SLINK;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.linkage.CentroidLinkage;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.linkage.CompleteLinkage;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.linkage.GroupAverageLinkage;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.linkage.Linkage;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.linkage.MedianLinkage;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.linkage.SingleLinkage;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.linkage.WardLinkage;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.linkage.WeightedAverageLinkage;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.datastore.DBIDDataStore;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.DoubleDataStore;
import de.lmu.ifi.dbs.elki.database.datastore.WritableIntegerDataStore;
import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBIDArrayIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDVar;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.ManhattanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Clusterer based on the ELKI library and always using the Andernberg algorithm.
 */
public class ClustererELKI implements Clusterer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClustererELKI.class);
    
    private boolean alwaysUseAnderberg;

    public ClustererELKI() {
        this(false);
    }
    
    /**
     * Constructor for ELKI which also needs information if anderberg should be always used or if also
     * SLINK and CLINK should be used.
     * @param alwaysUseAnderberg if true, only the anderberg algorithm is used. if false (default), then for single and complete linkage, SLINK/CLINK is used instead.
     */
    public ClustererELKI(boolean alwaysUseAnderberg) {
        this.alwaysUseAnderberg = alwaysUseAnderberg;
    }
    
    
    
    @Override
    public ClusterResult run(double[][] features, ClusterLinkage linkage, ClusterDistance distance) {
        
        //make sure that the DBIDs start from zero (last parameter)
        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(features, null, 0);
        Database db = new StaticArrayDatabase(dbc, null);
        db.initialize();
        
        DistanceFunction<NumberVector> elkiDistance = getDistance(distance);
        Linkage elkiLinkage = getLinkage(linkage);
        
        HierarchicalClusteringAlgorithm clusterer;
        if(alwaysUseAnderberg){
            clusterer = new AnderbergHierarchicalClustering<>(elkiDistance, elkiLinkage);
        }else{
            switch(linkage){
                case SINGLE:
                    clusterer = new SLINK<>(elkiDistance);
                    break;
                case COMPLETE:
                    clusterer = new CLINK<>(elkiDistance);
                    break;
                default:
                    clusterer = new AnderbergHierarchicalClustering<>(elkiDistance, elkiLinkage);
                    break;
            }
        }
        return transformPointerHierarchy(clusterer.run(db));
    }
    
    
    
    public static ClusterResult transformPointerHierarchy(PointerHierarchyRepresentationResult r) {
        DBIDDataStore parent = r.getParentStore();
        DoubleDataStore parentDistance = r.getParentDistanceStore();
        DBIDRange ids = (DBIDRange) r.getDBIDs();
        
        //data structures for the result
        int[][] merges = new int[ids.size()-1][2];
        double[] heights = new double[ids.size()-1];
        
        ArrayDBIDs order = r.topologicalSort();
        WritableIntegerDataStore clusterMap = DataStoreUtil.makeIntegerStorage(order, DataStoreFactory.HINT_TEMP, -1);
        DBIDVar successor = DBIDUtil.newVar();
        int newClusterID = ids.size();
        int counter = 0;
        for (DBIDArrayIter original = order.iter(); original.valid(); original.advance(), counter++) {            
            parent.assignVar(original, successor);
            // No root node
            if (DBIDUtil.equal(original, successor) == false) {
                heights[counter] = parentDistance.doubleValue(original);
                int originalMergeID = clusterMap.intValue(original);
                if(originalMergeID == -1){
                    originalMergeID = ids.getOffset(original);
                }
                
                int successorClusterID = clusterMap.intValue(successor);
                if(successorClusterID == -1){
                    successorClusterID = ids.getOffset(successor);
                }
                clusterMap.put(successor, newClusterID);
                newClusterID++;
                
                merges[counter][0] = originalMergeID;
                merges[counter][1] = successorClusterID;                
            }
        }
        return new ClusterResult(merges, heights);
    }
    

    private DistanceFunction<NumberVector> getDistance(ClusterDistance linkage) {
        switch(linkage){
            case EUCLIDEAN:
                return EuclideanDistanceFunction.STATIC;
            case SQUARED_EUCLIDEAN:
                return SquaredEuclideanDistanceFunction.STATIC;
            case MANHATTAN:
                return ManhattanDistanceFunction.STATIC;
            default:{
                LOGGER.warn("Distance was not found. Defaulting to EUCLIDEAN.");
                return EuclideanDistanceFunction.STATIC;
            }
        }
    }
    
    private Linkage getLinkage(ClusterLinkage linkage) {
        switch (linkage) {
            case SINGLE:
                return SingleLinkage.STATIC;
            case AVERAGE:
                return GroupAverageLinkage.STATIC;
            case COMPLETE:
                return CompleteLinkage.STATIC;
            case CENTROID:
                return CentroidLinkage.STATIC;
            case MEDIAN:
                return MedianLinkage.STATIC;
            case WARD:
                return WardLinkage.STATIC;
            case WPGMA:
                return WeightedAverageLinkage.STATIC;
            default: {
                LOGGER.warn("Linkage was not found. Defaulting to single link.");
                return SingleLinkage.STATIC;
            }
        }
    }
}
