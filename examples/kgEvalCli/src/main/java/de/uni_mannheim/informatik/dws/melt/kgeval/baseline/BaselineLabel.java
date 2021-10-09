package de.uni_mannheim.informatik.dws.melt.kgeval.baseline;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
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
