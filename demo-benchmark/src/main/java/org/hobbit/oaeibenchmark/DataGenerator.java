package org.hobbit.oaeibenchmark;

import java.io.IOException;
import java.util.Set;
import org.hobbit.commonelements.OaeiTask;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.commonelements.PlatformConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGenerator extends AbstractDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGenerator.class);

    private String task_uri;

    @Override
    public void init() throws Exception {
        LOGGER.info("Initializing Data Generator '" + getGeneratorId() + "'");
        super.init();        
        this.task_uri = System.getenv().get(PlatformConstants.TASK_ENV_NAME);
    }

    
    @Override
    protected void generateData() throws Exception {
        LOGGER.info("Generate data.. ");
        try {
            sendDataToSystem();
            sendTaskUriToTaskGenerator();
        } catch (IOException e) {
            LOGGER.error("Exception while sending file to System Adapter or Task Generator(s).", e);
        }
        LOGGER.info("Finished generate data");
    }
    
    
    private void sendDataToSystem() throws IOException {
        
        Set<OaeiTask> tasks = PlatformConstants.TASK_MAPPING.get(this.task_uri);
        if(tasks == null){
            LOGGER.error("Task is null. Found no TASK_MAPPING for {}", this.task_uri);
            return;
        }
        
        byte[][] generatedFileArray = new byte[tasks.size() + 1][];
        generatedFileArray[0] = RabbitMQUtils.writeString(PlatformConstants.FORMAT);// send format of the files (inherited from hobbit tasks)
        int i = 1;
        for(OaeiTask t : tasks){
            generatedFileArray[i] = RabbitMQUtils.writeString(t.getTaskQueueName());
            i++;
        }
        
        sendDataToSystemAdapter(RabbitMQUtils.writeByteArrays(generatedFileArray));
        LOGGER.info("Sent task queue name(s) to System Adapter.");
    }
    
    private void sendTaskUriToTaskGenerator() throws IOException {
        byte[][] generatedFileArray = new byte[1][];
        generatedFileArray[0] = RabbitMQUtils.writeString(this.task_uri);
        sendDataToTaskGenerator(RabbitMQUtils.writeByteArrays(generatedFileArray));
        LOGGER.info("Send task uri ({}) to task generator", this.task_uri);
    }
}
