package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceType;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

/**
 * This class is also the main class that will be run when executing the JAR.
 */
public class Main {
    
    public static void main(String[] args){                
        analyzeSupervisedLearningMatcher(0.3);
    }
        
    private static void analyzeSupervisedLearningMatcher(double fraction){
        List<TestCase> testCases = new ArrayList<>();
        for(TestCase tc : TrackRepository.Knowledgegraph.V3.getTestCases().subList(1, 2)){
            testCases.add(TrackRepository.generateTestCaseWithSampledReferenceAlignment(tc, fraction, 1324567));
        }
        
        ExecutionResultSet results = Executor.run(testCases, new SupervisedMatcher());

        results.addAll(Executor.run(testCases, new BaseMatcher()));
        EvaluatorCSV e = new EvaluatorCSV(results);
        e.setBaselineMatcher(new ForwardMatcher());
        e.setResourceExplainers(Arrays.asList(new ExplainerResourceProperty(RDFS.label, SKOS.altLabel), new ExplainerResourceType()));
        e.writeToDirectory();
    }
}
