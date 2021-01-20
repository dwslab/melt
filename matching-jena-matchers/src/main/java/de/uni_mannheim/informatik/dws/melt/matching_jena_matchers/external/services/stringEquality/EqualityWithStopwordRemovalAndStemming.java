package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringEquality;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;

/**
 * This class considers two Strings to be equal when they contain the same tokens with stopwords removed.
 * Individual tokens are stemmed in an aggressive manner (Porter).
 * Basic transformations like lower casing are applied.
 */
public class EqualityWithStopwordRemovalAndStemming implements StringEquality {

    @Override
    public boolean isSameString(String s1, String s2) {
        return StringOperations.isSameStringStemming(s1, s2);
    }

    @Override
    public String getName() {
        return "EqualityWithStopwordRemovalAndStemming";
    }
}
