package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptTypeTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConceptTypeTest.class);
    
    //@Test
    public void testSubsume(){
        assertEquals(ConceptType.CLASS, ConceptType.subsumeProperties(ConceptType.CLASS));
        assertEquals(ConceptType.RDF_PROPERTY, ConceptType.subsumeProperties(ConceptType.RDF_PROPERTY));
        assertEquals(ConceptType.RDF_PROPERTY, ConceptType.subsumeProperties(ConceptType.DATATYPE_PROPERTY));
        assertEquals(ConceptType.RDF_PROPERTY, ConceptType.subsumeProperties(ConceptType.OBJECT_PROPERTY));
        assertEquals(ConceptType.RDF_PROPERTY, ConceptType.subsumeProperties(ConceptType.ANNOTATION_PROPERTY));
        assertEquals(ConceptType.INSTANCE, ConceptType.subsumeProperties(ConceptType.INSTANCE));
        assertEquals(ConceptType.UNKNOWN, ConceptType.subsumeProperties(ConceptType.UNKNOWN));
    }
    
    
    @Test
    public void testWithJenaAsGoldFromConferenceOntology(){
        List<TestElement> tests = new ArrayList<>();
        for(TestCase tc: TrackRepository.Conference.V1.getTestCases()){
            tests.addAll(createTestWithJenaAsGold(tc.getSourceOntology(OntModel.class)));
            tests.addAll(createTestWithJenaAsGold(tc.getTargetOntology(OntModel.class)));
        }
        
        Collections.shuffle(tests);
        
        //test analyze
        long time = System.nanoTime();
        for(TestElement e : tests){
            e.testAnalyze();
        }
        long diff = System.nanoTime() - time;
        LOGGER.info("ConceptType::analyze needs {} milliseconds", diff / 1_000_000);
        
        time = System.nanoTime();
        for(TestElement e : tests){
            e.testAnalyzeWithJena();
        }
        diff = System.nanoTime() - time;
        LOGGER.info("ConceptType::analyzeWithJena needs {} milliseconds", diff / 1_000_000);
        
        
        //attach reasoner:
        //for(TestElement e : tests){
        //    e.attachReasoner();
        //}
        
        /*
        OntologyCacheJena.emptyCache();
        Properties p = new Properties();
        p.put(ParameterConfigKeys.JENA_ONTMODEL_SPEC, "OWL_DL_MEM_RDFS_INF");
        tests = new ArrayList<>();
        for(TestCase tc: TrackRepository.Conference.V1.getTestCases()){
            tests.addAll(createTestWithJenaAsGold(tc.getSourceOntology(OntModel.class, p)));
            tests.addAll(createTestWithJenaAsGold(tc.getTargetOntology(OntModel.class, p)));
        }
        Collections.shuffle(tests);
        
        
        
        time = System.nanoTime();
        for(TestElement e : tests){
            e.testAnalyzeWithInference();
        }
        diff = System.nanoTime() - time;
        LOGGER.info("ConceptType::analyzeWithInference needs {} milliseconds", diff / 1_000_000);
        */
        
    }
    
    

    private List<TestElement> createTestWithJenaAsGold(OntModel m){
    
        //if(withReasoner){
        //    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, m);
        //}
        
        List<TestElement> elements = new ArrayList<>();
        //StmtIterator stmtIterator = m.listStatements();
        //while(stmtIterator.hasNext()){
        //    Statement stmt = stmtIterator.next();
        //    if(stmt.getPredicate().isURIResource()){
        //        elements.add(new TestElement(ConceptType.RDF_PROPERTY, stmt.getPredicate().getURI(), m, true));
        //    }
        //}
        
        
        for(OntProperty p : m.listAllOntProperties().toList()){
            if(p.isURIResource()){
                elements.add(new TestElement(ConceptType.RDF_PROPERTY, p.getURI(), m, true));
            }
        }
        for(OntProperty p : m.listDatatypeProperties().toList()){
            if(p.isURIResource()){
                elements.add(new TestElement(ConceptType.DATATYPE_PROPERTY, p.getURI(), m, false));
            }
        }
        for(OntProperty p : m.listObjectProperties().toList()){
            if(p.isURIResource()){
                elements.add(new TestElement(ConceptType.OBJECT_PROPERTY, p.getURI(), m, false));
            }
        }
        for(OntProperty p : m.listAnnotationProperties().toList()){
            if(p.isURIResource()){
                elements.add(new TestElement(ConceptType.ANNOTATION_PROPERTY, p.getURI(), m, false));
            }
        }
        for(OntClass o : m.listClasses().toList()){
            if(o.isURIResource()){
                elements.add(new TestElement(ConceptType.CLASS, o.getURI(), m, false));
            }
        }
        for(Individual i : m.listIndividuals().toList()){
            if(i.isURIResource()){
                elements.add(new TestElement(ConceptType.INSTANCE, i.getURI(), m, false));
            }
        }
        return elements;
    }
    

    private OntModel createOwnModel(OntModelSpec spec){
        OntModel m = ModelFactory.createOntologyModel(spec);
        
        //RDF
        m.add(m.getResource("http://test.de/rdf_property"), RDF.type, RDF.Property);
        
        //RDFS
        m.add(m.getResource("http://test.de/rdfs_class"), RDF.type, RDFS.Class);
        
        //OWL
        m.add(m.getResource("http://test.de/owl_datatype_property"), RDF.type, OWL.DatatypeProperty);
        m.add(m.getResource("http://test.de/owl_object_property"), RDF.type, OWL.ObjectProperty);
        m.add(m.getResource("http://test.de/owl_annotation_property"), RDF.type, OWL.AnnotationProperty);
        m.add(m.getResource("http://test.de/owl_ontology_property"), RDF.type, OWL.OntologyProperty);
        m.add(m.getResource("http://test.de/owl_class"), RDF.type, OWL.Class);
        
        m.createIntersectionClass("http://test.de/owl_class_intersection", m.createList(Arrays.asList(
                m.getResource("http://test.de/owl_class_intersection_one"),
                m.getResource("http://test.de/owl_class_intersection_two")).iterator()));
        
        m.add(m.getResource("http://test.de/instance_rdfs_class"), RDF.type, m.getResource("http://test.de/rdfs_class"));
        m.add(m.getResource("http://test.de/instance_owl_class"), RDF.type, m.getResource("http://test.de/owl_class"));
        m.add(m.getResource("http://test.de/instance_owl_thing"), RDF.type, OWL.Thing);
        
        m.add(m.getResource("http://test.de/just_a_class_no_definition_instance"), RDF.type, m.getResource("http://test.de/just_a_class_no_definition"));
        
        //just a property:
        m.add(m.getResource("http://test.de/undefined_prop_subject"), m.getProperty("http://test.de/undefined_prop"), m.getResource("http://test.de/undefined_prop_object"));
        
        m.add(m.getResource("http://test.de/subclass"), RDFS.subClassOf, m.getResource("http://test.de/superclass"));
        return m;
    }
    
    private void testOwnModel(OntModel m, BiFunction<OntModel, String, ConceptType> function){        
        
        assertSame(ConceptType.RDF_PROPERTY, function.apply(m, "http://test.de/rdf_property"));
        assertSame(ConceptType.CLASS, function.apply(m, "http://test.de/rdfs_class"));
        assertSame(ConceptType.DATATYPE_PROPERTY, function.apply(m, "http://test.de/owl_datatype_property"));
        assertSame(ConceptType.OBJECT_PROPERTY, function.apply(m, "http://test.de/owl_object_property"));
        assertSame(ConceptType.ANNOTATION_PROPERTY, function.apply(m, "http://test.de/owl_annotation_property"));
        //assertSame(ConceptType.INSTANCE, function.apply(m, "http://test.de/owl_ontology_property")); //TODO: actually this is not correct but jena and the lie can not differentiate
        assertSame(ConceptType.CLASS, function.apply(m, "http://test.de/owl_class"));
        assertSame(ConceptType.CLASS, function.apply(m, "http://test.de/owl_class_intersection"));
        assertSame(ConceptType.INSTANCE, function.apply(m, "http://test.de/instance_rdfs_class"));
        assertSame(ConceptType.INSTANCE, function.apply(m, "http://test.de/instance_owl_class"));
        assertSame(ConceptType.INSTANCE, function.apply(m, "http://test.de/instance_owl_thing"));               
        
        assertSame(ConceptType.CLASS, function.apply(m, "http://test.de/just_a_class_no_definition"));        
        
        
        //assertSame(ConceptType.RDF_PROPERTY, function.apply(m, "http://test.de/undefined_prop"));
        
        assertSame(ConceptType.CLASS, function.apply(m, "http://test.de/subclass"));
        assertSame(ConceptType.CLASS, function.apply(m, "http://test.de/superclass"));
        
        assertSame(ConceptType.UNKNOWN, function.apply(m, "http://not.defined/test"));
    }
    
    
    
    //@Test
    public void testResourceTypeWithResoning(){
        //OntModel m = createModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        //makeTest(m);
    }
    
    //@Test
    public void testResourceTypeNoResoning(){
        //OntModel m = createModel(OntModelSpec.OWL_MEM_RDFS_INF);
        //makeTest(m);
        
        //logger.info(m.getOntClass("http://test.de/instance_rdfs_class").toString());
        //OntClassImpl
        //logger.info(m.listClasses().toSet().toString());
        //logger.info(m.list);
        
        
        //m.write(System.out, "TURTLE");
        
        
        /*
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(m);
        InfModel infmodel = ModelFactory.createInfModel(reasoner, m);
        
        OntModel sdaigh = ModelFactory.createOntologyModel();
        sdaigh.add(infmodel.getRawModel());
        sdaigh.add(infmodel.getDeductionsModel());
        makeTest(sdaigh);
        //OntModel tdezu = (OntModel) bla;
        //System.out.println("====================");
        //System.out.println("======Inf model=====");
        //System.out.println("====================");
        //sdaigh.write(System.out, "TURTLE");
*/
    }
    
    
    
    
    
    //@Test
    public void testResourceTypePerformance(){
        
        
        
        /*
        logger.info("Load model");
        OntModel src = getAnatomySource(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        
        logger.info("Collect URI Resource");
        Set<Resource> uriResources = getAllURIResources(src);
        
        logger.info("Run analyze for " + uriResources.size() + " Resouces.");
        for(Resource r : uriResources){
            ResourceType.analyzeOwnInf(src, r.getURI());
            //ResourceType.analyzeFast(src, r.getURI());
            //assertNotSame(ResourceType.UNKNOWN, ResourceType.analyze(src, r.getURI()));
        }
*/
    }
    
    
    private OntModel getAnatomySource(OntModelSpec spec){
        TestCase tc = TrackRepository.Anatomy.Default.getTestCases().get(0);
        OntModel m = ModelFactory.createOntologyModel(spec);
        m.read(tc.getSource().toString());
        return m;
    }
    
    private Set<Resource> getAllURIResources(OntModel m){
        Set<Resource> r = new HashSet<>();
        StmtIterator ei = m.listStatements();
        while(ei.hasNext()){
            Statement s = ei.nextStatement();
            if(s.getSubject().isURIResource()){
                r.add(s.getSubject());
            }            
            if(s.getObject().isURIResource()){
                r.add(s.getObject().asResource());
            }
            //if(r.size() >= 2000)
            //    break;
        }
        return r;
    }
}


