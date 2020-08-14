package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

/**
 * Matcher which creates correspondences based on exact string match.
 */
public class ExactStringMatcher extends StringMatcher {

    public ExactStringMatcher(){
        super(s -> s.toLowerCase().trim());
    }

}
