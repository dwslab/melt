package de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.PropertySpecificStringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale.ScalableStringProcessingMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorFallback;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorProperty;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorUrlFragment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;


public class RecallMatcherKgTrack extends MatcherYAAAJena{
    private static Pattern NON_ASCII_CHARS = Pattern.compile("[^\\x00-\\x7F]");
    private static Pattern NON_ALPHANUMERIC_CHARS = Pattern.compile("[^A-Za-z0-9\\s]");
    
    public static Property wikiPageWikiLinkText = ModelFactory.createDefaultModel().createProperty("http://dbkwik.webdatacommons.org/ontology/wikiPageWikiLinkText");
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        ScalableStringProcessingMatcher base = new ScalableStringProcessingMatcher(Arrays.asList(
                    new PropertySpecificStringProcessing((text) -> text, 1.0, 
                            new TextExtractorFallback(new TextExtractorProperty(RDFS.label), new TextExtractorUrlFragment()), 
                            new TextExtractorProperty(SKOS.altLabel)
                    ),
                    new PropertySpecificStringProcessing((text) -> normalize(text), 0.9, 
                            new TextExtractorFallback(new TextExtractorProperty(RDFS.label), new TextExtractorUrlFragment()), 
                            new TextExtractorProperty(SKOS.altLabel)
                    ),
                    new PropertySpecificStringProcessing((text) -> normalize(text), 0.8, 
                            new TextExtractorFallback(new TextExtractorProperty(RDFS.label), new TextExtractorUrlFragment()), 
                            new TextExtractorProperty(SKOS.altLabel),
                            new TextExtractorProperty(wikiPageWikiLinkText)
                    )
        ), false);
        return base.match(source, target, new Alignment(), properties);
    }
    
    public static String normalize(String text){
        String processed = NON_ALPHANUMERIC_CHARS.matcher(text.toLowerCase(Locale.ENGLISH).trim()).replaceAll("");
        return StringUtils.normalizeSpace(processed);
    }
}
