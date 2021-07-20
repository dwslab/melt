package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

/**
 * A python server exception in case something goes wrong or the server is not started or returned no result etc.
 */
public class PythonServerException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public PythonServerException(String message) {
        super(message);
    }

    public PythonServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public PythonServerException(Throwable cause) {
        super(cause);
    }
    
}
