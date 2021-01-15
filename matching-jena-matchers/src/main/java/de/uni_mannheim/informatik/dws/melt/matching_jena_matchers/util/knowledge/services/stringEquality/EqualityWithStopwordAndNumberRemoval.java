package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.stringEquality;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.stringOperations.StringOperations;

public class EqualityWithStopwordAndNumberRemoval implements StringEquality {

	/**
	 * Default constructor
	 */
	public EqualityWithStopwordAndNumberRemoval() {
	}
	
    @Override
    public boolean isSameString(String s1, String s2) {
        return StringOperations.isSameStringIgnoringStopwordsAndNumbers(s1, s2);
    }

    @Override
    public String getName() {
        return "EqualityWithStopwordAndNumberRemoval";
    }
    
	
}
