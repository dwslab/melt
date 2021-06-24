package de.uni_mannheim.informatik.dws.melt.matching_base.external.docker;

/**
 * An exception when docker is not running.
 */
public class DockerNotRunningException extends RuntimeException  {
    
    private static final long serialVersionUID = 1L;

    public DockerNotRunningException(String message, Throwable cause) {
        super(message, cause);
    }
}
