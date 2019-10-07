package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.alignmentanalyzer;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ResourceType;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.Metric;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.CorrespondenceRelation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AlignmentAnalyzerMetric is capable of calculating statistics about a finished alignment.
 *
 * @author Jan Portisch
 */
public class AlignmentAnalyzerMetric extends Metric<AlignmentAnalyzerResult> {

    private static Logger LOGGER = LoggerFactory.getLogger(AlignmentAnalyzerMetric.class);

    @Override
    public AlignmentAnalyzerResult compute(ExecutionResult executionResult) {
        Alignment alignment = executionResult.getSystemAlignment();
        double minimumConfidence = 1.0; // needs to be 1.0 for analyze() method
        double maximumConfidence = 0.0; // needs to be 0.0 for analyze() method
        boolean isHomogenousAlingment = true; // needs to be true for analyze() to work
        HashMap<CorrespondenceRelation, Integer> frequenciesOfRelations = new HashMap<>();
        OntModel sourceOntology = executionResult.getSourceOntology(OntModel.class);
        OntModel targetOntology = executionResult.getTargetOntology(OntModel.class);
        HashMap<String, Integer> frequenciesOfMappingTypes = new HashMap<>();
        int urisCorrectPosition = 0;
        int urisIncorrectPosition = 0;
        List<String> urisNotFound = new ArrayList<>();

        for (Correspondence cell : alignment) {

            // minimum and maximum confidence
            double currentConfidence = cell.getConfidence();
            if (currentConfidence > maximumConfidence) {
                maximumConfidence = currentConfidence;
            } else if (currentConfidence < minimumConfidence) {
                minimumConfidence = currentConfidence;
            }

            // relations
            CorrespondenceRelation relation = cell.getRelation();
            if (frequenciesOfRelations.containsKey(relation)) {
                frequenciesOfRelations.put(relation, frequenciesOfRelations.get(relation) + 1);
            } else {
                frequenciesOfRelations.put(relation, 1);
            }

            // types
            ResourceType entity1type = ResourceType.analyze(sourceOntology, cell.getEntityOne());
            ResourceType entity2type = ResourceType.analyze(targetOntology, cell.getEntityTwo());
            String key = entity1type.toString() + " - " + entity2type.toString();
            if (frequenciesOfMappingTypes.containsKey(key)) {
                frequenciesOfMappingTypes.put(key, frequenciesOfMappingTypes.get(key) + 1);
            } else {
                frequenciesOfMappingTypes.put(key, 1);
            }

            // homogeneity
            if (isHomogenousAlingment && entity1type != entity2type) {
                isHomogenousAlingment = false;
            }
            
            Resource entityOne = ResourceFactory.createResource(cell.getEntityOne());
            if(sourceOntology.containsResource(entityOne)){
                urisCorrectPosition++;
            }
            else if(targetOntology.containsResource(entityOne)){
                urisIncorrectPosition++;
            }else{
                urisNotFound.add(cell.getEntityOne());
            }

            Resource entityTwo = ResourceFactory.createResource(cell.getEntityTwo());
            if(targetOntology.containsResource(entityTwo)){
                urisCorrectPosition++;
            }
            else if(sourceOntology.containsResource(entityTwo)){
                urisIncorrectPosition++;
            }else{
                urisNotFound.add(cell.getEntityTwo());
            }

        } // end of loop over cells

        AlignmentAnalyzerResult result = new AlignmentAnalyzerResult(
                executionResult, minimumConfidence, maximumConfidence, frequenciesOfRelations, 
                isHomogenousAlingment, frequenciesOfMappingTypes, urisCorrectPosition, urisIncorrectPosition, urisNotFound);
        return result;
    }

}