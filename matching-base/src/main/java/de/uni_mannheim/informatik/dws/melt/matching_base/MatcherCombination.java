package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.net.URL;
import java.util.List;

/**
 * Combines multiple matchers.
 * This can be very inefficient because the alignment has to be serialized after each matcher.
 * Better use a more specialized MatcherCombination like: TODO
 * @author Sven Hertling
 */
public abstract class MatcherCombination extends MatcherURL{
    protected List<MatcherURL> matchers = initializeMatchers();
    
    protected abstract List<MatcherURL> initializeMatchers();    
    
    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception{
        for(MatcherURL matcher : this.matchers){
             inputAlignment = matcher.match(source, target, inputAlignment);
        }
        return inputAlignment;
    }

    public List<MatcherURL> getMatchers() {
        return matchers;
    }
}
