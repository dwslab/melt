package de.uni_mannheim.informatik.dws.melt.matching_jena;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * Better use MatcherYAAAPipeline because it can combine matchers which use different APIS like Jena and OWLAPI etc
 */
public abstract class MatcherPipelineYAAAJena extends MatcherYAAAJena{
    protected List<MatcherYAAAJena> matchers = initializeMatchers();
    
    protected abstract List<MatcherYAAAJena> initializeMatchers();    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        for(MatcherYAAAJena matcher : this.matchers){
             inputAlignment = matcher.match(source, target, inputAlignment, properties);
        }
        return inputAlignment;
    }

    public List<MatcherYAAAJena> getMatchers() {
        return matchers;
    }
    
}
