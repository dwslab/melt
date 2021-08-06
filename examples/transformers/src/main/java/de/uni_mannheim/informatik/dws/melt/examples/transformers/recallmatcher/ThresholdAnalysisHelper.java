package de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.ConfidenceFinder;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;


public class ThresholdAnalysisHelper {


    public static void main(String[] args) {
        String path = "/Users/janportisch/IdeaProjects/melt/examples/transformers/results/tests/results_2021-08-06_10" +
                "-28-16";
        optimize(path);
        apply(0.97, path);
        System.out.println("DONE");
    }

    static void optimize(String path){
        ExecutionResultSet ers = Executor.loadFromEvaluatorCsvResultsFolder(path);
        for (ExecutionResult executionResult : ers) {
            if(executionResult.getTrack().getName().equals("anatomy_track")) {
                System.out.println(executionResult.getMatcherName());
                System.out.println(executionResult.getTrack().getName());
                double bestConfidence = ConfidenceFinder.getBestConfidenceForFmeasure(executionResult);
                System.out.println(bestConfidence);
                System.out.println();
            }
        }
    }

    static void apply(double threshold, String path){
        ExecutionResultSet ers = Executor.loadFromEvaluatorCsvResultsFolder(path);
        ExecutionResultSet ersWithThresholding = new ExecutionResultSet();
        for (ExecutionResult executionResult : ers) {
            if (executionResult.getTrack().getName().equals("anatomy_track")) {
                ConfidenceFilter cf = new ConfidenceFilter(threshold);
                Track anatomyTrack = TrackRepository.Anatomy.Default;
                TestCase anatomyTestCase = anatomyTrack.getFirstTestCase();
                Alignment filteredAlignment = cf.filter(executionResult.getSystemAlignment(),
                        anatomyTestCase.getSourceOntology(OntModel.class),
                        anatomyTestCase.getTargetOntology(OntModel.class));
                ersWithThresholding.add(new ExecutionResult(executionResult.getTestCase(),
                        executionResult.getMatcherName() + "_t=" + threshold, filteredAlignment,
                        executionResult.getReferenceAlignment()));
            }
        }
        EvaluatorCSV evaluator = new EvaluatorCSV(ersWithThresholding);
        evaluator.writeToDirectory();
    }

}
