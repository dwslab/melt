package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;

import java.util.Properties;

/**
 * This is a simple matcher that forwards a given alignment.
 */
public class ForwardMatcher extends MatcherYAAAJena {

    /**
     * Alignment to be returned.
     */
    public Alignment alignmentToBeUsed;

    /**
     * Constructor
     * Alignment to be forwarded must be given in match operation.
     */
    public ForwardMatcher(){
    }

    /**
     * Constructor
     * @param alignmentToBeUsed The alignment to be forwarded.
     */
    public ForwardMatcher(Alignment alignmentToBeUsed){
        this.alignmentToBeUsed = alignmentToBeUsed;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(inputAlignment != null && inputAlignment.size() > 0){
            return inputAlignment;
        }
        return this.alignmentToBeUsed;
    }

}
