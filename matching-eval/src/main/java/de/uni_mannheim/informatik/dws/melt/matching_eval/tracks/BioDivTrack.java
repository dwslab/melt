package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BioDivTrack extends Track {
    private static final Logger logger = LoggerFactory.getLogger(BioDivTrack.class);
    
    private String urlToFile;

    public BioDivTrack(String urlToFile, String name, String version) {
        super(getNiceRemoteLocation(urlToFile), name, version);
        this.urlToFile = urlToFile;
    }

    @Override
    protected void downloadToCache() throws IOException {
        logger.info("Downloading track {}", this.name);
        File dir = downloadAndExtractToTmpDir(this.urlToFile);
        
        for (File fileEntry : dir.listFiles()) {
            if (fileEntry.isFile() && fileEntry.getName().endsWith(".rdf")) {
                String testCaseId = FilenameUtils.removeExtension(fileEntry.getName());
                String[] sourceTarget = testCaseId.split("-");
                if(sourceTarget.length != 2){
                    logger.warn("file ending with .rdf in zip file does not have source target. continuing...");
                    continue;
                }
                File source = new File(dir, sourceTarget[0] + ".owl");
                File target = new File(dir, sourceTarget[1] + ".owl");

                if(!source.exists() || !target.exists()){
                    logger.error("Source or Target is not defined - continue");
                    continue;
                }
                this.saveInDefaultLayout(source, testCaseId, TestCaseType.SOURCE);
                this.saveInDefaultLayout(target, testCaseId, TestCaseType.TARGET);
                this.saveInDefaultLayout(fileEntry, testCaseId, TestCaseType.REFERENCE);
            }
        }
    }
    
        
    private File downloadAndExtractToTmpDir(String url) throws IOException{        
        File tmpDir = Files.createTempDirectory(this.name).toFile();
        tmpDir.deleteOnExit();
        
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new URL(url).openStream()))) {                
            ZipEntry zipEntry = zis.getNextEntry();
            
            while (zipEntry != null) {
                File destFile = new File(tmpDir, zipEntry.getName());
                if (destFile.getCanonicalPath().startsWith(tmpDir.getCanonicalPath() + File.separator)) {
                    //Entry is not outside of the target dir
                    if(zipEntry.isDirectory() == false){
                        FileUtils.copyToFile(zis, destFile); // does not close the stream and creates all directories                        
                    }
                    destFile.deleteOnExit();
                }
                zipEntry = zis.getNextEntry();
            }
        }
        return tmpDir;
    }
        
}
