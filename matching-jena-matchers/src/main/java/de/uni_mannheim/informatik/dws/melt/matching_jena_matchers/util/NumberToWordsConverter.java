package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts number to words like 1 to one and 3 to three.
 */
public class NumberToWordsConverter {
    
    private static final Pattern numberPattern = Pattern.compile("\\d+");
    
    private Map<String, String> replacementMap;
    
    /**
     * Specify a range in which numbers should be converted to 
     * @param from from value (inclusive)
     * @param to to value (inclusive)
     */
    public NumberToWordsConverter(int from, int to){
        replacementMap = new HashMap();
        for(int i=from; i <= to; i++){
            this.replacementMap.put(Integer.toString(i), convert(i));
        }
    }
    
    /**
     * Specify a range in which numbers should be converted to 
     * @param to to value (inclusive)
     */
    public NumberToWordsConverter(int to){
        this(0, to);
    }
    
    /**
     * Default constructor which only replaces numbers between 0 and 9 (both inclusive).
     * Thus "11" is not converted to words.
     */
    public NumberToWordsConverter(){
        this(0, 9);
    }
    
    /**
     * Replace numbers in arbirary text like "hello 42 nice2have".
     * It will return "hello fourty two nicetwohave" (only in case the numbers are in the specified range).
     * @param text any text containing numbers
     * @return text with numbers replaced as words.
     */
    public String replaceNumbersInText(String text){
        Matcher matcher = numberPattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb, replacementMap.getOrDefault(matcher.group(0), matcher.group(0)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Returns the number (like "1" or "42") as words ("one" or "fourty two").
     * The parameter should only contain the number as single token and nothing more.
     * If you want to replace numbers in text use {@link #replaceNumbersInText(java.lang.String)}.
     * @param number a string containing only a number like "1" or "42"
     * @return the number as words 
     */
    public String replaceNumberToken(String number){
        return this.replacementMap.getOrDefault(number, number);
    }

    public Map<String, String> getReplacementMap() {
        return replacementMap;
    }

    /**
     * Replaces all number in text with corresponding words.
     * "hello 42 nice2have" will be converted to "hello fourty  two nicetwohave".
     * @param text any text containing numbers
     * @return text with numbers replaced as words.
     */
    public static String replaceAllNumbersInText(String text){
        Matcher matcher = numberPattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb, convert(Integer.parseInt(matcher.group(0))));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    public static String convert(String numberAsString){
        return convert(Integer.parseInt(numberAsString));
    }
    
    //Following mainly taken from:
    //https://javahungry.blogspot.com/2014/05/convert-math-number-to-equivalent-readable-word-in-java-code-with-example.html
    //did not find any licene etc
    
    private static final String[] specialNames = {
        "", " thousand", " million", " billion", " trillion", " quadrillion", " quintillion"
    };
    
    private static final String[] tensNames = {
        "", " ten", " twenty", " thirty", " forty", " fifty", " sixty", " seventy", " eighty", " ninety"
    };
    
    private static final String[] numNames = {
        "", " one", " two", " three", " four", " five", " six", " seven", " eight", " nine", " ten",
        " eleven", " twelve", " thirteen", " fourteen", " fifteen", " sixteen", " seventeen", " eighteen", " nineteen"
    };
    
    public static String convert(int number) {
        if (number == 0) { return "zero"; }        
        String prefix = "";        
        if (number < 0) {
            number = -number;
            prefix = "minus";
        }        
        String current = "";
        int place = 0;        
        do {
            int n = number % 1000;
            if (n != 0){
                String s = convertLessThanOneThousand(n);
                current = s + specialNames[place] + current;
            }
            place++;
            number /= 1000;
        } while (number > 0);        
        return (prefix + current).trim();
    }
    
    private static String convertLessThanOneThousand(int number) {
        String current;        
        if (number % 100 < 20){
            current = numNames[number % 100];
            number /= 100;
        }
        else {
            current = numNames[number % 10];
            number /= 10;
            
            current = tensNames[number % 10] + current;
            number /= 10;
        }
        if (number == 0) return current;
        return numNames[number] + " hundred" + current;
    }
    
}
