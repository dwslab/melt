package de.uni_mannheim.informatik.dws.melt.hobbit_wrapper;

import java.util.concurrent.Callable;
import org.hobbit.core.rabbit.SimpleFileReceiver;

public class FileReceiverCallable implements Callable<String[]> {

    private SimpleFileReceiver receiver;
    private String outputDirectory;

    public FileReceiverCallable(SimpleFileReceiver receiver, String outputDirectory) {
        this.receiver = receiver;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public String[] call() throws Exception {
        return receiver.receiveData(outputDirectory);
    }

    public void terminateReceiver() {
        receiver.terminate();
    }
}

