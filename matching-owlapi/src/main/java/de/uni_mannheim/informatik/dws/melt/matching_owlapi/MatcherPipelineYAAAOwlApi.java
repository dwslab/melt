package de.uni_mannheim.informatik.dws.melt.matching_owlapi;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.util.List;
import java.util.Properties;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Better use MatcherYAAAPipeline because it can combine matchers which use different APIS like Jena and OWLAPI etc
 */
public abstract class MatcherPipelineYAAAOwlApi extends MatcherYAAAOwlApi{
    protected List<MatcherYAAAOwlApi> matchers = initializeMatchers();
    
    protected abstract List<MatcherYAAAOwlApi> initializeMatchers();    
    
    @Override
    public Alignment match(OWLOntology source, OWLOntology target, Alignment inputAlignment, Properties p) throws Exception {
        for(MatcherYAAAOwlApi matcher : this.matchers){
             inputAlignment = matcher.match(source, target, inputAlignment, p);
        }
        return inputAlignment;
    }

    public List<MatcherYAAAOwlApi> getMatchers() {
        return matchers;
    }
    
}
