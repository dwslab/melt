package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replace multiple texts at once.
 */
public class MultipleTextReplacement {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleTextReplacement.class);
    
    
    private Pattern pattern;
    private Map<String, String> replacementLookup;
    
    
    
    /**
     * Initializes this object with a list of replacements. The order is important.
     * If the keys share the same prefix the one which comes first is used for replacement.
     * E.g. aa -&gt; x and aaa -&gt; y and input will be aaa, it will return aax
     * @param replacements map where key is the text to search for and value is the replacement text.
     * @param wholeWordsOnly if true, matches only whole words. if false, then also text within a word can be matched
     */
    public MultipleTextReplacement(List<Entry<String, String>> replacements, boolean wholeWordsOnly){
        StringJoiner textualPattern = new StringJoiner("|");
        this.replacementLookup = new HashMap();
        for(Entry<String, String> replacement : replacements){
            if(wholeWordsOnly){
                textualPattern.add("\\b" + Pattern.quote(replacement.getKey()) + "\\b");
            }else{
                textualPattern.add(Pattern.quote(replacement.getKey()));
            }
            this.replacementLookup.put(replacement.getKey(), Matcher.quoteReplacement(replacement.getValue()));
        }
        //pattern looks like this: (one|two|three|a|b|c)
        this.pattern = Pattern.compile("(" + textualPattern.toString() + ")");
    }
    
    /**
     * Initializes this object with a list of replacements. The order is important.
     * If the keys share the same prefix the one which comes first is used for replacement.
     * E.g. aa -&gt; x and aaa -&gt; y and input will be aaa, it will return aax
     * @param replacements map where key is the text to search for and value is the replacement text.
     */
    public MultipleTextReplacement(List<Entry<String, String>> replacements){
        this(replacements, false);
    }
    
    /**
     * Initializes this object with a replacement map.
     * @param replacements map where key is the text to search for and value is the replacement text.
     * @param wholeWordsOnly if true, matches only whole words. if false, then also text within a word can be matched
     */
    public MultipleTextReplacement(Map<String, String> replacements, boolean wholeWordsOnly){
        this(replacements.entrySet().stream().sorted(new Comparator<Entry<String, String>>() {
            @Override
            public int compare(Entry<String, String> o1, Entry<String, String> o2) {
                //larger key (in term of string length) should be first
                return Integer.compare(o2.getKey().length(), o1.getKey().length()); 
            }
        }).collect(Collectors.toList()), wholeWordsOnly);
    }
    
    /**
     * Initializes this object with a replacement map.
     * @param replacements map where key is the text to search for and value is the replacement text.
     */
    public MultipleTextReplacement(Map<String, String> replacements){
        this(replacements, false);
    }
    
    
    
    public String replace(String text){
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb  = new StringBuffer();
        while (matcher.find()) {
            String replacement = this.replacementLookup.get(matcher.group(1));
            if(replacement == null){
                LOGGER.error("Key of replacement found but lookup in map for replacement does not find any value. Key is {}", matcher.group(1));
                matcher.appendReplacement(sb, "");
            }else{
                matcher.appendReplacement(sb, replacement);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    //https://stackoverflow.com/questions/60160/how-to-escape-text-for-regular-expression-in-java
    // Pattern.quote and Matcher.quoteReplacement
}
