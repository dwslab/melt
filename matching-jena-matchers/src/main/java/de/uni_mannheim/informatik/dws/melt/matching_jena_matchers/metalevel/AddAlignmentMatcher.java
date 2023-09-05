package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * This is a simple matcher that adds a given alignment to the inputAlignment.
 * The given alignemnt has advantage.
 */
public class AddAlignmentMatcher extends MatcherYAAAJena {

    /**
     * Alignment to be added.
     */
    public Alignment alignmentToBeUsed;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddAlignmentMatcher.class);

    /**
     * Constructor
     * Alignment to be forwarded must be given in match operation.
     */
    public AddAlignmentMatcher(){
    }

    /**
     * Constructor
     * @param fileToLoadAlignmentFrom Alignment file.
     */
    public AddAlignmentMatcher(File fileToLoadAlignmentFrom){
        try {
            alignmentToBeUsed = new Alignment(fileToLoadAlignmentFrom);
        } catch (SAXException | IOException exception){
            LOGGER.error("Could not load the specified alignment file.", exception);
        }
    }

    /**
     * Constructor
     * @param filePathToLoadAlignmentFrom Alignment file path.
     */
    public AddAlignmentMatcher(String filePathToLoadAlignmentFrom){
        this(new File(filePathToLoadAlignmentFrom));
    }

    /**
     * Constructor
     * @param alignmentToBeUsed The alignment to be forwarded.
     */
    public AddAlignmentMatcher(Alignment alignmentToBeUsed){
        this.alignmentToBeUsed = alignmentToBeUsed;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(inputAlignment == null)
            inputAlignment = new Alignment();
        Alignment a = new Alignment(inputAlignment, false);        
        a.addAll(this.alignmentToBeUsed);
        a.addAll(inputAlignment);        
        return a;
    }

}
