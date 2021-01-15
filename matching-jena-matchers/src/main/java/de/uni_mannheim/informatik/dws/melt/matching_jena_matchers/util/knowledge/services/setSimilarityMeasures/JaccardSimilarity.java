package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.setSimilarityMeasures;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JaccardSimilarity implements SetSimilarityMeasure{

    private static final Logger LOG = LoggerFactory.getLogger(JaccardSimilarity.class);

    /**
     * Returns the Jaccard similarity of two sets represented by two String arrays.
     * @param set1 Set 1.
     * @param set2 Set 2.
     * @return Jaccard similarity between the provided String sets as double.
     */
    @Override
    public double calculateSimilarity(String[] set1, String[] set2) {

        if(set1.length == 0 || set2.length == 0){
            return 0.0;
        }

        final Set<String> allTokens = new HashSet<String>();
        Arrays.stream(set1).forEach(allTokens::add);

        final int termsInString1 = allTokens.size();
        final Set<String> secondStringTokens = new HashSet<String>();
        Arrays.stream(set2).forEach(secondStringTokens::add);
        final int termsInString2 = secondStringTokens.size();

        //now combine the sets
        allTokens.addAll(secondStringTokens);
        final int commonTerms = (termsInString1 + termsInString2) - allTokens.size();

        //return JaccardSimilarity
        return (double) (commonTerms) / (allTokens.size());
    }

    @Override
    public double calculateSimilarity(HashSet<String> set1, HashSet<String> set2) {
        return ((double) Sets.intersection(set1, set2).size()) / (Sets.union(set1, set2).size());
    }

    @Override
    public double calculateSimilarityWithNumbers(int common, int sizeSet1, int sizeSet2) {
        return (double) common / (double) (sizeSet1 + sizeSet2 - common);
    }

    @Override
    public String getName() {
        return "JaccardSimilarity";
    }
}
