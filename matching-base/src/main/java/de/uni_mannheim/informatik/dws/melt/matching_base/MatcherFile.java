package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.io.File;
import java.net.URL;

/**
 * For this matcher the results file that shall be written can be specified.
 * @author Sven Hertling
 */
public abstract class MatcherFile extends MatcherURL {


    protected static final String FILE_PREFIX = "alignment";
    protected static final String FILE_SUFFIX = ".rdf";
        
    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        File alignmentFile = File.createTempFile(FILE_PREFIX, FILE_SUFFIX);
        match(source, target, inputAlignment, alignmentFile);
        return alignmentFile.toURI().toURL();
    }
    
    public abstract void match(URL source, URL target, URL inputAlignment, File alignmentResult) throws Exception;    
}
