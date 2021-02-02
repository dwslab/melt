package org.hobbit.oaeibenchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import org.apache.commons.io.FileUtils;

import org.hobbit.commonelements.OaeiTask;
import org.hobbit.commonelements.PlatformConstants;
import org.hobbit.core.components.AbstractTaskGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.rabbit.SimpleFileSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskGenerator extends AbstractTaskGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskGenerator.class);

    public TaskGenerator() {
        super(1);
    }

    @Override
    public void init() throws Exception {
        LOGGER.info("Initializing Task Generator...");
        super.init();
    }

    @Override
    protected void generateTask(byte[] data) throws Exception {   
        LOGGER.info("Generate task...");
        ByteBuffer taskBuffer = ByteBuffer.wrap(data);
        String taskUri = RabbitMQUtils.readString(taskBuffer);

        Set<OaeiTask> tasks = PlatformConstants.TASK_MAPPING.get(taskUri);
        if(tasks == null){
            LOGGER.error("Task is null. Found no TASK_MAPPING for {}", taskUri);
            return;
        }
        
        for(OaeiTask task : tasks){
            LOGGER.info("Send one task");
            sendTaskToSystem(task);
            sendTaskToEval(task);
        }
        LOGGER.info("Finished generating task.");
    }
    
    
    private void sendTaskToSystem(OaeiTask t) throws IOException{
        //Send source and target to system
        try(SimpleFileSender sender = SimpleFileSender.create(this.outgoingDataQueuefactory, t.getTaskQueueName())){
            sendFile(sender, new File(PlatformConstants.DATASET_PATH + t.getSourceFileName()));
            sendFile(sender, new File(PlatformConstants.DATASET_PATH + t.getTargetFileName()));
        }
        
        //This order will be important for the System adapter
        byte[][] taskDataArray = new byte[8 + t.getAllowedInstanceTypes().size()][];
        taskDataArray[0] = RabbitMQUtils.writeString(PlatformConstants.FORMAT);
        //Important to send the name
        taskDataArray[1] = RabbitMQUtils.writeString(t.getSourceFileName());
        taskDataArray[2] = RabbitMQUtils.writeString(t.getTargetFileName());
        //For OAEI is a desired characteristic to tell what to match
        taskDataArray[3] = RabbitMQUtils.writeString(String.valueOf(t.isMatchClass()));
        taskDataArray[4] = RabbitMQUtils.writeString(String.valueOf(t.isMatchDataProp()));
        taskDataArray[5] = RabbitMQUtils.writeString(String.valueOf(t.isMatchObjectProp()));
        taskDataArray[6] = RabbitMQUtils.writeString(String.valueOf(t.isMatchInstances()));

        //Task name and queueID for system
        taskDataArray[7] = RabbitMQUtils.writeString(t.getTaskQueueName());
        
        int i = 8;
        for(String instancesType : t.getAllowedInstanceTypes()){
            taskDataArray[i] = RabbitMQUtils.writeString(instancesType);
            i++;
        }

        byte[] taskData = RabbitMQUtils.writeByteArrays(taskDataArray);
        sendTaskToSystemAdapter(t.getTaskId(), taskData);
    }
    
    private void sendFile(SimpleFileSender sender, File file){
        try(InputStream is = new FileInputStream(file)){
            sender.streamData(is, file.getName());
        } catch (IOException ex) {
            LOGGER.error("Error during sending file to System", ex);
        }
    }
    
    
    private void sendTaskToEval(OaeiTask t) throws IOException{
        File reference_file = new File(PlatformConstants.DATASET_PATH + t.getReferenceFileName());
        
        byte[][] generatedFileArray = new byte[3][];
        // send the file name and its content
        generatedFileArray[0] = RabbitMQUtils.writeString(PlatformConstants.FORMAT);
        generatedFileArray[1] = RabbitMQUtils.writeString(reference_file.getAbsolutePath());
        generatedFileArray[2] = FileUtils.readFileToByteArray(reference_file);
        // convert them to byte[]
        byte[] expectedAnswerData = RabbitMQUtils.writeByteArrays(generatedFileArray);
                
        long timestamp = System.currentTimeMillis();
        sendTaskToEvalStorage(t.getTaskId(), timestamp, expectedAnswerData);
    }
    
    
    @Override
    public void close() throws IOException {
        LOGGER.info("Closign Task Generator...");
        super.close();
        LOGGER.info("Task Genererator closed successfully.");
    }
}
