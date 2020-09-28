package de.uni_mannheim.informatik.dws.melt.matching_external;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ProcessOutputCollector extends Thread {
    private static final Pattern URL_PATTERN = Pattern.compile("(?:https?|ftp|file)://?[^\\s]*",Pattern.CASE_INSENSITIVE);
    
    
    private InputStream streamToCollect;
    private String messagePrefix;
    private PrintStream outStream;
    
    private String lastLine;
    private File alignmentFile;
    

    public ProcessOutputCollector(InputStream streamToCollect){
        this(streamToCollect, "", null);
    }
    
    public ProcessOutputCollector(InputStream streamToCollect, String messagePrefix, PrintStream outStream){
        this.streamToCollect = streamToCollect;
        this.messagePrefix = messagePrefix;
        this.outStream = outStream;
        this.lastLine = null;
        this.alignmentFile = null;
    }
    
    @Override
    public void run(){
        Scanner sc = new Scanner(streamToCollect);        
        if(sc.hasNextLine()){
            String message = sc.nextLine();
            if(message.contains("<?xml")){
                try {
                    this.alignmentFile = File.createTempFile("alignment", ".rdf");
                } catch (IOException ex) {
                    System.err.println("Cannot create tmp file: " + ex.getMessage());
                }
                try (BufferedWriter out = new BufferedWriter(new FileWriter(this.alignmentFile))) {
                    while (sc.hasNextLine()) {
                        message = sc.nextLine();
                        if(this.outStream != null){
                            this.outStream.println(this.messagePrefix + message);
                        }
                        out.write(message);
                        out.newLine();
                    }
                } catch (IOException ex) {
                    System.err.println("Cannot write to tmp file: " + ex.getMessage());
                }
            } else {
                if(this.outStream != null){
                    this.outStream.println(this.messagePrefix + message);
                }
                this.lastLine = message;                
                while (sc.hasNextLine()) {
                    message = sc.nextLine();
                    if(this.outStream != null){
                        this.outStream.println(this.messagePrefix + message);
                    }
                    this.lastLine = message;
                }
            }
        }
    }

    public URL getURL(){
        if(this.alignmentFile != null){
            try {
                return this.alignmentFile.toURI().toURL();
            } catch (MalformedURLException ex) {
                System.err.println("Cannot convert path to URL: " + ex.getMessage());
                return null;
            }
        } else {
            try {
                return new URL(this.lastLine);
            } catch (MalformedURLException ex) {
                return findLastURL(this.lastLine);
            }
        }
    }
    
    public static URL findLastURL(String text){
        if(text == null){
            return null;
        }
        Matcher matcher = URL_PATTERN.matcher(text);
        List<String> urlMatches = new ArrayList();
        while (matcher.find()) {
            urlMatches.add(matcher.group());
        }
        ListIterator<String> iterator = urlMatches.listIterator(urlMatches.size());
        while (iterator.hasPrevious()) {
            try {
                return new URL(iterator.previous());
            } catch (MalformedURLException ex) {}
        }
        return null;
    }
    
}
