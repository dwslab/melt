package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.setSimilarityMeasures;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 50% Jaccard, 50% Overlap Coefficient.
 */
public class CombinedJaccardAndOverlapCoefficient implements SetSimilarityMeasure{

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
        double jaccard = ((double) (commonTerms) / (allTokens.size()));
        double overlapCoefficient = ((double) (commonTerms)) / Math.min(termsInString1, termsInString2);
        return 0.5 * jaccard + 0.5* overlapCoefficient;
    }

    @Override
    public double calculateSimilarity(HashSet<String> set1, HashSet<String> set2) {
        double commonTerms = Sets.intersection(set1, set2).size();
        double jaccard = commonTerms / (Sets.union(set1, set2).size());
        double overlapCoefficient = commonTerms / Math.min(set1.size(), set2.size());
        return 0.5 * jaccard + 0.5 * overlapCoefficient;
    }

    @Override
    public double calculateSimilarityWithNumbers(int common, int sizeSet1, int sizeSet2) {
        double jaccard = (double) common / (double) (sizeSet1 + sizeSet2 - common);
        double overlapCoefficient = (double) common / Math.min(sizeSet1, sizeSet2);
        return 0.5 * jaccard + 0.5 * overlapCoefficient;
    }

    @Override
    public String getName() {
        return "CombinedJaccardAndRelativeSubsetSize";
    }
}
