/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.uni_mannheim.informatik.dws.melt.mldataset;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.LocalTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.ConfidenceFinder;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveAscendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shert
 */
public class EvaluateConferenceDataset {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Playground.class);
    
    public static void main(String[] args) throws Exception{
        Map<TestCase, TestCase> map = generateDataset();
        evaluate(map);
        
    }
    
    
    private static void evaluate(Map<TestCase, TestCase> map) throws Exception{
        LocalTrack track = new LocalTrack("conference-ml", "1.0", new File("./conference"));
        
        ExecutionResultSet overall = new ExecutionResultSet();
        ExecutionResultSet all = Executor.loadFromConferenceResultsFolder("./conference2023results");
        for(TestCase testCase : track.getTestCases()){
            TestCase old = TrackRepository.Conference.V1.getTestCase(testCase.getName());
                    
            for(String matcher : all.getDistinctMatchers()){
                ExecutionResult trainingResult = all.get(map.get(old), matcher);
                ExecutionResult testResult = all.get(old, matcher);
                
                
                double bestConfidence = ConfidenceFinder.getBestConfidenceForFmeasure(trainingResult.getReferenceAlignment(),
                                    trainingResult.getSystemAlignment(),
                                    GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);
                overall.add(testResult);
                overall.add(Executor.runMatcherOnTop(testResult,
                                        new ConfidenceFilter(bestConfidence),
                                        matcher+"ConfAdaptedFM"
                                ));
            }
            
            //make SBERT.
            
            //Executor.run(testCase, new SBertOnly(), "SBert"))
            //Executor.runMatcherOnTop(s, testCase, "SBert", new NaiveAscendingExtractor(), "SBertOneOneAsc");
            //Executor.runMatcherOnTop(s, testCase, "SBert", new NaiveDescendingExtractor(), "OneOneDesc");
            //Executor.runMatcherOnTop(s, testCase, "SBert", new MaxWeightBipartiteExtractor(), "OneOneMax");
            
        }        
        new EvaluatorCSV(overall).writeToDirectory();
    }
    
    private static Map<TestCase, TestCase> generateDataset() throws Exception{
        Track conference = TrackRepository.Conference.V1;
        
        Map<TestCase, TestCase> map = new HashMap<>();
        List<TestCase> testCasesNotUsed = new ArrayList<>(conference.getTestCases());
        
        for(TestCase testing : conference.getTestCases()){
            TestCase training = getUnsusedTestCase(testCasesNotUsed, testing);
            map.put(testing, training);
            MLDataGeneratorTransferLearning gen = new MLDataGeneratorTransferLearning(training, testing);
            gen.saveToFolder(new File("./conference"));
        }
        return map;
    }
    
    private static TestCase getUnsusedTestCase(List<TestCase> testCasesNotUsed, TestCase testing) throws Exception{        
        String[] testOntologies = testing.getName().split("-");        
        for(TestCase training : testCasesNotUsed){
            if(training.getName().contains(testOntologies[0])||
               training.getName().contains(testOntologies[1])){
                continue;
            }
            //training testcase can be used
            testCasesNotUsed.remove(training);
            return training;
        }
        
        for(TestCase training : testing.getTrack().getTestCases()){
            if(training.getName().contains(testOntologies[0])||
               training.getName().contains(testOntologies[1])){
                continue;
            }
            return training;
        }
        throw new Exception("Something gone wrong");
    }
            
            
            
    
}
