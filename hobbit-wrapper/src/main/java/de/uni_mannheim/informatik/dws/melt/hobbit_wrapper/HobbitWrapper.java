package de.uni_mannheim.informatik.dws.melt.hobbit_wrapper;

import de.uni_mannheim.informatik.dws.melt.matching_base.OaeiOptions;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.io.FileUtils;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.rabbit.SimpleFileReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the interface of HOBBIT platform and maps it to calls similar to SEALS.
 */
public class HobbitWrapper extends AbstractSystemAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HobbitWrapper.class);

    private ExecutorService executor;
    private Map<String, FileReceiverCallableState> receivers = Collections.synchronizedMap(new HashMap<String, FileReceiverCallableState>());

    @Override
    public void init() throws Exception {
        super.init();
        LOGGER.info("HobbitWrapper initialized.");
        executor = Executors.newCachedThreadPool();
    }
    
    @Override
    public void receiveGeneratedData(byte[] data) {
        try {
            LOGGER.info("Starting receiveGeneratedData...");
            ByteBuffer dataBuffer = ByteBuffer.wrap(data);
            String format = RabbitMQUtils.readString(dataBuffer);//TODO For the OAEI we just receive some "dummy" source data

            while (dataBuffer.hasRemaining()) {
                String queueName = RabbitMQUtils.readString(dataBuffer);

                SimpleFileReceiver receiver = SimpleFileReceiver.create(this.incomingDataQueueFactory, queueName);
                FileReceiverCallable callable = new FileReceiverCallable(receiver, "./results/");

                // Start a parallel thread that receives the data for us
                receivers.put(queueName, new FileReceiverCallableState(executor.submit(callable), callable));
            }
            LOGGER.info("Received '" + receivers.size() + "' queue names for the matching tasks");
        } catch (IOException ex) {
            LOGGER.error(ex.toString());
        }
    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
        LOGGER.info("Starting receiveGeneratedTask..");

        Set<String> allowed_instance_types = new HashSet<String>();

        ByteBuffer taskBuffer = ByteBuffer.wrap(data);
        //read the buffer in order (8 elements)
        //1. Format
        String format = RabbitMQUtils.readString(taskBuffer);
        //2. Source file name
        String sourceName = RabbitMQUtils.readString(taskBuffer);
        //3. Target file name
        String targetName = RabbitMQUtils.readString(taskBuffer);
        //4. If class matching is required
        boolean isMatchingClassesRequired = Boolean.valueOf(RabbitMQUtils.readString(taskBuffer));
        //5. If data property matching is required
        boolean isMatchingDataPropertiesRequired = Boolean.valueOf(RabbitMQUtils.readString(taskBuffer));
        //6. If object property matching is required
        boolean isMatchingObjectPropertiesRequired = Boolean.valueOf(RabbitMQUtils.readString(taskBuffer));
        //7. If instance matching is required
        boolean isMatchingInstancesRequired = Boolean.valueOf(RabbitMQUtils.readString(taskBuffer));
        //8. Queue name (task name and id to receive the files)
        //We should have defined above a Thread to receive the files in that queue, otherwise the task will not be processed (see below) 
        String queueName = RabbitMQUtils.readString(taskBuffer);

        // Allowed instance types (i.e., class URIs)
        if (isMatchingInstancesRequired) {
            while (taskBuffer.hasRemaining()) {
                //Update allowed_instance_types
                allowed_instance_types.add(RabbitMQUtils.readString(taskBuffer));
            }
        }
        LOGGER.info("Parsed task " + taskId + ". Queue name: " + queueName + ". Source: " + sourceName + ". Target: " + targetName);

        try {
            if (receivers.containsKey(queueName)) {
                FileReceiverCallableState status = receivers.get(queueName);
                // First, tell the receiver that it should have received all data
                status.callable.terminateReceiver();
                // Second, wait until the receiver has stopped
                try {
                    String files[] = status.result.get(); //to be stored in results_directory
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Exception while trying to receive data in queue " + queueName + ". Aborting.", e);
                }
            } else {
                LOGGER.error("The given queue name does not exist: " + queueName);
            }
            LOGGER.info("Received data for task " + taskId + ". Queue/task name: " + queueName);

            String resultsPath = System.getProperty("user.dir") + File.separator + "results";
            //SourceName and targetName play an important role as the order of files in receivedFiles 
            File file_source = new File(resultsPath + File.separator + sourceName);
            File file_target = new File(resultsPath + File.separator + targetName);

            LOGGER.info("Received source file " + file_source.getAbsolutePath() + " exists? " + file_source.exists());
            LOGGER.info("Received target file " + file_target.getAbsolutePath() + " exists? " + file_target.exists());

            //LogMap requires URIs of input ontologies
            URI sourcePath = file_source.toURI();
            URI targetPath = file_target.toURI();

            LOGGER.info("Task " + taskId + " received from task generator");
            LOGGER.info("Files in queue '" + queueName + "' received from task generator");
            LOGGER.info("Source " + sourcePath.toString());
            LOGGER.info("Target " + targetPath.toString());
            LOGGER.info("Flags: isMatchingClassesRequired " + isMatchingClassesRequired
                    + ",  isMatchingDataPropertiesRequired " + isMatchingDataPropertiesRequired
                    + ",  isMatchingObjectPropertiesRequired " + isMatchingObjectPropertiesRequired
                    + ",  isMatchingInstancesRequired " + isMatchingInstancesRequired
                    + ",  restricted_instance_types " + allowed_instance_types.size());
            
            //update HobbitOptions
            OaeiOptions.setFormat(format);
            OaeiOptions.setSourceName(sourceName);
            OaeiOptions.setTargetName(targetName);
            OaeiOptions.setMatchingClassesRequired(isMatchingClassesRequired);
            OaeiOptions.setMatchingDataPropertiesRequired(isMatchingDataPropertiesRequired);
            OaeiOptions.setMatchingObjectPropertiesRequired(isMatchingObjectPropertiesRequired);
            OaeiOptions.setMatchingInstancesRequired(isMatchingInstancesRequired);
            OaeiOptions.setAllowedInstanceTypes(allowed_instance_types);
            
            File result = runTool(sourcePath, targetPath);
            
            if(result != null){
                byte[][] resultsArray = new byte[1][];
                String str_results = FileUtils.readFileToString(result);
                String first2000 = str_results.substring(0, Math.min(str_results.length(), 2000));
                LOGGER.info("My results (truncated to 2000 characters) are: " + first2000.replace("\n", ""));
                resultsArray[0] = FileUtils.readFileToByteArray(result);
                byte[] results = RabbitMQUtils.writeByteArrays(resultsArray);

                try {
                    sendResultToEvalStorage(taskId, results);
                    LOGGER.info("HobbitWrapper: results sent to evaluation storage. Task " + taskId + ". Queue/Task name: " + queueName);
                } catch (IOException e) {
                    LOGGER.error("Exception while sending storage space cost to evaluation storage. Task " + taskId, e);
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.toString());
        }
    }
    
    
    private File runTool(URI source, URI target){
        //https://github.com/DanFaria/OAEI_SealsClient/blob/master/SealsClientSource/src/main/java/eu/sealsproject/omt/client/Client.java

        String implementingClass = System.getenv("OAEI_MAIN");
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
        //logger.error("This is a test ", new Exception("Search for it"));
        URL result;
        try {
            result = bridge.align(source.toURL(), target.toURL());
        } catch (MalformedURLException | ToolBridgeException ex) {
            LOGGER.error("Could not call align method of IOntologyMatchingToolBridge: " + ex.getMessage(), ex);
            return null;
        }
        if(result == null){
            LOGGER.error("Result of IOntologyMatchingToolBridge is null");
            return null;
        }
        try {       
            return new File(result.toURI());
        } catch (URISyntaxException ex) {
            LOGGER.error("Couldn't convert result URL to URI");
            return null;
        }
    }
}
