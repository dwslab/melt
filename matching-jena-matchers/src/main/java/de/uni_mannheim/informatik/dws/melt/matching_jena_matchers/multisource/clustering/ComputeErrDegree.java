package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can compute two things:<br>
 * 1) Communities detected by the <a href="https://arxiv.org/pdf/0803.0476.pdf">Louvrain algorithm</a>.
 * 2) Error degree of an edge as computed by <a href="https://link.springer.com/chapter/10.1007/978-3-030-00671-6_23">Detecting Erroneous Identity Links on the Web Using Network Metrics</a> 
 *    by Raad J., Beek W., van Harmelen F., Pernelle N., Saïs F. (2018).
 * 
 * This class represents a graph of sameas edges. The edges can be added by the addEdge method.
 * After adding some edges, you can call detectCommunities or computeLinkError.
 */
public class ComputeErrDegree <T>{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeErrDegree.class);
    
    private final Map<T, Integer> elementToID;
    private final Map<Integer, T> idToElement;
    private final Map<Integer, Map<Integer,Double>> uniformDirectedEdges;
    private int currentID;
    private int countUndirectedEdges;
    
    
    public ComputeErrDegree(){
        this.elementToID = new TreeMap<>(); //TODO: change to hashmap
        this.idToElement = new HashMap<>();
        this.uniformDirectedEdges = new TreeMap<>();
        this.currentID = 0;
        this.countUndirectedEdges = 0;
    }
    
    void addNodes(List<T> nodes){
        for(T n : nodes){
            getNodeID(n);
        }
    }
    
    public void addEdge(T source, T target){
        addEdge(source, target, 1.0);
    }
    
    public void addEdge(T source, T target, double weight){
        //check for reflexive edge (maps to itself: r(x,x))
        if(source.equals(target)){
            return;
        }
        
        int idSource = getNodeID(source);
        int idTarget = getNodeID(target);

        //make unique direction of edge because we want undirected graph
        if(idSource > idTarget){
            //three way switch of idSource and idTarget
            int tmp = idSource;
            idSource = idTarget;
            idTarget = tmp;
        }

        //add edge to uniformDirectedEdges, possibly sum the edge weights from two directions
        Map<Integer,Double> targetToWeight = uniformDirectedEdges.get(idSource);
        if(targetToWeight == null){
            targetToWeight = new TreeMap<>();
            uniformDirectedEdges.put(idSource, targetToWeight);
        }            
        Double existingWeight = targetToWeight.get(idTarget);
        if(existingWeight == null){
            existingWeight = 0.0;
            countUndirectedEdges++;
        }            
        targetToWeight.put(idTarget, existingWeight + weight);
    }
    
    private int getNodeID(T node){
        Integer id = elementToID.get(node);
        if(id == null){
            id = currentID;
            elementToID.put(node, id);
            idToElement.put(id, node);
            currentID++;
        }
        return id;
    }
    
    /**
     * Detect communities in this graph. Default values for the parameters are used.
     * @return a map from element to corresponding community number
     */
    public Map<T, Integer> detectCommunities(){
        return detectCommunities(1, 1.0, 0, 1, 5, ModularityAlgorithm.LOUVRAIN);
    }
    
    /**
     * Detect communities in this graph.
     * @param algorithm Algorithm for modularity optimization
     * @return a map from element to corresponding community number
     */
    public Map<T, Integer> detectCommunities(ModularityAlgorithm algorithm){
        return detectCommunities(1, 1.0, 0, 1, 5, algorithm);
    }
    
    /**
     * Detect communities in this graph.
     * @param modularityFunction Modularity function (1 = standard; 2 = alternative)
     * @param resolution Use a value of 1.0 for standard modularity-based community detection. Use a value above (below) 1.0 if you want to obtain a larger (smaller) number of communities.
     * @param randomSeed Seed of the random number generator
     * @param nRandomStarts Number of random starts
     * @param nIterations Number of iterations per random start
     * @param algorithm Algorithm for modularity optimization
     * @return a map from element to corresponding community number
     */
    public Map<T, Integer> detectCommunities(int modularityFunction, double resolution, long randomSeed, int nRandomStarts, int nIterations, ModularityAlgorithm algorithm){
        Clustering clustering = computeClustering(modularityFunction, resolution, randomSeed, nRandomStarts, nIterations, algorithm);
        if(clustering == null){
            LOGGER.warn("Return not communities because of clustering==null. Usually because graph is empty?");
            return new HashMap<>();
        }
        return computeElementToCluster(clustering);
    }
    
    
    private Clustering computeClustering(int modularityFunction, double resolution, long randomSeed, int nRandomStarts, int nIterations, ModularityAlgorithm algorithm){
        Network network = getNetwork(modularityFunction);
        
        double resolution2 = ((modularityFunction == 1) ? (resolution / (2 * network.getTotalEdgeWeight() + network.totalEdgeWeightSelfLinks)) : resolution);
        Clustering clustering = null;
        double maxModularity = Double.NEGATIVE_INFINITY;
        Random random = new Random(randomSeed);
        
        for (int i = 0; i < nRandomStarts; i++){
            LOGGER.debug("Random start {}", i + 1);
            VOSClusteringTechnique vosClusteringTechnique = new VOSClusteringTechnique(network, resolution2);
            int j = 0;
            double modularity = 0;
            boolean update = true;
            do
            {
                LOGGER.debug("Iteration {}", j + 1);
                switch(algorithm){
                    case LOUVRAIN:
                        update = vosClusteringTechnique.runLouvainAlgorithm(random);
                        break;
                    case LOUVRAIN_MULTILEVEL:
                        update = vosClusteringTechnique.runLouvainAlgorithmWithMultilevelRefinement(random);
                        break;
                    case SLM:
                        vosClusteringTechnique.runSmartLocalMovingAlgorithm(random);
                }
                j++;
                modularity = vosClusteringTechnique.calcQualityFunction();
                LOGGER.debug("Modularity: {}", modularity);
            }while ((j < nIterations) && update);
            
            if (modularity > maxModularity){
                clustering = vosClusteringTechnique.getClustering();
                maxModularity = modularity;
            }
        }
        return clustering;
    }
    
    
    public Map<Entry<T,T>, Double> computeLinkError(){
        return computeLinkError(1, 1.0, 0, 1, 5, ModularityAlgorithm.LOUVRAIN);
    }
    
    public Map<Entry<T,T>, Double> computeLinkError(ModularityAlgorithm algorithm){
        return computeLinkError(1, 1.0, 0, 1, 5, algorithm);
    }
    
    public Map<Entry<T,T>, Double> computeLinkError(int modularityFunction, double resolution, long randomSeed, int nRandomStarts, int nIterations, ModularityAlgorithm algorithm){
        if(this.elementToID.size() == 2){
            Map<Entry<T,T>, Double> res = new LinkedHashMap<>();
            Iterator<T> it = this.elementToID.keySet().iterator();
            res.put(new SimpleEntry<>(it.next(), it.next()), 0.5);
            
            
            //use treemap to have same order as in original paper
            //res.put(new SimpleEntry(this.elementToID.firstKey(), this.elementToID.lastKey()), 0.5); 
            return res;
        }
        
        Clustering clustering = computeClustering(modularityFunction, resolution, randomSeed, nRandomStarts, nIterations, algorithm);
        if(clustering == null){
            LOGGER.warn("Return not link errors because of clustering==null. Usually because graph is empty?");
            return new HashMap<>();
        }
        
        Map<Integer, List<Integer>> clusterToIDs = computeClusterToID(clustering);
        Map<Integer, Integer> idToCluster = computeIDToCluster(clusterToIDs);
                
        Map<Integer, Double> intraCommEdges = new HashMap<>();
	Map<String, Double> interCommEdges = new TreeMap<>();        
        for(Map.Entry<Integer, Map<Integer,Double>> sourceToMultiTarget : uniformDirectedEdges.entrySet()){
            int sourceCluster = idToCluster.get(sourceToMultiTarget.getKey());
            for(Map.Entry<Integer,Double> targetToWeight : sourceToMultiTarget.getValue().entrySet()){
                int targetCluster = idToCluster.get(targetToWeight.getKey());                
                if(sourceCluster == targetCluster){
                    intraCommEdges.put(sourceCluster, intraCommEdges.getOrDefault(sourceCluster, 0.0) + targetToWeight.getValue());
                }else{
                    String interCommunityKey = targetCluster < sourceCluster ? targetCluster + "-" + sourceCluster : sourceCluster + "-" + targetCluster;
                    interCommEdges.put(interCommunityKey, interCommEdges.getOrDefault(interCommunityKey, 0.0) + targetToWeight.getValue());
                }
            }
        }
        
        // Intra-Links Ranking
        Map<Integer, Double> measureValuesIntra = new TreeMap<>();
        for(Entry<Integer, List<Integer>> cluster: clusterToIDs.entrySet()){
            double E_in = 0;
            int clusterID = cluster.getKey();
            if(intraCommEdges.containsKey(clusterID)){
                E_in = intraCommEdges.get(clusterID);
            }
            double C = cluster.getValue().size();
            double err = 1 - (E_in /(C*(C-1)));
            measureValuesIntra.put(clusterID, err);
        }
        
        // Inter-Links Ranking
        Map<String, Double> measureValuesInter = new TreeMap<>();
        for(Entry<String, Double> interCommEdge: interCommEdges.entrySet()){
            double E_ex = interCommEdge.getValue();
            String[] linkedCommunities = interCommEdge.getKey().split("-");
            double C1 = clusterToIDs.get(Integer.parseInt(linkedCommunities[0])).size();
            double C2 = clusterToIDs.get(Integer.parseInt(linkedCommunities[1])).size();
            double err = 1 - (E_ex /(2*C1*C2));
            measureValuesInter.put(interCommEdge.getKey(), err);
        }
        
        Map<Entry<T,T>, Double> resultMap = new LinkedHashMap<>(); //TODO: change to HashMap
        for(Map.Entry<Integer, Map<Integer,Double>> sourceToMultiTarget : uniformDirectedEdges.entrySet()){
            int sourceCluster = idToCluster.get(sourceToMultiTarget.getKey());
            T sourceElement = idToElement.get(sourceToMultiTarget.getKey());
            for(Map.Entry<Integer,Double> targetToWeight : sourceToMultiTarget.getValue().entrySet()){
                int targetCluster = idToCluster.get(targetToWeight.getKey()); 
                T targetElement = idToElement.get(targetToWeight.getKey());
                if(sourceCluster == targetCluster){
                    double errValue = measureValuesIntra.get(sourceCluster);
                    resultMap.put(new SimpleEntry<>(sourceElement, targetElement), errValue);
                }else{
                    String interCommunityKey = targetCluster < sourceCluster ? targetCluster + "-" + sourceCluster : sourceCluster + "-" + targetCluster;
                    double errValue = measureValuesInter.get(interCommunityKey);
                    resultMap.put(new SimpleEntry<>(sourceElement, targetElement), errValue);
                }
            }
        }
        return resultMap;
    }
    
    
    private Map<Integer, List<Integer>> computeClusterToID(Clustering clustering){
        int nNodes = clustering.getNNodes();
        clustering.orderClustersByNNodes();
        Map<Integer, List<Integer>> clusterToID = new HashMap<>();
        for (int i = 0; i < nNodes; i++){
            Integer clusterNumber = clustering.getCluster(i);
            clusterToID.computeIfAbsent(clusterNumber, __->new ArrayList<>()).add(i);
        }
        return clusterToID;
    }
    
    private Map<Integer, Integer> computeIDToCluster(Map<Integer, List<Integer>> clusterToID){
        Map<Integer, Integer> idToCluster = new HashMap<>();
        for (Entry<Integer, List<Integer>> entry : clusterToID.entrySet()){
            for(Integer id : entry.getValue()){
                idToCluster.put(id, entry.getKey());
            }
        }
        return idToCluster;
    }
    
    
