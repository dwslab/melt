package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;

import java.util.Properties;

/**
 * This is a simple matcher that samples from the original reference alignment.
 */
public class ReferenceSampleMatcher extends MatcherYAAAJena {

    public Alignment alignmentToBeUsed;

    public ReferenceSampleMatcher(Alignment alignmentToBeUsed){
        this.alignmentToBeUsed = alignmentToBeUsed;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return this.alignmentToBeUsed;
    }

}
