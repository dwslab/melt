package de.uni_mannheim.informatik.dws.melt.matching_eval.profiling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *Class with helper methods to profile memory usage.
 */
public class MemoryProfiling {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryProfiling.class);
    
    private static final Runtime runtime = Runtime.getRuntime();
    
    /**
     * Returns the used memory in bytes by the current JVM.
     * @param callGC true, if the garbage collector (GC) should be run first.
     * @return used memory in bytes by the current JVM
     */
    public static long usedMemory(boolean callGC){
        if(callGC)
            runtime.gc();
        return runtime.totalMemory() - runtime.freeMemory();        
    }
    
    /**
     * Returns the used memory in bytes by the current JVM.
     * The garbage collector (GC) is exectuted before the measurement.
     * @return used memory in bytes by the current JVM
     */
    public static long usedMemory(){
        return usedMemory(true); 
    }
        
    /**
     * Returns the used memory of the current JVM in a human readable way e.g. 50 MB or 100 GB.
     * The garbage collector (GC) is exectuted before the measurement.
     * @return used memory of the current JVM in a human readable way
     */
    public static String usedMemoryHumanReadable(){
        return humanReadableByteCount(usedMemory());
    }
    
    /**
     * Logs the used memory of the current JVM in a human readable way e.g. 50 MB or 100 GB.
     * The garbage collector (GC) is exectuted before the measurement.
     */
    public static void logUsedMemory(){
        LOGGER.debug("Memory usage: " + humanReadableByteCount(usedMemory()));
    }
    
    public static String humanReadableByteCount(long bytes) {
        String s = bytes < 0 ? "-" : "";
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1000L ? bytes + " B"
                : b < 999_950L ? String.format("%s%.1f kB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
                : String.format("%s%.1f EB", s, b / 1e6);
    }
    
}