//    private Map<Integer, Integer> computeIDToCluster(Clustering clustering) {
//        int nNodes = clustering.getNNodes();
//        clustering.orderClustersByNNodes();
//        Map<Integer, Integer> idToCluster = new HashMap();
//        for (int i = 0; i < nNodes; i++) {
//            Integer clusterNumber = clustering.getCluster(i);
//            idToCluster.put(i, clusterNumber);
//        }
//        return idToCluster;
//    }
    
    private Map<T, Integer> computeElementToCluster(Clustering clustering){
        int nNodes = clustering.getNNodes();
        clustering.orderClustersByNNodes();
        Map<T, Integer> elementToCluster = new HashMap<>();
        for (int i = 0; i < nNodes; i++){
            Integer clusterNumber = clustering.getCluster(i);
            elementToCluster.put(this.idToElement.get(i), clusterNumber);
        }
        return elementToCluster;
    }
    
    /**
     * Based on the uniformDirectedEdges attribute of the class, computes the internal representation (Network).
     * @param modularityFunction the modularity Function
     * @return the internal representation (Network)
     */
    private Network getNetwork(int modularityFunction){
        int[] node1 = new int[countUndirectedEdges];
        int[] node2 = new int[countUndirectedEdges];
        double[] edgeWeight1 = new double[countUndirectedEdges];
        
        int j=0;
        for(Map.Entry<Integer, Map<Integer,Double>> sourceToMultiTarget : uniformDirectedEdges.entrySet()){
            for(Map.Entry<Integer,Double> targetToWeight : sourceToMultiTarget.getValue().entrySet()){
                node1[j] = sourceToMultiTarget.getKey();
                node2[j] = targetToWeight.getKey();
                edgeWeight1[j] = targetToWeight.getValue();
                j++;
            }
        }
        
        int numberOfNodes = idToElement.size();
        
        int[] nNeighbors = new int[numberOfNodes];
        for (int i = 0; i < countUndirectedEdges; i++){
            if (node1[i] < node2[i]){
                nNeighbors[node1[i]]++;
                nNeighbors[node2[i]]++;
            }
        }
        
        int[] firstNeighborIndex = new int[numberOfNodes + 1];
        int nEdges = 0;
        for (int i = 0; i < numberOfNodes; i++){
                firstNeighborIndex[i] = nEdges;
                nEdges += nNeighbors[i];
        }
        firstNeighborIndex[numberOfNodes] = nEdges;
        
        int[] neighbor = new int[nEdges];
        double[] edgeWeight2 = new double[nEdges];
        Arrays.fill(nNeighbors, 0);
			
        for (int i = 0; i < countUndirectedEdges; i++){
            if (node1[i] < node2[i]){
                j = firstNeighborIndex[node1[i]] + nNeighbors[node1[i]];
                neighbor[j] = node2[i];
                edgeWeight2[j] = edgeWeight1[i];
                nNeighbors[node1[i]]++;
                j = firstNeighborIndex[node2[i]] + nNeighbors[node2[i]];
                neighbor[j] = node1[i];
                edgeWeight2[j] = edgeWeight1[i];
                nNeighbors[node2[i]]++;
	    }
        }
        
        if (modularityFunction == 1){
            return new Network(numberOfNodes, firstNeighborIndex, neighbor, edgeWeight2);
        }else{
            double [] nodeWeight = new double[numberOfNodes];
            Arrays.fill(nodeWeight, 1);
            return new Network(numberOfNodes, nodeWeight, firstNeighborIndex, neighbor, edgeWeight2);
        }
    }
}

