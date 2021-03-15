package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.nlp.PorterStemmer;
import org.jetbrains.annotations.NotNull;
import org.simmetrics.metrics.Levenshtein;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations.isSameArrayContent;


/**
 * A helper class for string operations.
 */
public class StringOperations {


    private static final Logger LOGGER = LoggerFactory.getLogger(StringOperations.class);

    // signal words that separate entities
    private static final HashSet<String> separatingWords = new HashSet<String>(Arrays.asList("of", "Of", "and", "And")); // further:
    // "under",
    // "on",
    // "beneath",
    // "below"
    private static HashSet<String> stopwords;
    private static final String PATH_TO_STOPWORD_FILE = "stopwords.txt";
    private static final String PATH_TO_STOPWORD_FILE_JAR = "/stopwords.txt"; // the slash is strictly required

    /**
     * Function which indicates whether a phrase is in camel case or not.
     *
     * @param phrase The phrase to be checked.
     * @return true if phrase is in camel case, else false.
     */
    static public boolean isCamelCase(String phrase) {

        Pattern pattern = Pattern.compile("[a-z][A-Z]");
        Matcher matcher = pattern.matcher(phrase);
        boolean containsLowerCaseLetterFollowedByUppercaseLetter = matcher.find();

        if (!containsLowerCaseLetterFollowedByUppercaseLetter) {
            // very seldom case for instance to catch abbreviations at the beginning like
            // "FXoption"
            pattern = Pattern.compile("[A-Z][A-Z][a-z]");
            matcher = pattern.matcher(phrase);
            containsLowerCaseLetterFollowedByUppercaseLetter = matcher.find();
        }

        // Phrase may not contain spaces or underscores but has to contain a lowercase
        // letter followed by an uppercase
        // letter.
        return (!(phrase.contains(" ") || phrase.contains("_"))) && (containsLowerCaseLetterFollowedByUppercaseLetter);
    }

    /**
     * Function which indicates whether a phrase is in underscore case or not.
     *
     * @param phrase The phrase to be checked.
     * @return True if phrase is in underscore case, else false.
     */
    static public boolean isUnderscoreCase(String phrase) {
        // phrase must contain underscores
        return (phrase.contains("_"));
    }

    /**
     * Function which indicates whether a phrase is space separated or not.
     *
     * @param phrase The phrase to be checked.
     * @return True if space-separated, else false.
     */
    static public boolean isSpaceCase(String phrase) {
        phrase = phrase.trim();
        return (phrase.contains(" "));
    }

    /**
     * Enum which indicates how shortcuts in camel case are handeled. Example:
     * "IsIFRSHoldingCategory"
     * <p>
     * 1) LOWER_CASE_FOLLOWS_ABBREVIATION → {"Is", "IFRSH", "olding", "Category"}
     * <p>
     * 2) UPPER_CASE_FOLLOWS_ABBREVIATION → {"Is", "IFRS", "Holding", "Category"}
     * <p>
     * 3) CONSIDER_ALL → {"Is", "IFRS", "Holding", "Category", "olding", "IFRSH"}
     */
    public enum AbbreviationHandler {
        LOWER_CASE_FOLLOWS_ABBREVIATION, UPPER_CASE_FOLLOWS_ABBREVIATION, CONSIDER_ALL
    }

