package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class which actually runs a merge in {@link MultiSourceDispatcherIncrementalMerge}.
 * The source is merged into the target.
 */
public class MergeExecutor implements Callable<MergeResult>{
    private static final Logger LOGGER = LoggerFactory.getLogger(MergeExecutor.class);
    
    private final Object matcher;
    private final Set<Object> kgOne;
    private final Set<Object> kgTwo;
    private final Object inputAlignment;
    private final Properties parameters;
    private final boolean addInformationToUnion;
    private final int newPos;
    private final boolean removeUnusedJenaModels;
    private final CopyMode copyMode;
    private final String labelOfMergeTask;

    public MergeExecutor(Object matcher, Set<Object> kgOne, Set<Object> kgTwo, Object inputAlignment, Properties parameters, 
            boolean addInformationToUnion, int newPos, boolean removeUnusedJenaModels, CopyMode copyMode, String labelOfMergeTask) {
        this.matcher = matcher;
        this.kgOne = kgOne;
        this.kgTwo = kgTwo;
        this.inputAlignment = inputAlignment;
        this.parameters = parameters;
        this.addInformationToUnion = addInformationToUnion;
        this.newPos = newPos;
        this.removeUnusedJenaModels = removeUnusedJenaModels;
        this.copyMode = copyMode;
        this.labelOfMergeTask = labelOfMergeTask;
    }
    
    @Override
    public MergeResult call() throws Exception {
        Thread.currentThread().setName(this.labelOfMergeTask);
        //first check what is the source and target for the merge
        //decide what is source what is target - the target is the bigger one
        Set<Object> source = this.kgOne;
        Set<Object> target = this.kgTwo;
        try {
            if(hasFirstKgMoreTriples(this.kgOne, this.kgTwo, this.parameters)){
                source = this.kgTwo;
                target = this.kgOne;
            }
        } catch (TypeTransformationException ex) {
            LOGGER.warn("Could not transform model to jena model and cannot compare the size. Thus stick to default order."
                    + "Should not make any change unless the matcher is not symmetric.");
        }
        target = copyMode.getCopiedModel(target, parameters);
        return merge(matcher, source, target, inputAlignment, parameters, addInformationToUnion, newPos, removeUnusedJenaModels, labelOfMergeTask);
    }
    
    public static MergeResult merge(Object matcher, Set<Object> source, Set<Object> target, Object inputAlignment, Object parameters, 
            boolean addInformationToUnion, int newPos, boolean removeUnusedJenaModels, String labelOfMergeTask) throws Exception{
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        return merge(matcher, source, target, inputAlignment, p, addInformationToUnion, newPos, removeUnusedJenaModels, labelOfMergeTask);
    }
    
    public static MergeResult merge(Object matcher, Set<Object> source, Set<Object> target, Object inputAlignment, Properties parameters, 
            boolean addInformationToUnion, int newPos, boolean removeUnusedJenaModels, String labelOfMergeTask) throws Exception{
        LOGGER.info("Generate alignment with a 1:1 matcher for merge {}", labelOfMergeTask);
        long startRun = System.nanoTime();
        AlignmentAndParameters alignmentAndPrameters = GenericMatcherCaller.runMatcherMultipleRepresentations(matcher, source, target, inputAlignment, parameters);
        long runDuration = System.nanoTime() - startRun;
        LOGGER.info("Finished alignment in {} seconds ({}) for merge {}.", runDuration /1_000_000_000, 
                DurationFormatUtils.formatDurationWords(runDuration/1_000_000, true, true), labelOfMergeTask);
        
        
        Alignment alignment = TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), Alignment.class);
        if(alignment == null){
            LOGGER.error("Could not transform result of matcher to alignment. Return input alignment.");
            return null;
        }

        LOGGER.info("Merge source ontology with alignment into target ontology for merge {}.", labelOfMergeTask);
        //need to transform the model in something known like jena model.
        Model sourceModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(source, Model.class, parameters);
        Model targetModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(target, Model.class, parameters);
        if(sourceModel == null){
            LOGGER.error("Could not transform source to Model");
            return null;
        }
        if(targetModel == null){
            LOGGER.error("Could not transform target to Model");
            return null;
        }
        long startMergeTime = System.nanoTime();
        mergeSourceIntoTarget(sourceModel, targetModel, alignment, addInformationToUnion);
        long mergeDuration = System.nanoTime() - startMergeTime;
        LOGGER.info("The merging took {} seconds ({}) for merge {}.", mergeDuration /1_000_000_000, 
                DurationFormatUtils.formatDurationWords(mergeDuration/1_000_000, true, true), labelOfMergeTask);
        
        if(removeUnusedJenaModels){
            removeOntModelFromSet(source);
        }
        return new MergeResult(newPos, new HashSet<>(Arrays.asList(targetModel)), alignment);
    }
    
    public static void removeOntModelFromSet(Set<Object> set){
        for (Iterator<Object> i = set.iterator(); i.hasNext();) {
            Object element = i.next();
            // this selects Jena Model and OntModel
            if (element instanceof Model) {
                i.remove();
            }
        }
    }
    
    /**
     * Returns true if the first KG/model is greater than the second one.
     * Internally it will transform the KG to a jena model and compare it with the size (triple number).
     * @param firstKG the first KG
     * @param secondKG the second KG
     * @param parameters the parameters
     * @return true if the first KG/model is greater than the second one
     * @throws TypeTransformationException in case the transformation did not work out.
     */
    public static boolean hasFirstKgMoreTriples(Set<Object> firstKG, Set<Object> secondKG, Properties parameters) throws TypeTransformationException{
        Model leftModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(firstKG, Model.class, parameters);
        Model rightModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(secondKG, Model.class, parameters);
        if(leftModel == null || rightModel == null){
            //just return true because usually this does matter that much and if it cannot be transformed to Model it is usually broken
            //and will break at the end of the main loop
            return true;
        }
        return leftModel.size() > rightModel.size();
    }
    
    
    //merge section:
    
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

            //we do not add it if subject or obj etc is already matched.
            if(newSubject.isReplaced() == false && newObject.isReplaced() == false){
                targetGraph.add(new Triple(newSubject.getNode(),newPredicate.getNode(),newObject.getNode()));
            }
        }
        it.close();
    }
    
    private static Node getNode(Alignment alignment, Node node){
        if(node.isURI()){
            Iterator<Correspondence> correspondences = alignment.getCorrespondencesSourceRelation(node.getURI(), CorrespondenceRelation.EQUIVALENCE).iterator();
            if(correspondences.hasNext()){
                Node replace = NodeFactory.createURI(correspondences.next().getEntityTwo());
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
            Iterator<Correspondence> correspondences = alignment.getCorrespondencesSourceRelation(node.getURI(), CorrespondenceRelation.EQUIVALENCE).iterator();
            if(correspondences.hasNext()){
                Node replace = NodeFactory.createURI(correspondences.next().getEntityTwo());
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
