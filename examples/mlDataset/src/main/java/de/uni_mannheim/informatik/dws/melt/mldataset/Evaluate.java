package de.uni_mannheim.informatik.dws.melt.mldataset;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.LocalTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import static de.uni_mannheim.informatik.dws.melt.matching_eval.Executor.loadFromFolder;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.dashboard.DashboardBuilder;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.ConfidenceFinder;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.MaxWeightBipartiteExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveAscendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardAlwaysMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_ml.util.TrainTestSplitAlignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author shertlin
 */
public class Evaluate {
    private static final Logger LOGGER = LoggerFactory.getLogger(Evaluate.class);
    
    public static void main(String[] args) throws IOException{
        
        ExecutionResultSet overall = new ExecutionResultSet();
        
        //overall.addAll(Executor.loadFromEvaluatorCsvResultsFolder("./systemResults/anatomy", new LocalTrack("anatomy_track-ml", "default", new File("./datasets/anatomy"))));
        
        //overall.addAll(Executor.loadFromEvaluatorCsvResultsFolder("./systemResults/biodiv", new LocalTrack("biodiv-ml", "2023", new File("./datasets/biodiv"))));
        
        overall.addAll(Executor.loadFromEvaluatorCsvResultsFolder("./systemResults/knowledgegraph", new LocalTrack("knowledgegraph-ml", "v4", new File("./datasets/knowledgegraph"))));
        
        //overall.addAll(Executor.loadFromEvaluatorCsvResultsFolder("./systemResults/bioml", new LocalTrack("bio-ml-equiv-supervised_2022", "2022", new File("./datasets/bioml"))));
        
        List<String> matcherNames = overall.getDistinctMatchersSorted();
        for(TestCase testCase : overall.getDistinctTestCases()){            
            for(String matcher : matcherNames){                
                System.out.println(testCase.getName() + "  " + matcher);
                Alignment systemAlignment = overall.get(testCase, matcher).getSystemAlignment();
                
                overall.add(new ExecutionResult(
                        testCase, 
                        matcher+"ConfAdapted", 
                        systemAlignment.cut(
                            ConfidenceFinder.getBestConfidenceForFmeasure(testCase.getParsedInputAlignment(),
                                systemAlignment,
                                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE
                            )
                        ), 
                        testCase.getParsedReferenceAlignment()
                ));
                
                overall.add(new ExecutionResult(
                        testCase, 
                        matcher+"ConfAdaptedComplete", 
                        systemAlignment.cut(
                            ConfidenceFinder.getBestConfidenceForFmeasure(testCase.getParsedInputAlignment(),
                                systemAlignment,
                                GoldStandardCompleteness.COMPLETE)
                        ),
                        testCase.getParsedReferenceAlignment()
                ));
            }
        }
        new EvaluatorCSV(overall).writeToDirectory();
    }
}