    /**
     * Given a camel cased String, this method will split it into multiple tokens.
     *
     * @param phrase  The phrase to be tokenized.
     * @param handler Determines how to handle abbreviations.
     * @return The tokens of the phrase.
     */
    static public String[] tokenizeCamelCase(String phrase, AbbreviationHandler handler) {
        try {
            ArrayList<String> list = new ArrayList<String>();
            char[] phraseAsCharacterArray = phrase.toCharArray();

            int lastCut = 0;
            boolean isUppercaseRow = false; // example: IsIFRSholdingCategory → the "I" starts the uppercase row
            // "IFRS"
            boolean isTwoCutRelevant = false; // only relevant for CONSIDER_ALL option

            for (int i = 0; i < phraseAsCharacterArray.length; i++) {

                if (isUppercaseRow) {
                    if (Character.isLowerCase(phraseAsCharacterArray[i])) {

                        switch (handler) {
                            case LOWER_CASE_FOLLOWS_ABBREVIATION:
                                list.add(phrase.substring(lastCut, i));
                                lastCut = i;
                                break;

                            case UPPER_CASE_FOLLOWS_ABBREVIATION:
                                list.add(phrase.substring(lastCut, i - 1));
                                lastCut = i - 1;
                                break;

                            case CONSIDER_ALL:
                                list.add(phrase.substring(lastCut, i));
                                list.add(phrase.substring(lastCut, i - 1));
                                lastCut = i - 1;
                                isTwoCutRelevant = true;
                                break;
                        }
                        isUppercaseRow = false;

                    } else {
                        continue;
                    }
                }

                if (i < phraseAsCharacterArray.length - 1 && Character.isUpperCase(phraseAsCharacterArray[i])
                        && !Character.isUpperCase(phraseAsCharacterArray[i + 1])) {

                    list.add(phrase.substring(lastCut, i));

                    if (handler == AbbreviationHandler.CONSIDER_ALL && isTwoCutRelevant) {
                        list.add(phrase.substring(lastCut + 1, i));
                        isTwoCutRelevant = false;
                    }

                    lastCut = i;

                } else if (i < phraseAsCharacterArray.length - 1 && Character.isUpperCase(phraseAsCharacterArray[i])
                        && Character.isUpperCase(phraseAsCharacterArray[i + 1])) {
                    // character is uppercase and subsequent character is uppercase
                    // → add token and find end of uppercase row
                    list.add(phrase.substring(lastCut, i));
                    lastCut = i;
                    isUppercaseRow = true;
                }

            }

            // add the last cut
            list.add(phrase.substring(lastCut, phraseAsCharacterArray.length));

            if (handler == AbbreviationHandler.CONSIDER_ALL && isTwoCutRelevant) {
                list.add(phrase.substring(lastCut + 1, phraseAsCharacterArray.length));
                isTwoCutRelevant = false;
            }

            list.remove("");
            String[] result = new String[list.size()];

            int i = 0;
            for (String s : list) {
                result[i] = s;
                i++;
            }

            return result;

        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // keep the program running in case of any errors

            aioobe.printStackTrace();
            String[] result = new String[1];
            result[0] = phrase;
            return result;
        }
    }

    /**
     * Tokenizes phrase using strings.
     *
     * @param phrase The phrase to be tokenized.
     * @return The tokens of the phrase.
     */
    public static String[] tokenizeSpaceCase(String phrase) {
        return phrase.split(" ");
    }

    /**
     * Tokenizes phrase using lower scores.
     *
     * @param phrase The phrase to be tokenized.
     * @return The tokens of the phrase.
     */
    public static String[] tokenizeUnderScoreCase(String phrase) {
        return phrase.split("_");
    }

    /**
     * A method which prints the content of a string array to the command line.
     *
     * @param stringArray Array to be printed.
     */
    public static void printStringArray(String[] stringArray) {
        Arrays.stream(stringArray).forEach(System.out::println);
    }

    /**
     * Given an arbitrary phrase, the method determines which casing is used and
     * applies the suited tokenizer. The tokenizer is not very aggressive. A '-' for
     * instance, will not be used as splitter. For camel cased phrases with
     * abbreviations, all combinations are determined if no handler is defined.
     *
     * @param phrase  The phrase to be tokenized.
     * @param handler The handler which determines how abbreviations shall be handled.
     * @return Tokens.
     */
    static public String[] tokenizeBestGuess(String phrase, AbbreviationHandler handler) {
        if (isCamelCase(phrase)) {
            return tokenizeCamelCaseAndSlash(phrase, handler);
        } else {
            return tokenizeWithoutCamelCaseRecognition(phrase);
        }
    }

    /**
     * Split using slash, underscore and space.
     *
     * @param phrase Phrase to be splitted.
     * @return Array of individual tokens.
     */
    static private String[] tokenizeWithoutCamelCaseRecognition(String phrase) {
        return phrase.split(" |/|_");
    }

