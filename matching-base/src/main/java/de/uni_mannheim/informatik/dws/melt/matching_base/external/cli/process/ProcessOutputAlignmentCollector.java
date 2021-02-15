
package de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collector which searches for an alignment URL or creates a file with the content of the lines and returns the url of this file.
 */
public class ProcessOutputAlignmentCollector implements ProcessOutputConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessOutputAlignmentCollector.class);

    private static final Pattern URL_PATTERN = Pattern.compile("(?:https?|ftp|file)://?[^\\s]*",Pattern.CASE_INSENSITIVE);
    
    private File alignmentFile;
    private BufferedWriter writer;
    private String lastLine;
    private boolean isFirstLine = true;
    
    @Override
    public void processOutput(String line) {
        if(isFirstLine){
            isFirstLine = false;
            if(line.trim().startsWith("<?xml")){
                try {
                    this.alignmentFile = File.createTempFile("alignment", ".rdf");
                } catch (IOException ex) {
                    LOGGER.error("Cannot create tmp file for alignment", ex);
                    this.alignmentFile = null;
                    return;
                }
                try {
                    this.writer = new BufferedWriter(new FileWriter(this.alignmentFile));
                    this.writer.write(line);
                    this.writer.newLine();
                } catch (IOException ex) {
                    LOGGER.error("Cannot write to tmp file for alignment", ex);
                }
            }else{
                this.lastLine = line;
            }
        } else {
            if(writer != null){
                try {
                    writer.write(line);
                } catch (IOException ex) {
                    LOGGER.warn("Could not write to file", ex);
                }                
            }else{
                this.lastLine = line;
            }
        }
    }

    @Override
    public void close() throws Exception {
        if(this.writer != null){
            this.writer.close();
        }
    }
    
    public URL getURL(){
        if(this.alignmentFile != null){
            try {
                return this.alignmentFile.toURI().toURL();
            } catch (MalformedURLException ex) {
                LOGGER.error("Cannot convert path to URL", ex);
                return null;
            }
        } else {
            try {
                return new URL(this.lastLine);
            } catch (MalformedURLException ex) {
                LOGGER.info("The last line of the outout is not a URL - try to find a URL within the last line. Last line was: {}", this.lastLine);
                return findLastURL(this.lastLine);
            }
        }
    }
    
    public static URL findLastURL(String text){
        if(text == null){
            return null;
        }
        Matcher matcher = URL_PATTERN.matcher(text);
        List<String> urlMatches = new ArrayList<>();
        while (matcher.find()) {
            urlMatches.add(matcher.group());
        }
        ListIterator<String> iterator = urlMatches.listIterator(urlMatches.size());
        while (iterator.hasPrevious()) {
            String foundURL = iterator.previous();
            try {
                return new URL(foundURL);
            } catch (MalformedURLException ex) {
                LOGGER.info("Found url text can not be transformed to URL. FoundURL: {}", foundURL);
            }
        }
        LOGGER.warn("No text found which can be transformed to URL. Returning null.");
        return null;
    }
}