//in case one would like to reproduce results from the paper use the following:
/*
public class ComputeLinkError {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputeLinkError.class);
    
    public static void main(String[] args) throws IOException{
        long startTime = System.currentTimeMillis();
        String resultsPath = "links-score_my.tsv";
        // Path for the equivalence classes (the file id2terms.csv can be downloaded from https://zenodo.org/record/3345674)
        String identitySetsPath = "id2terms.csv";
        // Path for the sameAs network (sameAs-Network.hdt can be downloaded from https://zenodo.org/record/1973099)
        String explicitStatementsPath = "sameAs-Network.hdt";
                
        HDT hdt = HDTManager.mapHDT(explicitStatementsPath, null);        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(identitySetsPath)));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultsPath)))){
            
            Pattern uriPattern = Pattern.compile("<(.*?)>");
            String thisLine = "";
            while ((thisLine = br.readLine()) != null){	
                String[] dataArray = thisLine.split(" ", 2);
                String equalitySetID = dataArray[0];
                
                
                List<String> uris = new ArrayList();
                
                //Matcher m = uriPattern.matcher(dataArray[1]);
                //while(m.find()){
                //    uris.add(m.group(1));
                //}
                String[] terms = dataArray[1].split("> ");
                for(int i=0; i < terms.length; i++){
                    String term = terms[i];
                    if(i == terms.length -1){
                        if(term.startsWith("<")){
                            term = term.substring(1);
                            term = term.substring(0, term.length()-1);
			}
                        uris.add(term);
                    }else{
                        if(term.startsWith("<")){
                            uris.add(term.substring(1));
			}else{
                            uris.add(term + ">");
                        }
                    }                    
                }
                
                
                ComputeCommunities<String> cc = new ComputeCommunities<>();
                cc.addNodes(uris); //to keep the number internally the same (should not be neccessary)
                
                for(String uri : uris){
                    try {
                        Iterator<TripleString> it = hdt.search(uri, "", "");
                        while(it.hasNext()) {
                            String object = it.next().getObject().toString();
                            
                            if(uris.contains(object) == false){
                                //search for the right one
                                String bestObject = null;
                                float max = 0;
                                for(String s: uris){
                                    float res = StringMetrics.levenshtein().compare(object, s);
                                    if(res>max){
                                        max = res;
                                        bestObject = s;
                                    }
                                }
                                if(bestObject == null)
                                    continue;
                                object = bestObject;
                            }
                            
                            cc.addEdge(uri, object);
                        }
                    } catch (NotFoundException ex) {
                        LOGGER.warn("URI not found in HDT: {}", uri);
                    }
                }
                
                Map<Entry<String,String>, Double> errorScores = cc.computeLinkError();
                for(Entry<Entry<String,String>, Double> entry : errorScores.entrySet()){
                    float roundedValue = (float) (Math.round(entry.getValue().floatValue()*100.0)/100.0);
                    if(roundedValue < 0)
                        LOGGER.info("test");
                    bw.write(entry.getKey().getKey() + "\t" + entry.getKey().getValue()
							+ "\t" + roundedValue + "\n");
                }
            }
        }
        Long totalTimeMs = (System.currentTimeMillis() - startTime) ;
        System.out.println(String.format("%d min, %d sec", 
                        TimeUnit.MILLISECONDS.toMinutes(totalTimeMs),
                        TimeUnit.MILLISECONDS.toSeconds(totalTimeMs) - 
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTimeMs))));
    }
    
}
*/




/********************************************************************
 * The following code is copied from
 * https://github.com/raadjoe/LOD-Community-Detection/tree/master/src/Communities
 * which is again copied from an unknown source.
 * Thus we do not have any possibility to refer to a maven repository or see the licence.
 ********************************************************************/


/**
 * Clustering
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @version 1.3.1 11/17/14
 */
class Clustering implements Cloneable, Serializable
{
    private static final long serialVersionUID = 1;

    protected int nNodes;
    protected int nClusters;
    protected int[] cluster;

    public static Clustering load(String fileName) throws ClassNotFoundException, IOException
    {
        Clustering clustering;
        ObjectInputStream objectInputStream;

        objectInputStream = new ObjectInputStream(new FileInputStream(fileName));

        clustering = (Clustering)objectInputStream.readObject();

        objectInputStream.close();

        return clustering;
    }

    public Clustering(int nNodes)
    {
        this.nNodes = nNodes;
        cluster = new int[nNodes];
        nClusters = 1;
    }

    public Clustering(int[] cluster)
    {
        nNodes = cluster.length;
        this.cluster = (int[])cluster.clone();
        nClusters = Arrays2.calcMaximum(cluster) + 1;
    }

    public Object clone()
    {
        Clustering clonedClustering;

        try
        {
            clonedClustering = (Clustering)super.clone();
            clonedClustering.cluster = getClusters();
            return clonedClustering;
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }

    public void save(String fileName) throws IOException
    {
        ObjectOutputStream objectOutputStream;

        objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName));

        objectOutputStream.writeObject(this);

        objectOutputStream.close();
    }

    public int getNNodes()
    {
        return nNodes;
    }

    public int getNClusters()
    {
        return nClusters;
    }

    public int[] getClusters()
    {
        return (int[])cluster.clone();
    }

    public int getCluster(int node)
    {
        return cluster[node];
    }

    public int[] getNNodesPerCluster()
    {
        int i;
        int[] nNodesPerCluster;

        nNodesPerCluster = new int[nClusters];
        for (i = 0; i < nNodes; i++)
            nNodesPerCluster[cluster[i]]++;
        return nNodesPerCluster;
    }

    public int[][] getNodesPerCluster()
    {
        int i;
        int[] nNodesPerCluster;
        int[][] nodePerCluster;

        nodePerCluster = new int[nClusters][];
        nNodesPerCluster = getNNodesPerCluster();
        for (i = 0; i < nClusters; i++)
        {
            nodePerCluster[i] = new int[nNodesPerCluster[i]];
            nNodesPerCluster[i] = 0;
        }
        for (i = 0; i < nNodes; i++)
        {
            nodePerCluster[cluster[i]][nNodesPerCluster[cluster[i]]] = i;
            nNodesPerCluster[cluster[i]]++;
        }
        return nodePerCluster;
    }

    public void setCluster(int node, int cluster)
    {
        this.cluster[node] = cluster;
        nClusters = Math.max(nClusters, cluster + 1);
    }

    public void initSingletonClusters()
    {
        int i;

        for (i = 0; i < nNodes; i++)
            cluster[i] = i;
        nClusters = nNodes;
    }

    public void orderClustersByNNodes()
    {
        class ClusterNNodes implements Comparable<ClusterNNodes>
        {
            public int cluster;
            public int nNodes;

            public ClusterNNodes(int cluster, int nNodes)
            {
                this.cluster = cluster;
                this.nNodes = nNodes;
            }

            public int compareTo(ClusterNNodes clusterNNodes)
            {
                return (clusterNNodes.nNodes > nNodes) ? 1 : ((clusterNNodes.nNodes < nNodes) ? -1 : 0);
            }
        }

        ClusterNNodes[] clusterNNodes;
        int i;
        int[] newCluster, nNodesPerCluster;

        nNodesPerCluster = getNNodesPerCluster();
        clusterNNodes = new ClusterNNodes[nClusters];
        for (i = 0; i < nClusters; i++)
            clusterNNodes[i] = new ClusterNNodes(i, nNodesPerCluster[i]);

        Arrays.sort(clusterNNodes);

        newCluster = new int[nClusters];
        i = 0;
        do
        {
            newCluster[clusterNNodes[i].cluster] = i;
            i++;
        }
        while ((i < nClusters) && (clusterNNodes[i].nNodes > 0));
        nClusters = i;
        for (i = 0; i < nNodes; i++)
            cluster[i] = newCluster[cluster[i]];
    }

    public void mergeClusters(Clustering clustering)
    {
        int i;

        for (i = 0; i < nNodes; i++)
            cluster[i] = clustering.cluster[cluster[i]];
        nClusters = clustering.nClusters;
    }
}

class VOSClusteringTechnique
{
    protected Network network;
    protected Clustering clustering;
    protected double resolution;

    public VOSClusteringTechnique(Network network, double resolution)
    {
        this.network = network;
        clustering = new Clustering(network.nNodes);
        clustering.initSingletonClusters();
        this.resolution = resolution;
    }

    public VOSClusteringTechnique(Network network, Clustering clustering, double resolution)
    {
        this.network = network;
        this.clustering = clustering;
        this.resolution = resolution;
    }

    public Network getNetwork()
    {
        return network;
    }

    public Clustering getClustering()
    {
        return clustering;
    }

    public double getResolution()
    {
        return resolution;
    }

    public void setNetwork(Network network)
    {
        this.network = network;
    }

    public void setClustering(Clustering clustering)
    {
        this.clustering = clustering;
    }

    public void setResolution(double resolution)
    {
        this.resolution = resolution;
    }

