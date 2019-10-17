package de.uni_mannheim.informatik.dws.ontmatching.ml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manage the gensim python server.
 */
public class GensimPythonServer {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GensimPythonServer.class);

    /**
     * The python process.
     */
    protected Process serverProcess;


    public static void main(String[] args) {
        GensimPythonServer server = new GensimPythonServer();
        LOGGER.info("Start");
        server.start();
        server.stop();
    }


    /**
     * Constructor
     */
    public GensimPythonServer(){
        this.serverProcess = null;
    }

    /**
     * Initializes the server.
     */
    protected void start(){
        String canonicalPath = "";
        File serverFile = new File("./matching-ml/oaei-resources/python_server.py");
        try {
            if(!serverFile.exists()){
                LOGGER.error("Server File does not exist. Cannot start server. ABORTING.");
                return;
            }
            canonicalPath = serverFile.getCanonicalPath();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Server File does not exist. Cannot start server. ABORTING.");
            return;
        }
        List<String> command = Arrays.asList("python", "\"" + canonicalPath + "\"");
        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            pb.inheritIO();
            this.serverProcess = pb.start();
            for(int i = 0; i < 3; i++){
                HttpGet request = new HttpGet("http://127.0.0.1:41193/melt_ml.html");
                CloseableHttpClient httpClient = HttpClients.createDefault();
                try (CloseableHttpResponse response = httpClient.execute(request)){
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        LOGGER.info("Server is running.");
                        break;
                    }
                } catch (HttpHostConnectException hce){
                    LOGGER.info("Server is not yet running. Waiting 5 seconds.");
                    TimeUnit.SECONDS.sleep(5);
                } catch (IOException ioe){
                    LOGGER.error("Problem with http request.", ioe);
                }
                httpClient.close();
            }
        } catch (IOException ex) {
            LOGGER.error("Could not start python server.", ex);
        } catch (InterruptedException e) {
            LOGGER.error("Could not wait for python server.", e);
        }
    }

    /**
     * Stops the server.
     */
    protected void stop(){
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
