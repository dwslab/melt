package de.uni_mannheim.informatik.dws.melt.kgbaseline;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.util.Arrays;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.vocabulary.RDFS;

public class BaselineLabel extends MatcherYAAAJena{
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        return BaselineUtil.match(source, target, inputAlignment, Arrays.asList(
                RDFS.label
        ), false);
    }
}
