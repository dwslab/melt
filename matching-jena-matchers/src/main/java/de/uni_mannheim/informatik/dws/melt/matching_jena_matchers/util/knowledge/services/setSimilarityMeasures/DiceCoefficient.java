package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.setSimilarityMeasures;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;

public class DiceCoefficient implements SetSimilarityMeasure{

    /**
     * Returns the dice similarity of two sets represented by String Arrays.
     * @param s1 Set 1.
     * @param s2 Set 2.
     * @return Dice similarity as double.
     */
    @Override
    public double calculateSimilarity(String[] s1, String[] s2) {

        HashSet<String> set1 = new HashSet<String>();
        HashSet<String> set2 = new HashSet<String>();
        HashSet<String> allTokens = new HashSet<String>();

        // fill set
        Arrays.stream(s1).forEach(set1::add);
        Arrays.stream(s2).forEach(set2::add);

        final int termsString1 = set1.size();
        final int termsString2 = set2.size();
        allTokens.addAll(set1);
        allTokens.addAll(set2);
        final int allTerms = allTokens.size();
        final int commonTerms = termsString1 + termsString2 - allTerms;

        return (2.0 * commonTerms) / (double) (termsString1 + termsString2);
    }

    @Override
    public double calculateSimilarity(HashSet<String> set1, HashSet<String> set2){
        int commonTerms = Sets.intersection(set1, set2).size();
        return (2.0 * commonTerms) / (set1.size() + set2.size());
    }

    @Override
    public double calculateSimilarityWithNumbers(int common, int sizeSet1, int sizeSet2) {
        return (2.0 * common) / (double) (sizeSet1 + sizeSet2);
    }


    @Override
    public String getName() {
        return "DiceCoefficient";
    }

}
