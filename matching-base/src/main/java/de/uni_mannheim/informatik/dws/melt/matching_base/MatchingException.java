package de.uni_mannheim.informatik.dws.melt.matching_base;

/**
 * An exception which can be thrown by a matcher in case something goes wrong.
 */
public class MatchingException extends Exception{

    public MatchingException(String message) {
        super(message);
    }

    public MatchingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MatchingException(Throwable cause) {
        super(cause);
    }
    
}
