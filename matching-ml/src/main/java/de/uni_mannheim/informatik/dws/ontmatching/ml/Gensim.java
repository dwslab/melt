package de.uni_mannheim.informatik.dws.ontmatching.ml;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A client class to communicate with python gensim library.
 * Singleton pattern.
 */
public class Gensim {

    public static void main(String[] args) {
        Gensim gensim = new Gensim();
        gensim.printHello("Jan");
        gensim.shutDown();
    }

    /**
     * Contructor
     */
    private Gensim(){
        this.server = new GensimPythonServer();
        server.start();
    }

    /**
     * Default logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(Gensim.class);

    /**
     * A quick technical demo. If the service works, it will print "Hello {@code name}".
     * @param name The name that shall be printed.
     */
    private void printHello(String name){
        HttpGet request = new HttpGet("http://127.0.0.1:41193/hello");
        request.addHeader("name", name);
        try (CloseableHttpResponse response = httpClient.execute(request)){
            HttpEntity entity = response.getEntity();
            if (entity != null) System.out.println(EntityUtils.toString(entity));
        } catch (IOException ioe){
            LOGGER.error("Problem with http request.", ioe);
        }
    }

    /**
     * Load a gensim model.
     * @param pathToModel The path to the model.
     * @return True if success, else false.
     */
    public boolean load(String pathToModel){
        // TODO implement
        return false;
    }

    /**
     * Instance (singleton pattern.
     */
    private static Gensim instance;

    /**
     * Server to be used.
     */
    private GensimPythonServer server;

    /**
     * Client to communicate with the server.
     */
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * Get the instance.
     * @return
     */
    public static Gensim getInstance(){
        if(instance == null) instance = new Gensim();
        return instance;
    }

    /**
     * Shut down the service.
      */
    public void shutDown(){
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.error("Could not close client.", e);
        }
        server.stop();
    }

}
