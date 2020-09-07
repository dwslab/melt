package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replace multiple texts at once.
 * Returns multiple possible variants. Especially used for replacing synonyms.
 */
public class MultipleTextReplacementMultiReturn {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleTextReplacementMultiReturn.class);
    
    
    private Pattern pattern;
    private Map<String, Set<String>> replacementLookup;
    
    /**
     * Initializes this object with a list of replacements. The order is important.
     * If the keys share the same prefix the one which comes first is used for replacement.
     * E.g. aa -&gt; x and aaa -&gt; y and input will be aaa, it will return aax
     * @param replacements map where key is the text to search for and value is the replacement text.
     * @param wholeWordsOnly if true, matches only whole words. if false, then also text within a word can be matched
     */
    public MultipleTextReplacementMultiReturn(List<Entry<String, Set<String>>> replacements, boolean wholeWordsOnly){
        StringJoiner textualPattern = new StringJoiner("|");
        this.replacementLookup = new HashMap();
        for(Entry<String, Set<String>> replacement : replacements){
            if(wholeWordsOnly){
                textualPattern.add("\\b" + Pattern.quote(replacement.getKey()) + "\\b");
            }else{
                textualPattern.add(Pattern.quote(replacement.getKey()));
            }
            
            Set<String> replaceSet = this.replacementLookup.computeIfAbsent(replacement.getKey(), __->new HashSet());
            for(String r : replacement.getValue()){
                replaceSet.add(Matcher.quoteReplacement(r));
            }
        }
        //LOGGER.info("PAttern: {}", "(" + textualPattern.toString() + ")");
        //pattern looks like this: (one|two|three|a|b|c)
        this.pattern = Pattern.compile("(" + textualPattern.toString() + ")");
    }
    
    public MultipleTextReplacementMultiReturn(List<Entry<String, Set<String>>> replacements){
        this(replacements, false);
    }
    
    /**
     * Initializes this object with a replacement map.
     * @param replacements map where key is the text to search for and value is the replacement text.
     * @param wholeWordsOnly if true, matches only whole words. if false, then also text within a word can be matched
     */
    public MultipleTextReplacementMultiReturn(Map<String, Set<String>> replacements, boolean wholeWordsOnly){
        this(replacements.entrySet().stream().sorted(new Comparator<Entry<String, Set<String>>>() {
            @Override
            public int compare(Entry<String, Set<String>> o1, Entry<String, Set<String>> o2) {
                //larger key (in term of string length) should be first
                return Integer.compare(o2.getKey().length(), o1.getKey().length()); 
            }
        }).collect(Collectors.toList()), wholeWordsOnly);
    }
    
    public MultipleTextReplacementMultiReturn(Map<String, Set<String>> replacements){
        this(replacements, false);
    }
    
    public Set<String> replace(String text){
        Set<String> finalCombinations = new HashSet();
        finalCombinations.add("");
        
        Matcher matcher = pattern.matcher(text);
        int lastAppendPosition = 0;
        while (matcher.find()) {
            Set<String> possibleReplacementsTmp = new HashSet();
            for(String replacement : this.replacementLookup.getOrDefault(matcher.group(1), new HashSet<>())){
                String appendValue = text.substring(lastAppendPosition, matcher.start()).concat(replacement);
                possibleReplacementsTmp.addAll(append(finalCombinations, appendValue));
            }
            finalCombinations = possibleReplacementsTmp;            
            lastAppendPosition = matcher.end();
        }
        finalCombinations = append(finalCombinations, text.substring(lastAppendPosition, text.length())); //append postfix
        return finalCombinations;
    }
    
    private Set<String> append(Set<String> texts, String postfix){
        Set<String> ret = new HashSet();
        for(String s : texts){
            ret.add(s.concat(postfix));
        }
        return ret;
    }
    
    //https://stackoverflow.com/questions/60160/how-to-escape-text-for-regular-expression-in-java
    // Pattern.quote and Matcher.quoteReplacement
}
