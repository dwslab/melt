
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import static de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.LoggerFactory;

/**
 * This class allows to manually inspect the output of a {@link TextExtractor} by writing the results to a file or stdout.
 */
public class ManualInspection {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ManualInspection.class);
    
    private TextExtractor extractor;

    public ManualInspection(TextExtractor extractor) {
        this.extractor = extractor;
    }
    
        
    public void writeTestCaseSourceToFile(TestCase tc, File f){
        writeModelToFile(tc.getSourceOntology(OntModel.class), f);
    }
    
    public void writeTestCaseTargetToFile(TestCase tc, File f){
        writeModelToFile(tc.getTargetOntology(OntModel.class), f);
    }    
    
    public void writeTestCaseToFolder(TestCase tc, File folder){
        writeTestCaseSourceToFile(tc, new File(folder, tc.getName() + "-source.txt"));
        writeTestCaseTargetToFile(tc, new File(folder, tc.getName() + "-target.txt"));
    }
    
    public void writeTestCaseToFolder(TestCase tc){
        writeTestCaseToFolder(tc, new File("./"));
    }
    
    
    public void writeTrackToFolder(Track track){
        writeTrackToFolder(track, new File("./"));
    }
    
    public void writeTrackToFolder(Track track, File folder){
        for(Entry<String, URL> ontology : track.getDistinctOntologiesMap().entrySet()){
            writeModelToFile(ontology.getValue(), new File(folder, track.getName() + "-" + ontology.getKey() + ".txt"));
        }
    }
    
    public void writeTrackToStdOut(Track track){
        for(Entry<String, URL> ontology : track.getDistinctOntologiesMap().entrySet()){
            System.out.println("=======" + ontology.getKey() + "=======");
            writeModelToStdOut(ontology.getValue());
        }
    }
    
    public void writeModelToFile(URL model, File f){
        writeModelToFile(getModel(model), f);
    }
    
    public void writeModelToFile(OntModel m, File f){
        try(PrintStream stream = new PrintStream(f, "utf-8")){
            write(m, stream);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.warn("Cannot write model", ex);
        }
    }
    
    public void writeModelToStdOut(OntModel model){
        write(model, System.out);
    }
    public void writeModelToStdOut(URL model){
        writeModelToStdOut(getModel(model));
    }
    
    
    private void write(OntModel m, PrintStream f){
        f.println("concept_type,uri,extracted_text");
        //f.println("==========Classes==========");
        write("class", m.listClasses(), f);
        //f.println("==========Properties==========");
        write("prop", m.listOntProperties(), f);
        //f.println("==========Individuals==========");
        write("inst", m.listIndividuals(), f);
    }
    
    private void write(String type, ExtendedIterator<? extends OntResource> it, PrintStream f){
        while(it.hasNext()){
            OntResource r = it.next();
            if(r.isURIResource() == false)
                continue;
            f.println(type + "," + r.getURI() + "," + StringEscapeUtils.escapeCsv(String.join(" ", extractor.extract(r)).trim()));
        }
    }
    
    private OntModel getModel(URL url){
        OntologyCacheJena.setDeactivatedCache(true);
        return OntologyCacheJena.get(url.toString(), DEFAULT_JENA_ONT_MODEL_SPEC);
    }
    
    
    public void describeResources(TestCase tc, int samples, File f, boolean useSource){
        Alignment a = tc.getParsedReferenceAlignment();
        Set<String> referenceURIs = useSource ? a.getDistinctSourcesAsSet() : a.getDistinctTargetsAsSet();
        List<String> shuffeledReferenceURI = new ArrayList<>(referenceURIs);
        Collections.shuffle(shuffeledReferenceURI);
        
        OntModel model = useSource ? tc.getSourceOntology(OntModel.class) : tc.getTargetOntology(OntModel.class);
        
        Model selectedResources = ModelFactory.createDefaultModel();
        for(String s : shuffeledReferenceURI.subList(0, Math.min(shuffeledReferenceURI.size(), samples))){
            selectedResources.add(model.listStatements(model.createResource(s), null, (RDFNode)null));
        }
        selectedResources.setNsPrefixes(model);
        
        try {
            RDFDataMgr.write (new FileOutputStream(f), selectedResources, RDFFormat.TURTLE_PRETTY) ;
        } catch (FileNotFoundException ex) {
            LOGGER.warn("Could not write file", ex);
        }
    }
    
    public void describeResourcesWithExtractor(TestCase tc, int samples, File f, boolean useSource){
        describeResourcesWithExtractor(tc, samples, f, useSource, false, false);
    }
    
    public void describeResourcesWithExtractor(TestCase tc, int samples, File f, boolean useSource, boolean addInput, boolean nextJump){
        Alignment a = tc.getParsedReferenceAlignment();
        Set<String> referenceURIs = useSource ? a.getDistinctSourcesAsSet() : a.getDistinctTargetsAsSet();
        List<String> shuffeledReferenceURI = new ArrayList<>(referenceURIs);
        Collections.shuffle(shuffeledReferenceURI);
        
        OntModel model = useSource ? tc.getSourceOntology(OntModel.class) : tc.getTargetOntology(OntModel.class);
        
        try(BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(f))){
            for(String s : shuffeledReferenceURI.subList(0, Math.min(shuffeledReferenceURI.size(), samples))){
                Resource r = model.createResource(s);
                Model selectedResources = getSubModel(model, r, addInput, nextJump);
                bw.write("================\n".getBytes(StandardCharsets.UTF_8));
                bw.write(("=>" + s + "\n").getBytes(StandardCharsets.UTF_8));
                RDFDataMgr.write(bw, selectedResources, RDFFormat.TURTLE_PRETTY) ;
                for(String extracted :  extractor.extract(r)){
                     bw.write(("----> " + extracted.trim() + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not write file", ex);
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
    
    
    public void listAnnotationProperties(TestCase tc, File f){
        try(PrintStream stream = new PrintStream(f, "utf-8")){
            listAnnotationProperties(tc, stream);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            LOGGER.warn("Cannot write model", ex);
        }
    }
    
    public void listAnnotationProperties(TestCase tc, PrintStream stream){
        stream.println("====Source Annotation Properties====");
        List<AnnotationProperty> propertyIterator = tc.getSourceOntology(OntModel.class).listAnnotationProperties().toList();
        for(AnnotationProperty p : propertyIterator){
            stream.println(p.getURI() + " " + p.getLabel(null));
        }

        stream.println("====Target Annotation Properties====");
        propertyIterator = tc.getTargetOntology(OntModel.class).listAnnotationProperties().toList();
        for(AnnotationProperty p : propertyIterator){
            stream.println(p.getURI() + " " + p.getLabel(null));
        }
    }
}
