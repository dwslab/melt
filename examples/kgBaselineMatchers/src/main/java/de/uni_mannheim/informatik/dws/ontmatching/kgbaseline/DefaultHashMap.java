package de.uni_mannheim.informatik.dws.ontmatching.kgbaseline;

import java.util.HashMap;

public class DefaultHashMap <K, V> extends HashMap<K, V>{
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
                e.printStackTrace();
            }
            this.put((K) key, value);
        }
        return value;
    }
}