    /**
     * Tokenize and use camelCase and slashes as tokenization tokens.
     *
     * @param phrase  The phrase to be tokenized.
     * @param handler Abbreviation handler.
     * @return String array of tokens.
     */
    static public String[] tokenizeCamelCaseAndSlash(String phrase, AbbreviationHandler handler) {
        String[] slashTokens = phrase.split("/");
        if (slashTokens.length == 1) {
            return tokenizeCamelCase(phrase, handler);
        }
        ArrayList<String> result = new ArrayList<>();
        for (String s : slashTokens) {
            String[] r = tokenizeCamelCase(s, handler);
            for (String t : r) {
                result.add(t);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * Given an arbitrary phrase, the method determines which casing is used and
     * applies the suited tokenizer. For camel cased phrases with abbreviations, it
     * is assumed that an upper case follows an abbreviation.
     *
     * @param phrase The phrase to be tokenized.
     * @return Tokens.
     */
    static public String[] tokenizeBestGuess(String phrase) {
        return tokenizeBestGuess(phrase, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);
    }

    /**
     * Returns the number of tokens that were found in a phrase.
     *
     * @param phrase  The phrase to be checked.
     * @param handler defines the handling of abbreviations. Note that
     *                AbbreviationHandler.CONSIDER_ALL leads to more tokens than
     *                actually exist because combinations are employed.
     * @return Number of tokens.
     */
    static public int getNumberOfTokensBestGuess(String phrase, AbbreviationHandler handler) {
        return tokenizeBestGuess(phrase, handler).length;
    }

    /**
     * Returns the number of tokens that were found in a phrase. Note that the
     * number of tokens is obtained using
     * AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION in the default case. Note
     * further that stopword removal is not taken into account. Be careful when
     * mixing with stopword removal.
     *
     * @param phrase The phrase that shall be checked.
     * @return The number of tokens.
     */
    static public int getNumberOfTokensBestGuess(String phrase) {
        return tokenizeBestGuess(phrase, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION).length;
    }

    /**
     * @param phrase The phrase to be checked.
     * @return True if the phrase contains split words.
     */
    static public boolean containsSplitWords(String phrase) {
        String[] phraseTokens = phrase.trim().split(" ");
        phraseTokens = Arrays.stream(phraseTokens).map(s -> s.toLowerCase()).toArray(String[]::new);
        for (String s : phraseTokens) {
            if (separatingWords.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param phraseTokens The tokens that shall be processed.
     * @return True if the tokens contain split words, else false.
     */
    static public boolean containsSplitWords(String[] phraseTokens) {
        for (String s : phraseTokens) {
            if (separatingWords.contains(s)) {
                return true;
            }
        }
        return false;
    }

    static public String[] splitUsingSplitWords(String[] phraseTokens) {
        LinkedList<String> resultTokens = new LinkedList<>();

        if (containsSplitWords(phraseTokens)) {

            int lastSplit = 0;
            for (int i = 0; i < phraseTokens.length; i++) {
                if (containsSplitWords(phraseTokens[i])) {
                    if (i != 0) {
                        resultTokens.add(concatArray(Arrays.copyOfRange(phraseTokens, lastSplit, i)));
                    }
                    lastSplit = i + 1;
                }
            }

            // add last bit
            resultTokens.add(concatArray(Arrays.copyOfRange(phraseTokens, lastSplit, phraseTokens.length)));

            return resultTokens.stream().toArray(String[]::new);

        } else {
            String resultToken = concatArray(phraseTokens);
            String[] resultArray = new String[1];
            resultArray[0] = resultToken;
            return resultArray;
        }
    }

    /**
     * Concatenates a string array to one string separated by spaces.
     *
     * @param array Array that shall be concatenated.
     * @return Concatenated array as String.
     */
    private static String concatArray(String[] array) {
        StringBuffer resultBuffer = new StringBuffer();
        for (String s : array) {
            resultBuffer.append(" " + s);
        }
        return resultBuffer.toString().trim();
    }

    /**
     * This method removes illegal characters of a string when used in a SPARQL
     * query.
     *
     * @param inputString Input String.
     * @return Edited String.
     */
    public static String cleanStringForDBpediaQuery(String inputString) {

        String outputString = inputString;

        // illegal characters
        outputString = outputString.replace("<", "");
        outputString = outputString.replace(">", "");
        outputString = outputString.replace("|", "");
        outputString = outputString.replace("\"", "");
        // outputString = outputString.replace("/", "");

        // space replacement
        outputString = outputString.replace(" ", "_");

        return outputString;
    }

    /**
     * Cleans a string from anything that is not a letter.
     *
     * @param string String to be cleaned.
     * @return Cleaned String.
     */
    public static String reduceToLettersOnly(String string) {
        return string.replaceAll("[^a-zA-Z1-9 ]", "");
    }

    /**
     * This method writes the content of a {@code Set<String>} to a file. The file will be UTF-8 encoded.
     *
     * @param fileToWrite    File which will be created and in which the data will
     *                       be written.
     * @param setToWrite Set whose content will be written into fileToWrite.
     * @param <T> Type of the Set.
     */
    public static <T> void writeSetToFile(File fileToWrite, Set<T> setToWrite) {
        LOGGER.info("Start writing Set to file '" + fileToWrite.getName() + "'");
        Iterator<T> iterator = setToWrite.iterator();
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToWrite), StandardCharsets.UTF_8));
            String line;
            boolean firstLine = true;
            while (iterator.hasNext()) {
                line = iterator.next().toString();
                if (!(line.equals("") || line.equals("\n"))) { // do not write empty lines or just line breaks
                    if (firstLine) {
                        writer.write(line);
                        firstLine = false;
                    } else {
                        writer.write("\n");
                        writer.write(line);
                    }
                }
            } // end while
            writer.flush();
            writer.close();
            LOGGER.info("Finished writing file '" + fileToWrite.getName() + "'");
        } catch (IOException e) {
            LOGGER.error("Could not write file.", e);
        }
    }

    /**
     * Reads a Set from the file as specified by the file path.
     *
     * @param filePath The path to the file that is to be read.
     * @return The parsed file as HashSet.
     */
    public static @NotNull Set<String> readSetFromFile(String filePath) {
        return readSetFromFile(new File(filePath));
    }

    /**
     * Reads a Set from the file as specified by the file.
     *
     * @param file The file that is to be read.
     * @return The parsed file as HashSet.
     */
    public static @NotNull Set<String> readSetFromFile(File file) {
        Set<String> result = new HashSet<>();
        if (!file.exists()) {
            LOGGER.error("File does not exist.");
            return result;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found.", e);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("IOException occurred.", e);
            e.printStackTrace();
        }
        LOGGER.info("Entities read into cache.");
        return result;
    }

    /**
     * Converts a string to a tag. Example: "Hagrid" will be converted to
     * {@literal "<Hagrid>"}. If the string is already a tag, the string will be
     * returned as it is.s
     *
     * @param stringToConvert The String which shall be converted to a tag.
     * @return The String as tag.
     */
    public static String convertToTag(String stringToConvert) {
        if (stringToConvert == null) {
            return null;
        }
        if (!stringToConvert.startsWith("<")) {
            stringToConvert = "<" + stringToConvert;
        }
        if (!stringToConvert.endsWith(">")) {
            stringToConvert = stringToConvert + ">";
        }
        return stringToConvert;
    }

    /**
     * Removes the tags of a tag. Example: {@literal "<Hagrid>"} will be converted
     * to "Hagrid".
     *
     * @param tagToConvert The tag which shall be converted.
     * @return The string as non-tag.
     */
    public static String removeTag(String tagToConvert) {
        if (tagToConvert.startsWith("<")) {
            tagToConvert = tagToConvert.substring(1, tagToConvert.length());
        }
        if (tagToConvert.endsWith(">")) {
            tagToConvert = tagToConvert.substring(0, tagToConvert.length() - 1);
        }
        return tagToConvert;
    }

    /**
     * Adds tags if they are not there yet. {@literal "<Hagrid>"} will be converted
     * to {@literal "<Hagrid>"},  {@literal "Hagrid"} will be converted
     * to {@literal "<Hagrid>"}, {@literal "<Hagrid"} will be converted
     * to {@literal "<Hagrid>"} etc.
     * @param addTagString String to which tags shall be added.
     * @return Tagged string.
     */
    public static String addTagIfNotExists(String addTagString){
        if(!addTagString.startsWith("<")){
            addTagString = "<" + addTagString;
        }
        if(!addTagString.endsWith(">")){
            addTagString = addTagString + ">";
        }
        return addTagString;
    }

    /**
     * Remove the plural in English words.
     * @param stringToBeModified The string that shall be modified.
     * @return Modified string.
     */
    public static String removeEnglishPlural(String stringToBeModified){
        if(stringToBeModified.endsWith("ies")){
            stringToBeModified = stringToBeModified.substring(0, stringToBeModified.length() - 3) + "y";
        } else if(stringToBeModified.endsWith("s")){
            stringToBeModified = stringToBeModified.substring(0, stringToBeModified.length() - 1);
        }
        return stringToBeModified;
    }

    /**
     * Removes the language annotation from a string. If the string does not have a
     * language annotation, the string will be returned unchanged. Example:
     * "Hagrid@en" will be changed to "Hagrid".
     *
     * @param s String to be changed.
     * @return String without language annotation.
     */
    public static String removeLanguageAnnotation(String s) {
        if (s == null) {
            return null;
        }
        char[] c = s.toCharArray();
        if (c.length < 3) {
            return s;
        }
        if (c[c.length - 3] == '@') {
            return s.substring(0, c.length - 3);
        } else {
            return s;
        }
    }

    /**
     * Will clean a value from a type annotation. Example.
     * "0.816318^^http://www.w3.org/2001/XMLSchema#float" will be cleaned to
     * 0.816318.
     *
     * @param valueToClean The value that shall be cleaned.
     * @return The cleaned value as String.
     */
    public static String cleanValueFromTypeAnnotation(String valueToClean) {
        int index = valueToClean.indexOf("^^");
        return valueToClean.substring(0, index);
    }

    /**
     * This method checks whether two Strings are very similar by performing simple
     * string operations including Porter's stemmer.
     *
     * @param s1 String 1.
     * @param s2 String 2.
     * @return boolean
     */
    public static boolean isSameStringStemming(String s1, String s2) {
        if (s1.equalsIgnoreCase(s2)) {
            return true;
        }
        String[] sArray1 = tokenizeBestGuess(s1, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);
        String[] sArray2 = tokenizeBestGuess(s2, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);

        for (int i = 0; i < sArray1.length; i++) {
            sArray1[i] = sArray1[i].toLowerCase();
        }
        for (int i = 0; i < sArray2.length; i++) {
            sArray2[i] = sArray2[i].toLowerCase();
        }

        sArray1 = clearArrayFromStopwords(sArray1);
        sArray2 = clearArrayFromStopwords(sArray2);
        if (isSameArrayContent(sArray1, sArray2)) {
            return true;
        }

        for (int i = 0; i < sArray1.length; i++) {
            sArray1[i] = stemPorter(sArray1[i]);
        }
        for (int i = 0; i < sArray2.length; i++) {
            sArray2[i] = stemPorter(sArray2[i]);
        }

        return isSameArrayContent(sArray1, sArray2);
    }

    /**
     * This method checks whether two Strings are very similar by performing simple
     * string operations. Stopwords are retained.
     *
     * @param s1 String 1
     * @param s2 String 2
     * @return boolean
     */
    public static boolean isSameString(String s1, String s2) {
        if (s1.equalsIgnoreCase(s2)) {
            return true;
        }
        String[] sArray1 = tokenizeBestGuess(s1, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);
        String[] sArray2 = tokenizeBestGuess(s2, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);

        for (int i = 0; i < sArray1.length; i++) {
            sArray1[i] = sArray1[i].toLowerCase();
        }
        for (int i = 0; i < sArray2.length; i++) {
            sArray2[i] = sArray2[i].toLowerCase();
        }
        return isSameArrayContent(sArray1, sArray2);
    }

    public static boolean isSameStringIgnoringStopwordsAndNumbersWithSpellingCorrection(String s1, String s2, float maxAllowedEditDistance) {
        if (s1 == null || s2 == null) {
            return false;
        }
        if (s1.equalsIgnoreCase(s2)) {
            return true;
        }

        // some normaization: removing numbers, lower-casing, removing non-alphanumeric characters
        s1 = StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(s1)).toLowerCase();
        s2 = StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(s2)).toLowerCase();

        String[] sArray1 = tokenizeBestGuess(s1, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);
        String[] sArray2 = tokenizeBestGuess(s2, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);

        for (int i = 0; i < sArray1.length; i++) {
            sArray1[i] = sArray1[i].toLowerCase();
        }
        for (int i = 0; i < sArray2.length; i++) {
            sArray2[i] = sArray2[i].toLowerCase();
        }
        sArray1 = clearArrayFromStopwords(sArray1);
        sArray2 = clearArrayFromStopwords(sArray2);
        sArray1 = clearArrayFromNumbers(sArray1); // removes also roman numbers
        sArray2 = clearArrayFromNumbers(sArray2); // removes also roman numbers

        if (isSameArrayContent(sArray1, sArray2)) {
            return true;
        } else return hasSimilarTokenWriting(sArray1, sArray2, maxAllowedEditDistance);
    }

