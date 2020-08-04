package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * Matcher which just loads the specified alignment.
 */
public class LoadAlignmentMatcher extends MatcherYAAAJena {

    private File alignmentFile;
    
    public LoadAlignmentMatcher(File alignmentFile){
        this.alignmentFile = alignmentFile;
    }
    
    public LoadAlignmentMatcher(String alignmentFilePath){
        this(new File(alignmentFilePath));
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {        
        return new Alignment(alignmentFile);
    }
}
