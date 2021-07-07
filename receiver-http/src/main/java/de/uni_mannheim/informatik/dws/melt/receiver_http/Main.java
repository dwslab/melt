package de.uni_mannheim.informatik.dws.melt.receiver_http;

import de.uni_mannheim.informatik.dws.melt.matching_base.receiver.MainMatcherClassExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.rmi.ServerException;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.logging.Level;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.QoSFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    
    private static final File LOCATION = getLocation();
    
    public static void main(String[] args) throws Exception {    
        //Thread.sleep(20000);
        //curl -F 'source=@cmt.rdf' -F 'target=@conference.rdf' -d "param1=value1" http://127.0.0.1:8080/match
        
        //parameters:
        int port = getPort();
        int maxParallelRequests = getMaxParallelRequests();
       
        Server server = new Server(port);
        
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addFilter(getQoSFilter(maxParallelRequests), "/match", EnumSet.of(DispatcherType.REQUEST));
        server.setHandler(context);
        
        
        //TODO: make the index page show up
        //URI rootURI = getRootDir();
        //if(rootURI != null){
        //    LOGGER.info("Set root dir to: {}", rootURI);
        //    context.setBaseResource(Resource.newResource(rootURI));
        //    context.setWelcomeFiles(new String[]{"index.html"});
        //}
        //https://stackoverflow.com/questions/20207477/serving-static-files-from-alternate-path-in-embedded-jetty
        //https://stackoverflow.com/questions/39011587/jetty-default-servlet-context-path
        
        //first servlet which takes care about the match method and run the actual matcher
        ServletHolder uploadHolder = context.addServlet(MatcherServlet.class, "/match");
        //the last number which is one means that all files are written on disk and that no in memory caching applies
        uploadHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(LOCATION.getAbsolutePath(), -1, -1, 1));

        //default servlet for mapping the index / welcome page
        ServletHolder holderPwd = new ServletHolder("default", DefaultServlet.class);
        holderPwd.setInitParameter("dirAllowed","false");
        context.addServlet(holderPwd,"/");
        
        LOGGER.info("Matching service runs at: http://localhost:{}/match", port);
        
        server.start();
        server.join();
    }
    
    private static URI getRootDir(){
        URL indexURL = Main.class.getClassLoader().getResource("static/index.html");
        if (indexURL == null){
            return null;
        }
        LOGGER.info("indexURL: {}", indexURL);
        try {
            return indexURL.toURI().resolve("./").normalize();
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    public static class MatcherServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            // we expect a source and a target
            if(isMultipartContent(request)){
                LOGGER.info("Got multipart request - start matching");
                Part source = request.getPart("source");            
                if(source == null){
                    throw new ServerException("No multipart parameter source");
                }
                Part target = request.getPart("target");
                if(target == null){
                    throw new ServerException("No multipart parameter target");
                }
                Part inputAlignment = request.getPart("inputAlignment");
                Part parameters = request.getPart("parameters");
                
                File sourceFile = getFile(source, "source");
                File targetFile = getFile(target, "target");
                File inputAlignmentFile = getFile(inputAlignment, "inputAlignment");
                File parametersFile = getFile(parameters, "parameters");
                
                URL inputAlignmentURL = null;
                if(inputAlignmentFile != null)
                    inputAlignmentURL = inputAlignmentFile.toURI().toURL();
                
                URL parametersURL = null;
                if(parametersFile != null)
                    parametersURL = parametersFile.toURI().toURL();
                    
                URL resultURL = runTool(sourceFile.toURI().toURL(), targetFile.toURI().toURL(), inputAlignmentURL, parametersURL);
                
                sourceFile.delete();
                targetFile.delete();
                if(inputAlignmentFile != null)
                    inputAlignmentFile.delete();
                if(parametersFile != null)
                    parametersFile.delete();
                
                if(resultURL != null){
                    sendFileContent(resultURL, response);
                }
            }else{
                LOGGER.info("Got URL request - start matching");
                String sourceParam = request.getParameter("source");
                if(sourceParam == null ){
                    throw new ServerException("No parameter source");
                }
                String targetParam = request.getParameter("target");
                if(targetParam == null ){
                    throw new ServerException("No parameter target");
                }
                URL inputAlignment = null;
                String inputAlignmentStr = request.getParameter("inputAlignment");
                if(inputAlignmentStr != null)
                    inputAlignment = new URL(inputAlignmentStr);
                
                URL parameters = null;
                String parametersStr = request.getParameter("parameters");
                if(parametersStr != null)
                    parameters = new URL(parametersStr);
                        
                URL resultURL = runTool(new URL(sourceParam), new URL(targetParam), inputAlignment, parameters);
                
                if(resultURL != null)
                    response.getWriter().write(resultURL.toString());
            }
        }
    }
    
    private static void sendFileContent(URL url, HttpServletResponse response){
        File mappingFile;
        try {
            mappingFile = new File(url.toURI());
        } catch (URISyntaxException ex) {
            LOGGER.error("Couldn't convert result URL to URI");
            return;
        }
        if(mappingFile.exists() == false){
            LOGGER.error("Mapping file does not exists. No value is returned");
            return;
        }
        
        response.setContentType("text/plain");
        response.setContentLength((int) mappingFile.length());
        response.setHeader(
                "Content-Disposition", 
                String.format("attachment; filename=\"%s\"", mappingFile.getName()));

        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        try(InputStream inStream = new FileInputStream(mappingFile);
            OutputStream outStream = response.getOutputStream()){
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            LOGGER.error("Could not send the mapping file.", ex);
        }    
    }
    
    
    private static URL runTool(URL source, URL target, URL inputAlignment, URL parameters){
        String mainClass;
        try {
            mainClass = MainMatcherClassExtractor.extractMainClass();
        } catch (IOException ex) {
            LOGGER.error("Could not extract Main class name. Do nothing." + ex.getMessage());
            return null;
        }
        //mainClass = "de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.BaselineStringMatcher";
        
        LOGGER.info("Server starts matcher class {} for task:\nSource:{}\nTarget:{}\nInputAlignment:{}\nParameter:{}", 
                mainClass, source, target, inputAlignment, parameters);
        
        AlignmentAndParameters result = null;
        try {
            result = GenericMatcherCaller.runMatcher(mainClass, source, target, inputAlignment, parameters);
        } catch (Exception ex) {
            LOGGER.error("Exception during matching.", ex);
            return null;
        }
        if(result.getAlignment() == null){
            LOGGER.error("The resulting alignment of the matcher is null.");
            return null;
        }
        try {
            return TypeTransformerRegistry.getTransformedObject(result.getAlignment(), URL.class);
        } catch (TypeTransformationException ex) {
            LOGGER.error("Cannot transform the alignment to a URL and then to a file.", ex);
            return null;
        }
    }
    
    
    
    private static FilterHolder getQoSFilter(int maxRequests){
        FilterHolder holder = new FilterHolder(new QoSFilter());
        holder.setInitParameter("maxRequests", String.valueOf(maxRequests));
        return holder;
    }
    
    private static final String MULTIPART = "multipart/";
    
    /**
     * Determines whether the request contains multipart content.
     * @param request The request to be evaluated
     * @return true, if the request is multipart otherwise false
     */
    public static final boolean isMultipartContent(HttpServletRequest request) {
        if (!"post".equals(request.getMethod().toLowerCase())) {
            return false;
        }
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        if (contentType.toLowerCase().startsWith(MULTIPART)) {
            return true;
        }
        return false;
    }
    
    
    private static final SecureRandom random = new SecureRandom();

    private static File getFile(Part part, String prefix) throws IOException{
        if(part == null)
            return null;
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        
        String filename = prefix + "-" + Long.toString(n) + "." + getFilenameExtension(part.getSubmittedFileName(), "rdf");
        part.write(filename);
        return new File(LOCATION, filename);
    }
    
    private static String getFilenameExtension(String filename, String defaultValue){
        if (filename == null) {
            return defaultValue;
        }
        final int index = filename.lastIndexOf('.');
        if (index == -1) {
            return defaultValue;
        } else {
            return filename.substring(index + 1);
        }
    }
    
    private static int getPort(){
        int port = 8080;
        String envPort = System.getenv("MELT_PORT");
        if(envPort != null){
            try{
                port = Integer.parseInt(envPort);
            }catch(NumberFormatException e){
                LOGGER.warn("could not parse port number - using default port 8080");
            }
        }
        return port;
    }
    
    private static int getMaxParallelRequests(){
        int maxRequests = 1;
        String max = System.getenv("MELT_MAX_REQUESTS");
        if(max != null){
            try{
                maxRequests = Integer.parseInt(max);
            }catch(NumberFormatException e){
                LOGGER.warn("could not parse MELT_MAX_REQUESTS - using default value of 1");
            }
        }
        return maxRequests;
    }
    
    private static File getLocation(){
        String location = System.getenv("MELT_LOCATION");
        if(location == null){
            return getDefaultLocation();
        }
        File locationDir = new File(location);
        if(locationDir.isDirectory() == false){
            LOGGER.warn("Environment variable MELT_LOCATION is not a path to a directory. Use default");
            return getDefaultLocation();
        }
        locationDir.mkdirs();
        return locationDir;
    }
    
    private static File getDefaultLocation(){
        try {
            File f = Files.createTempDirectory("MELT-fileupload").toFile();
            f.mkdirs();
            return f;
        } catch (IOException ex) {
            LOGGER.error("Could not create a directory in the tmp folder", ex);
            return null;
        }
    }
    
}
