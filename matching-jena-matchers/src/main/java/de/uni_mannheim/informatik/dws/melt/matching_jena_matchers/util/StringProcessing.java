package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class StringProcessing {


    private static final Pattern CAMEL_CASE = Pattern.compile("(?<!^)(?<!\\s|\")(?=[A-Z][a-z])");
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
        return new ArrayList<>(Arrays.asList(normalizeToStringArray(stringToBeNormalized)));
    }

    /**
     * Normalizes a string and removes all (English) stopwords. Recognizes camelCase.
     * @param stringToBeNormalized The String that shall be normalized.
     * @return Bag of words.
     */
    public static List<String> normalizeAndRemoveStopwords(String stringToBeNormalized){
        String[] tokenized = StringOperations.clearArrayFromStopwords(normalizeToStringArray(stringToBeNormalized));
        return new ArrayList<>(Arrays.asList(tokenized));
    }

    private static String[] normalizeToStringArray(String stringToBeNormalized){
        if (stringToBeNormalized == null) return new String[0];

        stringToBeNormalized = stringToBeNormalized.trim();
        stringToBeNormalized = CAMEL_CASE.matcher(stringToBeNormalized).replaceAll("_");// convert camelCase to under_score_case
        stringToBeNormalized = stringToBeNormalized.replace(' ', '_');
        stringToBeNormalized = stringToBeNormalized.toLowerCase();

        try {
            stringToBeNormalized = URLDecoder.decode(stringToBeNormalized, "UTF-8");
        } catch (Exception ex) { } // do nothing - just continue with processing
        
        // delete non alpha-numeric characters:
        stringToBeNormalized = NON_ALPHA.matcher(stringToBeNormalized).replaceAll("_");

        stringToBeNormalized = ENGLISH_GENITIVE_S.matcher(stringToBeNormalized).replaceAll("");

        //remove all multi whitespaces
        stringToBeNormalized = MULTIPLE_UNDERSCORES.matcher(stringToBeNormalized).replaceAll("_");

        String[] tokenized = stringToBeNormalized.split("_");
        return tokenized;
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
    
    public static String normalizeOnlyCamelCaseUnderscoreAndHyphen(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return "";
        
        stringToBeNormalized = stringToBeNormalized.trim();
        stringToBeNormalized = CAMEL_CASE.matcher(stringToBeNormalized).replaceAll(" ");// convert camelCase to under_score_case
        stringToBeNormalized = stringToBeNormalized.replace('_', ' ');
        stringToBeNormalized = stringToBeNormalized.replace('-', ' ');

        //remove all multi whitespaces
        stringToBeNormalized = MULTIPLE_WHITESPACE.matcher(stringToBeNormalized).replaceAll(" ");
        stringToBeNormalized = stringToBeNormalized.trim();
        return stringToBeNormalized;
    }
}
