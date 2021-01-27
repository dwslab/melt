package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * This class provides static functionality for SPARQL calls.
 */
public class SparqlServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlServices.class);

    /**
     * When executing queries it sometimes comes to exceptions (most likely http exceptions).
     * This method executes in a safe environment and will retry after some seconds, when the execution fails.
     *
     * @param queryExecutionInstance Query Execution Object.
     * @return ResultSet Object. Null, if no result after second attempt.
     */
    public static ResultSet safeExecution(QueryExecution queryExecutionInstance) {
        ResultSet results;
        try {
            results = queryExecutionInstance.execSelect();
        } catch (Exception e) {
            // most likely a http exception
            e.printStackTrace();
            LOGGER.error("An exception occurred while querying. Waiting for 15 seconds...");
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            LOGGER.error("Retry");
            results = queryExecutionInstance.execSelect();
        }
        return results;
    }

    /**
     * When executing queries it sometimes comes to exceptions (most likely http exceptions).
     * This method executes in a safe environment and will retry after some seconds, when the execution fails.
     *
     * @param queryExecutionInstance Query execution object.
     * @return True if ask query evaluates to true, else false.
     */
    public static boolean safeAsk(QueryExecution queryExecutionInstance) {
        boolean result;
        try {
            result = queryExecutionInstance.execAsk();
        } catch (Exception e) {
            LOGGER.error("An exception occurred while querying. Waiting for 15 seconds...");
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            LOGGER.error("Retry.");
            result = queryExecutionInstance.execAsk();
        } // end of catch
        return result;
    }

    /**
     * When executing queries it sometimes comes to exceptions (most likely http exceptions).
     * This method executes in a safe environment and will retry after some seconds, when the execution fails.
     *
     * @param askQuery The query to be asked.
     * @param endpoint The SPARQL endpoint
     * @return True if ask query evaluates to true, else false.
     */
    public static boolean safeAsk(String askQuery, String endpoint) {
        return safeAsk(QueryExecutionFactory.sparqlService(endpoint, askQuery));
    }
}
