package de.uni_mannheim.informatik.dws.melt.matching_base.external.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.http.MatcherHTTPCall;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This matcher creates a docker container based on a given docker image name.
 * Within this container a matcher server should be started.
 * Therefore it will use the MatcherHTTPCall internally to run the matcher.
 * For this Matcher to work you have to add the following dependency to YOUR pom:
 * <pre>{@code 
 *<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-core</artifactId>
    <version>3.2.7</version><!--maybe update version-->
</dependency>
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-transport-httpclient5</artifactId>
    <version>3.2.7</version><!--maybe update version-->
</dependency>
 * }</pre>
With this in place everything should work.
*/
public class MatcherDockerFile extends MatcherURL implements AutoCloseable{

    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherDockerFile.class);
    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");
    

    private static final int DEFAULT_EXPOSED_PORT = 8080;
    //other ports can be: OAEI  15159 (position alphabet)  or 6234 ( cellphone )
    
    private DockerClient dockerClient;
    
    private String imageName;
    
    /**
     * If true, the container binds only on localhost instead of all interfaces.
     * True is the default.
     */
    private boolean runOnlyLocalhost;
    
    /**
     * The containerID for the current running container.
     * This is set to null if no container is running.
     */
    private String containerId;
    private int hostPort;
    
    private boolean freshInstance = false;
    
    //timeout for the inner MatcherHTTPCall
    private int socketTimeout = 0;
    private int connectTimeout = 0;
    private int connectionRequestTimeout = 0;
    

    /**
     * Initializes a matcher which starts a docker image to create a docker container in which a HTTP server runs.
     * @param imageName the image name to use (docker image name)
     * @param dockerImageFile a file which contains the image
     * @param config the config to connect to a docker machine
     * @param runOnlyLocalhost true if all ports should be bound to localhost. If false, all ports are bound to all interfaces (0.0.0.0) and makes the server also available from outside.
     * @param freshInstance if true, with every call of the match method, a new container is started (default is false because it should not be necessary).
     */
    public MatcherDockerFile(String imageName, File dockerImageFile, DockerClientConfig config, boolean runOnlyLocalhost, boolean freshInstance) {
        this.imageName = imageName;
        this.runOnlyLocalhost = runOnlyLocalhost;
        this.freshInstance = freshInstance;
        this.dockerClient = DockerClientImpl.getInstance(config, new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build());
        
        if(dockerImageFile != null)
            loadDockerFileInternal(dockerImageFile);
        if(freshInstance == false){
            startContainer();
        }
    }
    
    public MatcherDockerFile(String imageName, File dockerImageFile, DockerClientConfig config) {
        this(imageName, dockerImageFile, config, true, false);
    }
    
    /**
     * Loads the images in the docker file given by dockerImageFile.
     * And then runs the image given by imageName.
     * @param imageName the image name
     * @param dockerImageFile the file which contains the image given by imageName
     */
    public MatcherDockerFile(String imageName, File dockerImageFile) {
        this(imageName, dockerImageFile, getDockerConfigBuilder().build());
    }
    
    //public MatcherDockerFile(String imageName, DockerClientConfig config) {
    //    this(imageName, null, config);
    //}
    
    public MatcherDockerFile(String imageName) {
        this(imageName, (File) null);
    }
    
     
    public void loadDockerFile(File dockerImageFile){
        loadDockerFileInternal(dockerImageFile);
    }
    
    public static DefaultDockerClientConfig.Builder getDockerConfigBuilder(){
        DefaultDockerClientConfig.Builder b = DefaultDockerClientConfig.createDefaultConfigBuilder();
        if(IS_WINDOWS){
            //based on https://docs.docker.com/desktop/faqs/#how-do-i-connect-to-the-remote-docker-engine-api
            b.withDockerHost("npipe:////./pipe/docker_engine");
        }
        return b;
    }
     
    private void loadDockerFileInternal(File dockerImageFile){
        try (InputStream imagePayload = new BufferedInputStream(new FileInputStream(dockerImageFile))) {
            this.dockerClient.loadImageCmd(imagePayload).exec();
        } catch (IOException ex) {
            LOGGER.warn("Could not load dockerImageFile.", ex);
        }
    }
    
    
    private void startContainer(){
        int containerPort = this.getContainerPort();
        this.hostPort = this.getFreePortOnHost();
        
        String bindingHostIp = null;
        if(this.runOnlyLocalhost){
            bindingHostIp = "127.0.0.1";
        }
        
        PortBinding binding = new PortBinding(new Binding(bindingHostIp, "" + this.hostPort), new ExposedPort(containerPort));
        
        //HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(new PortBinding(new Binding(null, "8080"), new ExposedPort(8080)));
        //HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(PortBinding.parse("8080:8080"));
        HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(binding);
        
        LOGGER.info("Starting container from image {} (port {} from container is mapped to port {} in host)", this.imageName, containerPort, this.hostPort);
        CreateContainerResponse r = dockerClient.createContainerCmd(this.imageName)
                .withHostConfig(hostConfig)
                .exec();
        
        this.containerId = r.getId();
        dockerClient.startContainerCmd(this.containerId).exec();
    }
    
    private void stopContainer(){
        if(this.containerId == null)
            return;
        
        dockerClient.stopContainerCmd(this.containerId).exec();        
        dockerClient.removeContainerCmd(this.containerId).exec();
        this.containerId = null;
    }

    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        if(freshInstance){
            startContainer();
        }
        
        URI uri = new URI("http://localhost:" + this.hostPort + "/match");
        
        //TODO: wait for the service to be up and running
        MatcherHTTPCall httpCall = new MatcherHTTPCall(uri, true, this.socketTimeout, this.connectTimeout, this.connectionRequestTimeout);
        URL result = httpCall.match(source, target, inputAlignment);
        
        if(freshInstance){
            stopContainer();
        }
        return result;
    }
    
    /**
     * Return the exposed port of the docker image.
     * This port should be mapped to a host port to communicate with the started container.
     * @return the port which is exposed by the image.
     */
    private int getContainerPort(){
        try{
            InspectImageResponse response = this.dockerClient.inspectImageCmd(this.imageName).exec();
            if(response == null){
                LOGGER.warn("Wanted to inspect the docker image but somethin went wrong. Use default exposed port of docker image.");
                return DEFAULT_EXPOSED_PORT;
            }
            ContainerConfig config = response.getConfig();
            if(config == null){
                LOGGER.warn("No config section in docker inspect command. Use default exposed port of docker image.");
                return DEFAULT_EXPOSED_PORT;
            }
            ExposedPort[] ports = config.getExposedPorts();
            if(ports == null || ports.length == 0){
                //no exposed ports - use default
                LOGGER.debug("No exposed ports. Use the default one: {}", DEFAULT_EXPOSED_PORT);
                return DEFAULT_EXPOSED_PORT;
            }
            if(ports.length > 1){
                LOGGER.warn("Multiple ports are exposed by docker image. Just choose the first one.");
            }
            return ports[0].getPort();
        }catch(NotFoundException ex){
            LOGGER.warn("Docker image with name {} is not found. Use default exposed port of docker image.", this.imageName, ex);
            return DEFAULT_EXPOSED_PORT;
        }
    }

    /**
     * Returns a free port number on localhost.
     * @return a free port number on localhost
     */
    private int getFreePortOnHost() {
        //https://gist.github.com/vorburger/3429822
        try (ServerSocket socket = new ServerSocket(0)) {
            //socket.setReuseAddress(true);//why? 
            return socket.getLocalPort();
        } catch (IOException ex) {
            LOGGER.error("Could not find unsed port. Returning -1", ex);
            return -1;
        }
    }

    @Override
    public void close() throws Exception {
        stopContainer();
        this.dockerClient.close();
    }
    
    
    /**
     * Sets the timeouts for the HTTP call which happens when the docker container is started and the call to the service is executed.
     * @param socketTimeout the time in milliseconds waiting for data – after the connection is established; 
     *      maximum time between two data packets. Zero means infinite timeout. Negative usually means systems default.
     * @param connectTimeout the timeout in milliseconds until a connection is established.
     *      Zero means infinite timeout. Negative usually means systems default.
     * @param connectionRequestTimeout timeout in milliseconds when requesting a connection from the connection manager.
     *      Zero means infinite timeout. Negative usually means systems default.
     */
    public void setTimeouts(int socketTimeout, int connectTimeout, int connectionRequestTimeout){
        this.socketTimeout = socketTimeout;
        this.connectTimeout = connectTimeout;
        this.connectionRequestTimeout = connectionRequestTimeout;
    }
    
}
