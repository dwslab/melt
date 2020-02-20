package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File cache which can be used to store a java object in a file and load it from that file if the program runs a second time.
 */
public class FileCache <T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCache.class);
    
    protected File file;
    protected Supplier<T> instanceSuplier;
    protected T instance;
    
    
    public FileCache(String file, Supplier<T> instanceSuplier){
        this(new File(file), instanceSuplier);
    }
    
    public FileCache(File file, Supplier<T> instanceSuplier){
        this.file = file;
        this.instanceSuplier = instanceSuplier;
        load();
    }
    
    protected void load(){
        if(this.file.exists()){
            LOGGER.info("Load from cache file");
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
                this.instance = (T) ois.readObject();
            } catch (Exception ex) {
                LOGGER.error("Could not load the instance from file. Call the suplier to get a new one.", ex);
                this.instance = this.instanceSuplier.get();
            }
        }else{
            LOGGER.info("Call instance supplier");
            this.instance = this.instanceSuplier.get();
        }
    }
    
    /**
     * Returns the instance which can be cached in file.
     * @return the instance
     */
    public T get(){
        return instance;
    }
    
    /**
     * Save instance to file when JVM terminates.
     * Call this function only once.
     */
    public void saveAtShutdown(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                save();
            }
        });
    }
    
    /**
     * Save instance to file when JVM terminates but only if cache file does not exists.
     * Call this function only once.
     */
    public void saveAtShutdownIfCacheNotExistent(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                saveIfCacheNotExistent();
            }
        });
    }
    
    /**
     * Save instance to file now.
     */
    public void save(){
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))){
            out.writeObject(instance);
        } catch (IOException ex) {
            LOGGER.error("Could not save the instance of FileCache to file.", ex);
        }
    }
    
    /**
     * Save instance to file now but only if cache file is not existent.
     */
    public void saveIfCacheNotExistent(){
        if(this.file.exists() == false){
            save();
        }
    }
    
}
