package de.uni_mannheim.informatik.dws.melt.matching_ml.python.openea;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OpenEAConfigurationTest {
    
    @Test
    void loadJson() throws IOException {
        File file = new File(getClass().getClassLoader().getResource("rdgcn_args_100K.json").getFile());
        OpenEAConfiguration config = new OpenEAConfiguration(file);
        
        assertEquals(0.001, config.getArgumentMap().get("learning_rate"));        
        assertEquals(true, config.getArgumentMap().get("ordered"));
        assertEquals(Arrays.asList(1,5,10,50), config.getArgumentMap().get("top_k"));//[1,5,10,50
        assertEquals("RDGCN", config.getArgumentMap().get("embedding_module"));
        
        assertFalse(config.getArgumentMap().containsKey("training_data"));
                
        for(Entry<String, Object> entry : config.getArgumentMap().entrySet()){
            assertFalse(entry.getKey().isEmpty());
            assertNotNull(entry.getValue());
        }
        
        List<String> t = config.getArgumentLine();
        config.getArgumentLine().contains("--top_k [1, 5, 10, 50]");
        
        File f = new File("./paramTest.json");
        try{
            config.writeArgumentsToFile(f);
        
            String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);

            assertTrue(s.contains("\"top_k\" : [ 1, 5, 10, 50 ]")); //array
            assertTrue(s.contains("\"embedding_module\" : \"RDGCN\"")); //string
            assertTrue(s.contains("\"is_save\" : true")); //boolean
            assertTrue(s.contains("\"alpha\" : 0.1")); //float
            assertTrue(s.contains("\"test_threads_num\" : 12")); //integer
        }finally{
            f.delete();
        }
    }
}
