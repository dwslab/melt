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
 * This is a simple matcher that forwards a given alignment.
 */
public class ForwardMatcher extends MatcherYAAAJena {

    /**
     * Alignment to be returned.
     */
    public Alignment alignmentToBeUsed;

    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardMatcher.class);

    /**
     * Constructor
     * Alignment to be forwarded must be given in match operation.
     */
    public ForwardMatcher(){
    }

    /**
     * Constructor
     * @param fileToLoadAlignmentFrom Alignment file.
     */
    public ForwardMatcher(File fileToLoadAlignmentFrom){
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
    public ForwardMatcher(String filePathToLoadAlignmentFrom){
        this(new File(filePathToLoadAlignmentFrom));
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
