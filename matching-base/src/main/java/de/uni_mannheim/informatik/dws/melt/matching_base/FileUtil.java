package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for creating files etc.
 */
public class FileUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

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
    
    /**
     * Determines the count of lines in a file in a fast way.
     * If counts the number of newlines. Thus, if the file contains only one row and no newline it will return zero.
     * @param file the file to use
     * @return the number of lines in the file
     */
    public static long lineCount(File file){
        long count = 0;
        try(InputStream is = new BufferedInputStream(new FileInputStream(file))){
            byte[] c = new byte[1024];            
            int readChars;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.info("Could not determine the number of lines", ex);
            return 0;
        }
        return count;
    }
    
    /**
     * Returns the canonical path (resolved symlink, and relative paths) of a file if possible.
     * If this calls fails, then the absolute path is returned.
     * @param file the file to get the canonical path from
     * @return the canonical path or if not possible, the absolute path 
     */
    public static String getCanonicalPathIfPossible(File file){
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            return file.getAbsolutePath();
        }
    }
    
    /**
     * Returns the canonical path (resolved symlink, and relative paths) of a file if possible.
     * If this calls fails, then the absolute path is returned.
     * @param filePath the file patha s a string. This can be a relative file path like ./fooBar
     * @return the canonical path or if not possible, the absolute path 
     */
    public static String getCanonicalPathIfPossible(String filePath){
        return getCanonicalPathIfPossible(new File(filePath));
    }
}
