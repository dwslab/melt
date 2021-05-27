package de.uni_mannheim.informatik.dws.melt.hobbit_wrapper;

import java.util.concurrent.Future;

public class FileReceiverCallableState {

    public final Future<String[]> result;
    
    public final FileReceiverCallable callable;

    public FileReceiverCallableState(Future<String[]> result, FileReceiverCallable callable) {
        super();
        this.result = result;
        this.callable = callable;
    }
}
