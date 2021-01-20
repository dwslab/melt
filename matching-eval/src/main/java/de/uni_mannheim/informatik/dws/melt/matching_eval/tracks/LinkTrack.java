package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkTrack extends Track {
    private static final Logger logger = LoggerFactory.getLogger(LinkTrack.class);
    
    protected String urlToFile;
    protected String filenameSource;
    protected String filenameTarget;
    protected String filenameReference;

    public LinkTrack(String urlToFile, String name, String version, String filenameSource, String filenameTarget, String filenameReference) {
        super(getNiceRemoteLocation(urlToFile), name, version);
        
        this.urlToFile = urlToFile;
        this.filenameSource = filenameSource;
        this.filenameTarget = filenameTarget;
        this.filenameReference = filenameReference;
    }
    
    @Override
    protected void downloadToCache() throws IOException {
        logger.info("Downloading track {}", this.name);
        try (TarArchiveInputStream in = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new URL(urlToFile).openStream())))) {
            TarArchiveEntry entry = in.getNextTarEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    entry = in.getNextTarEntry();
                    continue;
                }
                
                String[] pathEntries = entry.getName().split("/|\\\\");                
                String concatedParents = String.join("_", Arrays.copyOfRange(pathEntries, 0, pathEntries.length - 1));
                String filename = pathEntries[pathEntries.length - 1];
                
                TestCaseType type = getTypeFromFileName(filename);
                if(type == null){
                    //logger.warn("Cannot determine the testcase type of filename " + filename + " - do not store file.");
                    entry = in.getNextTarEntry();
                    continue;
                }                
                this.saveInDefaultLayout(in, concatedParents, type);
                entry = in.getNextTarEntry();
            }
        }
    }
    
    protected TestCaseType getTypeFromFileName(String filename){
        if(filename.equals(this.filenameSource))
            return TestCaseType.SOURCE;
        if(filename.equals(this.filenameTarget))
            return TestCaseType.TARGET;
        if(filename.equals(this.filenameReference))
            return TestCaseType.REFERENCE;        
        return null;
        //no parameters file available
    }
        
}
