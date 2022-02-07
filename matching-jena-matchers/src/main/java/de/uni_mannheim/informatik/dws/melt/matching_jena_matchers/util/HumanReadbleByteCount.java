package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Convert bytes in human readable values like 12.12GB or 5.23 MB etc.
 */
public class HumanReadbleByteCount {
    
    /**
     * Convert bytes in human readable values like 12.12GB or 5.23 MB etc.
     * @param bytes the number of bytes to convert
     * @return a string representig the number of bytes in human readable format
     */
    public static String convert(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.2f %cB", bytes / 1000.0, ci.current());
    }
}
