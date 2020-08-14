package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.PropertySpecificStringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ScalableStringProcessingMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ValueExtractorFallback;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ValueExtractorProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ValueExtractorUrlFragment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Arrays;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

public class CategoryMatcher extends MatcherYAAAJena {

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        ScalableStringProcessingMatcher mTest = new ScalableStringProcessingMatcher(Arrays.asList(
                new PropertySpecificStringProcessing((text) -> text.replace("_", " "), 1.0, 
                    new ValueExtractorFallback(new ValueExtractorProperty(RDFS.label), new ValueExtractorUrlFragment())),
                new PropertySpecificStringProcessing((text) -> BaseMatcher.normalize(text.replace("_", " ")), 0.9, 
                    new ValueExtractorFallback(new ValueExtractorProperty(RDFS.label), new ValueExtractorUrlFragment()))
        ), false);        
        Alignment a = new Alignment();
        mTest.matchResources(source.listResourcesWithProperty(RDF.type, SKOS.Concept), target.listResourcesWithProperty(RDF.type, SKOS.Concept), a);
        return a;
    }
    
}
