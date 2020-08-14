package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.PropertySpecificStringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ScalableStringProcessingMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ValueExtractorFallback;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ValueExtractorProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ValueExtractorUrlFragment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;


public class BaseMatcher extends MatcherYAAAJena{
    private static Pattern NON_ASCII_CHARS = Pattern.compile("[^\\x00-\\x7F]");
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        ScalableStringProcessingMatcher base = new ScalableStringProcessingMatcher(Arrays.asList(
                    new PropertySpecificStringProcessing((text) -> text, 1.0, 
                            new ValueExtractorFallback(new ValueExtractorProperty(RDFS.label), new ValueExtractorUrlFragment()), 
                            new ValueExtractorProperty(SKOS.altLabel)
                    ),
                    new PropertySpecificStringProcessing((text) -> normalize(text), 0.9, 
                            new ValueExtractorFallback(new ValueExtractorProperty(RDFS.label), new ValueExtractorUrlFragment()), 
                            new ValueExtractorProperty(SKOS.altLabel)
                    )
        ), false);
        return base.match(source, target, new Alignment(), properties);
    }
    
    public static String normalize(String text){
        return NON_ASCII_CHARS.matcher(text.toLowerCase(Locale.ENGLISH)).replaceAll("");
    }
}
