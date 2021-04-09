package de.uni_mannheim.informatik.dws.melt.matching_jena;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * A matcher template for matchers that are based on YAAA.
 *
 * @author Sven Hertling
 */
public abstract class MatcherPipelineYAAA extends MatcherYAAA {


    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherPipelineYAAAJenaConstructor.class);

    protected List<MatcherYAAA> matchers = initializeMatchers();

    protected abstract List<MatcherYAAA> initializeMatchers();

    public List<MatcherYAAA> getMatchers() {
        return matchers;
    }

    @Override
    public Alignment match(URL source, URL target, Alignment inputAlignment, Properties properties) throws Exception {
        for (MatcherYAAA matcher : this.matchers) {
            LOGGER.info("Matcher pipeline: Running now matcher '" + matcher.getClass().getName() + "'");
            inputAlignment = matcher.match(source, target, inputAlignment, properties);
        }
        LOGGER.info("Matcher pipeline completed.");
        return inputAlignment;
    }
}
