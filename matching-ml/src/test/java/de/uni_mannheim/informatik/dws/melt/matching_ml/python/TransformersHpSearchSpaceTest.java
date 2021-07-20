
package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersHpSearchSpace;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class TransformersHpSearchSpaceTest {
    

    @Test
    public void testSerialization() {        
        TransformersHpSearchSpace space = new TransformersHpSearchSpace()
                .choice("num_train_epochs", Arrays.asList(1,2,3,4,5));
        
        String json = space.toJsonString();    
        assertEquals("{\"num_train_epochs\":{\"name\":\"choice\",\"params\":{\"categories\":[1,2,3,4,5]}}}", json);
    }
    
}