    public double calcQualityFunction()
    {
        double qualityFunction;
        double[] clusterWeight;
        int i, j, k;

        qualityFunction = 0;

        for (i = 0; i < network.nNodes; i++)
        {
            j = clustering.cluster[i];
            for (k = network.firstNeighborIndex[i]; k < network.firstNeighborIndex[i + 1]; k++)
                if (clustering.cluster[network.neighbor[k]] == j)
                    qualityFunction += network.edgeWeight[k];
        }
        qualityFunction += network.totalEdgeWeightSelfLinks;

        clusterWeight = new double[clustering.nClusters];
        for (i = 0; i < network.nNodes; i++)
            clusterWeight[clustering.cluster[i]] += network.nodeWeight[i];
        for (i = 0; i < clustering.nClusters; i++)
            qualityFunction -= clusterWeight[i] * clusterWeight[i] * resolution;

        qualityFunction /= 2 * network.getTotalEdgeWeight() + network.totalEdgeWeightSelfLinks;

        return qualityFunction;
    }

    public boolean runLocalMovingAlgorithm()
    {
        return runLocalMovingAlgorithm(new Random());
    }

    public boolean runLocalMovingAlgorithm(Random random)
    {
        boolean update;
        double maxQualityFunction, qualityFunction;
        double[] clusterWeight, edgeWeightPerCluster;
        int bestCluster, i, j, k, l, nNeighboringClusters, nStableNodes, nUnusedClusters;
        int[] neighboringCluster, newCluster, nNodesPerCluster, nodePermutation, unusedCluster;

        if (network.nNodes == 1)
            return false;

        update = false;

        clusterWeight = new double[network.nNodes];
        nNodesPerCluster = new int[network.nNodes];
        for (i = 0; i < network.nNodes; i++)
        {
            clusterWeight[clustering.cluster[i]] += network.nodeWeight[i];
            nNodesPerCluster[clustering.cluster[i]]++;
        }

        nUnusedClusters = 0;
        unusedCluster = new int[network.nNodes];
        for (i = 0; i < network.nNodes; i++)
            if (nNodesPerCluster[i] == 0)
            {
                unusedCluster[nUnusedClusters] = i;
                nUnusedClusters++;
            }

        nodePermutation = Arrays2.generateRandomPermutation(network.nNodes, random);

        edgeWeightPerCluster = new double[network.nNodes];
        neighboringCluster = new int[network.nNodes - 1];
        nStableNodes = 0;
        i = 0;
        do
        {
            j = nodePermutation[i];

            nNeighboringClusters = 0;
            for (k = network.firstNeighborIndex[j]; k < network.firstNeighborIndex[j + 1]; k++)
            {
                l = clustering.cluster[network.neighbor[k]];
                if (edgeWeightPerCluster[l] == 0)
                {
                    neighboringCluster[nNeighboringClusters] = l;
                    nNeighboringClusters++;
                }
                edgeWeightPerCluster[l] += network.edgeWeight[k];
            }

            clusterWeight[clustering.cluster[j]] -= network.nodeWeight[j];
            nNodesPerCluster[clustering.cluster[j]]--;
            if (nNodesPerCluster[clustering.cluster[j]] == 0)
            {
                unusedCluster[nUnusedClusters] = clustering.cluster[j];
                nUnusedClusters++;
            }

            bestCluster = -1;
            maxQualityFunction = 0;
            for (k = 0; k < nNeighboringClusters; k++)
            {
                l = neighboringCluster[k];
                qualityFunction = edgeWeightPerCluster[l] - network.nodeWeight[j] * clusterWeight[l] * resolution;
                if ((qualityFunction > maxQualityFunction) || ((qualityFunction == maxQualityFunction) && (l < bestCluster)))
                {
                    bestCluster = l;
                    maxQualityFunction = qualityFunction;
                }
                edgeWeightPerCluster[l] = 0;
            }
            if (maxQualityFunction == 0)
            {
                bestCluster = unusedCluster[nUnusedClusters - 1];
                nUnusedClusters--;
            }

            clusterWeight[bestCluster] += network.nodeWeight[j];
            nNodesPerCluster[bestCluster]++;
            if (bestCluster == clustering.cluster[j])
                nStableNodes++;
            else
            {
                clustering.cluster[j] = bestCluster;
                nStableNodes = 1;
                update = true;
            }

            i = (i < network.nNodes - 1) ? (i + 1) : 0;
        }
        while (nStableNodes < network.nNodes);

        newCluster = new int[network.nNodes];
        clustering.nClusters = 0;
        for (i = 0; i < network.nNodes; i++)
            if (nNodesPerCluster[i] > 0)
            {
                newCluster[i] = clustering.nClusters;
                clustering.nClusters++;
            }
        for (i = 0; i < network.nNodes; i++)
            clustering.cluster[i] = newCluster[clustering.cluster[i]];

        return update;
    }

    public boolean runLouvainAlgorithm()
    {
        return runLouvainAlgorithm(new Random());
    }

    public boolean runLouvainAlgorithm(Random random)
    {
        boolean update, update2;
        VOSClusteringTechnique VOSClusteringTechnique;

        if (network.nNodes == 1)
            return false;

        update = runLocalMovingAlgorithm(random);

        if (clustering.nClusters < network.nNodes)
        {
            VOSClusteringTechnique = new VOSClusteringTechnique(network.createReducedNetwork(clustering), resolution);

            update2 = VOSClusteringTechnique.runLouvainAlgorithm(random);

            if (update2)
            {
                update = true;

                clustering.mergeClusters(VOSClusteringTechnique.clustering);
            }
        }

        return update;
    }

    public boolean runIteratedLouvainAlgorithm(int maxNIterations)
    {
        return runIteratedLouvainAlgorithm(maxNIterations, new Random());
    }

    public boolean runIteratedLouvainAlgorithm(int maxNIterations, Random random)
    {
        boolean update;
        int i;

        i = 0;
        do
        {
            update = runLouvainAlgorithm(random);
            i++;
        }
        while ((i < maxNIterations) && update);
        return ((i > 1) || update);
    }

    public boolean runLouvainAlgorithmWithMultilevelRefinement()
    {
        return runLouvainAlgorithmWithMultilevelRefinement(new Random());
    }

    public boolean runLouvainAlgorithmWithMultilevelRefinement(Random random)
    {
        boolean update, update2;
        VOSClusteringTechnique VOSClusteringTechnique;

        if (network.nNodes == 1)
            return false;

        update = runLocalMovingAlgorithm(random);

        if (clustering.nClusters < network.nNodes)
        {
            VOSClusteringTechnique = new VOSClusteringTechnique(network.createReducedNetwork(clustering), resolution);

            update2 = VOSClusteringTechnique.runLouvainAlgorithmWithMultilevelRefinement(random);

            if (update2)
            {
                update = true;

                clustering.mergeClusters(VOSClusteringTechnique.clustering);

                runLocalMovingAlgorithm(random);
            }
        }

        return update;
    }

    public boolean runIteratedLouvainAlgorithmWithMultilevelRefinement(int maxNIterations)
    {
        return runIteratedLouvainAlgorithmWithMultilevelRefinement(maxNIterations, new Random());
    }

    public boolean runIteratedLouvainAlgorithmWithMultilevelRefinement(int maxNIterations, Random random)
    {
        boolean update;
        int i;

        i = 0;
        do
        {
            update = runLouvainAlgorithmWithMultilevelRefinement(random);
            i++;
        }
        while ((i < maxNIterations) && update);
        return ((i > 1) || update);
    }

    public boolean runSmartLocalMovingAlgorithm()
    {
        return runSmartLocalMovingAlgorithm(new Random());
    }

