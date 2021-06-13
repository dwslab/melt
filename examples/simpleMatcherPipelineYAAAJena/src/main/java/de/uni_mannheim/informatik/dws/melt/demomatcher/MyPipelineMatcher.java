package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.ExactStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.BackgroundMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.ImplementedBackgroundMatchingStrategies;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetKnowledgeSource;

import java.util.ArrayList;
import java.util.List;

public class MyPipelineMatcher extends MatcherPipelineYAAAJena {


    @Override
    protected List<MatcherYAAAJena> initializeMatchers() {
        List<MatcherYAAAJena> result = new ArrayList<>();

        // let's add a simple exact string matcher
        result.add(new ExactStringMatcher());

        // let's add a background matcher
        result.add(new BackgroundMatcher(new WordNetKnowledgeSource(),
                ImplementedBackgroundMatchingStrategies.SYNONYMY, 0.5));

        return result;
    }

    public static void main(String[] args) {
        // let's initialize our matcher
        MyPipelineMatcher myMatcher = new MyPipelineMatcher();

        // let's execute our matcher on the OAEI Anatomy test case
        ExecutionResultSet ers = Executor.run(TrackRepository.Anatomy.Default.getFirstTestCase(), myMatcher);

        // let's evaluate our matcher (you can find the results in the `results` folder (will be created if it
        // does not exist).
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(ers);
        evaluatorCSV.writeToDirectory();
    }
}
