package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ResourceType;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.EvaluatorCopyResults;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.refinement.TypeRefiner;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class offering multiple services to evaluators (building blocks for quick evaluator development).
 * @author Sven Hertling, Jan Portisch
 */
public class EvaluatorUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorCopyResults.class);

    /**
     * Writes the system alignment to the specified file.
     * @param executionResult The execution result whose system alignment shall be written to the specified alignmentFile.
     * @param alignmentFileToBeWritten File that shall be written.
     */
    public static void copySystemAlignment(ExecutionResult executionResult, File alignmentFileToBeWritten){
        try {
            if(executionResult.getOriginalSystemAlignment() != null){
                FileUtils.copyURLToFile(executionResult.getOriginalSystemAlignment(), alignmentFileToBeWritten);
            }else{
                executionResult.getSystemAlignment().serialize(alignmentFileToBeWritten);
            }
        } catch (IOException ex) {
            LOGGER.error("Couldn't copy the results from the matcher to the results directory.", ex);
        }
    }

    /**
     * Divides an execution result into multiple execution results according to the type of the objects (classes, properties, instances).
     * This operation can be used, to calculate metrics on a fine-granular level, for instance.
     * @param executionResult Execution result that shall be divided into classes, properties, and instances
     * @return A Map of form {@link ConfusionMatrixType} -> {@link ExecutionResult} where the execution results
     * contain only a subset of the data according to the type.
     */
    public static Map<ConfusionMatrixType, ExecutionResult> divideIntoClassPropertyInstance(ExecutionResult executionResult){
        // individual mappings
        Alignment fullSystemAlignment = executionResult.getSystemAlignment();
        Alignment classSystemAlignment = new Alignment();
        Alignment propertiesSystemAlignment = new Alignment();
        Alignment instancesSystemAlignment = new Alignment();

        // reference alignments
        Alignment fullReferenceAlignment = executionResult.getReferenceAlignment();
        Alignment classReferenceAlignment = new Alignment();
        Alignment propertiesReferenceAlignment = new Alignment();
        Alignment instancesReferenceAlignment = new Alignment();

        divideToClassPropertyOrInstance(executionResult.getSourceOntology(OntModel.class),
                executionResult.getTargetOntology(OntModel.class),
                fullSystemAlignment, classSystemAlignment, propertiesSystemAlignment, instancesSystemAlignment);

        divideToClassPropertyOrInstance(executionResult.getSourceOntology(OntModel.class),
                executionResult.getTargetOntology(OntModel.class),
                fullReferenceAlignment, classReferenceAlignment, propertiesReferenceAlignment, instancesReferenceAlignment);
        
        Map<ConfusionMatrixType, ExecutionResult> map = new HashMap<>();
        map.put(ConfusionMatrixType.ALL, executionResult);
        map.put(ConfusionMatrixType.CLASSES, new ExecutionResult(executionResult, classSystemAlignment, classReferenceAlignment, new TypeRefiner(ResourceType.CLASS, false)));
        map.put(ConfusionMatrixType.PROPERTIES, new ExecutionResult(executionResult, propertiesSystemAlignment, propertiesReferenceAlignment, new TypeRefiner(ResourceType.RDF_PROPERTY, true)));
        map.put(ConfusionMatrixType.INSTANCES, new ExecutionResult(executionResult, instancesSystemAlignment, instancesReferenceAlignment, new TypeRefiner(ResourceType.INSTANCE, false)));
        
        return map;
    }

    
    /**
     * simple division into different groups of mappings.
     * @param source Source Ontology
     * @param target Target Ontology
     * @param alignment The alignment that shall be divided. Note that this alignment will be used as ALL in confusion matrices.
     * @param clazz Class Alignment that is to be filled.
     * @param properties Property Alignment that is to be filled.
     * @param instances Instance Alignment that is to be filled.
     */
    private static void divideToClassPropertyOrInstance(OntModel source, OntModel target, Alignment alignment, Alignment clazz, Alignment properties, Alignment instances){
        for(Correspondence correspondence : alignment){
            divideToClassPropertyOrInstance(source, target, correspondence, clazz, properties, instances);
        }
    }

    /**
     * Sort the given {@code correspondence} into one of the given mappings ({@code classConfusionMatrix}, {@code property}, {@code instanceConfusionMatrix}).
     * @param source Source Ontology
     * @param target Target Ontology
     * @param correspondence Alignment Cell
     * @param clazz Class Alignment that is to be filled.
     * @param property Property Alignment that is to be filled.
     * @param instance Instance Alignment that is to be filled.
     */
    private static void divideToClassPropertyOrInstance(OntModel source, OntModel target, Correspondence correspondence, Alignment clazz, Alignment property, Alignment instance){
        ResourceType sourceType = ResourceType.subsumeProperties(ResourceType.analyze(source, correspondence.getEntityOne()));

        if( sourceType == ResourceType.CLASS){
            clazz.add(correspondence);
        }else if(sourceType == ResourceType.RDF_PROPERTY){
            property.add(correspondence);
        }else if(sourceType == ResourceType.INSTANCE){
            instance.add(correspondence);
        }else{
            //try on entity two
            ResourceType targetType = ResourceType.subsumeProperties(ResourceType.analyze(target, correspondence.getEntityTwo()));
            if(targetType == ResourceType.CLASS){
                clazz.add(correspondence);
            }else if(targetType == ResourceType.RDF_PROPERTY){
                property.add(correspondence);
            }else if(targetType == ResourceType.INSTANCE){
                instance.add(correspondence);
            }else{
                LOGGER.warn("Could not divide mapping cell to class, property or instanceConfusionMatrix: " + correspondence.toString() + ". Choose class as default. Have a look at the Alignment - some URIs might be wrong and do not appear in the ontology.");
                clazz.add(correspondence);
            }
        }
    }

    /**
     * Enumeration indicating the type of the objects of a confusion matrix.
     */
    public enum ConfusionMatrixType {
        ALL, CLASSES, PROPERTIES, INSTANCES;

        public String toString() {
            return this.name().toLowerCase();
        }
    }
}
