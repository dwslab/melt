package de.uni_mannheim.informatik.dws.melt.matching_jena;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Better use MatcherYAAAPipeline because it can combine matchers which use different APIS like Jena and OWLAPI etc
 */
public abstract class MatcherPipelineYAAAJena extends MatcherYAAAJena {


    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherPipelineYAAAJena.class);
    
    protected List<MatcherYAAAJena> matchers = initializeMatchers();
    
    protected abstract List<MatcherYAAAJena> initializeMatchers();    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        for(MatcherYAAAJena matcher : this.matchers){
            //long startTime = System.nanoTime();
            inputAlignment = matcher.match(source, target, inputAlignment, properties);
            //long runTime = System.nanoTime() - startTime;
            //LOGGER.info("Submatcher {} completed in {}.", matcher.getClass().getSimpleName(), DurationFormatUtils.formatDurationWords((long)(runTime/1_000_000), true, true));
        }
        return inputAlignment;
    }

    public List<MatcherYAAAJena> getMatchers() {
        return matchers;
    }
    
}
