package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ConfidenceCombiner;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;


public class OneFeatureMatcher extends MatcherYAAAJena {
    
    private final MatcherYAAAJena featureMatcher;
    
    public OneFeatureMatcher(MatcherYAAAJena featureMatcher) {
        this.featureMatcher = featureMatcher;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        List<MatcherYAAAJena> matchers = Arrays.asList(
                new BaseMatcher(), 
                this.featureMatcher, 
                new ConfidenceCombiner(this.featureMatcher.getClass()), 
                new NaiveDescendingExtractor()
        );
        for(MatcherYAAAJena matcher : matchers){
            inputAlignment = matcher.match(source, target, inputAlignment, properties);
        }
        return inputAlignment;
    }
}
