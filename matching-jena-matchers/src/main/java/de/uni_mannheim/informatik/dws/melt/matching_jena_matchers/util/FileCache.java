package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File cache which can be used to store a java object in a file and load it from that file if the program runs a second time.
 * @param <T> the type of object to store.
 */
public class FileCache <T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCache.class);
    
    protected File file;
    protected T instance;
    
    
    public FileCache(String file, Supplier<T> instanceSuplier){
        this(new File(file), instanceSuplier);
    }
    
    public FileCache(File file, Supplier<T> instanceSuplier){
        this.file = file;
        load(instanceSuplier);
    }
    
    public FileCache(String file){
        this(new File(file));
    }
    public FileCache(File file){
        this.file = file;
    }
    
    
    public T loadFromFileOrUseSuplier(Supplier<T> instanceSuplier){
        load(instanceSuplier);
        return this.instance;
    }
    
    
    @SuppressWarnings("unchecked")
    private void load(Supplier<T> instanceSuplier){
        if(this.file.exists()){
            LOGGER.info("Load from cache file {}", this.file);
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
                this.instance = (T) ois.readObject();
            } catch (Exception ex) {
                LOGGER.error("Could not load the instance from file {}. Call the suplier to get a new one.", this.file, ex);
                this.instance = instanceSuplier.get();
            }
        }else{
            LOGGER.info("Call instance supplier");
            this.instance = instanceSuplier.get();
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
            file.delete();
            LOGGER.error("Could not save the instance of FileCache to file {}.", file, ex);
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

    public File getFile() {
        return file;
    }
}