class TestElement{    
    private ConceptType gold;
    private String uri;
    private OntModel model;
    private boolean subsume;

    public TestElement(ConceptType gold, String uri, OntModel model, boolean subsume) {
        this.gold = gold;
        this.uri = uri;
        this.model = model;
        this.subsume = subsume;
    }
    
    public void testAnalyze(){
        ConceptType actual = ConceptType.analyze(model, uri);
        if(subsume)
            actual = ConceptType.subsumeProperties(actual);
        assertEquals(gold, actual, "We expect " + gold + " for element " + uri + " but was " + actual);
    }
    
    public void testAnalyzeWithInference(){
        ConceptType actual = ConceptType.analyzeWithInference(model, uri);
        if(subsume)
            actual = ConceptType.subsumeProperties(actual);
        assertEquals(gold, actual, "We expect " + gold + " for element " + uri + " but was " + actual);
    }
    
    public void testAnalyzeWithJena(){
        ConceptType actual = ConceptType.analyzeWithJena(model, uri);
        if(subsume)
            actual = ConceptType.subsumeProperties(actual);
        assertEquals(gold, actual, "We expect " + gold + " for element " + uri + " but was " + actual);
    }
    
    public void testAll(){
        testAnalyze();
        testAnalyzeWithInference();
        testAnalyzeWithJena();
    }
    
    /*
    public void attachReasoner(){
        if(this.model.getReasoner() == null){
            this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF, this.model);
        }
    }
*/
}