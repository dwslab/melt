package de.uni_mannheim.informatik.dws.melt.matching_jena;

import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.system.ErrorHandler;

/**
 * An Error handler that does nothing except for throwing an Exception in fatal cases.
 */
public class ErrorHandlerCarryOn implements ErrorHandler {
    
    @Override
    public void warning(String message, long line, long col) {
    }

    @Override
    public void error(String message, long line, long col) {
    }

    @Override
    public void fatal(String message, long line, long col) {
        throw new RiotException(SysRIOT.fmtMessage(message, line, col)) ;
    }
}
