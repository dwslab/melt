
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorMap;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.*;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.LoggerFactory;

/**
 * This class allows to manually inspect the output of a {@link TextExtractor} by writing the results to a file or stdout.
 */
public class ManualInspectionMap {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ManualInspection.class);
    
    private TextExtractorMap extractor;

    public ManualInspectionMap(TextExtractorMap extractor) {
        this.extractor = extractor;
    }
    
    public void describeSampleSource(TestCase tc, int samples, boolean listAnnotationProperties, boolean includeSubModel, boolean addInput, boolean nextJump){
        try(PrintStream f = new PrintStream(new File(tc.getName() + "-source"), "utf-8")){
            describe(f, tc.getSourceOntology(OntModel.class), sample(tc.getParsedReferenceAlignment().getDistinctSourcesAsSet(), samples),
                listAnnotationProperties, includeSubModel, addInput, nextJump);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.warn("Cannot write model", ex);
        }
    }
    public void describeSampleTarget(TestCase tc, int samples, boolean listAnnotationProperties, boolean includeSubModel, boolean addInput, boolean nextJump){
        try(PrintStream f = new PrintStream(new File(tc.getName() + "-target"), "utf-8")){
            describe(f, tc.getTargetOntology(OntModel.class), sample(tc.getParsedReferenceAlignment().getDistinctTargetsAsSet(), samples) ,
                listAnnotationProperties, includeSubModel, addInput, nextJump);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.warn("Cannot write model", ex);
        }
    }
    
    public void describeSample(TestCase tc, int samples, boolean listAnnotationProperties, boolean includeSubModel, boolean addInput, boolean nextJump){
        describeSampleSource(tc,samples, listAnnotationProperties, includeSubModel, addInput, nextJump);
        describeSampleTarget(tc,samples, listAnnotationProperties, includeSubModel, addInput, nextJump);
    }
    
    private Collection<String> sample(Set<String> set, int samples){
        List<String> list = new ArrayList<>(set);
        Collections.shuffle(list);
        return list.subList(0, Math.min(list.size(), samples));
    }
    
    public void describeAll(TestCase tc, boolean listAnnotationProperties, boolean includeSubModel, boolean addInput, boolean nextJump){
        
        try(PrintStream f = new PrintStream(new File(tc.getName() + "-source"), "utf-8")){
            describe(f, tc.getSourceOntology(OntModel.class), tc.getParsedReferenceAlignment().getDistinctSourcesAsSet(),
                listAnnotationProperties, includeSubModel, addInput, nextJump);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.warn("Cannot write model", ex);
        }
        
        try(PrintStream f = new PrintStream(new File(tc.getName() + "-target"), "utf-8")){
            describe(f, tc.getTargetOntology(OntModel.class), tc.getParsedReferenceAlignment().getDistinctTargetsAsSet(),
                listAnnotationProperties, includeSubModel, addInput, nextJump);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.warn("Cannot write model", ex);
        }
    }
    
    public void describe(PrintStream f, OntModel m, Collection<String> describeConcepts, boolean listAnnotationProperties, boolean includeSubModel, boolean addInput, boolean nextJump){
        if(listAnnotationProperties){
            f.println("Annotation properties:");
            for(AnnotationProperty p : m.listAnnotationProperties().toList()){
                f.println("  " + p.getURI() + " " + p.getLabel(null));
            }
        }
        for(String uri : describeConcepts){
            f.println(uri);
            Resource r = m.createResource(uri);
            if(includeSubModel){
                Model subModel = getSubModel(m, r, addInput, nextJump);
                f.println("Sub model:");
                RDFDataMgr.write(f, subModel, RDFFormat.TURTLE_PRETTY) ;
            }
            f.println("Extracted texts:");
            for(Entry<String, Set<String>> entry : this.extractor.extract(r).entrySet()){                
                f.println("  Group: " + entry.getKey());
                for(String text : entry.getValue()){
                    f.println("    " + text.replace('\n', ' '));
                }
            }
        }
    }    
    
    
    private Model getSubModel(OntModel model, Resource r, boolean addInput, boolean nextJump){
        Model selectedResources = ModelFactory.createDefaultModel();
        selectedResources.setNsPrefixes(model);        
        selectedResources.add(model.listStatements(r, null, (RDFNode)null));
        if(nextJump){
            StmtIterator i = model.listStatements(r, null, (RDFNode)null);
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isResource()){
                    selectedResources.add(n.asResource().listProperties());
                }
            }
        }
        
        if(addInput){
            selectedResources.add(model.listStatements(null, null, r));
        }
        return selectedResources;
    }
}
