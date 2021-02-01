package de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessOutputInfoLogging implements ProcessOutputConsumer{

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessOutputInfoLogging.class);
    
    /**
     * The prefix before the actual message
     */
    private String prefix;
    
    /**
     * Constructor
     * @param prefix the prefix before the actual message
     */
    public ProcessOutputInfoLogging(String prefix){
        this.prefix = prefix;
    }
    
    public ProcessOutputInfoLogging(){
        this.prefix = "";
    }
    
    @Override
    public void processOutput(String line) {
        LOGGER.info(this.prefix + line);
    }
}
