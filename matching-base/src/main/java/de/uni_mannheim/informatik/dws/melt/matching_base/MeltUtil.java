package de.uni_mannheim.informatik.dws.melt.matching_base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util methods for melt
 */
public class MeltUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeltUtil.class);
    
    /**
     * ASCII welcome text logged to INFO (also includes the version).
     */
    public static void logWelcomeMessage(){
        LOGGER.info(" __  __ ______ _   _______ ");
        LOGGER.info("|  \\/  |  ____| | |__   __|");
        LOGGER.info("| \\  / | |__  | |    | |   ");
        LOGGER.info("| |\\/| |  __| | |    | |   ");
        LOGGER.info("| |  | | |____| |____| |   ");
        LOGGER.info("|_|  |_|______|______|_|    version " + getMeltVersion());
    }
    
    /**
     * Get the melt version.
     * @return the melt version as a string.
     */
    public static String getMeltVersion(){
        return MeltUtil.class.getPackage().getImplementationVersion();
    }
}
