package de.uni_mannheim.informatik.dws.melt.matching_base;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {


    @Test
    void dataStoreTest(){
        DataStore ds = DataStore.getGlobal();
        ds.put("key", 12345);
        assertEquals(12345, ds.get("key", Integer.class));
        assertEquals(12345, (Integer) ds.get("key"));
        Integer i1 = ds.get("key");
        assertEquals(12345, i1);
        assertTrue(ds.containsKey("key"));
        assertFalse(ds.containsKey("abc"));

        ds = DataStore.getGlobal();
        assertEquals(12345, ds.get("key", Integer.class));

        ds.clear();
        assertFalse(ds.containsKey("key"));
    }
}