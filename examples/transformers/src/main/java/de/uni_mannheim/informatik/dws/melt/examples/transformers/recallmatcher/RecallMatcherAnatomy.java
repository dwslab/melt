package de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;


public class RecallMatcherAnatomy extends MatcherYAAAJena{
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        //TODO: recall alignment antomy
        return inputAlignment;
    }
}
