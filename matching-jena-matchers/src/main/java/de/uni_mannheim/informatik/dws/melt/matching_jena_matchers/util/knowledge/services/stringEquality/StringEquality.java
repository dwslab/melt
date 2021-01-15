package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.stringEquality;

/**
 * An interface for classes which define under what conditions two Strings are considered equal.
 */
public interface StringEquality {

    public boolean isSameString(String s1, String s2);

    public String getName();

}
