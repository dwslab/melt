package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatchingException;
import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MultiSourceDispatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_jena.multisource.IndexBasedJenaMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.JenaTransformerHelper;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.FileCache;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 * This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 * The order how they are merged is defined by subclasses.
 */
public abstract class MultiSourceDispatcherIncrementalMerge extends MatcherMultiSourceURL implements MultiSourceDispatcher, IMatcherMultiSourceCaller{
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourceDispatcherIncrementalMerge.class);
    
    private final Object oneToOneMatcher;
    
    private int numberOfThreads;
    private boolean addingInformationToUnion;
    private boolean removeUnusedJenaModels;
    private CopyMode copyMode;
    private List<Alignment> intermediateAlignments;
    private FileCache<MergeOrder> mergeOrderFileCache;

    /**
     * Constructor which expects the actual one to one matcher and a boolean if information should be added to the union.
     * @param oneToOneMatcher ont to one matcher
     * @param addInformationToUnion if true all information from matched entities are in the union.
     */
    public MultiSourceDispatcherIncrementalMerge(Object oneToOneMatcher, boolean addInformationToUnion) {
        this.oneToOneMatcher = oneToOneMatcher;
        this.addingInformationToUnion = addInformationToUnion;
        this.intermediateAlignments = null; // default is not to save intermediate alignments
        this.removeUnusedJenaModels = false;
        this.copyMode = CopyMode.NONE;
        this.numberOfThreads = 1;
        this.mergeOrderFileCache = null;
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
        MergeOrder mergeOrder = null;
        if(mergeOrderFileCache != null){
            mergeOrder = mergeOrderFileCache.loadFromFileOrUseSuplier(()-> getMergeTree(models, parameters));
            mergeOrderFileCache.saveIfCacheNotExistent();
        }else{
            mergeOrder = getMergeTree(models, parameters);
        }
        
        if(mergeOrder == null){
            LOGGER.warn("Merging order is null. Please check subclasses, expecially what they return at method getMergeTree. Returning input alignment.");
            return new AlignmentAndParameters(inputAlignment, parameters);
        }
        
        if(mergeOrder.getTree().length != models.size()-1){
            throw new IllegalArgumentException("Merging tree has unexpected number of merges. There are " + models.size() + " model(s) but " + mergeOrder.getTree().length + " entries in tree (expected " + (models.size()-1) + " ). Stopping merging.");
        }
        
        if(this.intermediateAlignments != null)
            this.intermediateAlignments = new ArrayList<>(mergeOrder.getTree().length);
        callClearIndex(); // clear index if some index already exists.
        
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        
        mergeOrder.setLabels(JenaTransformerHelper.getShortNameForModelRepresentations(models));
        
        if(this.numberOfThreads > 1){
            return runParallel(mergeOrder, models, inputAlignment, p);
        }else{
            return runSequential(mergeOrder, models, inputAlignment, p);
        }
    }
    
    private AlignmentAndParameters runSequential(MergeOrder mergeOrder, List<Set<Object>> models, Object inputAlignment, Properties p) throws MatchingException, Exception{
        List<Set<Object>> mergedOntologies = new ArrayList<>();
        int[][] mergeTree = mergeOrder.getTree();
        double[] distances = mergeOrder.getDistances();
        double[] distancesNormalized  = mergeOrder.getDistancesNormalized();
                
        int n = models.size();
        int mergeCount = mergeTree.length;
        LOGGER.info("Now performing {} merges.", mergeCount);
        Alignment finalAlignment = new Alignment();
        for (int i = 0; i < mergeCount; i++) {
            LOGGER.info("Prepare merge {} / {}", i + 1, mergeCount);
            int[] mergePair = mergeTree[i];
            if(mergePair.length < 2){
                throw new IllegalArgumentException("Merge tree is not valid. In row " + i + " less than two elements appear: " + Arrays.toString(mergePair));
            }
            int left = mergePair[0];
            int right = mergePair[1];
            
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
                        target = this.copyMode.getCopiedModel(leftOntology, p);
                    }else{
                        //copy target because otherwise the original model will be modified. Which can be persisted on disk.
                        source = leftOntology;
                        target = this.copyMode.getCopiedModel(rightOntology, p);
                    }
                }
            }
            String mergeLabel = mergeOrder.getLabel(left) + "-" + mergeOrder.getLabel(right);
            Properties parameters = addDistance(DispatcherHelper.deepCopy(p), distances[i], distancesNormalized[i]);
            Object copiedInputAlignment = DispatcherHelper.deepCopy(inputAlignment);
            
            MergeResult mergeResult = MergeExecutor.merge(oneToOneMatcher, source, target, copiedInputAlignment, parameters, addingInformationToUnion, -1, this.removeUnusedJenaModels, mergeLabel);
            mergedOntologies.add(mergeResult.getResult());
            
            Alignment resultingAlignment = mergeResult.getAlignment();
            if(resultingAlignment == null){
                LOGGER.error("The resulting alignment is null. Maybe a transformetrion error. The whole merge will be canceled.");
                throw new MatchingException("The resulting alignment is null. Maybe a transformetrion error. The whole merge will be canceled.");
            }
            finalAlignment.addAll(resultingAlignment);
            if(this.intermediateAlignments != null)
                this.intermediateAlignments.add(resultingAlignment);
        }
        return new AlignmentAndParameters(finalAlignment, p);
    }
    
    private AlignmentAndParameters runParallel(MergeOrder mergeOrder, List<Set<Object>> models, Object inputAlignment, Properties p) throws MatchingException{
        if(this.copyMode == CopyMode.CREATE_TDB)
            throw new IllegalArgumentException("Copy mode for parallel merge should not be set to CopyMode.CREATE_TDB");
        Alignment finalAlignment = new Alignment();
        
        List<Set<Object>> mergedModels = new ArrayList<>();
        mergedModels.addAll(models);
        
        int n = models.size();
        for(int i = 0; i < n; i++){
            mergedModels.add(null);
        }
        
        int[][] mergeTree = mergeOrder.getTree();
        double[] distances = mergeOrder.getDistances();
        double[] distancesNormalized = mergeOrder.getDistancesNormalized();
        List<MergeTaskPos> merges = new ArrayList<>();
        for(int i=0; i < mergeTree.length; i++){
            int[] mergePair = mergeTree[i];
            if(mergePair.length < 2)
                throw new IllegalArgumentException("Merge tree is not valid. In row " + i + " less than two elements appear: " + Arrays.toString(mergePair));
            merges.add(new MergeTaskPos(mergePair[0], mergePair[1], n + i, distances[i], distancesNormalized[i]));
        }
        
        //to be removed        
        //mergeOrder.displayTree();
        
        //in the parallel mode, it is not necessary to look for already merged elements because everything happens in parallel
        //and the matcher cannot improve speed (by using a cache)
        //thus we look only for the larger KG which is the target.
        
        //the order in which the results are processed is irrelevant
        ExecutorService exec = Executors.newFixedThreadPool(this.numberOfThreads);
        ExecutorCompletionService<MergeResult> completionService = new ExecutorCompletionService<>(exec);
        List<Integer> parallelMergesPossible = mergeOrder.getCountOfParallelExecutions();
        LOGGER.info("Run parallel merge with {} merges ({} threads).", mergeTree.length, this.numberOfThreads);
        LOGGER.info("Following the list of counts which show how many matching tasks could be processed in parallel for each stage: {}", parallelMergesPossible);
        try{
            int stage = 1;
            while(!merges.isEmpty()){
                List<MergeTaskPos> runnable = new ArrayList<>();
                for(MergeTaskPos task : merges){
                    Set<Object> one = mergedModels.get(task.getClusterOnePos());
                    Set<Object> two = mergedModels.get(task.getClusterTwoPos());
                    if(one != null && two != null){
                        runnable.add(task);
                        String mergeLabel = mergeOrder.getLabel(task.getClusterOnePos()) + "-" + mergeOrder.getLabel(task.getClusterTwoPos());
                        Properties parameters = addDistance(DispatcherHelper.deepCopy(p), task.getDistance(), task.getDistanceNormalized());
                        Object copiedInputAlignment = DispatcherHelper.deepCopy(inputAlignment);
                        completionService.submit(new MergeExecutor(this.oneToOneMatcher, one, two, copiedInputAlignment, parameters, 
                                addingInformationToUnion, task.getClusterResultPos(), this.removeUnusedJenaModels, this.copyMode, mergeLabel));
                        
                        /*
                        //to be removed
                        MergeResult result = null;
                        try {
                            result = new MergeExecutor(this.oneToOneMatcher, one, two,
                                    DispatcherHelper.deepCopy(inputAlignment), DispatcherHelper.deepCopy(p),
                                    addingInformationToUnion, task.getClusterResultPos(), this.removeUnusedJenaModels, this.copyMode, mergeLabel).call();
                        } catch (Exception ex) {
                            java.util.logging.Logger.getLogger(MultiSourceDispatcherIncrementalMerge.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        mergedModels.set(result.getNewPos(), result.getResult());
                        Alignment resultingAlignment = result.getAlignment();
                        if(resultingAlignment == null){
                            LOGGER.error("The resulting alignment is null. Maybe a transformation error. The whole merge will be canceled.");
                            throw new MatchingException("The resulting alignment is null. Maybe a transformetrion error. The whole merge will be canceled.");
                        }
                        finalAlignment.addAll(resultingAlignment);
                        if(this.intermediateAlignments != null)
                            this.intermediateAlignments.set(result.getNewPos() - n, resultingAlignment);
                        //to be removed - end
                        */
                    }
                }
                
                LOGGER.info("Run matching stage {}/{} with possibly {} tasks in parallel (actual number of parallel threads: {})", stage++, parallelMergesPossible.size(), runnable.size(), this.numberOfThreads);
                merges.removeAll(runnable);
                
                for(int i = 0; i < runnable.size(); i++ ) {
                    try {
                        MergeResult result = completionService.take().get();// get is the blocking call here
                        if(result == null){
                            LOGGER.error("The result of a merge is null. The whole merge will be canceled.");
                            throw new MatchingException("The result of a merge is null. The whole merge will be canceled.");
                        }
                        mergedModels.set(result.getNewPos(), result.getResult());
                        Alignment resultingAlignment = result.getAlignment();
                        if(resultingAlignment == null){
                            LOGGER.error("The resulting alignment is null. Maybe a transformation error. The whole merge will be canceled.");
                            throw new MatchingException("The resulting alignment is null. Maybe a transformetrion error. The whole merge will be canceled.");
                        }
                        finalAlignment.addAll(resultingAlignment);
                        if(this.intermediateAlignments != null)
                            this.intermediateAlignments.set(result.getNewPos() - n, resultingAlignment);
                    } catch (InterruptedException | ExecutionException ex) {
                        LOGGER.warn("Error when waiting for parallel results of matcher execution.", ex);
                    }
                }
            }
        }finally{
            exec.shutdown();
        }
        return new AlignmentAndParameters(finalAlignment, p);
    }
    
    
    private static Properties addDistance(Properties p, double distance, double normalizedDistance){
        p.put(ParameterConfigKeys.TOPIC_DISTANCE, distance);
        p.put(ParameterConfigKeys.TOPIC_DISTANCE_NORMALIZED, normalizedDistance);
        return p;
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
    public abstract MergeOrder getMergeTree(List<Set<Object>> models, Object parameters);
    
    
    
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
    
    
    
    //getter and setter:

    /**
     * Returns the number of thread which are used during merge.
     * A number equal to one means sequential processing and greater than one means parallel processing.
     * @return the number of thread used.
     */
    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    /**
     * Sets the number of threads which are used during merge.
     * A number equal to one means sequential processing and greater than one means parallel processing with the specified number of threads.
     * @param numberOfThreads the number of threads to use. Values greater or equal to one are allowed.
     */
    public void setNumberOfThreads(int numberOfThreads) {
        if(numberOfThreads < 1)
            throw new IllegalArgumentException("Number of threads are smaller than one: " + numberOfThreads);
        this.numberOfThreads = numberOfThreads;
    }
    
    /**
     * Sets the number of threads which are used during merge to the number of available CPU cores.
     * 
     * A number equal to one means sequential processing and greater than one means parallel processing with the specified number of threads.
     */
    public void setNumberOfThreadsToCpuCores() {
        setNumberOfThreads(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Return true if all information / triples are added to the union.
     * If set to false, only the information of non matched entities is added to the union.
     * @return true if all information / triples are added to the union
     */
    public boolean isAddingInformationToUnion() {
        return addingInformationToUnion;
    }

    /**
     * Sets the value if all information / triples are added to the union.
     * If set to false, only the information of non matched entities is added to the union.
     * @param addInformationToUnion true if all information / triples are added to the union
     */
    public void setAddingInformationToUnion(boolean addInformationToUnion) {
        this.addingInformationToUnion = addInformationToUnion;
    }

    /**
     * Returns true if OntModels are removed.
     * @return true if unused OntModels are removed
     */
    public boolean isRemoveUnusedJenaModels() {
        return removeUnusedJenaModels;
    }
    
    /**
     * If set to true, this removes OntModel/Model which are not needed anymore.
     * This helps to match a large number of KGs in a memory friendly way.
     * @param removeUnusedJenaModels if true, unused OntModels will be removed
     */
    public void setRemoveUnusedJenaModels(boolean removeUnusedJenaModels) {
        this.removeUnusedJenaModels = removeUnusedJenaModels;
    }
    
    /**
     * Returns the intermediate alignments. This only works if {@link #setSavingIntermediateAlignments(boolean) } is set to true before the match method is called.
     * @return a list of intermediate alignments or null if {@link #setSavingIntermediateAlignments(boolean) } was set to false (the default).
     */
    public List<Alignment> getIntermediateAlignments() {
        return this.intermediateAlignments;
    }
    
    /**
     * Returns true if intermediate alignments are stored.
     * @return true if intermediate alignments are stored
     */
    public boolean isSavingIntermediateAlignments() {
        return this.intermediateAlignments != null;
    }
    
    /**
     * Set to true if the intermediate alignments should be stored.
     * @param intermediateAlignmentsNew true if the intermediate alignments should be stored
     */
    public void setSavingIntermediateAlignments(boolean intermediateAlignmentsNew) {
        if(intermediateAlignmentsNew){
            this.intermediateAlignments = new ArrayList<>();
        }else{
            this.intermediateAlignments = null;
        }
    }

    /**
     * Returns the used copy mode.
     * @return the used copy mode
     */
    public CopyMode getCopyMode() {
        return copyMode;
    }

    /**
     * Sets the copy mode which is used during the merging.
     * Defaults to None.
     * @param copyMode new copy mode to use
     */
    public void setCopyMode(CopyMode copyMode) {
        this.copyMode = copyMode;
    }
    
    /**
     * Returns the cache file which is used to store the merge tree.
     * In case no cache file is set, then null is returned.
     * @return the cache file or null
     */
    public File getCacheFile() {
        if(this.mergeOrderFileCache == null)
            return null;
        return this.mergeOrderFileCache.getFile();
    }
    
    /**
     * Sets the cache file which should be used to cache the merge tree.
     * Only use this method if the same models are merged over and over again.
     * Thus the merge tree do not need to be computed multiple times.
     * In case the file does not exists, the new computed merge tree is stored in this file.
     * In all other cases, the file will not be modified. The cache file needs to be deleted to compute the merge tree again.
     * @param cacheFile the file for storing the merge tree.
     */
    public void setCacheFile(File cacheFile) {
        this.mergeOrderFileCache = new FileCache<>(cacheFile);
    }
}
