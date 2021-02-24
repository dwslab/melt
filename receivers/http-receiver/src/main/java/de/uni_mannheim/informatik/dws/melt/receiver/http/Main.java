package de.uni_mannheim.informatik.dws.melt.receiver.http;

import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.ServerException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.QoSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    
    private static final File LOCATION = getLocation();
    
    public static void main(String[] args) throws Exception {    
        
        //curl -F 'source=@cmt.rdf' -F 'target=@conference.rdf' -d "param1=value1" http://127.0.0.1:8080/match
        Server server = new Server(getPort());
        
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        
        context.addFilter(getQoSFilter(getMaxParallelRequests()), "/match", EnumSet.of(DispatcherType.REQUEST));
        
        ServletHolder uploadHolder = context.addServlet(MatcherServlet.class, "/match");
        
        //the last number which is one means that all files are written on disk and that no in memory caching applies
        uploadHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(LOCATION.getAbsolutePath(), -1, -1, 1));

        server.setHandler(context);
        
        server.start();
        server.join();
    }

    public static class MatcherServlet extends HttpServlet {

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            //we exepect a source and a target
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
                
                File sourceFile = getFile(source, "source");
                File targetFile = getFile(target, "target");
                File inputAlignmentFile = getFile(inputAlignment, "inputAlignment");
                
                URL inputAlignmentURL = null;
                if(inputAlignmentFile != null)
                    inputAlignmentURL = inputAlignmentFile.toURI().toURL();
                    
                URL resultURL = runTool(sourceFile.toURI().toURL(), targetFile.toURI().toURL(), inputAlignmentURL,
                        reduceToFirstValueAndExcludeKeys(request.getParameterMap()));
                
                sourceFile.delete();
                targetFile.delete();
                if(inputAlignmentFile != null)
                    inputAlignmentFile.delete();
                
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
                        
                URL resultURL = runTool(new URL(sourceParam), new URL(targetParam), inputAlignment,
                        reduceToFirstValueAndExcludeKeys(request.getParameterMap()));
                
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
            return;
        }    
    }
    
    private static final Set<String> EXCLUDE_KEYS = new HashSet<>(Arrays.asList("source", "target", "inputAlignment"));
    private static Map<String, String> reduceToFirstValueAndExcludeKeys(Map<String, String[]> map){
        Map<String, String> firstValueMap = new HashMap<>();
        for(Entry<String, String[]> entry : map.entrySet()){
            if(EXCLUDE_KEYS.contains(entry.getKey()) == false &&  entry.getValue().length > 0){
                firstValueMap.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        return firstValueMap;
    }
    
    private static URL runTool(URL source, URL target, URL inputAlignment, Map<String, String> parameters){
        String implementingClass = "de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.BaselineStringMatcher";
        //TODO: delete
        //String implementingClass = System.getenv("OAEI_MAIN");
        if(implementingClass == null){
            LOGGER.error("The system environment variable \"OAEI_MAIN\" is not defined - abort");
            return null;
        }
        
        IOntologyMatchingToolBridge bridge;
        try {
            Class clazz = Class.forName(implementingClass);
            bridge = (IOntologyMatchingToolBridge) clazz.newInstance();
        } catch (ClassNotFoundException ex) { 
            LOGGER.error("Could not find class " + implementingClass, ex);
            return null;
        } catch (InstantiationException ex) {
            LOGGER.error("Could not instantiate class " + implementingClass, ex);
            return null;
        } catch (IllegalAccessException ex) {
            LOGGER.error("Could not access class " + implementingClass, ex);
            return null;
        }
        LOGGER.info("Server starts matcher class {} for task:\nSource:{}\nTarget:{}\nInputAlignment:{}\nParameter:{}", 
                implementingClass, source, target, inputAlignment, parameters);
        URL result;
        try {
            if(inputAlignment == null)
                result = bridge.align(source, target);
            else
                result = bridge.align(source, target, inputAlignment);
        } catch (ToolBridgeException ex) {
            LOGGER.error("Could not call align method of IOntologyMatchingToolBridge: " + ex.getMessage(), ex);
            return null;
        }
        if(result == null){
            LOGGER.error("Result of IOntologyMatchingToolBridge is null");
            return null;
        }
        return result;
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
            return null;
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
                System.out.println("could not parse port number");
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
                System.out.println("could not parse MELT_MAX_REQUESTS");
            }
        }
        return maxRequests;
    }
    
    
    private static File getLocation(){
        String defaultLocation = new File(new File(System.getProperty("java.io.tmpdir")), "MELT-fileupload").getAbsolutePath();
        
        String location = System.getenv("MELT_LOCATION");
        if(location == null){
            location = defaultLocation;
        }
        File locationDir = new File(location);
        if(locationDir.isDirectory() == false){
            LOGGER.warn("Environment variable MELT_LOCATION is not a path to a directory. Use default: {}", defaultLocation);
            locationDir = new File(defaultLocation);
        }
        
        if (!locationDir.exists()) locationDir.mkdirs();
        return locationDir;
    }
}
