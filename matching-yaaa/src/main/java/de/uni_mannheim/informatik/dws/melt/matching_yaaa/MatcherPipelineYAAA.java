package de.uni_mannheim.informatik.dws.melt.matching_yaaa;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * A matcher template for matchers that are based on YAAA.
 * @author Sven Hertling
 */
public abstract class MatcherPipelineYAAA extends MatcherYAAA implements IMatcher<Alignment, URL> {

    protected List<MatcherYAAA> matchers = initializeMatchers();
    
    protected abstract List<MatcherYAAA> initializeMatchers();

    public List<MatcherYAAA> getMatchers() {
        return matchers;
    }

    @Override
    public Alignment match(URL source, URL target, Alignment inputAlignment, Properties properties) throws Exception {
        for(MatcherYAAA matcher : this.matchers){
             inputAlignment = matcher.match(source, target, inputAlignment, properties);
        }
        return inputAlignment;
    }
}
