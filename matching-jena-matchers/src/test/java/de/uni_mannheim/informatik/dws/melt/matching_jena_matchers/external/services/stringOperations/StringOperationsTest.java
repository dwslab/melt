package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.testTools.TestOperations;
import org.junit.jupiter.api.Test;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.Levenshtein;
import org.simmetrics.metrics.StringMetrics;

class StringOperationsTest {

    private static final double DELTA = 0.00001;


    @Test
    void testRemoveFreeFloatingS() {
        String[] s1 = new String[4];
        s1[0] = ("Greta's");
        s1[1] = ("home-made");
        s1[2] = ("food");
        s1[3] = ("s");
        String[] result1 = StringOperations.removeEnglishGenitiveS(s1);
        HashSet<String> result1set = new HashSet<String>(Arrays.stream(result1).collect(Collectors.toSet()));
        assertTrue(result1set.size() == 3);
        assertTrue(result1set.contains("Greta"));
        assertTrue(result1set.contains("home-made"));
        assertTrue(result1set.contains("food"));
    }


    @Test
    void testAddAlternativeWritingsSimple() {
        HashSet<String> set1 = new HashSet<>();
        set1.add("hello world-peace");
        set1.add("hello world");
        HashSet<String> result1 = StringOperations.addAlternativeWritingsSimple(set1);
        assertTrue(result1.contains("hello world-peace"));
        assertTrue(result1.contains("hello world"));
        assertTrue(result1.contains("hello worldpeace"));
        assertTrue(result1.contains("hello world peace"));

        HashSet<String> set2 = new HashSet<>();
        set2.add("hind-brain");
        HashSet<String> result2 = StringOperations.addAlternativeWritingsSimple(set2);
        assertTrue(result2.contains("hindbrain"));
        assertTrue(result2.contains("hind brain"));
        assertTrue(result2.contains("hind-brain"));
        assertTrue(result2.size() == 3);
    }


