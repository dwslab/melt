package de.uni_mannheim.informatik.dws.melt.matching_eval;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

public class ResourceTypeTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTypeTest.class);

    private void testOntology(OntModel m){
        
        StmtIterator stmtIterator = m.listStatements();
        while(stmtIterator.hasNext()){
            Statement stmt = stmtIterator.next();
            assertEquals(ResourceType.RDF_PROPERTY, ResourceType.subsumeProperties(ResourceType.analyze(m, stmt.getPredicate().getURI().toString())));
        }
        
        for(OntProperty p : m.listAllOntProperties().toList()){
            assertEquals(ResourceType.RDF_PROPERTY, ResourceType.subsumeProperties(ResourceType.analyze(m, p.getURI())));
        }
        for(OntClass o : m.listClasses().toList()){
            if(o.isURIResource()){
                //LOGGER.info(o.getURI());
                assertEquals(ResourceType.CLASS, ResourceType.subsumeProperties(ResourceType.analyze(m, o.getURI())));
            }
        }
        for(Individual i : m.listIndividuals().toList()){
            assertEquals(ResourceType.INSTANCE, ResourceType.subsumeProperties(ResourceType.analyze(m, i.getURI())));
        }
    }
    
    @Test
    public void testConferenceOntology(){
        for(TestCase tc: TrackRepository.Conference.V1.getTestCases()){
            testOntology(tc.getSourceOntology(OntModel.class));
            testOntology(tc.getTargetOntology(OntModel.class)); 
        }
    }
    

    private OntModel createModel(OntModelSpec spec){
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
    
    //@Test
    public void testResourceTypeWithResoning(){
        OntModel m = createModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
        makeTest(m);
    }
    
    //@Test
    public void testResourceTypeNoResoning(){
        OntModel m = createModel(OntModelSpec.OWL_MEM_RDFS_INF);
        makeTest(m);
        
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
    
    private void makeTest(OntModel m){
        /*
        assertSame(ResourceType.RDF_PROPERTY, ResourceType.analyzeWithInferenceFast(m, "http://test.de/rdf_property"));
        assertSame(ResourceType.CLASS, ResourceType.analyzeWithInferenceFast(m, "http://test.de/rdfs_class"));
        assertSame(ResourceType.DATATYPE_PROPERTY, ResourceType.analyzeWithInferenceFast(m, "http://test.de/owl_datatype_property"));
        assertSame(ResourceType.OBJECT_PROPERTY, ResourceType.analyzeWithInferenceFast(m, "http://test.de/owl_object_property"));
        assertSame(ResourceType.ANNOTATION_PROPERTY, ResourceType.analyzeWithInferenceFast(m, "http://test.de/owl_annotation_property"));
        //assertSame(ResourceType.INSTANCE, ResourceType.analyze(m, "http://test.de/owl_ontology_property")); //TODO: actually this is not correct but jena and the lie can not differentiate
        assertSame(ResourceType.CLASS, ResourceType.analyzeWithInferenceFast(m, "http://test.de/owl_class"));
        assertSame(ResourceType.CLASS, ResourceType.analyzeWithInferenceFast(m, "http://test.de/owl_class_intersection"));
        assertSame(ResourceType.INSTANCE, ResourceType.analyzeWithInferenceFast(m, "http://test.de/instance_rdfs_class"));
        assertSame(ResourceType.INSTANCE, ResourceType.analyzeWithInferenceFast(m, "http://test.de/instance_owl_class"));
        assertSame(ResourceType.INSTANCE, ResourceType.analyzeWithInferenceFast(m, "http://test.de/instance_owl_thing"));               
        
        assertSame(ResourceType.CLASS, ResourceType.analyzeWithInferenceFast(m, "http://test.de/just_a_class_no_definition"));        
        
        
        //assertSame(ResourceType.RDF_PROPERTY, ResourceType.analyze(m, "http://test.de/undefined_prop"));
        
        assertSame(ResourceType.CLASS, ResourceType.analyzeWithInferenceFast(m, "http://test.de/subclass"));
        assertSame(ResourceType.CLASS, ResourceType.analyzeWithInferenceFast(m, "http://test.de/superclass"));
        
        assertSame(ResourceType.UNKNOWN, ResourceType.analyzeWithInferenceFast(m, "http://not.defined/test"));
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
