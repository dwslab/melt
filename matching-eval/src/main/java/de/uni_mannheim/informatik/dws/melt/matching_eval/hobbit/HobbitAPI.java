package de.uni_mannheim.informatik.dws.melt.matching_eval.hobbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Provides a way to interact with HOBBIT.
 * Especially to retive the log files produced in HOBBIT.
 */
public class HobbitAPI {

    private static final Logger logger = LoggerFactory.getLogger(HobbitAPI.class);

    private static final String newline = System.getProperty("line.separator");
    private static final ObjectMapper mapper = new ObjectMapper();

    private static String baseHobbitURL = "project-hobbit.eu";
    private static String accessToken = "";

    public static void setAccessToken(String _accessToken) {
        accessToken = _accessToken;
    }
    
    public static void setAccessTokenByCredentials(String username, String password){
        //keycloak session get bearer token
        //https://gist.github.com/rac021/623e4f4c87069acd0c38d952568f8a3d
        //https://gist.github.com/amacoder/ca2c2193172068724f86dde91081e317
        try{
            String urlParameters  = String.format("grant_type=password&client_id=Hobbit-GUI&username=%s&password=%s", username, password);
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            URL myURL = new URL("https", "keycloak." + baseHobbitURL, "/auth/realms/Hobbit/protocol/openid-connect/token");
            HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
            myURLConnection.setRequestMethod("POST");
            myURLConnection.setDoOutput(true);            
            myURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
            myURLConnection.setRequestProperty("charset", "utf-8");
            myURLConnection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            try( DataOutputStream wr = new DataOutputStream(myURLConnection.getOutputStream())) {
                wr.write(postData);
            }            
            myURLConnection.connect();
            JsonNode rootNode = mapper.readTree(myURLConnection.getInputStream());            
            accessToken = rootNode.path("access_token").asText();
        }catch(IOException e){
            logger.error("Couldn't set the access token based on credentials.", e);
        }
    }
    
    public static void setAccessTokenByMavenCredentials(){
        Settings settings;
        try {
            settings = new SettingsXpp3Reader().read(new FileReader(new File(System.getProperty("user.home"), ".m2/settings.xml")));
        } catch (FileNotFoundException ex) {
            logger.error("Found no maven settings.xml file at {userhome}/.m2/settings.xml", ex);
            return;
        } catch (IOException | XmlPullParserException ex) {
            logger.error("Could not read maven settings.xml file at {userhome}/.m2/settings.xml", ex);
            return;
        }
        for (Server server : settings.getServers()) {
            String id = server.getId();
            if(id.equals("git." + baseHobbitURL)){
                setAccessTokenByCredentials(server.getUsername(), server.getPassword());
                return;
            }
        }
        logger.error("Found no server in maven settings.xml to extract username and password");
    }
    
