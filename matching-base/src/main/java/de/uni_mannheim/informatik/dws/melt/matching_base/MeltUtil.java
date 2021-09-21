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
        String welcomeMessage = String.join(NEWLINE, Arrays.asList(
        "Welcome to",
        " __  __ ______ _   _______ ",
        "|  \\/  |  ____| | |__   __|",
        "| \\  / | |__  | |    | |   ",
        "| |\\/| |  __| | |    | |   ",
        "| |  | | |____| |____| |   ",
        "|_|  |_|______|______|_|    "
        ));
        String meltversion = getMeltVersion();
        if(!meltversion.isEmpty()){
            welcomeMessage += meltversion;
        }
        String melthash = getMeltShortHash();
        if(!melthash.isEmpty()){
            welcomeMessage += " (git commit " + melthash + ")";
        }
        return welcomeMessage;
    }
    
    /**
     * ASCII welcome text logged to INFO (also includes the version).
     */
    public static String getWelcomeMessage(){
        return WELCOME;
    }
            
    /**
     * ASCII welcome text logged to INFO (also includes the version).
     */
    public static void logWelcomeMessage(){
        LOGGER.info(WELCOME);
    }
    
    /**
     * Get the melt version.
     * If version can not be detected or found, return the empty string.
     * @return the melt version as a string.
     */
    public static String getMeltVersion(){
        return getMeltPropertyOrDefault("melt.version", "", "${project.version}");
    }
    
    /**
     * Returns the git commit hash of the MELT build.
     * If hash can not be detected or found, return the empty string.
     * @return the git commit hash of the MELT build
     */
    public static String getMeltHash(){
        return getMeltPropertyOrDefault("melt.build.hash", "", "${env.GITHUB_SHA}");
    }
    
    /**
     * Returns the short git commit hash of the MELT build.
     * If hash can not be detected or found, return the empty string.
     * @return the short git commit hash of the MELT build
     */
    public static String getMeltShortHash(){
        String longHash = getMeltHash();
        if(longHash.isEmpty())
            return "";
        
        if(longHash.length() < 7){
            return "";
        }
        return longHash.substring(0, 7);
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
        String val = MELT_PROPERTIES.getProperty(key);
        if(val == null)
            return defaultValue;
        val = val.trim();
        if(val.isEmpty())
            return defaultValue;
        return val;
    }
    
    public static String getMeltPropertyOrDefault(String key, String defaultValue, String initialValue){
        if(MELT_PROPERTIES == null)
            MELT_PROPERTIES = loadMeltProperties();
        String val = MELT_PROPERTIES.getProperty(key);
        if(val == null)
            return defaultValue;
        val = val.trim();
        if(val.isEmpty())
            return defaultValue;
        if(val.equals(initialValue))
            return defaultValue;
        return val;
    }
}
