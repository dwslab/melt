package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just saves the ontologies in a specific format.
 */
public class AlignmentSaveMatcher extends MatcherYAAAJena{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlignmentSaveMatcher.class);
    
    private File alignmentFile;

    /**
     * Choose in which file the alignment is stored.
     * @param alignmentFile file the alignment is stored
     */
    public AlignmentSaveMatcher(File alignmentFile) {
        this.alignmentFile = alignmentFile;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        inputAlignment.serialize(alignmentFile);
        return inputAlignment;
    }
}
