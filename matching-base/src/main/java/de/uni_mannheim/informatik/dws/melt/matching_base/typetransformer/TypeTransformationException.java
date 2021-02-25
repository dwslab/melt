package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

/**
 * The exception which is thrown if a transformation does not work.
 */
public class TypeTransformationException extends Exception {

    private static final long serialVersionUID = 1L;

    public TypeTransformationException() {
    }

    public TypeTransformationException(String message) {
        super(message);
    }

    public TypeTransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeTransformationException(Throwable cause) {
        super(cause);
    }    
}
