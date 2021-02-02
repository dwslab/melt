package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformer;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.ObjectTransformationRoute;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestPropertiesLoader {
    @Test
    public void getAllSuperClassesAndIterfacesTest() throws Exception{
        URI paramsURI = new File("src/test/resources/paramsyaml.txt").toURI();
        
        ObjectTransformationRoute route = TypeTransformerRegistry.transformObject(paramsURI, Properties.class);
        if(route == null)
            throw new Exception("route is null");
        Properties p = (Properties) route.getTransformedObject();
        assertEquals(true, p.get(ParameterConfigKeys.MATCHING_CLASSES));
        assertEquals(Arrays.asList("http://example.com/one", "http://example.com/two"), 
                p.get(ParameterConfigKeys.MATCHING_INSTANCE_TYPES));
    }
}
