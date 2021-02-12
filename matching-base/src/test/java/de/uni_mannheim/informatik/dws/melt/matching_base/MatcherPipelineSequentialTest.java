package de.uni_mannheim.informatik.dws.melt.matching_base;


import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
public class MatcherPipelineSequentialTest {
    @Test
    public void testRunMatcherMultipleRepresentations() throws MalformedURLException, Exception{
        
        TypeTransformerRegistry.clear();
        TypeTransformerRegistry.addTransformer(new URLtoURI());
        
        
        Set<Object> sources = new HashSet<>();
        Set<Object> targets = new HashSet<>();
        
        sources.add(new URL("http://source.com"));
        targets.add(new URL("http://target.com"));
        
        Object x = GenericMatcherCaller.runMatcherMultipleRepresentations(
                new MatcherPipelineSequential(new myTestMatcher()), sources, targets);
        
        assertEquals(2, sources.size());
        assertEquals(2, targets.size());
        
        x = GenericMatcherCaller.runMatcherMultipleRepresentations(
                new MatcherPipelineSequential(new myTestMatcher()), sources, targets);
        
        assertEquals(2, sources.size());
        assertEquals(2, targets.size());
    }
    
    @Test
    public void getAllSuperClassesAndIterfacesTest() throws MalformedURLException, Exception{
        
        TypeTransformerRegistry.clear();
        TypeTransformerRegistry.addTransformer(new URLtoURI());
        
        
        Set<Object> sources = new HashSet<>();
        Set<Object> targets = new HashSet<>();
        
        sources.add(new URL("http://source.com"));
        targets.add(new URL("http://target.com"));
        
        Object x = GenericMatcherCaller.runMatcherMultipleRepresentations(
                new MatcherPipelineSequential(new myTestMatcher()), sources, targets);
        
        assertEquals(2, sources.size());
        assertEquals(2, targets.size());
        
        x = GenericMatcherCaller.runMatcherMultipleRepresentations(
                new MatcherPipelineSequential(new myTestMatcher()), sources, targets);
        
        assertEquals(2, sources.size());
        assertEquals(2, targets.size());
    }
}


class addParameterOne implements IMatcher<Object, Object, Properties>{
    @Override
    public Object match(Object source, Object target, Object inputAlignment, Properties parameter) throws Exception {
        parameter.put("one", 1);
        return inputAlignment;
    }
}

class addParameterTwo implements IMatcher<Object, Object, Map<String,Object>>{
    @Override
    public Object match(Object source, Object target, Object inputAlignment, Map<String,Object> parameter) throws Exception {
        parameter.put("two", 2);
        return inputAlignment;
    }
}


class myTestMatcher implements IMatcher<URI, Object, Object>{
    @Override
    public Object match(URI source, URI target, Object inputAlignment, Object parameter) throws Exception {
        return inputAlignment;
    }
}

class URLtoURI extends AbstractTypeTransformer<URL, URI>{
    public URLtoURI() {
        super(URL.class, URI.class);
    }

    @Override
    public URI transform(URL value, Properties parameters) throws Exception {
        return value.toURI();
    }
}

class ParameterTransform extends AbstractTypeTransformer<Properties, Map<String, Object>>{
    public ParameterTransform() {
        super(Properties.class, Map<String, Object>.class);
    }

    @Override
    public Map<String, Object> transform(Properties value, Properties parameters) throws Exception {
        return value.toURI();
    }
}
*/