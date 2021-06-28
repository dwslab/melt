package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MultiSourceDispatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_jena.JenaHelper;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TdbUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena.multisource.IndexBasedJenaMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.JenaTransformerHelper;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 * This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 * The order how they are merged is defined by subclasses.
 */
public abstract class MultiSourceDispatcherIncrementalMerge extends MatcherMultiSourceURL implements MultiSourceDispatcher, IMatcherMultiSourceCaller{
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourceDispatcherIncrementalMerge.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();    
    
    private final Object oneToOneMatcher;
    private final Map<List<Set<Object>>, int[][]> mergeTreeCache;
    
    private boolean useCacheForMergeTree;
    private boolean addInformationToUnion;
    private boolean lowMemoryOverhead;
    private List<Alignment> intermediateAlignments;

    /**
     * Constructor which expects the actual one to one matcher and a boolean if the cache should be used.
     * If true, then check that the subclasses do not change a parameter (make them final) which would change the result of getMergeTree.
     * @param oneToOneMatcher ont to one matcher
     * @param addInformationToUnion if true all information from matched entities are in the union.
     */
    public MultiSourceDispatcherIncrementalMerge(Object oneToOneMatcher, boolean addInformationToUnion) {
        this.oneToOneMatcher = oneToOneMatcher;
        this.useCacheForMergeTree = true;// default is to cache merge tree
        this.mergeTreeCache = new HashMap<>();
        this.addInformationToUnion = addInformationToUnion;
        this.intermediateAlignments = null; // default is not to save intermediate alignments
        this.lowMemoryOverhead = false;
    }
    
    public MultiSourceDispatcherIncrementalMerge(Object oneToOneMatcher) {
        this(oneToOneMatcher, true);
    }
    
