package de.uni_mannheim.informatik.dws.melt.hobbit_wrapper.spimbench;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Map;

import org.hobbit.core.data.FileReceiveState;
import org.hobbit.core.data.RabbitQueue;
import org.hobbit.core.rabbit.RabbitQueueFactory;
import org.hobbit.core.rabbit.SimpleFileReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

public class SingleFileReceiver extends SimpleFileReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleFileReceiver.class);

    public static SingleFileReceiver create(RabbitQueueFactory factory, String queueName) throws IOException {
        return create(factory.createDefaultRabbitQueue(queueName));
    }

    public static SingleFileReceiver create(RabbitQueue queue) throws IOException {
        QueueingConsumer consumer = new QueueingConsumer(queue.channel);
        queue.channel.basicConsume(queue.name, true, consumer);
        queue.channel.basicQos(20);
        return new SingleFileReceiver(queue, consumer);
    }

    private int expectedNumberOfFiles = 1;

    protected SingleFileReceiver(RabbitQueue queue, QueueingConsumer consumer) {
        super(queue, consumer);
        setWaitingForMsgTimeout(10L);
    }

    @Override
    public String[] receiveData(String outputDirectory)
            throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
        if (!outputDirectory.endsWith(File.separator)) {
            outputDirectory = outputDirectory + File.separator;
        }
        try {
            Delivery delivery = null;
            // while the receiver should not terminate, the last delivery was
            // not empty or there are still deliveries in the (servers) queue
            while ((this.fileStates.size() < expectedNumberOfFiles) || (!allFilesClosed(this.fileStates))) {
                delivery = consumer.nextDelivery(DEFAULT_TIMEOUT);
                if (delivery != null) {
                    executor.execute(new MessageProcessing(this, outputDirectory, delivery.getBody()));
                }
            }
        } finally {
            close();
        }
        return fileStates.keySet().toArray(new String[fileStates.size()]);
    }

    private boolean allFilesClosed(Map<String, FileReceiveState> fileStates) {
        try {
            for (String file : fileStates.keySet()) {
                FileReceiveState state = fileStates.get(file);
                if (state.outputStream != null) {
                    return false;
                }
            }
        } catch (ConcurrentModificationException e) {
            // nothing to do
            return false;
        } catch (Exception e) {
            LOGGER.error("Exception while iterating file states.", e);
            return false;
        }
        return true;
    }
}
