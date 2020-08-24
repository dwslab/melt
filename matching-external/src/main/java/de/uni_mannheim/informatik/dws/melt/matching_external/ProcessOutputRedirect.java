package de.uni_mannheim.informatik.dws.melt.matching_external;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


/**
 *
 */
public class ProcessOutputRedirect extends Thread {
    
    
    private InputStream streamToCollect;
    private String messagePrefix;
    private PrintStream outStream;
    
    
    public ProcessOutputRedirect(InputStream streamToCollect, String messagePrefix, PrintStream outStream){
        this.streamToCollect = streamToCollect;
        this.messagePrefix = messagePrefix;
        this.outStream = outStream;
    }
    
    @Override
    public void run(){
        Scanner sc = new Scanner(streamToCollect);
        while (sc.hasNextLine()) {
            this.outStream.println(this.messagePrefix + sc.nextLine());
        }
    }
}
