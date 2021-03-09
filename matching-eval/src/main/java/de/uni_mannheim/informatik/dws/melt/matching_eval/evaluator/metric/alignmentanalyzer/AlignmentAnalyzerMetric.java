package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.alignmentanalyzer;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ResourceType;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.Metric;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The AlignmentAnalyzerMetric is capable of calculating statistics about a finished alignment.
 *
 * @author Jan Portisch
 */
public class AlignmentAnalyzerMetric extends Metric<AlignmentAnalyzerResult> {

    private static Logger LOGGER = LoggerFactory.getLogger(AlignmentAnalyzerMetric.class);
    
    private static final String newline = System.getProperty("line.separator");

    @Override
    public AlignmentAnalyzerResult compute(ExecutionResult executionResult) {
        Alignment alignment = new Alignment();
        String parsingErrorMessage = "";
        if(executionResult.getOriginalSystemAlignment() != null){
            try {
                alignment = AlignmentParser.parse(executionResult.getOriginalSystemAlignment());
            } catch (SAXException | IOException ex) {
                parsingErrorMessage = ex.getMessage();
            }
        }else if(executionResult.getSystemAlignment() != null){
            alignment = executionResult.getSystemAlignment();
        }else{
            LOGGER.warn("originalSystemAlignment and systemAlignment is null - empty alignment will be analyzed.");
            alignment = new Alignment();
        }
        
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
        Map<Arity, Integer> arityCounts = new HashMap<>();

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
            
            //arity
            List<String> targets = new ArrayList<>();
            for(Correspondence c : alignment.getCorrespondencesSource(cell.getEntityOne())){
                targets.add(c.getEntityTwo());
            }
            Arity arity = Arity.OneToOne;
            if(targets.size() == 1){
                if(size(alignment.getCorrespondencesTarget(targets.get(0))) > 1){
                    arity = Arity.OneToMany;
                }else{
                    arity = Arity.OneToOne;
                }
            }else{
                arity = Arity.ManyToOne;
                for(String target : targets){
                    if(size(alignment.getCorrespondencesTarget(target)) > 1){
                        //n:m
                        arity = Arity.ManyToMany;
                        break;
                    }
                }
            }
            arityCounts.put(arity, arityCounts.getOrDefault(arity, 0) + 1);
        } // end of loop over cells

        AlignmentAnalyzerResult result = new AlignmentAnalyzerResult(
                executionResult, minimumConfidence, maximumConfidence, frequenciesOfRelations, 
                isHomogenousAlingment, frequenciesOfMappingTypes, urisCorrectPosition, urisIncorrectPosition, urisNotFound, 
                arityCounts, parsingErrorMessage);
        return result;
    }
    
    
    public static void writeAnalysisFile(ExecutionResultSet resultSet, File outFile){
        AlignmentAnalyzerMetric metric = new AlignmentAnalyzerMetric();
        
        List<TestCase> testCases = resultSet.getDistinctTestCasesSorted();
        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"))){
            writer.write("matcher," + testCases.stream().map(TestCase::getName).collect(Collectors.joining(",")) + newline);
            List<String> appendix = new ArrayList<>();
            for(String matcher : resultSet.getDistinctMatchersSorted()){
                writer.write(matcher + ",");
                for(TestCase testcase : testCases){
                    ExecutionResult r = resultSet.get(testcase, matcher);
                    if(r == null){
                        writer.write("No result,");
                        continue;//result for matcher not available
                    }
                    LOGGER.info("Check result {}", r);
                    
                    AlignmentAnalyzerResult analyse = metric.get(r);
                    if(analyse.hasParsingError()){
                        writer.write("Parsing error,");
                        
                        StringBuilder sb = new StringBuilder();
                        sb.append(matcher).append(",").append(testcase.getName())
                                .append(",Parsing error: " + analyse.getParsingErrorMessage())
                                .append(newline);
                        appendix.add(sb.toString());
                    }
                    else if(analyse.isSwitchOfSourceTargetBetter()){
                        writer.write("Need switch,");
                    }else if(analyse.getUrisNotFound().isEmpty() == false){
                        writer.write(analyse.getUrisNotFound().size() + " URIs not found,"); 
                        
                        //create appendix
                        StringBuilder sb = new StringBuilder();
                        sb.append(matcher).append(",").append(testcase.getName()).append(",URIs not found: ");
                        for(String uri : analyse.getUrisNotFound()){
                            sb.append(StringEscapeUtils.escapeCsv(uri)).append(",");
                        }
                        sb.append(newline);
                        appendix.add(sb.toString());
                    }else{
                        writer.write("OK,"); 
                    }
                }
                writer.write(newline);                
            }
            writer.write(newline+newline);
            writer.write("matcher,testcase,info" + newline);
            for(String a : appendix){
                writer.write(a);
            }
        } catch (IOException ex) {
            LOGGER.error("Could not write analysis file", ex);
        }
    }
    
    private static int size(Iterable<?> iterable) {
    return (iterable instanceof Collection)
            ? ((Collection<?>) iterable).size()
        : size(iterable.iterator());
  }
    private static int size(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
          iterator.next();
          count++;
        }
        return count;
   }
}