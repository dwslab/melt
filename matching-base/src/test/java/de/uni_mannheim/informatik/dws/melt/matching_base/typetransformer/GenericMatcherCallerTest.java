package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;


import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class GenericMatcherCallerTest {
    @Test
    public void testGenericMatcherCaller() throws MalformedURLException, Exception{
        TypeTransformerRegistry.clear();
        TypeTransformerRegistry.addTransformer(new TypeTransformerForTest<>(URL.class, MyModel.class));
        TypeTransformerRegistry.addTransformer(new TypeTransformerForTest<>(URL.class, MyAlignment.class));
        
        List<Object> possibleMatchers = Arrays.asList(
                new myTestMatcher(), 
                myTestMatcher.class, 
                "de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.myTestMatcher");
        
        for(Object matcher : possibleMatchers){
            AlignmentAndParameters o = GenericMatcherCaller.runMatcherMultipleRepresentations(matcher, 
                new HashSet<>(Arrays.asList(URI.create("http://source.com").toURL())), 
                new HashSet<>(Arrays.asList(URI.create("http://target.com").toURL())),
                URI.create("http://myAlignment.com").toURL(), 
                new Properties());
            assertNotNull(o.getAlignment());
            assertTrue(o.getAlignment() instanceof MyAlignment);
        }
    }
}


class MyModel{ }
class MyAlignment {}

class myTestMatcher implements IMatcher<MyModel, MyAlignment, Properties>{
    @Override
    public MyAlignment match(MyModel source, MyModel target, MyAlignment inputAlignment, Properties parameter) throws Exception {
        if(source != null && target != null && inputAlignment != null){
            return new MyAlignment();
        }
        throw new Exception("Does not work");
    }    
}