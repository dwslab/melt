package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A collection of useful String operations that can be used for matcher development.
 */
public class StringUtil {

    private static HashMap<String, List<String>> tokenMap = new HashMap<String, List<String>>();

    private static String myFormat = String.format("%s|%s|%s", "(?<=[\\p{Lu}])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[\\p{Lu}])", "(?<=[A-Za-z])(?=[^\\p{L}])");

    /**
     * Make tokens out of a String.
     *
     * @param text String to be tokenized.
     * @return A list of tokens.
     */
    public static List<String> tokenize(String text) {
        if (tokenMap.containsKey(text)) {
            return tokenMap.get(text);
        }

        List<String> tokenList = new LinkedList<String>();
        String ret = text;
        ret = ret.replace(",", " ");
        ret = ret.replace(";", " ");
        ret = ret.replace(":", " ");
        ret = ret.replace("(", " ");
        ret = ret.replace(")", " ");
        ret = ret.replace("?", " ");
        ret = ret.replace("!", " ");
        ret = ret.replace(".", " ");
        ret = ret.replace("_", " ");
        ret = ret.replace("-", " ");
        ret = ret.replace("\"", " ");
        ret = ret.replace("\r", " ");
        ret = ret.replace("\n", " ");
        ret = ret.replace("\t", " ");
        ret = ret.replaceAll(myFormat, " ");
        for (String tokens : ret.split(" ")) {
            if (tokens.length() != 0) {
                tokenList.add(tokens.toLowerCase());
            }
        }
        tokenMap.put(text, tokenList);
        return tokenList;
    }
    
    
    public static String tokenizeToString(String text) {
        return String.join(" ", tokenize(text));
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
    
    
    public static String getProcessedString(String text) {
        if(containsMostlyNumbers(text))
            return "";
        return String.join(" ", removeStopwords(tokenize(text)));
    }

    public static List<String> getTokensWithoutStopword(String text) {
        return removeStopwords(tokenize(text));
    }

    /**
     * A set of English stopwords.
     */
    private static final Set<String> ENGLISH_STOPWORDS = new HashSet<String>(Arrays.asList(
            //new String[]{"is", "by", "of", "a", "an", "the"}// ,, "has"}
            "a","an","and","are","as","at","be","but","by","for","if","in","into","is","it","no","not","of","on","or","such",
            "that","the","their","then","there","these","they","this","to","was","will","with"
    ));
    /*
    private static final Set<String> STOPWORDS = new HashSet<String>(Arrays.asList(
            "a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow",
            "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", 
            "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", 
            "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", 
            "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", 
            "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", 
            "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", 
            "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each",
            "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", 
            "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", 
            "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going",
            "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help",
            "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully",
            "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", 
            "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", 
            "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", 
            "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", 
            "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", 
            "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", 
            "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", 
            "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", 
            "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", 
            "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", 
            "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", 
            "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", 
            "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", 
            "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", 
            "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough",
            "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries",
            "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", 
            "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were",
            "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", 
            "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", 
            "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", 
            "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"));
	*/

    public static List<String> removeStopwords(List<String> tokens) {
        return removeStopwords(tokens, ENGLISH_STOPWORDS);
    }

    public static List<String> removeStopwords(List<String> tokens, Set<String> stopwords) {
        LinkedList<String> list = new LinkedList<String>();
        for (String token : tokens) {
            if (stopwords.contains(token) == false) {
                list.add(token);
            }
        }
        return list;
    }

    //compute the edit distance between s1 and s2
    public static int editDistance(String a, String b, boolean cased) {
        String s1, s2;
        if (cased) {
            if (a == null) {
                a = "";
            }
            if (b == null) {
                b = "";
            }
            s1 = a.toLowerCase();
            s2 = b.toLowerCase();
        } else {
            s1 = a;
            s2 = b;
        }

        int[][] f = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 1; i < s1.length() + 1; i++) {
            f[i][0] = i;
        }
        for (int j = 1; j < s2.length() + 1; j++) {
            f[0][j] = j;
        }
        for (int i = 1; i < s1.length() + 1; i++) {
            for (int j = 1; j < s2.length() + 1; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    f[i][j] = f[i - 1][j - 1];
                } else {
                    f[i][j] = f[i - 1][j] + 1; // delete i-th character in s1

                    if (f[i][j] > f[i][j - 1] + 1) // insert j-th character of s2 to i-th position in s1
                    {
                        f[i][j] = f[i][j - 1] + 1;
                    }

                    if (f[i][j] > f[i - 1][j - 1] + 1) // replace i-th characther of s1 by j-th character of s2
                    {
                        f[i][j] = f[i - 1][j - 1] + 1;
                    }
                }
            }
        }
        return f[s1.length()][s2.length()];
    }

    public static double editDistanceNormalised(String a, String b) {
        //editDistance(a, b, true) true, because Hello = hello (upper and lower case equals)
        return getNormalised((double) editDistance(a, b, true), getMaxLength(a, b));
    }

    //return true if s1 is a suffix of s2
    public static boolean isSuffix(String s1, String s2) {
        return s2.endsWith(s1);
    }

    //return true if s1 is a prefix of s2
    public static boolean isPrefix(String s1, String s2) {
        return s2.startsWith(s1);
    }

    public static int damerauLevenshtein(String compOne, String compTwo) {
        //System.out.println("one: " + compOne + "\ttwo: " + compTwo);
        int res = -1;
        int INF = compOne.length() + compTwo.length();
        int maxLength = 0;
        if (compOne.length() > compTwo.length()) {
            maxLength = compOne.length();
        } else {
            maxLength = compTwo.length();
        }

        int[][] matrix = new int[compOne.length() + 1][compTwo.length() + 1];

        for (int i = 0; i < compOne.length(); i++) {
            matrix[i + 1][1] = i;
            matrix[i + 1][0] = INF;
        }

        for (int i = 0; i < compTwo.length(); i++) {
            matrix[1][i + 1] = i;
            matrix[0][i + 1] = INF;
        }

        int[] DA = new int[maxLength];

        for (int i = 0; i < maxLength; i++) {
            DA[i] = 0;
        }

        for (int i = 1; i < compOne.length(); i++) {
            int db = 0;

            for (int j = 1; j < compTwo.length(); j++) {

                int i1 = DA[compTwo.indexOf(compTwo.charAt(j - 1))];
                int j1 = db;
                int d = ((compOne.charAt(i - 1) == compTwo.charAt(j - 1)) ? 0 : 1);
                if (d == 0) {
                    db = j;
                }

                matrix[i + 1][j + 1] = Math.min(Math.min(matrix[i][j] + d, matrix[i + 1][j] + 1), Math.min(matrix[i][j + 1] + 1, matrix[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1)));
            }
            DA[compOne.indexOf(compOne.charAt(i - 1))] = i;
        }

        return matrix[compOne.length()][compTwo.length()];
    }

    public static double damerauLevenshteinNormalised(String a, String b) {
        double edit = (double) damerauLevenshtein(a, b);
        double maxLength = getMaxLength(a, b);
        return getNormalised(edit, maxLength);
    }

    public static String exactLength(String in, int length) {
        if (in.length() > length) {
            in = in.substring(0, length);
        } else {
            int spaceCount = length - in.length();
            String spaces = "";
            for (int i = 0; i < spaceCount; i++) {
                spaces = spaces.concat(" ");
            }
            in = in.concat(spaces);
        }
        return in;
    }

    //PRIVATE:
    private static double getNormalised(double editDistance, double maxLength) {
        return 1.0d - (editDistance / maxLength);
    }

    private static double getMaxLength(String a, String b) {
        if (a.length() > b.length()) {
            return (double) a.length();
        } else {
            return (double) b.length();
        }
    }

}
