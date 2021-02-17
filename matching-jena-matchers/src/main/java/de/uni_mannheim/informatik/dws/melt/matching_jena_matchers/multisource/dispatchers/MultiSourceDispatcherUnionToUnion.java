package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MultiSourceDispatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_jena.JenaHelper;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This 
 * @author shertlin
 */
public class MultiSourceDispatcherUnionToUnion extends MatcherMultiSourceURL implements MultiSourceDispatcher, IMatcherMultiSourceCaller{

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourceDispatcherUnionToUnion.class);
    
    private final Object oneToOneMatcher;
    
    public MultiSourceDispatcherUnionToUnion(Object oneToOneMatcher) {
        this.oneToOneMatcher = oneToOneMatcher;
    }
    
    @Override
    public URL match(List<URL> models, URL inputAlignment, URL parameters) throws Exception {
        List<Set<Object>> list = new ArrayList<>(models.size());
        for(URL ontology : models){
            list.add(new HashSet<>(Arrays.asList(ontology)));
        }
        AlignmentAndParameters alignmentAndPrameters = match(list, inputAlignment, parameters);
        return TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), URL.class);
    }

    @Override
    public AlignmentAndParameters match(List<Set<Object>> models, Object inputAlignment, Object parameters) throws Exception {
        
        LOGGER.info("Building union of all graphs");
        Properties p = TypeTransformerRegistry.getTransformedProperties(parameters);
        Model union = JenaHelper.createNewModel(p);
        for(Set<Object> m : models){
            Model transformedModel = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(m, Model.class, p);
            if(transformedModel == null){
                LOGGER.warn("Could not transform the ontologies into jena model. Continue with the next. The result will be incomplete.");
                continue;
            }
            union.add(transformedModel);
        }
        
        //LOGGER.info("Retrive classes");
        //union.listClasses();
        //LOGGER.info("list individuals");
        //union.listIndividuals();
        
        
        //Set<Individual> results = new HashSet<>();
        //for (Iterator<Statement> i = union.listStatements(null, RDF.type, (RDFNode) null); i.hasNext(); ) {
        //    OntResource r = i.next().getSubject().as( OntResource.class );
        //    LOGGER.info("-> {}", r);
        //    if (r.isIndividual()) {
        //        results.add( r.as( Individual.class ) );
        //    }
        //}
        
        LOGGER.info("Run matcher union to union");
        return GenericMatcherCaller.runMatcher(this.oneToOneMatcher, union, union, inputAlignment, parameters);
    }
    
    
    /*
    @Override
    public URL match(List<URL> graphs, URL inputAlignment) throws Exception{
        //faster way, because we can read in the ontologies directly into one model
        LOGGER.info("Building union of all graphs");
        OntModel union = createOntology();
        for(URL uri : graphs){
            union.read(uri.toString());
        }
        
        Alignment alignment = new Alignment();
        if(inputAlignment != null){
            alignment = AlignmentParser.parse(inputAlignment);
        }
        
        Alignment result = runMatcher(union, alignment);
        File alignmentFile = File.createTempFile(FILE_PREFIX, FILE_SUFFIX);
        AlignmentSerializer.serialize(result, alignmentFile);
        return alignmentFile.toURI().toURL();
    }
    
    @Override
    public Alignment match(List<OntModel> graphs, Alignment inputAlignment) throws Exception {
        LOGGER.info("Building union of all graphs");
        OntModel union = createOntology();   
        for(OntModel m : graphs){
            union.add(m);
        }   
        
        //LOGGER.info("Retrive classes");
        //union.listClasses();
        //LOGGER.info("list individuals");
        //union.listIndividuals();
        
        
        Set<Individual> results = new HashSet<>();
            for (Iterator<Statement> i = union.listStatements(null, RDF.type, (RDFNode) null); i.hasNext(); ) {
                OntResource r = i.next().getSubject().as( OntResource.class );
                LOGGER.info("-> {}", r);
                if (r.isIndividual()) {
                    results.add( r.as( Individual.class ) );
                }
            }
        
        LOGGER.info("finish");
        

        return runMatcher(union, inputAlignment);
    }
    
    protected Alignment runMatcher(OntModel union, Alignment inputAlignment) throws Exception{
        LOGGER.info("Run matcher on union graph");
        Properties properties = new Properties();
        return oneToOneMatcherJena.match(union, union, inputAlignment, properties);
    }
    
    @Override
    public boolean needsTransitiveClosureForEvaluation(){
        return false;
    }
*/

    
}
