package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultHashMap <K, V> extends HashMap<K, V>{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHashMap.class);
    
    private final Class<V> cls;

    public DefaultHashMap(Class factory) {
        this.cls = factory;
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value == null) {
            try {
                value = cls.newInstance();
            } catch (Exception e) {
                LOGGER.warn("Could not generate new default instance in DefaultHashMap.", e);
            }
            this.put((K) key, value);
        }
        return value;
    }
}