    /**
     * Checks whether two arrays have a similar writing. Every token is matched to its most similar token.
     * Tokens can be used multiple times.
     *
     * @param sarray1   Array 1
     * @param sarray2   Array 2
     * @param tolerance The minimal tolerance that is allowed.
     * @return True if the distance is less or equal to the allowed distance.
     */
    public static boolean hasSimilarTokenWriting(String[] sarray1, String[] sarray2, float tolerance) {
        float totalDistance = Math.max(getLevenshteinDistanceSimilarTokensOneWay(sarray1, sarray2), getLevenshteinDistanceSimilarTokensOneWay(sarray2, sarray1));
        return totalDistance <= tolerance;
    }

    /**
     * Return the Levenshtein similarity between two token sets.
     * This is only a one-way test: if sarray2 contains all tokens of sarray1, then the distance will be 0
     * even though sarray2 might contain additional tokens that are not contained in sarray2.
     * Tokens can be used multiple times
     *
     * @param sarray1 Array 1
     * @param sarray2 Array 2
     * @return Distance as float.
     */
    public static float getLevenshteinDistanceSimilarTokensOneWay(String[] sarray1, String[] sarray2) {
        Levenshtein levenshtein = new Levenshtein();
        float totalDistance = 0;
        // match from sarray1 to sarray2
        outer:
        for (String s1 : sarray1) {
            float minLevenshtein1 = -1;
            for (String s2 : sarray2) {
                float distance = levenshtein.distance(s1, s2);
                if (distance == 0) {
                    continue outer;
                } else if (minLevenshtein1 == -1 || distance < minLevenshtein1) {
                    // initial distance
                    minLevenshtein1 = distance;
                }
            }
            totalDistance = totalDistance + minLevenshtein1;
        } // end of outer loop
        return totalDistance;
    }

