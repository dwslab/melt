package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformer;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.ObjectTransformationRoute;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPropertiesLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestPropertiesLoader.class);
    
    @Test
    public void testPropertiesLoader() throws Exception{
        URI paramsURI = Paths.get("src", "test", "resources", "paramsyaml.txt").toUri();
        if(paramsURI == null)
            throw new Exception("test resource is missing");
        LOGGER.info("Type transformer: {}", TypeTransformerRegistry.getAllRegisteredTypeTransformersAsString());        
        ObjectTransformationRoute route = TypeTransformerRegistry.transformObject(paramsURI, Properties.class);
        if(route == null)
            throw new Exception("route is null");
        Properties p = (Properties) route.getTransformedObject();
        assertEquals(true, p.get(ParameterConfigKeys.MATCHING_CLASSES));
        assertEquals(Arrays.asList("http://example.com/one", "http://example.com/two"), 
                p.get(ParameterConfigKeys.MATCHING_INSTANCE_TYPES));
    }
}
