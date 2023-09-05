package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import static de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV.getFormattedRuntime;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.refinement.AlignmentClosureRefiner;
import de.uni_mannheim.informatik.dws.melt.matching_eval.refinement.RelationTypeRefiner;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluatorRelationTypePrediction extends Evaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorRelationTypePrediction.class);

    private CSVFormat csvFormat = CSVFormat.DEFAULT;
    private ConfusionMatrixMetric metric;
    
    public EvaluatorRelationTypePrediction(ExecutionResultSet results){
        super(results);
        metric = new ConfusionMatrixMetric();
    }
    
    @Override
    protected void writeResultsToDirectory(File baseDirectory) {
        try {
            if(!baseDirectory.exists()){
                baseDirectory.mkdirs();
            } else if (baseDirectory.isFile()) {
                LOGGER.error("The baseDirectory needs to be a directory, not a file. ABORTING writing process.");
                return;
            }
            
            List<CorrespondenceRelation> usedRelations = getAllUsedDistinctRelations();
            List<RelationTypeRefiner> relationTypeRefiner = getRelationRefiner(usedRelations);
            
            try (CSVPrinter printer = csvFormat.print(new File(baseDirectory, "relationTypePerformance.csv"), StandardCharsets.UTF_8)) {
                printer.printRecord("Track", "Track Version", "Test Case", "Matcher", "Relation", "Closure", 
                        "Precision (P)", "Recall (R)", "F1", "# of TP", "# of FP", "# of FN", "# of Correspondences", "Time", "Time (HH:MM:SS)");
                for (String matcher : this.results.getDistinctMatchers()) {
                    for (TestCase testCase : this.results.getDistinctTestCases(matcher)) {
                        writeOverviewFileMatcherTestCase(relationTypeRefiner, testCase, matcher, printer);
                    }
                }
            }
            for(ExecutionResult r : this.results.getUnrefinedResults()){
                File cmFile = new File(getResultsDirectoryTrackTestcaseMatcher(baseDirectory, r), "relationTypeConfusionMatrix.csv");
                writeConfusionMatrixMultiClass(r, cmFile);
            }
            
        } catch (IOException ioe){
            LOGGER.error("Problem with results writer.", ioe);
        }
    }
    
    private List<CorrespondenceRelation> getAllUsedDistinctRelations(){
        Set<CorrespondenceRelation> relations = new HashSet<>();
        for(ExecutionResult r : results){
            relations.addAll(r.getSystemAlignment().getDistinctRelationsAsSet());
            relations.addAll(r.getReferenceAlignment().getDistinctRelationsAsSet());
            relations.addAll(r.getInputAlignment().getDistinctRelationsAsSet());
        }
        
        List<CorrespondenceRelation> sortedRelation = new ArrayList<>(relations);
        Collections.sort(sortedRelation);
        return sortedRelation;
    }
    
    private List<RelationTypeRefiner> getRelationRefiner(List<CorrespondenceRelation> usedRelations){
        List<RelationTypeRefiner> relationTypeRefiner = new ArrayList<>();
        for(CorrespondenceRelation r : usedRelations){
            relationTypeRefiner.add(new RelationTypeRefiner(r));
        }
        return relationTypeRefiner;
    }
    
    private static AlignmentClosureRefiner CLOSURE_REFINER = new AlignmentClosureRefiner();
    private void writeOverviewFileMatcherTestCase(List<RelationTypeRefiner> refiners, TestCase testCase, String matcher, CSVPrinter printer) throws IOException {
        ExecutionResult result = results.get(testCase, matcher);
        
        //TODO: or no inlcude exclude but micro macro from all refiners?
        
        //ALL
        ConfusionMatrix cmAll = metric.compute(result);
        printCM(result, cmAll, "ALL", "false", printer);
        ConfusionMatrix cmAllClosure = metric.compute(results.get(result, CLOSURE_REFINER));
        printCM(result, cmAllClosure, "ALL", "true", printer);
        
        List<ConfusionMatrix> relationSpecificCMs = new ArrayList<>();
        List<ConfusionMatrix> relationSpecificCMsWithClosure = new ArrayList<>();
        for(RelationTypeRefiner refiner : refiners){
            ConfusionMatrix cm = metric.compute(results.get(result, refiner));
            relationSpecificCMs.add(cm);
            printCM(result, cm, refiner.getRelation().toString(), "false", printer);
            ConfusionMatrix cmClosure = metric.compute(results.get(result, CLOSURE_REFINER, refiner));
            relationSpecificCMsWithClosure.add(cmClosure);
            printCM(result, cmClosure, refiner.getRelation().toString(), "true", printer);
        }
        
        printCM(result, metric.getMicroAverages(relationSpecificCMs), "MicroAvg", "false", printer);
        printCM(result, metric.getMicroAverages(relationSpecificCMsWithClosure), "MicroAvg", "true", printer); 
        printCM(result, metric.getMacroAverages(relationSpecificCMs), "MacroAvg", "false", printer); 
        printCM(result, metric.getMacroAverages(relationSpecificCMsWithClosure), "MacroAvg", "true", printer); 
    }
    
    private void writeConfusionMatrixMultiClass(ExecutionResult r, File outFile) throws IOException {
        Map<Entry<CorrespondenceRelation, CorrespondenceRelation>, Alignment> cmMap = getConfusionMap(r);
        List<CorrespondenceRelation> usedRelations = getRelationsFromCM(cmMap);
        try (CSVPrinter printer = csvFormat.print(outFile, StandardCharsets.UTF_8)) {
            List<String> header = new ArrayList<>();
            header.add("");
            for(CorrespondenceRelation relation : usedRelations){
                header.add("Predicted " + relation.toString());
            }
            printer.printRecord(header);
            
            for(CorrespondenceRelation actual : usedRelations){
                List<String> row = new ArrayList<>();
                row.add("Actual " + actual.toString());
                for(CorrespondenceRelation predicted : usedRelations){
                    Alignment a = cmMap.getOrDefault(new SimpleEntry<>(actual, predicted), new Alignment());
                    row.add(Integer.toString(a.size()));
                }
                printer.printRecord(row);
            }
        }
    }
    
    
    private List<CorrespondenceRelation> getRelationsFromCM(Map<Entry<CorrespondenceRelation, CorrespondenceRelation>, Alignment> cm){
        Set<CorrespondenceRelation> relations = new HashSet<>();
        for(Entry<CorrespondenceRelation, CorrespondenceRelation> r : cm.keySet()){            
            relations.add(r.getKey());
            relations.add(r.getValue());
        }
        List<CorrespondenceRelation> sortedRelation = new ArrayList<>(relations);
        Collections.sort(sortedRelation);
        return sortedRelation;
    }
    
    private Map<Entry<CorrespondenceRelation, CorrespondenceRelation>, Alignment> getConfusionMap(ExecutionResult r){        
        Map<Entry<CorrespondenceRelation, CorrespondenceRelation>, Alignment> map = new HashMap<>();        
        Alignment referenceAlignment = r.getReferenceAlignment();
        Alignment systemAlignment = r.getSystemAlignment();
        Alignment systemNotInReference = new Alignment(systemAlignment);
        for(Correspondence referenceCell : referenceAlignment){
            if(referenceCell.getRelation().equals(CorrespondenceRelation.UNKNOWN) == false){                
                boolean found = false;
                for(Correspondence systemCell : systemAlignment.getCorrespondencesSourceTarget(referenceCell.getEntityOne(), referenceCell.getEntityTwo())){
                    map.computeIfAbsent(new AbstractMap.SimpleEntry<>(referenceCell.getRelation(), systemCell.getRelation()), __-> new Alignment()).add(systemCell);
                    found = true;
                    systemNotInReference.remove(systemCell);
                }
                if(found == false){
                    map.computeIfAbsent(new AbstractMap.SimpleEntry<>(referenceCell.getRelation(), CorrespondenceRelation.UNKNOWN), __-> new Alignment()).add(referenceCell);
                }
            }
        }
        for(Correspondence c : systemNotInReference){
            map.computeIfAbsent(new AbstractMap.SimpleEntry<>(CorrespondenceRelation.UNKNOWN, c.getRelation()), __-> new Alignment()).add(c);
        }
        
        return map;
    }
    
    private void printCM(ExecutionResult r, ConfusionMatrix cm, String name, String closure, CSVPrinter printer) throws IOException{
        printer.printRecord(r.getTestCase().getTrack().getName(), r.getTestCase().getTrack().getVersion(), r.getTestCase().getName(), r.getMatcherName(),
            name, closure,  cm.getPrecision(), cm.getRecall(),cm.getF1measure(), 
            cm.getTruePositiveSize(), cm.getFalsePositiveSize(), cm.getFalseNegativeSize(), cm.getNumberOfCorrespondences(),
            r.getRuntime(), getFormattedRuntime(r.getRuntime()));
    }

    public CSVFormat getCsvFormat() {
        return csvFormat;
    }

    public void setCsvFormat(CSVFormat csvFormat) {
        this.csvFormat = csvFormat;
    }
}
