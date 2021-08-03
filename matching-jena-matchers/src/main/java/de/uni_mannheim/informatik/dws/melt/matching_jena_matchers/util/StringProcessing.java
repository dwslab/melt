package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class StringProcessing {
    private static final Pattern CAMEL_CASE = Pattern.compile("(?<!^)(?<!\\s)(?=[A-Z][a-z])");
    private static final Pattern NON_ALPHA = Pattern.compile("[^a-zA-Z\\d\\s:_]");// regex: [^a-zA-Z\d\s:]
    private static final Pattern ENGLISH_GENITIVE_S = Pattern.compile("'s");
    private static final Pattern MULTIPLE_UNDERSCORES = Pattern.compile("_+");
    private static final Pattern MULTIPLE_WHITESPACE = Pattern.compile(" +");
    
    /**
     * Normalizes a string. Recognizes camelCase.
     *
     * @param stringToBeNormalized The String that shall be normalized.
     * @return Bag of Words
     */
    public static List<String> normalize(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return new ArrayList<>();
        
        stringToBeNormalized = stringToBeNormalized.trim();
        stringToBeNormalized = CAMEL_CASE.matcher(stringToBeNormalized).replaceAll("_");// convert camelCase to under_score_case
        stringToBeNormalized = stringToBeNormalized.replace(' ', '_');
        stringToBeNormalized = stringToBeNormalized.toLowerCase();

        // delete non alpha-numeric characters:
        stringToBeNormalized = NON_ALPHA.matcher(stringToBeNormalized).replaceAll("_");
        
        stringToBeNormalized = ENGLISH_GENITIVE_S.matcher(stringToBeNormalized).replaceAll("");
        
        //remove all multi whitespaces
        stringToBeNormalized = MULTIPLE_UNDERSCORES.matcher(stringToBeNormalized).replaceAll("_");
        
        String[] tokenized = stringToBeNormalized.split("_");
        return new ArrayList<>(Arrays.asList(tokenized));
    }
    
    public static boolean containsMostlyNumbers(String term) {
        int numbers = 0;
        int allNonWhiteSpace = 0;
        for (int i = 0; i < term.length(); i++) {
            char c = term.charAt(i);
            if (c >= '0' && c <= '9') {
                numbers++;
            }
            if(Character.isWhitespace(c) == false){
                allNonWhiteSpace++;
            }
        }
        if (numbers >= allNonWhiteSpace / 2)
            return true;
        return false;
    }
    
    public static String normalizeOnlyCamelCaseAndUnderscore(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return "";
        
        stringToBeNormalized = stringToBeNormalized.trim();
        stringToBeNormalized = CAMEL_CASE.matcher(stringToBeNormalized).replaceAll(" ");// convert camelCase to under_score_case
        stringToBeNormalized = stringToBeNormalized.replace('_', ' ');

        //remove all multi whitespaces
        stringToBeNormalized = MULTIPLE_WHITESPACE.matcher(stringToBeNormalized).replaceAll(" ");
        return stringToBeNormalized;
    }
}