    @Test
    void splitUsingSplitWords() {
        String[] input = new String[]{"Of", "all", "the", "challenges", "and", "problems"};
        String[] output = new String[]{"all the challenges", "problems"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.splitUsingSplitWords(input), output));
    }

    @Test
    void containsSplitWords() {
        assertTrue(StringOperations.containsSplitWords(new String[]{"of", "hello"}));
        assertFalse(StringOperations.containsSplitWords(new String[]{"world", "hello"}));
        assertFalse(StringOperations.containsSplitWords("Hello World"));
        assertTrue(StringOperations.containsSplitWords("Bank of America"));
        assertTrue(StringOperations.containsSplitWords("Credit Of Other Facility"));
    }

    @Test
    void tokenizeBestGuess() {
        String[] result1 = {"hello", "WORLD", "peace"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeBestGuess("hello WORLD peace"), result1));

        String[] result2 = {"hello", "WORLD", "peace"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeBestGuess("hello_WORLD_peace"), result2));

        String[] result3 = {"Is", "Holding", "Category", "IFRS"};
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeBestGuess("IsIFRSHoldingCategory"), result3));

        String[] result4 = {"Is", "IFRS"};
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeBestGuess("IsIFRS"), result4));

        String[] result5 = {"FX", "Option"};
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeBestGuess("FXOption"), result5));

        String[] result6 = {"hello", "WORLD", "EARTH", "peace"};
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeBestGuess("hello_WORLD/EARTH_peace"), result6));
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeBestGuess("hello WORLD/EARTH peace"), result6));

        // mixtures
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeBestGuess("hello_WORLD EARTH_peace"), result6));
    }

    @Test
    void isCamelCase() {
        assertFalse(StringOperations.isCamelCase("hello_world"));
        assertFalse(StringOperations.isCamelCase("Hello World"));

        assertTrue(StringOperations.isCamelCase("helloWorld"));
        assertFalse(StringOperations.isCamelCase("helloworld"));

        // special case: abbreviations at the beginning
        assertTrue(StringOperations.isCamelCase("IFRScategory"));
        assertTrue(StringOperations.isCamelCase("FXoption"));

        assertTrue(StringOperations.isCamelCase("WhatAbeautifulDay"));
    }

    @Test
    void isSpaceCase(){
        assertFalse(StringOperations.isSpaceCase("hello_world_peace"));
        assertFalse(StringOperations.isSpaceCase("HelloWorldPeace"));
        assertTrue(StringOperations.isSpaceCase("Hello World"));
    }

    @Test
    void isUnderscoreCase(){
        assertTrue(StringOperations.isUnderscoreCase("hello_world_peace"));
        assertFalse(StringOperations.isUnderscoreCase("HelloWorldPeace"));
        assertFalse(StringOperations.isUnderscoreCase("Hello World"));
    }

    @Test
    void tokenizeCamelCase() {
        StringOperations.AbbreviationHandler lowerCaseHandler = StringOperations.AbbreviationHandler.LOWER_CASE_FOLLOWS_ABBREVIATION;
        StringOperations.AbbreviationHandler upperCaseHandler = StringOperations.AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION;
        StringOperations.AbbreviationHandler considerAllHandler = StringOperations.AbbreviationHandler.CONSIDER_ALL;

        String[] result1 = {"hello", "World"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeCamelCase("helloWorld", lowerCaseHandler), result1));

        String[] result2 = {"hello", "WORLD"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeCamelCase("helloWORLD", lowerCaseHandler), result2));

        String[] result3 = {"hello", "WORLD", "peace"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeCamelCase("helloWORLDpeace", lowerCaseHandler), result3));

        String[] result4 = {"Is", "IFRS", "holding", "Category"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeCamelCase("IsIFRSholdingCategory", lowerCaseHandler), result4));

        String[] result5 = {"Is", "IFRS"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeCamelCase("IsIFRS", lowerCaseHandler), result5));

        String[] result6 = {"Is", "IFRS", "Holding", "Category"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeCamelCase("IsIFRSHoldingCategory", upperCaseHandler), result6));

        String[] result8 = {"Is", "IFRS"};
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeCamelCase("IsIFRS", lowerCaseHandler), result8));
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeCamelCase("IsIFRS", upperCaseHandler), result8));
        assertTrue(TestOperations.isSameArrayContent(StringOperations.tokenizeCamelCase("IsIFRS", considerAllHandler), result8));
    }

    @Test
    void tokenizeSpaceCase(){
        String[] result1 = {"hello", "WORLD", "peace"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeSpaceCase("hello WORLD peace"), result1));
    }

    @Test
    void tokenizeUnderScoreCase(){
        String[] result1 = {"hello", "WORLD", "peace"};
        assertTrue(TestOperations.isSameStringArray(StringOperations.tokenizeUnderScoreCase("hello_WORLD_peace"), result1));
    }

    @Test
    void removeLanguageAnnotation(){
        assertEquals("Rubeus Hagrid", StringOperations.removeLanguageAnnotation("Rubeus Hagrid@en"));
        assertEquals("Rubeus Hagrid", StringOperations.removeLanguageAnnotation("Rubeus Hagrid@es"));
        assertEquals("Rubeus Hagrid", StringOperations.removeLanguageAnnotation("Rubeus Hagrid@fr"));
        assertEquals("Rubeus Hagrid", StringOperations.removeLanguageAnnotation("Rubeus Hagrid"));
        assertEquals("Rubeus Hagrid 2", StringOperations.removeLanguageAnnotation("Rubeus Hagrid 2"));
        assertEquals("Rubeus Hagrid en", StringOperations.removeLanguageAnnotation("Rubeus Hagrid en"));
        assertEquals("en", StringOperations.removeLanguageAnnotation("en"));
        assertEquals("e", StringOperations.removeLanguageAnnotation("e"));
        assertEquals("", StringOperations.removeLanguageAnnotation(""));
        assertEquals(null, StringOperations.removeLanguageAnnotation(null));
    }

    @Test
    void getNumberOfTokensBestGuess(){
        assertEquals(StringOperations.getNumberOfTokensBestGuess("year"), 1);
        assertEquals(StringOperations.getNumberOfTokensBestGuess("yearBook"), 2);
        assertEquals(StringOperations.getNumberOfTokensBestGuess("year Book"), 2);
    }

    @Test
    void convertToTag(){
        assertEquals(StringOperations.convertToTag("Hagrid"), "<Hagrid>");
        assertEquals(StringOperations.convertToTag("Hagrid>"), "<Hagrid>");
        assertEquals(StringOperations.convertToTag("<Hagrid"), "<Hagrid>");
        assertEquals(StringOperations.convertToTag("<Hagrid>"), "<Hagrid>");
    }

    @Test
    void cleanFromTypeAnnotation(){
        assertEquals(StringOperations.cleanValueFromTypeAnnotation("0.816318^^http://www.w3.org/2001/XMLSchema#float"), "0.816318");
    }

    @Test
    void removeTag(){
        assertEquals("Hagrid", StringOperations.removeTag("<Hagrid>"));
    }

    @Test
    void isSameString(){
        assertFalse(StringOperations.isSameStringIgnoringStopwordsAndNumbers("Pyramid","pallidum"));
        assertTrue(StringOperations.isSameStringIgnoringStopwordsAndNumbers("Pyramid","pyramid"));
        assertTrue(StringOperations.isSameStringIgnoringStopwordsAndNumbers("PyramidEgypt","pyramid_Egypt"));
        assertTrue(StringOperations.isSameStringIgnoringStopwordsAndNumbers("hair shaft","Shaft_of_the_Hair"));
        assertTrue(StringOperations.isSameStringIgnoringStopwordsAndNumbers("trunk skin","Skin_of_the_Trunk"));
    }

    @Test
    void clearArrayFromNumbers() {
        // test 1: deleting everything
        String[] array1 = {"one", "two", "three"};
        String[] result1 = StringOperations.clearArrayFromNumbers(array1);
        assertTrue(result1.length == 0);

        // test 2: keeping the order
        String[] array2 = {"X", "hello", "World", "1", "nineth"};
        String[] result2 = StringOperations.clearArrayFromNumbers(array2);
        assertTrue(result2.length == 2);
        assertTrue(result2[0].equals("hello"));
        assertTrue(result2[1].equals("World"));
    }

    @Test
    void hasSimilarTokenWriting() {
        // same number of tokens
        String[] s1 = {"hello", "World"};
        String[] s2 = {"hello", "world"};
        assertTrue(StringOperations.hasSimilarTokenWriting(s1, s2, 1));
        assertFalse(StringOperations.hasSimilarTokenWriting(s1, s2, 0));

        // different number of tokens
        String[] s3 = {"hello", "World", "peace"};
        assertFalse(StringOperations.hasSimilarTokenWriting(s1, s3, 3));
        assertFalse(StringOperations.hasSimilarTokenWriting(s3, s1, 3));
    }


    @Test
    void tokenizeCamelCaseAndSlash(){
        String input = "HelloEurope/World";
        String[] result = new String[]{"Hello", "Europe", "World"};
        assertTrue(TestOperations.isSameArrayContent(result, StringOperations.tokenizeCamelCaseAndSlash(input, StringOperations.AbbreviationHandler.UPPER_CASE_FOLLOWS_ABBREVIATION)));
    }

    @Test
    void levenshtein(){
        // this test just ensures that the given library for the Levenshtein edit distance works as intended.
        Levenshtein lev = new Levenshtein();
        assertEquals(lev.compare("A", "A"), 1.0);
        assertEquals(lev.compare("ABC", "ABD"), 1.0-(1.0/3.0), DELTA);
        assertEquals(lev.compare("Paul", "Pual"), 0.5);

        StringMetric metric = StringMetrics.levenshtein();
        assertEquals(metric.compare("A", "A"),1.0);
        assertEquals(metric.compare("ABC", "ABD"), 1.0-(1.0/3.0), DELTA);
        assertEquals(metric.compare("Paul", "Pual"), 0.5);
    }

    @Test
    void stemPorter(){
        assertEquals(StringOperations.stemPorter("historic"), "histor");
    }

    @Test
    void isMeaningfulFragment(){
        assertTrue(StringOperations.isMeaningfulFragment("humanAnatomy_123"));
        assertFalse(StringOperations.isMeaningfulFragment("NCI123"));
        assertFalse(StringOperations.isMeaningfulFragment("MA_12345"));
    }

    @Test
    void isNumberTest() {
        assertTrue(StringOperations.isNaturalNumber("1234567890"));
        assertTrue(StringOperations.isNaturalNumber("5"));
        assertTrue(StringOperations.isNaturalNumber("XXI"));
        assertTrue(StringOperations.isNaturalNumber("xxix"));
        assertTrue(StringOperations.isNaturalNumber("18"));
        assertTrue(StringOperations.isNaturalNumber("VI"));
        assertFalse(StringOperations.isNaturalNumber("1m"));
        assertFalse(StringOperations.isNaturalNumber("l09b"));
        assertFalse(StringOperations.isNaturalNumber("garden"));
    }

    @Test
    void removeNonAlphanumericCharactersTest() {
        String s1 = "Hello, World. This is my notebook! \"Hello World\" 'Hi' That's new?";
        String s1solution = "Hello World This is my notebook Hello World Hi Thats new";
        assertTrue(StringOperations.removeNonAlphanumericCharacters(s1).equals(s1solution));
    }

    @Test
    void removeEnglishGenitiveSTest() {
        String s1 = "That's really great!";
        String s1solution = "That really great!";
        assertTrue(StringOperations.removeEnglishGenitiveS(s1).equals(s1solution));
    }

    @Test
    void hasSimilarTokenWritingTest() {
        // prerequisite: make sure that the library works as intended
        Levenshtein lev = new Levenshtein();
        assertTrue(lev.distance("ABC", "ABC") == 0.0);
        assertTrue(lev.distance("ABC", "ABc") == 1.0);

        String[] s11 = {"ABC", "def"};
        String[] s12 = {"Abc", "deF"};
        assertTrue(StringOperations.hasSimilarTokenWriting(s11, s12, 3.0f));
        assertFalse(StringOperations.hasSimilarTokenWriting(s11, s12, 1.9f));
        assertTrue(StringOperations.hasSimilarTokenWriting(s11, s12, 5f));
    }

    @Test
    void isSameStringIgnoringStopwords(){
        assertTrue(StringOperations.isSameStringIgnoringStopwords("the rib 1", "rib 1"));
        assertFalse(StringOperations.isSameStringIgnoringStopwords("the rib 1", "rib 2"));
    }

    @Test
    void clearArrayFromStopwords(){
        String[] s1 = {"a", "car",  "is", "a", "vehicle"};
        String[] s1solution = {"car", "vehicle"};
        String[] s1result = StringOperations.clearArrayFromStopwords(s1);
        assertTrue(s1result.length == s1solution.length);
        for(int i = 0; i < s1result.length; i++){
            assertTrue(s1result[i] == s1result[i]);
        }
    }

    @Test
    void addTagIfNotExists(){
        assertEquals("<Hagrid>", StringOperations.addTagIfNotExists("Hagrid"));
        assertEquals("<Hagrid>", StringOperations.addTagIfNotExists("<Hagrid"));
        assertEquals("<Hagrid>", StringOperations.addTagIfNotExists("Hagrid>"));
        assertEquals("<Hagrid>", StringOperations.addTagIfNotExists("<Hagrid>"));
    }

    @Test
    void clearHashSetFromStopwords(){
        HashSet<String> set1 = new HashSet<>();
        set1.add("a");
        set1.add("car");
        set1.add("is");
        set1.add("vehicle");

        HashSet<String> set1solution = new HashSet<>();
        set1solution.add("car");
        set1solution.add("vehicle");

        HashSet<String> set1result = StringOperations.clearHashSetFromStopwords(set1);
        assertEquals(set1solution.size(), set1result.size());

        for (String s : set1solution){
            assertTrue(set1result.contains(s));
        }
    }

    @Test
    void getCommaSeparatedString(){
        HashSet<String> set1 = new HashSet<>();
        set1.add("a");
        set1.add("car");
        set1.add("is");
        set1.add("vehicle");

        assertEquals("a, car, is, vehicle".length(), StringOperations.getCommaSeparatedString(set1).length());
    }

}