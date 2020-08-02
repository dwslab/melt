package de.uni_mannheim.informatik.dws.melt.hobbit_wrapper.spimbench;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;
import de.uni_mannheim.informatik.dws.melt.matching_base.OaeiOptions;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import org.apache.commons.io.FileUtils;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.rabbit.SimpleFileReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


/**
 * Wrapper for the interface of HOBBIT SPIMBENCH described here:
 * https://project-hobbit.eu/challenges/om2020/om2020-tasks/
 * and
 * https://github.com/hobbit-project/LinkingBenchmark/blob/master/src/main/java/org/hobbit/spatiotemporalbenchmark/platformConnection/systems/LinkingSystemAdapter.java
 * without many changes.
 */
public class HobbitSPIMBENCHWrapper extends AbstractSystemAdapter{

    private static final Logger LOGGER = LoggerFactory.getLogger(HobbitSPIMBENCHWrapper.class);
    private String receivedGeneratedDataFilePath;
    private SimpleFileReceiver sourceReceiver;
    private SimpleFileReceiver targetReceiver;

    @Override
    public void init() throws Exception {
        LOGGER.info("Initializing HobbitSPIMBENCHWrapper...");
        super.init();
        sourceReceiver = SimpleFileReceiver.create(this.incomingDataQueueFactory, "source_file");
        LOGGER.info("HobbitSPIMBENCHWrapper initialized successfully.");
    }

    @Override
    public void receiveGeneratedData(byte[] data) {
        try {
            LOGGER.info("Starting receiveGeneratedData...");

            ByteBuffer dataBuffer = ByteBuffer.wrap(data);
            // read the file path
            String dataFormat = RabbitMQUtils.readString(dataBuffer);
            OaeiOptions.setFormat(dataFormat);
            receivedGeneratedDataFilePath = RabbitMQUtils.readString(dataBuffer);

            String[] receivedFiles = sourceReceiver.receiveData("./datasets/SourceDatasets/");
            receivedGeneratedDataFilePath = "./datasets/SourceDatasets/"+receivedFiles[0];
            LOGGER.info("Received data from receiveGeneratedData..");
        } catch (IOException | ShutdownSignalException | ConsumerCancelledException | InterruptedException ex) {
            LOGGER.info("error in receiveGeneratedData", ex);
        }

    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
        LOGGER.info("Starting receiveGeneratedTask..");
        long time = System.currentTimeMillis();
        try {

            ByteBuffer taskBuffer = ByteBuffer.wrap(data);
            // read the file path
            String taskFormat = RabbitMQUtils.readString(taskBuffer);
            LOGGER.info("Parsed task " + taskId + ". It took {}ms.", System.currentTimeMillis() - time);
            time = System.currentTimeMillis();
            String receivedGeneratedTaskFilePath = null;
            try {
                targetReceiver = SingleFileReceiver.create(this.incomingDataQueueFactory,"task_target_file");
                String[] receivedFiles = targetReceiver.receiveData("./datasets/TargetDatasets/");
                receivedGeneratedTaskFilePath = "./datasets/TargetDatasets/"+receivedFiles[0];
             } catch (Exception e) {
                LOGGER.error("Exception while trying to receive data. Aborting.", e);
            }
            LOGGER.info("Received task data. It took {}ms.", System.currentTimeMillis() - time);
            time = System.currentTimeMillis();

            LOGGER.info("Task " + taskId + " received from task generator");
            LOGGER.info("receivedGeneratedDataFilePath " + receivedGeneratedDataFilePath);
            LOGGER.info("receivedGeneratedTaskFilePath " + receivedGeneratedTaskFilePath);
            
            File resultsFile = runTool(receivedGeneratedDataFilePath, receivedGeneratedTaskFilePath);
            
            byte[][] resultsArray = new byte[1][];
            resultsArray[0] = FileUtils.readFileToByteArray(resultsFile);
            byte[] results = RabbitMQUtils.writeByteArrays(resultsArray);
            try {
                sendResultToEvalStorage(taskId, results);
                LOGGER.info("Results sent to evaluation storage.");
            } catch (IOException e) {
                LOGGER.error("Exception while sending storage space cost to evaluation storage.", e);
            }
        } catch (IOException ex) {
            LOGGER.error("error in receiveGeneratedTask", ex);
        }
    }
    
     public File runTool(String source, String target) throws IOException{         
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

        URL result;
        try {
            result = bridge.align(new File(source).toURI().toURL(), new File(target).toURI().toURL());
        } catch (MalformedURLException | ToolBridgeException ex) {
            LOGGER.error("Could not call align method of IOntologyMatchingToolBridge: " + ex.getMessage(), ex);
            return null;
        }
        if(result == null){
            LOGGER.error("Result of IOntologyMatchingToolBridge is null");
            return null;
        }
        return createCorrectFormattedFile(result);        
    }
     
    private File createCorrectFormattedFile(URL fileUrl) throws IOException{        
        Alignment alignment;
        try {
            alignment = AlignmentParser.parse(fileUrl);
        } catch (SAXException ex) {
            throw new IOException("Could not parse geenrated alignment from system.", ex);
        }
        
        File resultingFile = File.createTempFile("resultFile", ".csv");
        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultingFile), "UTF-8"))) {
            for(Correspondence cell : alignment){
                out.write("<" + cell.getEntityOne() + "> <" + cell.getEntityTwo() + ">");
                out.newLine();
            }
        }
        return resultingFile;
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        if (Commands.DATA_GENERATION_FINISHED == command) {
            LOGGER.info("receiveCommand is DATA_GENERATION_FINISHED");
            sourceReceiver.terminate();
        }
        super.receiveCommand(command, data);
    }

    @Override
    public void close() throws IOException {
        LOGGER.info("Closing System Adapter...");
        super.close();
        LOGGER.info("System Adapter closed successfully.");
    }
}