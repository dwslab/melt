package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

/**
 * Exception representing a error when data does not fit to SSSOM schema
 */
public class SSSOMFormatException extends Exception {
    
    private static final long serialVersionUID = 5451825154955645498L;


    public SSSOMFormatException(String msg) {
        super(msg);
    }

    public SSSOMFormatException(String msg, Throwable inner) {
        super(msg, inner);
    }
    
}
