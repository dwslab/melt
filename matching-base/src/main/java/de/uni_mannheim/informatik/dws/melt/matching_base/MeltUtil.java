package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util methods for melt
 */
public class MeltUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeltUtil.class);
    private static final String NEWLINE = System.getProperty("line.separator");
    
    private static final String WELCOME = createWelcomeMessage();
   
    private static String createWelcomeMessage(){
        return String.join(NEWLINE, Arrays.asList(
        "Welcome to",
        " __  __ ______ _   _______ ",
        "|  \\/  |  ____| | |__   __|",
        "| \\  / | |__  | |    | |   ",
        "| |\\/| |  __| | |    | |   ",
        "| |  | | |____| |____| |   ",
        "|_|  |_|______|______|_|    " + getMeltVersion()
        ));
    }
            
            
    /**
     * ASCII welcome text logged to INFO (also includes the version).
     */
    public static void logWelcomeMessage(){
        LOGGER.info(WELCOME);
    }
    
    /**
     * Get the melt version.
     * @return the melt version as a string.
     */
    public static String getMeltVersion(){
        return getMeltPropertyOrDefault("melt.version", "version not detected");
    }
    
    private static Properties MELT_PROPERTIES;
    
    private static Properties loadMeltProperties(){
        Properties properties = new Properties();
        try {
            properties.load(MeltUtil.class.getClassLoader().getResourceAsStream("meltVersionInfo.properties"));
        } catch (IOException ex) {
            LOGGER.warn("Could not load the melt version properties. ", ex);
        }
        return properties;
    }
    
    public static String getMeltPropertyOrDefault(String key, String defaultValue){
        if(MELT_PROPERTIES == null)
            MELT_PROPERTIES = loadMeltProperties();
        return MELT_PROPERTIES.getProperty(key, defaultValue);
    }
}
