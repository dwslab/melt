package de.uni_mannheim.informatik.dws.melt.kgbaseline;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.util.Arrays;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;


public class BaselineAnchorLabel extends MatcherYAAAJena{
    
    private static Property anchorText = ModelFactory.createDefaultModel().createProperty("http://dbkwik.webdatacommons.org/ontology/wikiPageWikiLinkText");
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        return BaselineUtil.match(source, target, inputAlignment, Arrays.asList(
                RDFS.label, SKOS.altLabel, anchorText
        ), true);
    }
}