package de.uni_mannheim.informatik.dws.melt.matching_base;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class MatcherPipelineSequentialTest {
    @Test
    public void testMatcherPipelineSequentialWithInputAlignment() throws MalformedURLException, Exception{
        TypeTransformerRegistry.clear();
        TypeTransformerRegistry.addTransformer(new URLtoMyAlignment());
        
        Set<Object> sources = new HashSet<>();
        Set<Object> targets = new HashSet<>();
        
        sources.add(new URL("http://source.com"));
        targets.add(new URL("http://target.com"));
        
        
        AlignmentAndParameters result = GenericMatcherCaller.runMatcherMultipleRepresentations(
                new MatcherPipelineSequential(
                    new IMatcher<URL, MyAlignment, Object>() {
                        @Override
                        public MyAlignment match(URL source, URL target, MyAlignment inputAlignment, Object parameters) throws Exception {
                            inputAlignment.add("one");
                            inputAlignment.add("two");
                            return inputAlignment;
                        }
                    },
                    new IMatcher<URL, MyAlignment, Object>() {
                        @Override
                        public MyAlignment match(URL source, URL target, MyAlignment inputAlignment, Object parameters) throws Exception {
                            inputAlignment.remove("two");
                            return inputAlignment;
                        }
                    }
                ), sources, targets);
        
        MyAlignment alignment = result.getAlignment(MyAlignment.class);
        
        assertEquals(1, alignment.size(), "Alignment size is not one");
        assertTrue(alignment.contains("one"));        
    }
}

class URLtoMyAlignment extends AbstractTypeTransformer<URL, MyAlignment>{
    public URLtoMyAlignment() {
        super(URL.class, MyAlignment.class);
    }

    @Override
    public MyAlignment transform(URL value, Properties parameters) throws TypeTransformationException{
        return new MyAlignment();
    }
}