    public boolean runSmartLocalMovingAlgorithm(Random random)
    {
        boolean update;
        int i, j, k;
        int[] nNodesPerClusterReducedNetwork;
        int[][] nodePerCluster;
        Network[] subnetwork;
        VOSClusteringTechnique VOSClusteringTechnique;

        if (network.nNodes == 1)
            return false;

        update = runLocalMovingAlgorithm(random);

        if (clustering.nClusters < network.nNodes)
        {
            subnetwork = network.createSubnetworks(clustering);

            nodePerCluster = clustering.getNodesPerCluster();

            clustering.nClusters = 0;
            nNodesPerClusterReducedNetwork = new int[subnetwork.length];
            for (i = 0; i < subnetwork.length; i++)
            {
                VOSClusteringTechnique = new VOSClusteringTechnique(subnetwork[i], resolution);

                VOSClusteringTechnique.runLocalMovingAlgorithm(random);

                for (j = 0; j < subnetwork[i].nNodes; j++)
                    clustering.cluster[nodePerCluster[i][j]] = clustering.nClusters + VOSClusteringTechnique.clustering.cluster[j];
                clustering.nClusters += VOSClusteringTechnique.clustering.nClusters;
                nNodesPerClusterReducedNetwork[i] = VOSClusteringTechnique.clustering.nClusters;
            }

            VOSClusteringTechnique = new VOSClusteringTechnique(network.createReducedNetwork(clustering), resolution);

            i = 0;
            for (j = 0; j < nNodesPerClusterReducedNetwork.length; j++)
                for (k = 0; k < nNodesPerClusterReducedNetwork[j]; k++)
                {
                    VOSClusteringTechnique.clustering.cluster[i] = j;
                    i++;
                }
            VOSClusteringTechnique.clustering.nClusters = nNodesPerClusterReducedNetwork.length;

            update |= VOSClusteringTechnique.runSmartLocalMovingAlgorithm(random);

            clustering.mergeClusters(VOSClusteringTechnique.clustering);
        }

        return update;
    }

    public boolean runIteratedSmartLocalMovingAlgorithm(int nIterations)
    {
        return runIteratedSmartLocalMovingAlgorithm(nIterations, new Random());
    }

    public boolean runIteratedSmartLocalMovingAlgorithm(int nIterations, Random random)
    {
        boolean update;
        int i;

        update = false;
        for (i = 0; i < nIterations; i++)
            update |= runSmartLocalMovingAlgorithm(random);
        return update;
    }

    public int removeCluster(int cluster)
    {
        double maxQualityFunction, qualityFunction;
        double[] clusterWeight, totalEdgeWeightPerCluster;
        int i, j;

        clusterWeight = new double[clustering.nClusters];
        totalEdgeWeightPerCluster = new double[clustering.nClusters];
        for (i = 0; i < network.nNodes; i++)
        {
            clusterWeight[clustering.cluster[i]] += network.nodeWeight[i];
            if (clustering.cluster[i] == cluster)
                for (j = network.firstNeighborIndex[i]; j < network.firstNeighborIndex[i + 1]; j++)
                    totalEdgeWeightPerCluster[clustering.cluster[network.neighbor[j]]] += network.edgeWeight[j];
        }

        i = -1;
        maxQualityFunction = 0;
        for (j = 0; j < clustering.nClusters; j++)
            if ((j != cluster) && (clusterWeight[j] > 0))
            {
                qualityFunction = totalEdgeWeightPerCluster[j] / clusterWeight[j];
                if (qualityFunction > maxQualityFunction)
                {
                    i = j;
                    maxQualityFunction = qualityFunction;
                }
            }

        if (i >= 0)
        {
            for (j = 0; j < network.nNodes; j++)
                if (clustering.cluster[j] == cluster)
                    clustering.cluster[j] = i;
            if (cluster == clustering.nClusters - 1)
                clustering.nClusters = Arrays2.calcMaximum(clustering.cluster) + 1;
        }

        return i;
    }

    public void removeSmallClusters(int minNNodesPerCluster)
    {
        int i, j, k;
        int[] nNodesPerCluster;
        VOSClusteringTechnique VOSClusteringTechnique;

        VOSClusteringTechnique = new VOSClusteringTechnique(network.createReducedNetwork(clustering), resolution);

        nNodesPerCluster = clustering.getNNodesPerCluster();

        do
        {
            i = -1;
            j = minNNodesPerCluster;
            for (k = 0; k < VOSClusteringTechnique.clustering.nClusters; k++)
                if ((nNodesPerCluster[k] > 0) && (nNodesPerCluster[k] < j))
                {
                    i = k;
                    j = nNodesPerCluster[k];
                }

            if (i >= 0)
            {
                j = VOSClusteringTechnique.removeCluster(i);
                if (j >= 0)
                    nNodesPerCluster[j] += nNodesPerCluster[i];
                nNodesPerCluster[i] = 0;
            }
        }
        while (i >= 0);

        clustering.mergeClusters(VOSClusteringTechnique.clustering);
    }
}

/**
 * Network
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * , 08/30/15
 */
class Network implements Serializable
{
    private static final long serialVersionUID = 1;

    protected int nNodes;
    protected int nEdges;
    protected double[] nodeWeight;
    protected int[] firstNeighborIndex;
    protected int[] neighbor;
    protected double[] edgeWeight;
    protected double totalEdgeWeightSelfLinks;

    public static Network load(String fileName) throws ClassNotFoundException, IOException
    {
        Network network;
        ObjectInputStream objectInputStream;

        objectInputStream = new ObjectInputStream(new FileInputStream(fileName));

        network = (Network)objectInputStream.readObject();

        objectInputStream.close();

        return network;
    }

    public Network(int nNodes, int[][] edge)
    {
        this(nNodes, null, edge, null);
    }

    public Network(int nNodes, double[] nodeWeight, int[][] edge)
    {
        this(nNodes, nodeWeight, edge, null);
    }

    public Network(int nNodes, int[][] edge, double[] edgeWeight)
    {
        this(nNodes, null, edge, edgeWeight);
    }

    public Network(int nNodes, double[] nodeWeight, int[][] edge, double[] edgeWeight)
    {
        double[] edgeWeight2;
        int i, j;
        int[] neighbor;

        this.nNodes = nNodes;

        nEdges = 0;
        firstNeighborIndex = new int[nNodes + 1];
        neighbor = new int[edge[0].length];
        edgeWeight2 = new double[edge[0].length];
        totalEdgeWeightSelfLinks = 0;
        i = 1;
        for (j = 0; j < edge[0].length; j++)
            if (edge[0][j] != edge[1][j])
            {
                if (edge[0][j] >= i)
                    for (; i <= edge[0][j]; i++)
                        firstNeighborIndex[i] = nEdges;
                neighbor[nEdges] = edge[1][j];
                edgeWeight2[nEdges] = (edgeWeight != null) ? edgeWeight[j] : 1;
                nEdges++;
            }
            else
                totalEdgeWeightSelfLinks += (edgeWeight != null) ? edgeWeight[j] : 1;
        for (; i <= nNodes; i++)
            firstNeighborIndex[i] = nEdges;
        this.neighbor = Arrays.copyOfRange(neighbor, 0, nEdges);
        this.edgeWeight = Arrays.copyOfRange(edgeWeight2, 0, nEdges);

        this.nodeWeight = (nodeWeight != null) ? (double[])nodeWeight.clone() : getTotalEdgeWeightPerNode();
    }

    public Network(int nNodes, int[] firstNeighborIndex, int[] neighbor)
    {
        this(nNodes, null, firstNeighborIndex, neighbor, null);
    }

    public Network(int nNodes, double[] nodeWeight, int[] firstNeighborIndex, int[] neighbor)
    {
        this(nNodes, nodeWeight, firstNeighborIndex, neighbor, null);
    }

    public Network(int nNodes, int[] firstNeighborIndex, int[] neighbor, double[] edgeWeight)
    {
        this(nNodes, null, firstNeighborIndex, neighbor, edgeWeight);
    }

    public Network(int nNodes, double[] nodeWeight, int[] firstNeighborIndex, int[] neighbor, double[] edgeWeight)
    {
        this.nNodes = nNodes;

        nEdges = neighbor.length;
        this.firstNeighborIndex = (int[])firstNeighborIndex.clone();
        this.neighbor = (int[])neighbor.clone();
        if (edgeWeight != null)
            this.edgeWeight = (double[])edgeWeight.clone();
        else
        {
            this.edgeWeight = new double[nEdges];
            Arrays.fill(this.edgeWeight, 1);
        }
        totalEdgeWeightSelfLinks = 0;

        this.nodeWeight = (nodeWeight != null) ? (double[])nodeWeight.clone() : getTotalEdgeWeightPerNode();
    }

