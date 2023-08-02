package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A basic evaluator that is easy on Memory and prints the performance results per test case in CSV format.
 * No track aggregation is performed. If you are interested in an in-depth analysis (and if you have enough memory), you should use
 * {@link EvaluatorCSV}.
 */
public class EvaluatorPipeline extends Evaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorPipeline.class);
    
    private List<Evaluator> evaluators;
    
    public EvaluatorPipeline(Evaluator... evaluators) {
        super(null);
        this.evaluators = Arrays.asList(evaluators);
    }
    
    public EvaluatorPipeline(ExecutionResultSet results, List<Class<Evaluator>> evaluators)  {
        super(results);
        this.evaluators = new ArrayList<>();
        for(Class<Evaluator> e : evaluators){
            try {
                this.evaluators.add(e.getDeclaredConstructor(ExecutionResultSet.class).newInstance(results));
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.warn("Could not instantiate the evaluator {} - skip it", e.getName(), ex);
            }
        }
    }

    @Override
    protected void writeResultsToDirectory(File baseDirectory) {
        for(Evaluator e : this.evaluators){
            e.writeToDirectory(baseDirectory);
        }
    }
}
