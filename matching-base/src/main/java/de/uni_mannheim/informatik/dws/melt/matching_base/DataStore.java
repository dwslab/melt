package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.util.HashMap;

/**
 * Store accessible to all matchers where variables and results can be persisted in.
 * Available as local store and as global store.
 */
public class DataStore {

    /**
     * Constructor
     */
    public DataStore(){
    }

    /**
     * Get global data store instance.
     * @return Instance of the global data store.
     */
    public static DataStore getGlobal(){
        if(instance == null){
            instance = new DataStore();
            return instance;
        } else {
            return instance;
        }
    }

    /**
     * Singleton instance
     */
    private static DataStore instance;

    /**
     * Central Store Object
     */
    private HashMap<String, Object> centralStore = new HashMap<>();

    /**
     * Put an object to the data store.
     * @param key Key
     * @param value Value
     */
    public void put(String key, Object value){
        centralStore.put(key, value);
    }

    /**
     * Get an object from the data store using a key.
     * @param key Key used to retrieve object.
     * @param clazz The class.
     * @return Value stored for key.
     */
    public Object get(String key, Class clazz){
        return clazz.cast(centralStore.get(key));
    }

    public <T> T get(String key){
        return (T) centralStore.get(key);
    }

    /**
     * Check if key exists in store.
     * @param key Key that shall be looked up.
     * @return true if key contained, else false.
     */
    public boolean containsKey(String key){
        return centralStore.containsKey(key);
    }

    /**
     * Delete store.
     */
    public void clear(){
        centralStore = new HashMap<>();
    }

}