    @Override
    public URL match(List<URL> models, URL inputAlignment, URL parameters) throws Exception {        
        List<Set<Object>> list = new ArrayList<>(models.size());
        for(URL ontology : models){
            list.add(new HashSet<>(Arrays.asList(ontology)));
        }
        AlignmentAndParameters alignmentAndPrameters = match(list, inputAlignment, parameters);
        return TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), URL.class);
    }
    
    @Override
    public boolean needsTransitiveClosureForEvaluation() { return true; } 
    
    @Override
    public AlignmentAndParameters match(List<Set<Object>> models, Object inputAlignment, Object parameters) throws Exception {
        int[][] mergingTree;
        if(useCacheForMergeTree){
            mergingTree = mergeTreeCache.get(models);
            if(mergingTree == null){
                LOGGER.debug("Compute mergeTree");
                mergingTree = getMergeTree(models, parameters);
                if(mergingTree == null){
                    LOGGER.warn("Merging tree is null. Please check subclasses, expecially what they return at method getMergeTree. Returning input alignment.");
                    return new AlignmentAndParameters(inputAlignment, parameters);
                }else{
                    LOGGER.debug("Put mergeTree into cache.");
                    mergeTreeCache.put(models, mergingTree);
                }
            }else{
                LOGGER.debug("Use cached mergedTree.");
            }
        }else{
            mergingTree = getMergeTree(models, parameters);
            if(mergingTree == null){
                LOGGER.warn("Merging tree is null. Please check subclasses, expecially what they return at method getMergeTree. Returning input alignment.");
                return new AlignmentAndParameters(inputAlignment, parameters);
            }
        }
        
        List<Set<Object>> mergedOntologies = new ArrayList<>();
        
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        
        int n = models.size();
        if(mergingTree.length != n-1){
            throw new IllegalArgumentException("Merging tree has not enough entries. There are " + n + "model but only " + mergingTree.length + " entries in tree (expected " + (n-1) + " ). Stopping merging.");
        }
        callClearIndex(); // clear index if some index already exists.
        int mergeCount = mergingTree.length;
        LOGGER.info("Now performing {} merges.", mergeCount);
        Alignment finalAlignment = new Alignment();
        for (int i = 0; i < mergeCount; i++) {
            LOGGER.info("Prepare merge {} / {}", i + 1, mergeCount);
            int[] merges = mergingTree[i];
            if(merges.length != 2){
                LOGGER.warn("mergingTree contains less or more than 2 entries. Returning input alignment.");
                return new AlignmentAndParameters(inputAlignment, parameters);
            }
            int left = merges[0];
            int right = merges[1];
            
            
            Set<Object> source;
            Set<Object> target;
            //source is merged to target
            //goal is to always merge into the already merged element
            //in case both are merged, then the preference has if something appears previously
            //otherwise choose the one with less triples as source.
            if(left >= n){
                Set<Object> leftOntology = mergedOntologies.get(left - n);
                if(right >= n){
                    //merge two already merged elements
                    Set<Object> rightOntology = mergedOntologies.get(right - n);
                    //is one element previously merged?
                    if(left - n == i - 1){
                        //no clear index necessary
                        source = rightOntology; //TODO: check
                        target = leftOntology;
                    }else if(right - n == i - 1){
                        //no clear index necessary
                        source = leftOntology;//TODO: check
                        target = rightOntology;
                    }else{
                        //otherwise choose the one with less triples as source
                        callClearIndex();// clear index because previously no match of already merged elements.
                        if(isLeftModelGreater(leftOntology, rightOntology, p)){
                            source = rightOntology;
                            target = leftOntology;
                        }else{
                            source = leftOntology;
                            target = rightOntology;
                        }
                    }
                    
                }else{
                    //left is merged and right is leaf node (unmerged element)
                    Set<Object> rightOntology = models.get(right);
                    source = rightOntology;
                    target = leftOntology;
                    if(left - n != i - 1){
                        callClearIndex();
                    }
                }
            }else{
                Set<Object> leftOntology = models.get(left);
                if(right >= n){
                    //right is merged and left is leaf node (unmerged element)
                    Set<Object> rightOntology = mergedOntologies.get(right - n);
                    source = leftOntology;
                    target = rightOntology;
                    if(right - n != i - 1){
                        callClearIndex();
                    }
                }else{
                    //merge two leaf nodes (unmerged elements)
                    callClearIndex();
                    Set<Object> rightOntology = models.get(right);                     
                    if(isLeftModelGreater(leftOntology, rightOntology, p)){
                        //copy target because otherwise the original model will be modified. Which can be persisted on disk.
                        source = rightOntology;
                        target = getCopiedModel(leftOntology, p);
                    }else{
                        //copy target because otherwise the original model will be modified. Which can be persisted on disk.
                        source = leftOntology;
                        target = getCopiedModel(rightOntology, p);
                    }
                }
            }
            
            LOGGER.info("Run one to one match");
            AlignmentAndParameters alignmentAndPrameters = GenericMatcherCaller.runMatcherMultipleRepresentations(this.oneToOneMatcher, source, target, 
                    DispatcherHelper.deepCopy(inputAlignment), DispatcherHelper.deepCopy(parameters));
            Alignment alignment = TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), Alignment.class);
            if(alignment == null){
                LOGGER.error("Could not transform result of matcher to alignment. Return input alignment.");
                return new AlignmentAndParameters(inputAlignment, parameters);
            }
            finalAlignment.addAll(alignment);
            if(this.intermediateAlignments != null)
                this.intermediateAlignments.add(alignment);
            
            LOGGER.info("Merge source ontology with alignment into target ontology.");
            //need to transform the model in something known like jena model.
            Model sourceModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(source, Model.class, p);
            Model targetModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(target, Model.class, p);
            if(sourceModel == null || targetModel == null){
                LOGGER.error("Could not transform source or target to Model");
                return new AlignmentAndParameters(inputAlignment, parameters);
            }
            mergeSourceIntoTarget(sourceModel, targetModel, alignment, addInformationToUnion);
            
            if(this.lowMemoryOverhead){
                //in low memory overhead the target is tdb and doesn't need to be removed.
                removeOntModelFromSet(source);
            }
            mergedOntologies.add(new HashSet<>(Arrays.asList(targetModel)));
        }
        
        return new AlignmentAndParameters(finalAlignment, parameters);
    }
    
    private void removeOntModelFromSet(Set<Object> set){
        for (Iterator<Object> i = set.iterator(); i.hasNext();) {
            Object element = i.next();
            if (element instanceof Model) {
                i.remove();
            }
        }
    }
    
    public void clearAndStartSavingIntermediateAlignments(){
        this.intermediateAlignments = new ArrayList<>();
    }

    /**
     * Returns the intermediate alignments. This only works if clearAndStartSavingIntermediateAlignments is called before the match method.
     * @return a list of intermediate alignments or null if clearAndStartSavingIntermediateAlignments is not called.
     */
    public List<Alignment> getIntermediateAlignments() {
        return intermediateAlignments;
    }
    
    
    public void setUseCacheForMergeTree(boolean useCache){
        this.useCacheForMergeTree = useCache;
    }
    
    public void setAddInformationToUnion(boolean addInformationToUnion){
        this.addInformationToUnion = addInformationToUnion;
    }

    /**
     * If set to true, the memory overhead is reduced by two facts:
     * 1) intermediate KG are stored in TDB 2) Jena model are deleted when not needed.
     * Thus the matcher needs to deal with TDB folder as input. This is not the case for most matchers.
     * @param lowMemoryOverhead if true, the matching will be memory performant.
     */
    public void setLowMemoryOverhead(boolean lowMemoryOverhead) {
        this.lowMemoryOverhead = lowMemoryOverhead;
    }
    
             
    /**
     * Returns the merging tree (which ontologies are merged in which order).
     * Have a look at the return description to see the merging tree format.
     * @param models the models
     * @param parameters object representing additional parameters.
     * @return mergingTree for n models, this is a n-1 by 2 matrix where row i describes the merging of clusters at step i of the clustering. 
     *      If an element j in the row is less than n, then observation j was merged at this stage. 
     *      If j &ge; n then the merge was with the cluster formed at the (earlier) stage j-n of the algorithm.
     */
    public abstract int[][] getMergeTree(List<Set<Object>> models, Object parameters);
    
    
    
    protected boolean isLeftModelGreater(Set<Object> leftOntology, Set<Object> rightOntology, Properties p) throws TypeTransformationException{
        Model leftModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(leftOntology, Model.class, p);
        Model rightModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(rightOntology, Model.class, p);
        if(leftModel == null || rightModel == null){
            //just return true because usually this does matter that much and if it cannot be transformed to Model it is usually broken
            //and will break at the end of the main loop
            return true;
        }
        return leftModel.size() > rightModel.size();
    }
    
    private void callClearIndex(){
        if(this.oneToOneMatcher instanceof IndexBasedJenaMatcher){
            ((IndexBasedJenaMatcher)this.oneToOneMatcher).clearIndex();
        }
    }
    
    private Set<Object> getCopiedModel(Set<Object> modelRepresentations, Properties parameters) throws TypeTransformationException{
        if(this.lowMemoryOverhead){
            URL modelURL = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(modelRepresentations, URL.class, parameters);
            //File tdbLocation = new File("incrementalMergeintermediateKG");
            File tdbLocation = FileUtil.createFolderWithRandomNumberInDirectory(new File("./"), "incrementalMergeintermediateKG");
            tdbLocation.mkdirs();
            OntModel copiedModel = TdbUtil.bulkLoadToTdbOntModel(tdbLocation.getAbsolutePath(), modelURL.toString(), JenaTransformerHelper.getSpec(parameters));
            Set<Object> models = new HashSet<>();
            models.add(copiedModel);
            try {
                models.add(tdbLocation.toURI().toURL());
            } catch (MalformedURLException ex) {} //Do nothing
            return models;
        }else{
            Model model = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(modelRepresentations, Model.class, parameters);
            if(model == null){
                throw new IllegalArgumentException("Could not transform model during copying.");
            }
            OntModel copiedModel = JenaHelper.createNewOntModel(parameters);

            copiedModel.add(model);
            return new HashSet<>(Arrays.asList(copiedModel));
        }
    }
    
    
        
    /**
     * Merges all triples from the source model into the target model.
     * @param source the source where all triples originates
     * @param target the target model where all triples should end up
     * @param alignment the alignment which is used.
     * @param addInformationToUnion if true, all information will be added to the merged ontology
     */
    public static void mergeSourceIntoTarget(Model source, Model target, Alignment alignment, boolean addInformationToUnion){
        if(addInformationToUnion){
            mergeSourceIntoTargetFullInformation(source, target, alignment);
        }else{
            mergeSourceIntoTargetPartialInformation(source, target, alignment);
        }
        /*
        Graph targetGraph = target.getGraph();
        ExtendedIterator<Triple> it = source.getGraph().find();
        while(it.hasNext()){
            Triple t = it.next();
            
            NodeAndReplaced newSubject = getNodeAndReplaced(alignment, t.getSubject());
            NodeAndReplaced newPredicate = getMatchedNode(alignment, t.getPredicate());
            NodeAndReplaced newObject = getMatchedNode(alignment, t.getObject());
            
            if(addInformationToUnion){
                targetGraph.add(new Triple(newSubject.getNode(),newPredicate.getNode(),newObject.getNode()));
            }else{
                //we do not add it if subject or objetc is already matched.
                if(newSubject.isReplaced() == false && newObject.isReplaced() == false){
                    targetGraph.add(new Triple(newSubject.getNode(),newPredicate.getNode(),newObject.getNode()));
                }
            }
        }
        it.close();
        */
    }
    
    
    private static void mergeSourceIntoTargetFullInformation(Model source, Model target, Alignment alignment){
        Graph targetGraph = target.getGraph();
        ExtendedIterator<Triple> it = source.getGraph().find();
        while(it.hasNext()){
            Triple t = it.next();
            targetGraph.add(new Triple(
                    getNode(alignment, t.getSubject()),
                    getNode(alignment, t.getPredicate()),
                    getNode(alignment, t.getObject())
            ));
        }
        it.close();
    }
    
    private static void mergeSourceIntoTargetPartialInformation(Model source, Model target, Alignment alignment){
        Graph targetGraph = target.getGraph();
        ExtendedIterator<Triple> it = source.getGraph().find();
        while(it.hasNext()){
            Triple t = it.next();
            NodeAndReplaced newSubject = getNodeAndReplaced(alignment, t.getSubject());
            NodeAndReplaced newPredicate = getNodeAndReplaced(alignment, t.getPredicate());
            NodeAndReplaced newObject = getNodeAndReplaced(alignment, t.getObject());

            //we do not add it if subject or objetc is already matched.
            if(newSubject.isReplaced() == false && newObject.isReplaced() == false){
                targetGraph.add(new Triple(newSubject.getNode(),newPredicate.getNode(),newObject.getNode()));
            }
        }
        it.close();
    }
    
    private static Node getNode(Alignment alignment, Node node){
        if(node.isURI()){
            Iterator<Correspondence> correspondences = alignment.getCorrespondencesTargetRelation(node.getURI(), CorrespondenceRelation.EQUIVALENCE).iterator();
            if(correspondences.hasNext()){
                Node replace = NodeFactory.createURI(correspondences.next().getEntityOne());
                if(correspondences.hasNext()){
                    LOGGER.info("The alignment matches one entity from the target to multiple from the source. "
                            + "Currently uing the canonical one. Better filter the alignment in the base matcher to select the correct one.");
                }
                return replace;
            }
        }
        return node;
    }
    
    private static NodeAndReplaced getNodeAndReplaced(Alignment alignment, Node node){
        if(node.isURI()){
            Iterator<Correspondence> correspondences = alignment.getCorrespondencesTargetRelation(node.getURI(), CorrespondenceRelation.EQUIVALENCE).iterator();
            if(correspondences.hasNext()){
                Node replace = NodeFactory.createURI(correspondences.next().getEntityOne());
                if(correspondences.hasNext()){
                    LOGGER.info("The alignment matches one entity from the target to multiple from the source. "
                            + "Currently uing the canonical one. Better filter the alignment in the base matcher to select the correct one.");
                }
                return new NodeAndReplaced(replace, true);
            }
        }
        return new NodeAndReplaced(node, false);   
    }    
}

class NodeAndReplaced {
    private final Node node;
    private final boolean replaced;

    public NodeAndReplaced(Node node, boolean replaced) {
        this.node = node;
        this.replaced = replaced;
    }

    public Node getNode() {
        return node;
    }

    public boolean isReplaced() {
        return replaced;
    }
}