    public void save(String fileName) throws IOException
    {
        ObjectOutputStream objectOutputStream;

        objectOutputStream = new ObjectOutputStream(new FileOutputStream(fileName));

        objectOutputStream.writeObject(this);

        objectOutputStream.close();
    }

    public int getNNodes()
    {
        return nNodes;
    }

    public double getTotalNodeWeight()
    {
        return Arrays2.calcSum(nodeWeight);
    }

    public double[] getNodeWeights()
    {
        return (double[])nodeWeight.clone();
    }

    public double getNodeWeight(int node)
    {
        return nodeWeight[node];
    }

    public int getNEdges()
    {
        return nEdges / 2;
    }

    public int getNEdges(int node)
    {
        return firstNeighborIndex[node + 1] - firstNeighborIndex[node];
    }

    public int[] getNEdgesPerNode()
    {
        int i;
        int[] nEdgesPerNode;

        nEdgesPerNode = new int[nNodes];
        for (i = 0; i < nNodes; i++)
            nEdgesPerNode[i] = firstNeighborIndex[i + 1] - firstNeighborIndex[i];
        return nEdgesPerNode;
    }

    public int[][] getEdges()
    {
        int i;
        int[][] edge;

        edge = new int[2][];
        edge[0] = new int[nEdges];
        for (i = 0; i < nNodes; i++)
            Arrays.fill(edge[0], firstNeighborIndex[i], firstNeighborIndex[i + 1], i);
        edge[1] = (int[])neighbor.clone();
        return edge;
    }

    public int[] getEdges(int node)
    {
        return Arrays.copyOfRange(neighbor, firstNeighborIndex[node], firstNeighborIndex[node + 1]);
    }

    public int[][] getEdgesPerNode()
    {
        int i;
        int[][] edgePerNode;

        edgePerNode = new int[nNodes][];
        for (i = 0; i < nNodes; i++)
            edgePerNode[i] = Arrays.copyOfRange(neighbor, firstNeighborIndex[i], firstNeighborIndex[i + 1]);
        return edgePerNode;
    }

    public double getTotalEdgeWeight()
    {
        return Arrays2.calcSum(edgeWeight) / 2;
    }

    public double getTotalEdgeWeight(int node)
    {
        return Arrays2.calcSum(edgeWeight, firstNeighborIndex[node], firstNeighborIndex[node + 1]);
    }

    public double[] getTotalEdgeWeightPerNode()
    {
        double[] totalEdgeWeightPerNode;
        int i;

        totalEdgeWeightPerNode = new double[nNodes];
        for (i = 0; i < nNodes; i++)
            totalEdgeWeightPerNode[i] = Arrays2.calcSum(edgeWeight, firstNeighborIndex[i], firstNeighborIndex[i + 1]);
        return totalEdgeWeightPerNode;
    }

    public double[] getEdgeWeights()
    {
        return (double[])edgeWeight.clone();
    }

    public double[] getEdgeWeights(int node)
    {
        return Arrays.copyOfRange(edgeWeight, firstNeighborIndex[node], firstNeighborIndex[node + 1]);
    }

    public double[][] getEdgeWeightsPerNode()
    {
        double[][] edgeWeightPerNode;
        int i;

        edgeWeightPerNode = new double[nNodes][];
        for (i = 0; i < nNodes; i++)
            edgeWeightPerNode[i] = Arrays.copyOfRange(edgeWeight, firstNeighborIndex[i], firstNeighborIndex[i + 1]);
        return edgeWeightPerNode;
    }

    public double getTotalEdgeWeightSelfLinks()
    {
        return totalEdgeWeightSelfLinks;
    }

    public Network createNetworkWithoutNodeWeights()
    {
        Network networkWithoutNodeWeights;

        networkWithoutNodeWeights = new Network();
        networkWithoutNodeWeights.nNodes = nNodes;
        networkWithoutNodeWeights.nEdges = nEdges;
        networkWithoutNodeWeights.nodeWeight = new double[nNodes];
        Arrays.fill(networkWithoutNodeWeights.nodeWeight, 1);
        networkWithoutNodeWeights.firstNeighborIndex = firstNeighborIndex;
        networkWithoutNodeWeights.neighbor = neighbor;
        networkWithoutNodeWeights.edgeWeight = edgeWeight;
        networkWithoutNodeWeights.totalEdgeWeightSelfLinks = totalEdgeWeightSelfLinks;
        return networkWithoutNodeWeights;
    }

    public Network createNetworkWithoutEdgeWeights()
    {
        Network networkWithoutEdgeWeights;

        networkWithoutEdgeWeights = new Network();
        networkWithoutEdgeWeights.nNodes = nNodes;
        networkWithoutEdgeWeights.nEdges = nEdges;
        networkWithoutEdgeWeights.nodeWeight = nodeWeight;
        networkWithoutEdgeWeights.firstNeighborIndex = firstNeighborIndex;
        networkWithoutEdgeWeights.neighbor = neighbor;
        networkWithoutEdgeWeights.edgeWeight = new double[nEdges];
        Arrays.fill(networkWithoutEdgeWeights.edgeWeight, 1);
        networkWithoutEdgeWeights.totalEdgeWeightSelfLinks = 0;
        return networkWithoutEdgeWeights;
    }

    public Network createNetworkWithoutNodeAndEdgeWeights()
    {
        Network networkWithoutNodeAndEdgeWeights;

        networkWithoutNodeAndEdgeWeights = new Network();
        networkWithoutNodeAndEdgeWeights.nNodes = nNodes;
        networkWithoutNodeAndEdgeWeights.nEdges = nEdges;
        networkWithoutNodeAndEdgeWeights.nodeWeight = new double[nNodes];
        Arrays.fill(networkWithoutNodeAndEdgeWeights.nodeWeight, 1);
        networkWithoutNodeAndEdgeWeights.firstNeighborIndex = firstNeighborIndex;
        networkWithoutNodeAndEdgeWeights.neighbor = neighbor;
        networkWithoutNodeAndEdgeWeights.edgeWeight = new double[nEdges];
        Arrays.fill(networkWithoutNodeAndEdgeWeights.edgeWeight, 1);
        networkWithoutNodeAndEdgeWeights.totalEdgeWeightSelfLinks = 0;
        return networkWithoutNodeAndEdgeWeights;
    }

    public Network createNormalizedNetwork1()
    {
        double totalNodeWeight;
        int i, j;
        Network normalizedNetwork;

        normalizedNetwork = new Network();

        normalizedNetwork.nNodes = nNodes;
        normalizedNetwork.nEdges = nEdges;
        normalizedNetwork.nodeWeight = new double[nNodes];
        Arrays.fill(normalizedNetwork.nodeWeight, 1);
        normalizedNetwork.firstNeighborIndex = firstNeighborIndex;
        normalizedNetwork.neighbor = neighbor;

        normalizedNetwork.edgeWeight = new double[nEdges];
        totalNodeWeight = getTotalNodeWeight();
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndex[i]; j < firstNeighborIndex[i + 1]; j++)
                normalizedNetwork.edgeWeight[j] = edgeWeight[j] / ((nodeWeight[i] * nodeWeight[neighbor[j]]) / totalNodeWeight);

        normalizedNetwork.totalEdgeWeightSelfLinks = 0;

