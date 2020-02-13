package de.uni_mannheim.informatik.dws.ontmatching.matchingjena;

import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.util.Arrays;

import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * Better use MatcherYAAAPipeline because it can combine matchers which use different APIS like Jena and OWLAPI etc
 */
public class MatcherPipelineYAAAJenaConstructor extends MatcherYAAAJena{
    protected List<MatcherYAAAJena> matchers;
    
    public MatcherPipelineYAAAJenaConstructor(List<MatcherYAAAJena> matchers){
        this.matchers = matchers;
    }
    
    public MatcherPipelineYAAAJenaConstructor(MatcherYAAAJena... matchers){
        this(Arrays.asList(matchers));
    }
    
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
