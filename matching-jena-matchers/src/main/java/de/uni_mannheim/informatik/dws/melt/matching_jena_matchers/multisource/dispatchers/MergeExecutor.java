package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
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
    
    private Object matcher;
    private Set<Object> source;
    private Set<Object> target;
    private Object inputAlignment;
    private Object parameters;
    private boolean addInformationToUnion;
    private int newPos;

    public MergeExecutor(Object matcher, Set<Object> source, Set<Object> target, Object inputAlignment, Object parameters, boolean addInformationToUnion, int newPos) {
        this.matcher = matcher;
        this.source = source;
        this.target = target;
        this.inputAlignment = inputAlignment;
        this.parameters = parameters;
        this.addInformationToUnion = addInformationToUnion;
        this.newPos = newPos;
    }
    
    @Override
    public MergeResult call() throws Exception {
        return merge(matcher, source, target, inputAlignment, parameters, addInformationToUnion, newPos);
    }
    
    
    public static MergeResult merge(Object matcher, Set<Object> source, Set<Object> target, Object inputAlignment, Object parameters, boolean addInformationToUnion, int newPos) throws Exception{
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        return merge(matcher, source, target, inputAlignment, p, addInformationToUnion, newPos);
    }
    
    public static MergeResult merge(Object matcher, Set<Object> source, Set<Object> target, Object inputAlignment, Properties parameters, boolean addInformationToUnion, int newPos) throws Exception{
        LOGGER.info("Generate alignment with a 1:1 matcher");
        long startRun = System.nanoTime();
        AlignmentAndParameters alignmentAndPrameters = GenericMatcherCaller.runMatcherMultipleRepresentations(matcher, source, target, inputAlignment, parameters);
        long runDuration = System.nanoTime() - startRun;
        LOGGER.info("Finished alignment in {} seconds ({}).", runDuration /1_000_000_000, DurationFormatUtils.formatDurationWords(runDuration/1_000_000, true, true));
        
        
        Alignment alignment = TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), Alignment.class);
        if(alignment == null){
            LOGGER.error("Could not transform result of matcher to alignment. Return input alignment.");
            return null;
        }

        LOGGER.info("Merge source ontology with alignment into target ontology.");
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
        LOGGER.info("The merging took {} seconds ({}).", mergeDuration /1_000_000_000, DurationFormatUtils.formatDurationWords(mergeDuration/1_000_000, true, true));

        return new MergeResult(newPos, new HashSet<>(Arrays.asList(targetModel)), alignment);
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