        return normalizedNetwork;
    }

    public Network createNormalizedNetwork2()
    {
        int i, j;
        Network normalizedNetwork;

        normalizedNetwork = new Network();

        normalizedNetwork.nNodes = nNodes;
        normalizedNetwork.nEdges = nEdges;
        normalizedNetwork.nodeWeight = new double[nNodes];
        Arrays.fill(normalizedNetwork.nodeWeight, 1);
        normalizedNetwork.firstNeighborIndex = firstNeighborIndex;
        normalizedNetwork.neighbor = neighbor;

        normalizedNetwork.edgeWeight = new double[nEdges];
        for (i = 0; i < nNodes; i++)
            for (j = firstNeighborIndex[i]; j < firstNeighborIndex[i + 1]; j++)
                normalizedNetwork.edgeWeight[j] = edgeWeight[j] / (2 / (nNodes / nodeWeight[i] + nNodes / nodeWeight[neighbor[j]]));

        normalizedNetwork.totalEdgeWeightSelfLinks = 0;

        return normalizedNetwork;
    }

    public Network createPrunedNetwork(int nEdges)
    {
        return createPrunedNetwork(nEdges, new Random());
    }

    public Network createPrunedNetwork(int nEdges, Random random)
    {
        double edgeWeightThreshold, randomNumberThreshold;
        double[] edgeWeight, randomNumber;
        int i, j, k, nEdgesAboveThreshold, nEdgesAtThreshold;
        int[] nodePermutation;
        Network prunedNetwork;

        nEdges *= 2;

        if (nEdges >= this.nEdges)
            return this;

        edgeWeight = new double[this.nEdges / 2];
        i = 0;
        for (j = 0; j < nNodes; j++)
            for (k = firstNeighborIndex[j]; k < firstNeighborIndex[j + 1]; k++)
                if (neighbor[k] < j)
                {
                    edgeWeight[i] = this.edgeWeight[k];
                    i++;
                }
        Arrays.sort(edgeWeight);
        edgeWeightThreshold = edgeWeight[(this.nEdges - nEdges) / 2];

        nEdgesAboveThreshold = 0;
        while (edgeWeight[this.nEdges / 2 - nEdgesAboveThreshold - 1] > edgeWeightThreshold)
            nEdgesAboveThreshold++;
        nEdgesAtThreshold = 0;
        while ((nEdgesAboveThreshold + nEdgesAtThreshold < this.nEdges / 2) && (edgeWeight[this.nEdges / 2 - nEdgesAboveThreshold - nEdgesAtThreshold - 1] == edgeWeightThreshold))
            nEdgesAtThreshold++;

        nodePermutation = Arrays2.generateRandomPermutation(nNodes, random);

        randomNumber = new double[nEdgesAtThreshold];
        i = 0;
        for (j = 0; j < nNodes; j++)
            for (k = firstNeighborIndex[j]; k < firstNeighborIndex[j + 1]; k++)
                if ((neighbor[k] < j) && (this.edgeWeight[k] == edgeWeightThreshold))
                {
                    randomNumber[i] = generateRandomNumber(j, neighbor[k], nodePermutation);
                    i++;
                }
        Arrays.sort(randomNumber);
        randomNumberThreshold = randomNumber[nEdgesAboveThreshold + nEdgesAtThreshold - nEdges / 2];

        prunedNetwork = new Network();

        prunedNetwork.nNodes = nNodes;
        prunedNetwork.nEdges = nEdges;
        prunedNetwork.nodeWeight = nodeWeight;

        prunedNetwork.firstNeighborIndex = new int[nNodes + 1];
        prunedNetwork.neighbor = new int[nEdges];
        prunedNetwork.edgeWeight = new double[nEdges];
        i = 0;
        for (j = 0; j < nNodes; j++)
        {
            for (k = firstNeighborIndex[j]; k < firstNeighborIndex[j + 1]; k++)
                if ((this.edgeWeight[k] > edgeWeightThreshold) || ((this.edgeWeight[k] == edgeWeightThreshold) && (generateRandomNumber(j, neighbor[k], nodePermutation) >= randomNumberThreshold)))
                {
                    prunedNetwork.neighbor[i] = neighbor[k];
                    prunedNetwork.edgeWeight[i] = this.edgeWeight[k];
                    i++;
                }
            prunedNetwork.firstNeighborIndex[j + 1] = i;
        }

        prunedNetwork.totalEdgeWeightSelfLinks = totalEdgeWeightSelfLinks;

        return prunedNetwork;
    }

    public Network createSubnetwork(int[] node)
    {
        double[] subnetworkEdgeWeight;
        int i, j, k;
        int[] subnetworkNode, subnetworkNeighbor;
        Network subnetwork;

        subnetwork = new Network();

        subnetwork.nNodes = node.length;

        if (subnetwork.nNodes == 1)
        {
            subnetwork.nEdges = 0;
            subnetwork.nodeWeight = new double[] {nodeWeight[node[0]]};
            subnetwork.firstNeighborIndex = new int[2];
            subnetwork.neighbor = new int[0];
            subnetwork.edgeWeight = new double[0];
        }
        else
        {
            subnetworkNode = new int[nNodes];
            Arrays.fill(subnetworkNode, -1);
            for (i = 0; i < node.length; i++)
                subnetworkNode[node[i]] = i;

            subnetwork.nEdges = 0;
            subnetwork.nodeWeight = new double[subnetwork.nNodes];
            subnetwork.firstNeighborIndex = new int[subnetwork.nNodes + 1];
            subnetworkNeighbor = new int[nEdges];
            subnetworkEdgeWeight = new double[nEdges];
            for (i = 0; i < subnetwork.nNodes; i++)
            {
                j = node[i];
                subnetwork.nodeWeight[i] = nodeWeight[j];
                for (k = firstNeighborIndex[j]; k < firstNeighborIndex[j + 1]; k++)
                    if (subnetworkNode[neighbor[k]] >= 0)
                    {
                        subnetworkNeighbor[subnetwork.nEdges] = subnetworkNode[neighbor[k]];
                        subnetworkEdgeWeight[subnetwork.nEdges] = edgeWeight[k];
                        subnetwork.nEdges++;
                    }
                subnetwork.firstNeighborIndex[i + 1] = subnetwork.nEdges;
            }
            subnetwork.neighbor = Arrays.copyOfRange(subnetworkNeighbor, 0, subnetwork.nEdges);
            subnetwork.edgeWeight = Arrays.copyOfRange(subnetworkEdgeWeight, 0, subnetwork.nEdges);
        }

        subnetwork.totalEdgeWeightSelfLinks = 0;

        return subnetwork;
    }

    public Network createSubnetwork(boolean[] nodeInSubnetwork)
    {
        int i, j;
        int[] node;

        i = 0;
        for (j = 0; j < nNodes; j++)
            if (nodeInSubnetwork[j])
                i++;
        node = new int[i];
        i = 0;
        for (j = 0; j < nNodes; j++)
            if (nodeInSubnetwork[j])
            {
                node[i] = j;
                i++;
            }
        return createSubnetwork(node);
    }

    public Network createSubnetwork(Clustering clustering, int cluster)
    {
        double[] subnetworkEdgeWeight;
        int[] subnetworkNeighbor, subnetworkNode;
        int[][] nodePerCluster;
        Network subnetwork;

        nodePerCluster = clustering.getNodesPerCluster();
        subnetworkNode = new int[nNodes];
        subnetworkNeighbor = new int[nEdges];
        subnetworkEdgeWeight = new double[nEdges];
        subnetwork = createSubnetwork(clustering, cluster, nodePerCluster[cluster], subnetworkNode, subnetworkNeighbor, subnetworkEdgeWeight);
        return subnetwork;
    }

    public Network[] createSubnetworks(Clustering clustering)
    {
        double[] subnetworkEdgeWeight;
        int i;
        int[] subnetworkNeighbor, subnetworkNode;
        int[][] nodePerCluster;
        Network[] subnetwork;

        subnetwork = new Network[clustering.nClusters];
        nodePerCluster = clustering.getNodesPerCluster();
        subnetworkNode = new int[nNodes];
        subnetworkNeighbor = new int[nEdges];
        subnetworkEdgeWeight = new double[nEdges];
        for (i = 0; i < clustering.nClusters; i++)
            subnetwork[i] = createSubnetwork(clustering, i, nodePerCluster[i], subnetworkNode, subnetworkNeighbor, subnetworkEdgeWeight);
        return subnetwork;
    }

    public Network createSubnetworkLargestComponent()
    {
        return createSubnetwork(identifyComponents(), 0);
    }

    public Network createReducedNetwork(Clustering clustering)
    {
        double[] reducedNetworkEdgeWeight1, reducedNetworkEdgeWeight2;
        int i, j, k, l, m, n;
        int[] reducedNetworkNeighbor1, reducedNetworkNeighbor2;
        int[][] nodePerCluster;
        Network reducedNetwork;

        reducedNetwork = new Network();

        reducedNetwork.nNodes = clustering.nClusters;

        reducedNetwork.nEdges = 0;
        reducedNetwork.nodeWeight = new double[clustering.nClusters];
        reducedNetwork.firstNeighborIndex = new int[clustering.nClusters + 1];
        reducedNetwork.totalEdgeWeightSelfLinks = totalEdgeWeightSelfLinks;
        reducedNetworkNeighbor1 = new int[nEdges];
        reducedNetworkEdgeWeight1 = new double[nEdges];
        reducedNetworkNeighbor2 = new int[clustering.nClusters - 1];
        reducedNetworkEdgeWeight2 = new double[clustering.nClusters];
        nodePerCluster = clustering.getNodesPerCluster();
        for (i = 0; i < clustering.nClusters; i++)
        {
            j = 0;
            for (k = 0; k < nodePerCluster[i].length; k++)
            {
                l = nodePerCluster[i][k];

                reducedNetwork.nodeWeight[i] += nodeWeight[l];

                for (m = firstNeighborIndex[l]; m < firstNeighborIndex[l + 1]; m++)
                {
                    n = clustering.cluster[neighbor[m]];
                    if (n != i)
                    {
                        if (reducedNetworkEdgeWeight2[n] == 0)
                        {
                            reducedNetworkNeighbor2[j] = n;
                            j++;
                        }
                        reducedNetworkEdgeWeight2[n] += edgeWeight[m];
                    }
                    else
                        reducedNetwork.totalEdgeWeightSelfLinks += edgeWeight[m];
                }
            }

            for (k = 0; k < j; k++)
            {
                reducedNetworkNeighbor1[reducedNetwork.nEdges + k] = reducedNetworkNeighbor2[k];
                reducedNetworkEdgeWeight1[reducedNetwork.nEdges + k] = reducedNetworkEdgeWeight2[reducedNetworkNeighbor2[k]];
                reducedNetworkEdgeWeight2[reducedNetworkNeighbor2[k]] = 0;
            }
            reducedNetwork.nEdges += j;
            reducedNetwork.firstNeighborIndex[i + 1] = reducedNetwork.nEdges;
        }
        reducedNetwork.neighbor = Arrays.copyOfRange(reducedNetworkNeighbor1, 0, reducedNetwork.nEdges);
        reducedNetwork.edgeWeight = Arrays.copyOfRange(reducedNetworkEdgeWeight1, 0, reducedNetwork.nEdges);

        return reducedNetwork;
    }

    public Clustering identifyComponents()
    {
        boolean[] nodeVisited;
        Clustering clustering;
        int i, j, k, l;
        int[] node;

        clustering = new Clustering(nNodes);

        clustering.nClusters = 0;
        nodeVisited = new boolean[nNodes];
        node = new int[nNodes];
        for (i = 0; i < nNodes; i++)
            if (!nodeVisited[i])
            {
                clustering.cluster[i] = clustering.nClusters;
                nodeVisited[i] = true;
                node[0] = i;
                j = 1;
                k = 0;
                do
                {
                    for (l = firstNeighborIndex[node[k]]; l < firstNeighborIndex[node[k] + 1]; l++)
                        if (!nodeVisited[neighbor[l]])
                        {
                            clustering.cluster[neighbor[l]] = clustering.nClusters;
                            nodeVisited[neighbor[l]] = true;
                            node[j] = neighbor[l];
                            j++;
                        }
                    k++;
                }
                while (k < j);

                clustering.nClusters++;
            }

        clustering.orderClustersByNNodes();

        return clustering;
    }

    private Network()
    {
    }

    private double generateRandomNumber(int node1, int node2, int[] nodePermutation)
    {
        int i, j;
        Random random;

        if (node1 < node2)
        {
            i = node1;
            j = node2;
        }
        else
        {
            i = node2;
            j = node1;
        }
        random = new Random(nodePermutation[i] * nNodes + nodePermutation[j]);
        return random.nextDouble();
    }

    private Network createSubnetwork(Clustering clustering, int cluster, int[] node, int[] subnetworkNode, int[] subnetworkNeighbor, double[] subnetworkEdgeWeight)
    {
        int i, j, k;
        Network subnetwork;

        subnetwork = new Network();

        subnetwork.nNodes = node.length;

        if (subnetwork.nNodes == 1)
        {
            subnetwork.nEdges = 0;
            subnetwork.nodeWeight = new double[] {nodeWeight[node[0]]};
            subnetwork.firstNeighborIndex = new int[2];
            subnetwork.neighbor = new int[0];
            subnetwork.edgeWeight = new double[0];
        }
        else
        {
            for (i = 0; i < node.length; i++)
                subnetworkNode[node[i]] = i;

            subnetwork.nEdges = 0;
            subnetwork.nodeWeight = new double[subnetwork.nNodes];
            subnetwork.firstNeighborIndex = new int[subnetwork.nNodes + 1];
            for (i = 0; i < subnetwork.nNodes; i++)
            {
                j = node[i];
                subnetwork.nodeWeight[i] = nodeWeight[j];
                for (k = firstNeighborIndex[j]; k < firstNeighborIndex[j + 1]; k++)
                    if (clustering.cluster[neighbor[k]] == cluster)
                    {
                        subnetworkNeighbor[subnetwork.nEdges] = subnetworkNode[neighbor[k]];
                        subnetworkEdgeWeight[subnetwork.nEdges] = edgeWeight[k];
                        subnetwork.nEdges++;
                    }
                subnetwork.firstNeighborIndex[i + 1] = subnetwork.nEdges;
            }
            subnetwork.neighbor = Arrays.copyOfRange(subnetworkNeighbor, 0, subnetwork.nEdges);
            subnetwork.edgeWeight = Arrays.copyOfRange(subnetworkEdgeWeight, 0, subnetwork.nEdges);
        }

        subnetwork.totalEdgeWeightSelfLinks = 0;

        return subnetwork;
    }
}

