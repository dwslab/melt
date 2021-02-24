package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A class which provides supporting functionalities mainly for writing unit tests.
 */
public class TestOperations {


    private static final Logger LOGGER = LoggerFactory.getLogger(TestOperations.class);

    /**
     * Checks whether two sets contain the same contents.
     * @param s1 Set 1
     * @param s2 Set 2
     * @return True if the content of the sets is equal, else false.
     */
    public static boolean setContainsSameContent(Set s1, Set s2){
        if(s1.size() != s2.size()){
            LOGGER.error("Sets have different size.");
            return false;
        }

        for(Object o : s1){
            if(!s2.contains(o)){
                LOGGER.error(o.toString() + " not contained in both sets.");

                LOGGER.error("Contents of set 1:");
                s1.stream().forEach(x -> LOGGER.error(x.toString()));

                LOGGER.error("Contents of set 2:");
                s2.stream().forEach(x -> LOGGER.error(x.toString()));

                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether two string arrays contain the same contents.
     * @param array_1 Array 1.
     * @param array_2 Array 2.
     * @return True if the content of the arrays is equal, else false.
     */
    public static boolean isSameStringArray(String[] array_1, String[] array_2) {
        if (array_1.length != array_2.length) {
            //LOG.info("Arrays have different length.");
            return false;
        } else if(array_1 == null && array_2 == null) {
            return true;
        } else {
            for (int i = 0; i < array_1.length; i++) {
                if (!array_1[i].equals(array_2[i])) {
                    LOGGER.info(array_1[i] + " != " + array_2[i]);
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Checks whether two double arrays contain the same contents.
     * @param array_1 Array 1.
     * @param array_2 Array 2.
     * @return True if the content of the arrays is equal, else false.
     */
    public static boolean isSameDoubleArray(double[] array_1, double[] array_2) {
        if (array_1.length != array_2.length) {
            //LOG.info("Arrays have different length.");
            return false;
        } else if(array_1 == null && array_2 == null) {
            return true;
        } else {
            for (int i = 0; i < array_1.length; i++) {
                if (array_1[i] != (array_2[i])) {
                    LOGGER.info(array_1[i] + " != " + array_2[i]);
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Checks whether two double arrays contain the same contents.
     * @param array_1 Array 1.
     * @param array_2 Array 2.
     * @return True if the content of the arrays is equal, else false.
     */
    public static boolean isSameDoubleArray(Double[] array_1, Double[] array_2) {
        return isSameDoubleArray(ArrayUtils.toPrimitive(array_1), ArrayUtils.toPrimitive(array_2));
    }

    /**
     * Checks whether the String components within the two given arrays are equal. The position of the elements does not matter.
     * @param array_1 Array 1.
     * @param array_2 Array 2.
     * @return True if the content of the arrays is equal, else false.
     */
    public static boolean isSameArrayContent(String[] array_1, String[] array_2){
        if (array_1.length != array_2.length) {
            //LOG.info("Arrays have different length.");
            return false;
        } else if(array_1 == null && array_2 == null) {
            return true;
        }
        if(Arrays.asList(array_2).containsAll(Arrays.asList(array_1))){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Prints the contents of a String Array.
     * @param stringArray The String Array to be printed.
     */
    private void printStringArray(String[] stringArray){
        Arrays.stream(stringArray).forEach(System.out::println);
    }

    /**
     * Returns the key from the specified resource bundle.
     * @param resourceBundle The resource bundle name.
     * @param key The key to be looked up.
     * @return Null if nothing was found or if there was an Exception - else value as String.
     */
    public static String getStringKeyFromResourceBundle(String resourceBundle, String key){
        if(resourceBundle.endsWith(".properties")){
            resourceBundle = resourceBundle.substring(0, resourceBundle.length() - 11);
        }
        if(resourceBundle == null || key == null){
            return null;
        }
        try {
            String s = ResourceBundle.getBundle(resourceBundle).getString(key);
            return s;
        } catch (MissingResourceException mre){
            LOGGER.error("Cannot find resource file: " + resourceBundle + ".properties or key '" + key + "'");
            return null;
        }
    }

    /**
     * Delete the persistence directory.
     */
    public static void deletePersistenceDirectory() {
        PersistenceService.getService().closePersistenceService();
        File result = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if (result.exists() && result.isDirectory()) {
            try {
                FileUtils.deleteDirectory(result);
            } catch (IOException e) {
                LOGGER.error("Failed to remove persistence directory.", e);
            }
        }
    }
}
