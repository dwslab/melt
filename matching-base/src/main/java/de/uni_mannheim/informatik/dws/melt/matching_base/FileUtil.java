package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.io.File;
import java.security.SecureRandom;

/**
 * Helper for creating files etc.
 */
public class FileUtil {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    public static final File SYSTEM_TMP_FOLDER = new File(System.getProperty("java.io.tmpdir"));
    
    public static File createFolderWithRandomNumberInDirectory(File folder, String prefix){
        long n = RANDOM.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        return new File(folder, prefix + "-" + n);
    }
    public static File createFileWithRandomNumber(File folder, String prefix, String suffix){
        long n = RANDOM.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        return new File(folder, prefix + Long.toString(n) + suffix);
    }
    
    public static Long getRandomPositiveNumber(){
        long n = RANDOM.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        return n;
    }
}