    /**
     * This method checks whether two Strings are very similar by performing simple
     * string operations. Stopwords are removed.
     *
     * @param s1 String 1
     * @param s2 String 2
     * @return boolean
     */
    public static boolean isSameStringIgnoringStopwords(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        if (s1.equalsIgnoreCase(s2)) {
            return true;
        }

        // some normalization: removing non-alphanumeric characters
        // do not lowercase yet because tokenization did not take place
        s1 = StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(s1));
        s2 = StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(s2));

        String[] sArray1 = tokenizeBestGuess(s1, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);
        String[] sArray2 = tokenizeBestGuess(s2, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);

        // lowercase tokens
        for (int i = 0; i < sArray1.length; i++) {
            sArray1[i] = sArray1[i].toLowerCase();
        }
        for (int i = 0; i < sArray2.length; i++) {
            sArray2[i] = sArray2[i].toLowerCase();
        }
        sArray1 = clearArrayFromStopwords(sArray1);
        sArray2 = clearArrayFromStopwords(sArray2);
        return isSameArrayContent(sArray1, sArray2);
    }

    /**
     * This method checks whether two Strings are very similar by performing simple
     * string operations. Stopwords and numbers are removed.
     *
     * @param s1 String 1
     * @param s2 String 2
     * @return boolean
     */
    public static boolean isSameStringIgnoringStopwordsAndNumbers(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        if (s1.equalsIgnoreCase(s2)) {
            return true;
        }

        // some normalization: removing non-alphanumeric characters
        // do not lowercase yet because tokenization did not take place
        s1 = StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(s1));
        s2 = StringOperations.removeNonAlphanumericCharacters(StringOperations.removeEnglishGenitiveS(s2));

        String[] sArray1 = tokenizeBestGuess(s1, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);
        String[] sArray2 = tokenizeBestGuess(s2, AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION);

        // lowercase tokens
        for (int i = 0; i < sArray1.length; i++) {
            sArray1[i] = sArray1[i].toLowerCase();
        }
        for (int i = 0; i < sArray2.length; i++) {
            sArray2[i] = sArray2[i].toLowerCase();
        }
        sArray1 = clearArrayFromStopwords(sArray1);
        sArray2 = clearArrayFromStopwords(sArray2);
        sArray1 = clearArrayFromNumbers(sArray1); // removes also Roman numbers
        sArray2 = clearArrayFromNumbers(sArray2); // removes also Roman numbers
        return isSameArrayContent(sArray1, sArray2);
    }

    /**
     * Returns an array cleaned from stopwords. Retains the ordering.
     *
     * @param arrayWithStopwords Array with stopwords.
     * @return Array without stopwords.
     */
    public static String[] clearArrayFromStopwords(String[] arrayWithStopwords) {
        lazyInitStopwords();
        LinkedList<String> resultList = new LinkedList<>();
        Arrays.stream(arrayWithStopwords).filter(term -> !stopwords.contains(term.toLowerCase()))
                .forEach(resultList::add);
        String[] result = new String[resultList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) resultList.toArray()[i];
        }
        return result;
    }

    /**
     * Removes the stopwords from the given HashSet.
     *
     * @param hashSetWithStopwords HashSet from which the stopwords shall be removed.
     * @return Cleared HashSet
     */
    public static HashSet<String> clearHashSetFromStopwords(HashSet<String> hashSetWithStopwords) {
        lazyInitStopwords();
        return hashSetWithStopwords.stream().filter(term -> !stopwords.contains(term.toLowerCase())).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Removes free floating "s", "S", and cuts "'s".
     *
     * @param array Array to be transformed.
     * @return New array.
     */
    public static String[] removeEnglishGenitiveS(String[] array) {
        HashSet<String> result = new HashSet<>(Arrays.stream(array).collect(Collectors.toSet()));

        // remove free floating "S"
        result.remove("s");
        result.remove("S");

        // transform
        ArrayList<String> list = new ArrayList<>();
        for (String s : result) {
            if (s.endsWith("'s")) {
                list.add(s);
            }
        }

        for (String s : list) {
            result.remove(s);
            result.add(s.substring(0, s.length() - 2));
        }

        return result.toArray(new String[0]);
    }

    /**
     * Remove free floating s from the given set.
     *
     * @param set Set from which s shall be removed.
     * @return Set with removed s/S.
     */
    public static HashSet<String> removeEnglishGenitiveS(HashSet<String> set) {
        set.remove("s");
        set.remove("S");
        return set;
    }

    /**
     * Wrapping of Porter's Stemming Code.
     *
     * @param word Word to be stemmed.
     * @return Stemmed word.
     */
    public static String stemPorter(String word) {
        PorterStemmer s = new PorterStemmer();
        char[] charsequence = word.toCharArray();
        s.add(charsequence, charsequence.length);
        s.stem();
        return s.toString();
    }

    /**
     * Initialize reading stopwords file if it has not been read before.
     */
    private static void lazyInitStopwords() {
        if (stopwords == null || stopwords.size() == 0) {
            InputStream inputStream = StringOperations.class.getResourceAsStream(PATH_TO_STOPWORD_FILE_JAR);
            if (inputStream == null) {
                LOGGER.error("Did not find resource.");
            }
            TermFromFileReader termFromFileReader = new TermFromFileReader(inputStream);
            stopwords = termFromFileReader.getReadLines();
        }
    }

    /**
     * Initialize reading stopwords.
     */
    public static void initStopwords() {
        TermFromFileReader termFromFileReader = new TermFromFileReader(PATH_TO_STOPWORD_FILE);
        stopwords = termFromFileReader.getReadLines();
    }

    /**
     * Checks whether a fragment is meaningful by counting the number of digits.
     *
     * @param fragment The fragment for which relevance shall be checked.
     * @return Returns false if at least half of the fragment is composed of digits.
     */
    public static boolean isMeaningfulFragment(String fragment) {
        if (fragment.length() / 2.0 > fragment.replaceAll("\\D", "").length()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Generate alternative writings (particularly interesting for English and German hyphenation).
     *
     * @param set The set which shall be processed..
     * @return The new set with alternative writings.
     */
    public static HashSet<String> addAlternativeWritingsSimple(HashSet<String> set) {
        HashSet<String> toBeAdded = new HashSet<>();
        for (String setComponent : set) {
            if (setComponent.contains("-")) {
                toBeAdded.add(setComponent.replaceAll("-", "").toLowerCase()); // Example: Hind-Brain -> hindbrain
                toBeAdded.add(setComponent.replaceAll("-", " "));
            }
        }
        set.addAll(toBeAdded);
        return set;
    }

    /**
     * Remove numbers from a set of strings.
     *
     * @param set Set from which numbers shall be removed.
     * @return A new set with no number instances.
     */
    public static HashSet<String> removeNumbers(HashSet<String> set) {
        HashSet<String> result = new HashSet<>();
        for (String s : set) {
            ArrayList<String> resultSetComponent = new ArrayList<String>();
            String[] tokens = StringOperations.tokenizeBestGuess(s);
            for (String token : tokens) {
                if (isNaturalNumber(token) || isEnglishNumberWord(token)) {
                    // do nothing
                } else {
                    resultSetComponent.add(token);
                }
            }
            // build string with space separation
            String newString = "";
            for (String component : resultSetComponent) {
                newString = newString + component + " ";
            }
            // cut last space and add to result
            result.add(newString.substring(0, newString.length() - 1));
        }
        return result;
    }

    /**
     * Given a String array, numeric tokens will be removed.
     *
     * @param array The array from which numeric components shall be removed.
     * @return The new array will be of smaller length while the order of tokens will be retained.
     */
    public static String[] clearArrayFromNumbers(String[] array) {
        ArrayList<String> resultList = new ArrayList<String>();
        for (String s : array) {
            if (isNaturalNumber(s) || isEnglishNumberWord(s)) {
                // do nothing
            } else {
                resultList.add(s);
            }
        }
        return resultList.toArray(new String[0]);
    }

    /**
     * Returns whether the stringToBeChecked is a number e.g. '123' or 'XI'.
     * For reasons of performance, the syntax of roman numbers is not checked.
     *
     * @param stringToBeChecked The string for numeric properties shall be checked.
     * @return True if roman or arabic number, else false.
     */
    public static boolean isNaturalNumber(String stringToBeChecked) {
        char[] tokenArray = stringToBeChecked.trim().toCharArray();
        boolean roman = true;
        boolean arabic = true;
        for (int i = 0; i < tokenArray.length; i++) {
            char c = tokenArray[i];
            if (arabic && (c >= 48 && c <= 57)) {
                roman = false;
                if (i == tokenArray.length - 1) {
                    return true;
                }
            } else if (roman && (c == 'I' || c == 'V' || c == 'X' || c == 'C' || c == 'L' || c == 'd' || c == 'M' ||
                    c == 'i' || c == 'v' || c == 'x' || c == 'c' || c == 'l' || c == 'd' || c == 'm')) {
                arabic = false;
                if (i == tokenArray.length - 1) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * A set containing nominal and cardinal numbers from 1 to 1ß
     */
    public static HashSet<String> ENGLISH_NUMBER_WORDS_SET =
            new HashSet<String>(
                    Arrays.asList("zero", "one", "two", "three", "four", "five", "six", "seven", "eight",
                            "nine", "ten", "first", "second", "third", "fourth", "fifth", "sixth",
                            "seventh", "eighth", "nineth", "tenth", "0th", "1st", "2nd", "3rd", "4th",
                            "5th", "6th", "7th", "8th", "9th", "10th"));

    /**
     * Checks whether the stringToBeChecked is a nominal or cardinal number in English in written
     * format. The number must be between 0 and 10 in order to be detected.
     *
     * @param stringToBeChecked The string that shall be checked.
     * @return True if the String is an English number word (e.g. 'nine' or 'fifth'), else false.
     */
    public static boolean isEnglishNumberWord(String stringToBeChecked) {
        stringToBeChecked = stringToBeChecked.trim().toLowerCase();
        return ENGLISH_NUMBER_WORDS_SET.contains(stringToBeChecked);
    }

    /**
     * Removes everything that is not a digit, character, space, or underscore. Note: In English, this may lead to a concatenations
     * of the genitive s together with the latter word e.g. that's → thats. It might make sense to remove those first.
     *
     * @param stringWithPunctuation String with punctuation.
     * @return String without punctuation.
     */
    public static String removeNonAlphanumericCharacters(String stringWithPunctuation) {
        return stringWithPunctuation.replaceAll("[^a-zA-Z\\d\\s:_]", ""); // regex: [^a-zA-Z\d\s:]
    }

    /**
     * Removes the English genitive s.
     *
     * @param string String that might contain genitive s.
     * @return Edited String.
     */
    public static String removeEnglishGenitiveS(String string) {
        return string.replace("'s", "");
    }

    /**
     * Get a comma separated list of the given {@code HashSet<String>}.
     *
     * @param set The set that shall be represented as comma separated String.
     * @return The elements of the Set in a String separated by a comma.
     */
    public static String getCommaSeparatedString(HashSet<String> set) {
        String result = "";
        for (String s : set) {
            result = result + s + ", ";
        }
        return result.substring(0, result.length() - 2);
    }
}