/**
 * Arrays2
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 * @version 1.3.1, 11/17/14
 */
class Arrays2
{
    public static double calcSum(double[] value)
    {
        double sum;
        int i;

        sum = 0;
        for (i = 0; i < value.length; i++)
            sum += value[i];
        return sum;
    }

    public static double calcSum(double[] value, int beginIndex, int endIndex)
    {
        double sum;
        int i;

        sum = 0;
        for (i = beginIndex; i < endIndex; i++)
            sum += value[i];
        return sum;
    }

    public static double calcAverage(double[] value)
    {
        double average;
        int i;

        average = 0;
        for (i = 0; i < value.length; i++)
            average += value[i];
        average /= value.length;
        return average;
    }

    public static double calcMedian(double[] value)
    {
        double median;
        double[] sortedValue;

        sortedValue = (double[])value.clone();
        Arrays.sort(sortedValue);
        if (sortedValue.length % 2 == 1)
            median = sortedValue[(sortedValue.length - 1) / 2];
        else
            median = (sortedValue[sortedValue.length / 2 - 1] + sortedValue[sortedValue.length / 2]) / 2;
        return median;
    }

    public static double calcMinimum(double[] value)
    {
        double minimum;
        int i;

        minimum = value[0];
        for (i = 1; i < value.length; i++)
            minimum = Math.min(minimum, value[i]);
        return minimum;
    }

    public static double calcMaximum(double[] value)
    {
        double maximum;
        int i;

        maximum = value[0];
        for (i = 1; i < value.length; i++)
            maximum = Math.max(maximum, value[i]);
        return maximum;
    }

    public static int calcMaximum(int[] value)
    {
        int i, maximum;

        maximum = value[0];
        for (i = 1; i < value.length; i++)
            maximum = Math.max(maximum, value[i]);
        return maximum;
    }

    public static int[] generateRandomPermutation(int nElements)
    {
        return generateRandomPermutation(nElements, new Random());
    }

    public static int[] generateRandomPermutation(int nElements, Random random)
    {
        int i, j, k;
        int[] permutation;

        permutation = new int[nElements];
        for (i = 0; i < nElements; i++)
            permutation[i] = i;
        for (i = 0; i < nElements; i++)
        {
            j = random.nextInt(nElements);
            k = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = k;
        }
        return permutation;
    }
}

