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
 * This is a simple matcher that forwards a given alignment always (even if the input alignment is available).
 * In case the input alignment should be used when availabel, use {@link ForwardMatcher }.
 */
public class ForwardAlwaysMatcher extends MatcherYAAAJena {

    /**
     * Alignment to be returned.
     */
    public Alignment alignmentToBeUsed;

    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardAlwaysMatcher.class);

    /**
     * Constructor
     * Alignment to be forwarded must be given in match operation.
     */
    public ForwardAlwaysMatcher(){
    }

    /**
     * Constructor
     * @param fileToLoadAlignmentFrom Alignment file.
     */
    public ForwardAlwaysMatcher(File fileToLoadAlignmentFrom){
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
    public ForwardAlwaysMatcher(String filePathToLoadAlignmentFrom){
        this(new File(filePathToLoadAlignmentFrom));
    }

    /**
     * Constructor
     * @param alignmentToBeUsed The alignment to be forwarded.
     */
    public ForwardAlwaysMatcher(Alignment alignmentToBeUsed){
        this.alignmentToBeUsed = alignmentToBeUsed;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return this.alignmentToBeUsed;
    }

}
