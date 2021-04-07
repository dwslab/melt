package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A collection of individual {@link ExecutionResultMultiSource} instances that are typically returned by an {@link ExecutorMultiSource}.
 */
public class ExecutionResultSetMultiSource extends HashSet<ExecutionResultMultiSource>{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionResultSetMultiSource.class);
    private static final long serialVersionUID = 1L;

    public ExecutionResultSet toExecutionResultSet(){
        ExecutionResultSet set = new ExecutionResultSet();
        for(ExecutionResultMultiSource results : this){
            set.addAll(results.toExecutionResultSet());
        }
        return set;
    }
}
