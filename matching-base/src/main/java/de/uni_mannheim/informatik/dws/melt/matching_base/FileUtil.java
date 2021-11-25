package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for creating files etc.
 */
public class FileUtil {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    private static final SecureRandom RANDOM = new SecureRandom();
    
    public static final File SYSTEM_TMP_FOLDER = new File(System.getProperty("java.io.tmpdir"));
    
    private static File userTmpFolder = SYSTEM_TMP_FOLDER;
    
    /**
     * Returns the folder which the user wants to use a temporary directory.
     * The default is the systems tmp.
     * @return the folder which the user wants to use a temporary directory.
     */
    public static File getUserTmpFolder(){
        return userTmpFolder;
    }
    
    /**
     * Sets a folder which represents a location for temporary data.
     * This does not necessarily needs to be the systems tmp folder but any folder the user would like to use.
     * @param newUserTmpFolder the new folder - it should be a directory and existent.
     */
    public static void setUserTmpFolder(File newUserTmpFolder){
        if(newUserTmpFolder.isDirectory() == false)
            throw new IllegalArgumentException("The given userTmpFolder is not a directory.");
        userTmpFolder = newUserTmpFolder;
    }
    
    /**
     * Creates a folder with a random number attached to the prefix in a given folder.
     * The new folder will look like: {givenfolder}/prefix-1234/
     * @param folder the folder to use
     * @param prefix the prefix for the newly created folder.
     * @return the new folder (not yet created)
     */
    public static File createFolderWithRandomNumberInDirectory(File folder, String prefix){
        long n = RANDOM.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        return new File(folder, prefix + "-" + n);
    }
    
    /**
     * Creates a new folder in the user defined tmp folder.
     * The new folder will look like: {userDefinedTmp}/prefix-1234/ (1234 is a randomly generated number)
     * @param prefix the prefix to use for the folder
     * @return the new folder (not yet created)
     */
    public static File createFolderWithRandomNumberInUserTmp(String prefix){
        return createFolderWithRandomNumberInDirectory(userTmpFolder, prefix);
    }
    
    /**
     * Create a new file in the given directory with prefix and suffix and a random number.
     * The file path will look like: {folder}/{prefix}1234{suffix}
     * @param folder the folder to use
     * @param prefix the prefix (usually the file name)
     * @param suffix suffix (usually the file extension) like .txt (the dot needs to be included
     * @return the file to use.
     */
    public static File createFileWithRandomNumber(File folder, String prefix, String suffix){
        long n = RANDOM.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        return new File(folder, prefix + Long.toString(n) + suffix);
    }
    
    /**
     * Create a new file in the user defined tmp directory with prefix and suffix and a random number.
     * The file path will look like: {userDefinedTmp}/{prefix}1234{suffix}
     * @param prefix the prefix (usually the file name)
     * @param suffix suffix (usually the file extension) like .txt (the dot needs to be included
     * @return the file to use.
     */
    public static File createFileWithRandomNumber(String prefix, String suffix){
        return createFileWithRandomNumber(userTmpFolder, prefix, suffix);
    }
    
    /**
     * Returns a positive random number which can be used to create file names with random numbers in it.
     * @return a positive random number
     */
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
