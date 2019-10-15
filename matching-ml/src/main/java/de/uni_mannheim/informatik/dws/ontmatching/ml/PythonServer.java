package de.uni_mannheim.informatik.dws.ontmatching.ml;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PythonServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PythonServer.class);
    
    protected Process serverProcess;
    
    public PythonServer(){
        this.serverProcess = null;
    }
    
    protected void start(){
        List<String> command = Arrays.asList("python", "oaei-resources/python_server.py");
        
        ProcessBuilder pb = new ProcessBuilder(command);

        try {
            this.serverProcess = pb.start();
        } catch (IOException ex) {
            LOGGER.error("Could not start python server", ex);
        }
    } 
    
    
    protected void stop(){
        //TODO: send command to gracefully stop
        if(serverProcess == null)
            return;
        if(serverProcess.isAlive()){
            try {
                serverProcess.destroyForcibly().waitFor();
            } catch (InterruptedException ex) {
                LOGGER.error("Interruption while forcibly terminating python server process.", ex);
            }
        }
    } 
    
}
