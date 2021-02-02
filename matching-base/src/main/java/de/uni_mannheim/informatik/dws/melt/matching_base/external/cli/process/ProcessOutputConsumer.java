package de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process;

public interface ProcessOutputConsumer extends AutoCloseable {
    
    /**
     * Processes one line of output.
     * @param line the whole text line from an external process.
     */
    void processOutput(String line);
    
    @Override
    default void close() throws Exception {}
}
