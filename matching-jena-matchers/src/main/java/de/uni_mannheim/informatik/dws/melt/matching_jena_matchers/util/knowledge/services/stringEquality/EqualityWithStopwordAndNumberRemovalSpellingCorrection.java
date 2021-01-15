package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.stringEquality;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.stringOperations.StringOperations;

/**
 * This class considers two Strings to be equal when they contain the same tokens with stopwords removed.
 * Basic transformations like lowercasing are applied.
 */
public class EqualityWithStopwordAndNumberRemovalSpellingCorrection implements StringEquality {

	/**
	 * Default constructor with edit distance tolerance = 2.0f.
	 */
	public EqualityWithStopwordAndNumberRemovalSpellingCorrection() {
		
	}
	
	public EqualityWithStopwordAndNumberRemovalSpellingCorrection(float maxAllowedDistance) {
		this.maxAllowedTolerance = maxAllowedDistance;
	}
	
    @Override
    public boolean isSameString(String s1, String s2) {
        return StringOperations.isSameStringIgnoringStopwordsAndNumbersWithSpellingCorrection(s1, s2, maxAllowedTolerance);
    }

    @Override
    public String getName() {
        return "BasicEqualityWithStopwordRemoval";
    }
    
    public float maxAllowedTolerance = 2.0f; // default tolerance 

}
