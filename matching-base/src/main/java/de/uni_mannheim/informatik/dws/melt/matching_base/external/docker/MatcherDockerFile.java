package de.uni_mannheim.informatik.dws.melt.matching_base.external.docker;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Info;
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
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang.StringUtils;
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
public class MatcherDockerFile extends MatcherURL implements AutoCloseable {


    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherDockerFile.class);
    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");
    
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int DEFAULT_EXPOSED_PORT = 8080;
    //other ports can be: OAEI  15159 (position alphabet)  or 6234 ( cellphone )
    
    private DockerClient dockerClient;
    /**
     * The callback for log entries.
     * This is an attribute of the class because it should be closed.
     */
    private DockerLogCallback logCallback = null; 
    
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
    private int initialWaitingTimeInSeconds = 0;

    /**
     * Initializes a matcher which starts a docker image to create a docker container in which a HTTP server runs.
     * @param imageName the image name to use (docker image name).
     * @param dockerImageFile a file which contains the image.
     * @param config the config to connect to a docker machine.
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
        try {
            Info i = this.dockerClient.infoCmd().exec();
            LOGGER.info("Connected to docker machine: {}", i.getName());
        } catch(Exception ex) {
            LOGGER.warn("No connection to docker could be established. Check if docker is running on your machine.");
            throw new DockerNotRunningException("Docker is probably not running.", ex);
        }
        
        if(dockerImageFile != null)
            loadDockerFileInternal(dockerImageFile);
        if(!freshInstance){
            startContainer();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if(this.containerId != null){
                    LOGGER.info("JVM shutdown detected and container {} is shut down now. Please call close method of MatcherDockerFile to remove this message.", this.containerId);
                    this.stopContainer();
                    LOGGER.info("Shutdown completed.");
                }
            }));
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

    /**
     * Constructor.
     * @param imageName Name of the image. The image must already be in the local docker registry.
     */
    public MatcherDockerFile(String imageName) {
        this(imageName, (File) null);
    }

    /**
     * Constructor. Obtains the image name form the file name.
     * @param dockerImageFile The file which contains the image. The file must carry the name of the image optionally
     *                       succeeded by a '-latest' postfix.
     */
    public MatcherDockerFile(File dockerImageFile){
        this(getImageNameFromFileContent(dockerImageFile), dockerImageFile);
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
        LOGGER.info("Load docker image from file {} to docker local registry. If image is already there, use constructor MatcherDockerFile(String imageName).", dockerImageFile);
        try (InputStream imagePayload = new BufferedInputStream(new FileInputStream(dockerImageFile))) {
            this.dockerClient.loadImageCmd(imagePayload).exec();
        } catch (IOException ex) {
            LOGGER.warn("Could not load dockerImageFile.", ex);
        }
    }

    private void startContainer(){
        int containerPort = this.getContainerPort();
        this.hostPort = this.getFreePortOnHost();
        
        String bindingHostIp = null; //bind on all interfaces - this makes it also public
        if(this.runOnlyLocalhost){
            bindingHostIp = "127.0.0.1"; // bind only on localhost - access only from the same machine
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
        if(this.containerId == null){
            LOGGER.warn("Could not create container and start it because the container id is null.");
            return;
        }            
        this.logCallback = dockerClient.attachContainerCmd(r.getId())
                .withStdErr(true)
                .withStdOut(true)                
                .withFollowStream(true)
                .withLogs(true)
                .exec(new DockerLogCallback());
        
        dockerClient.startContainerCmd(this.containerId).exec();
        LOGGER.info("Container started with id {}", this.containerId);
        //if(this.containerId.length() > 12){
        //    LOGGER.info("To see log output of container during execution, run: docker container logs {}", this.containerId.substring(0, 12));
        //}else{
        //    LOGGER.info("To see log output of container during execution, run: docker container logs {}", this.containerId);
        //}
        /*
        this.dockerClient.attachContainerCmd(this.containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withLogs(true)
                .exec(new DockerLogCallback());
        */
    }
    
    private void stopContainer(){
        if(this.containerId == null)
            return;
        if(this.logCallback != null){
            try {
                this.logCallback.close();
            } catch (IOException ex) {
                LOGGER.warn("Could not close stream of docker callback for standard out and standard error.", ex);
            }
        }
        
        dockerClient.stopContainerCmd(this.containerId).exec();        
        dockerClient.removeContainerCmd(this.containerId).exec();
        this.containerId = null;
    }

    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        if(freshInstance){
            startContainer();
        }
        try{
            URI uri = new URI("http://localhost:" + this.hostPort + "/match");

            // Let's wait for some seconds before we try to connect via HTTP.
            // Docker is typically not yet ready.
            if(initialWaitingTimeInSeconds > 0) {
                try {
                    Thread.sleep((long) initialWaitingTimeInSeconds * 1000);
                } catch (InterruptedException ie) {
                    LOGGER.error("Problem occurred while trying to sleep.", ie);
                }
            }
            MatcherHTTPCall httpCall = new MatcherHTTPCall(uri, true, this.socketTimeout, this.connectTimeout, this.connectionRequestTimeout);
            URL result = httpCall.match(source, target, inputAlignment);
            return result;
        }finally{
            if(freshInstance){
                stopContainer();
            }
        }
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
                LOGGER.warn("Wanted to inspect the docker image but something went wrong. Use default exposed port of" +
                        " docker image.");
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
        } catch(NotFoundException ex) {
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
            LOGGER.error("Could not find free port. Returning -1", ex);
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
    
    /**
     * Calling this function will log the last x lines from the container.
     * @param numberOfLastLines the number of last lines to log.
     */
    public void logLastLinesFromContainer(int numberOfLastLines){
        if(this.containerId == null || this.containerId.isEmpty()){
            LOGGER.warn("Would like to log last lines of container but container is not started or already stopped. " +
                    "Maybe the close method was already called?");
            return;
        }
        
        DockerLogCallback callback = dockerClient.logContainerCmd(this.containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTail(numberOfLastLines)
                .exec(new DockerLogCallback());
        try {
            callback.awaitCompletion(5, TimeUnit.SECONDS);

        } catch (InterruptedException ex) {
            LOGGER.warn("Interrupted during wait", ex);
        }
    }


    /**
     * The naming convention of the MELT Web Docker files is such that the files carry the name of the image
     * with an optional postfix of '-latest' such as 'my_image-latest.tar.gz'
     * @param dockerFile The docker file of which the image name shall be retrieved.
     * @return The image name as String.
     */
    public static String getImageNameFromFileName(File dockerFile){
        String fileName = dockerFile.getName();

        // step 1: remove '.tar.gz'
        String imageName = fileName.substring(0, fileName.length() - 7);

        // step 2: remove '-latest' if it is there
        if(imageName.endsWith("-latest")){
            imageName = imageName.substring(0, imageName.length() - 7);
        }
        return imageName;
    }
    
    
    /**
     * Extracts the image name from the docker file content.
     * In more detail, it analyzes the 'repositories' file and returns the key of the corresponding json.
     * If something goes wrong, null is returned.
     * If multiple images are contained in the file, only the first one is returned.
     * @param dockerFile the docker file to extract the image name from. This file is usally created from a docker save command.
     * @return the image name contained in the docker file.
     */
    public static String getImageNameFromFileContent(File dockerFile){
        try(ArchiveInputStream archiveStream  = getUncompressedStream(new BufferedInputStream(new FileInputStream(dockerFile)))){
            ArchiveEntry archiveEntry;
            while ((archiveEntry = archiveStream.getNextEntry()) != null) {
                if(archiveEntry.getName().equals("repositories")){
                    JsonNode rootNode = mapper.readTree(archiveStream);
                    if(rootNode == null){
                        LOGGER.warn("File 'repositories' within docker file {} could not be parsed because it is empty.", dockerFile);
                        return null;
                    }
                    Iterator<String> fields = rootNode.fieldNames();
                    if(fields.hasNext() == false){
                        LOGGER.warn("Could not extract image name from file. Repositories file has no elements.");
                        return null;
                    }
                    String imageName = fields.next();
                    if(StringUtils.isBlank(imageName)){
                        LOGGER.warn("Extracted image name is blank.");
                        return null;
                    }
                    
                    if(fields.hasNext()){
                        LOGGER.warn("Multiple images names exists in 'repositories' file within docker file {}. Choosing the first one.", dockerFile);
                    }
                    return imageName;
                }
            }
            LOGGER.warn("Did not find the 'repositories' file within docker file {}.", dockerFile);
            return null;
        } catch(JsonParseException ex){
            LOGGER.info("Could not parse json file 'repositories' within docker file " + dockerFile.getPath(), ex);
            return null;
        }
        catch (IOException ex) {
            LOGGER.warn("IOException occured during extraction of docker image name. Return null.", ex);
            return null;
        } catch (ArchiveException ex) {
            LOGGER.warn("Docker file is not a archive (e.g. tar etc)", ex);
            return null;
        }
    }
    
    private static ArchiveInputStream getUncompressedStream(InputStream inputStream) throws ArchiveException{
        try{
            inputStream = new CompressorStreamFactory().createCompressorInputStream(inputStream);
        }catch(CompressorException ex){
            // Stream not compressed (or unknown compression scheme)
        }
        if (!inputStream.markSupported()) {
          inputStream = new BufferedInputStream(inputStream);
        }
        return new ArchiveStreamFactory().createArchiveInputStream(inputStream);
    }
    
    public String getAllLogLinesFromContainer(){
        if(this.containerId == null || this.containerId.isEmpty()){
            LOGGER.warn("Would like to log last lines of container but container is not started or already stopped. " +
                    "Maybe the close method was already called?");
            return "";
        }

        DockerStringCallback callback = dockerClient.logContainerCmd(this.containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTailAll()
                .exec(new DockerStringCallback());

        try {
            callback.awaitCompletion(5, TimeUnit.SECONDS);
            return callback.getLog();
        } catch (InterruptedException ex) {
            LOGGER.warn("Interrupted during wait", ex);
            return "";
        }
    }

    /**
     * Calling this function will log all lines from the container using SLF4J.
     */
    public void logAllLinesFromContainer(){
        if(this.containerId == null || this.containerId.isEmpty()){
            LOGGER.warn("Would like to log last lines of container but container is not started or already stopped. " +
                    "Maybe the close method was already called?");
            return;
        }
        
        DockerLogCallback callback = dockerClient.logContainerCmd(this.containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTailAll()
                .exec(new DockerLogCallback());
        try {
            callback.awaitCompletion(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            LOGGER.warn("Interrupted during wait", ex);
        }
    }

    public int getInitialWaitingTimeInSeconds() {
        return initialWaitingTimeInSeconds;
    }

    public void setInitialWaitingTimeInSeconds(int initialWaitingTimeInSeconds) {
        this.initialWaitingTimeInSeconds = initialWaitingTimeInSeconds;
    }
}

/**
 * ResultCallback collecting the full log and returning it as a single string for further processing.
 * May be memory-intense for very large logs.
 */
class DockerStringCallback extends ResultCallback.Adapter<Frame> {

    StringBuilder logBuilder = new StringBuilder();

    @Override
    public void onNext(Frame item){
        logBuilder.append(item.toString()).append("\n");
    }

    public String getLog(){
        return logBuilder.toString();
    }
}

/**
 * ResultCallback logging directly using the SLF4J logger.
 */
class DockerLogCallback extends ResultCallback.Adapter<Frame> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerLogCallback.class);

    @Override
    public void onNext(Frame item) {
        LOGGER.info(item.toString());
    }
}
