package de.uni_mannheim.informatik.dws.melt.matching_jena;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.util.Arrays;

import java.util.List;
import java.util.Properties;

import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Better use {@link MatcherPipelineYAAA} because it can combine matchers which use different APIS like Jena and
 * OWLAPI etc.
 */
public class MatcherPipelineYAAAJenaConstructor extends MatcherYAAAJena {


    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherPipelineYAAAJenaConstructor.class);

    protected List<MatcherYAAAJena> matchers;

    public MatcherPipelineYAAAJenaConstructor(List<MatcherYAAAJena> matchers) {
        this.matchers = matchers;
    }

    public MatcherPipelineYAAAJenaConstructor(MatcherYAAAJena... matchers) {
        this(Arrays.asList(matchers));
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        for (MatcherYAAAJena matcher : this.matchers) {
            LOGGER.info("Matcher pipeline: Running now matcher '" + matcher.getClass().getName() + "'");
            inputAlignment = matcher.match(source, target, inputAlignment, properties);
        }
        LOGGER.info("Matcher pipeline completed.");
        return inputAlignment;
    }

    public List<MatcherYAAAJena> getMatchers() {
        return matchers;
    }
}