    private static String makeRequest(String pathURL, String request, String payload) throws IOException {        
        BufferedReader reader = null;
        try {
            URL myURL = new URL("https", "master." + baseHobbitURL, pathURL);
            HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
            myURLConnection.setRequestMethod(request);
            myURLConnection.setRequestProperty("authorization", "bearer " + accessToken);
            if(payload != null){
                myURLConnection.setRequestProperty("Content-Type", "application/json"); 
                myURLConnection.setDoOutput(true);
                try(OutputStreamWriter writer = new OutputStreamWriter(myURLConnection.getOutputStream(), "UTF-8")){
                    writer.write(payload);
                }
            }
            myURLConnection.connect();
            reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
    
    private static String makeRequestGet(String pathURL) throws IOException {
        return makeRequest(pathURL, "GET", null);
    }
    
    private static String makeRequestPost(String pathURL, String payload) throws IOException {
        return makeRequest(pathURL, "POST", payload);
    }
    
    public static List<String> parseJsonLog(String json) throws IOException {
        return parseJsonLog(mapper.readTree(json));
    }
    
    public static List<String> parseJsonLog(File f) throws IOException {
        return parseJsonLog(mapper.readTree(f));
    }

    private static List<String> parseJsonLog(JsonNode rootNode) throws IOException {
        List<String> messages = new ArrayList<>();
        if (rootNode.isArray() == false) {
            logger.error("Json root node of log is not an array. Returning empty log.");
            return messages;
        }
        for (JsonNode messsageObject : rootNode) {
            messages.add(messsageObject.path("_source").path("message").asText());
        }
        Collections.reverse(messages);
        return messages;
    }

    public static List<String> getSystemLog(String experimentID) throws IOException {
        String json = makeRequestGet("/rest/logs/system/query?id=" + experimentID);
        return parseJsonLog(json);
    }

    public static List<String> getBenchmarkLog(String experimentID) throws IOException {
        String json = makeRequestGet("/rest/logs/benchmark/query?id=" + experimentID);
        return parseJsonLog(json);
    }

    public static void printSystemLog(String experimentID) {
        try {
            logger.info("Following the system log for experiment id " + experimentID + newline + 
                    String.join(newline, getSystemLog(experimentID)));
        } catch (IOException ex) {
            logger.error("Could not print log.", ex);
        }
    }

    public static void printBenchmarkLog(String experimentID) {
        try {
            logger.info("Following the system log for experiment id " + experimentID + newline + 
                    String.join(newline, getBenchmarkLog(experimentID)));
        } catch (IOException ex) {
            logger.error("Could not print log.", ex);
        }
    }
    
    public static void saveBenchmarkLog(String experimentID, File f) {
        try (PrintWriter out = new PrintWriter(f)) {
            out.println(String.join(newline, getBenchmarkLog(experimentID)));
        } catch (IOException ex) {
            logger.error("Could not save benchmark log to file.", ex);
        }
    }
    
    public static void saveSystemLog(String experimentID, File f) {
        try (PrintWriter out = new PrintWriter(f)) {
            out.println(String.join(newline, getSystemLog(experimentID)));
        } catch (IOException ex) {
            logger.error("Could not save system log to file.", ex);
        }
    }
    
    public static void saveSystemAndBenchmarkLog(String experimentID, File directory) {
        directory.mkdirs();
        saveBenchmarkLog(experimentID, new File(directory, experimentID + "_benchmark_log.txt"));
        saveSystemLog(experimentID, new File(directory, experimentID + "_system_log.txt"));
    }
    
    public static void saveSystemAndBenchmarkLog(String experimentID, String directory) {
        saveSystemAndBenchmarkLog(experimentID, new File(directory));
    }
    
    
    public static void printLog(File f) {
        try {
            logger.info("Following the log from file " + f.getPath() + newline + 
                    String.join(newline, parseJsonLog(f)));
        } catch (IOException ex) {
            logger.error("Could not print log.", ex);
        }
    }
    
    
    public static void waitForResults(String experimentID) throws IOException {
        
        String s = makeRequestGet("/rest/experiments/query?id=" + experimentID);
        logger.info(s);
    }
    
    
    public static String runBenchmarkAnatomy(String systemUrl, String systemName) throws IOException{
        ObjectNode root = mapper.createObjectNode();
        root.put("benchmark", "http://w3id.org/bench#Anatomy");        
        root.put("benchmarkName", "OAEI Anatomy benchmark");
        root.put("system", systemUrl);
        root.put("systemName", systemName);
        ArrayNode configurationParams = root.putArray("configurationParams");        
        ObjectNode oneconfig = configurationParams.addObject();
        oneconfig.put("id", "http://w3id.org/bench#anatomyTask");
        oneconfig.put("name", "The name of the task within the Anatomy track.");
        oneconfig.put("description", "The name of the task within the Anatomy track. Options: AMA-NCI.");
        oneconfig.put("range", "http://w3id.org/bench#AnatomySubTask");
        oneconfig.put("value", "http://w3id.org/bench#AMA-NCI");    
        String s = mapper.writeValueAsString(root);
        return mapper.readTree(makeRequestPost("/rest/benchmarks", s)).path("id").asText();
    }
}
