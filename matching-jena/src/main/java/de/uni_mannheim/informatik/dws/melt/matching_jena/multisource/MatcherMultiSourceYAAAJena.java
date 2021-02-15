package de.uni_mannheim.informatik.dws.melt.matching_jena.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.URL2PropertiesTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.typetransformation.Alignment2URLTransformer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;


public abstract class MatcherMultiSourceYAAAJena extends MatcherMultiSourceURL implements IMatcherMultiSource<OntModel, Alignment, Properties>{

    @Override
    public URL match(List<URL> graphs, URL inputAlignment, URL parameters) throws Exception {
        //use hard coded transformers and not the generic variant:        
        Properties p = URL2PropertiesTransformer.parse(parameters);
        
        List<OntModel> ontModels = new ArrayList<>(graphs.size());
        for(URL url : graphs){
            ontModels.add(readOntology(url, getModelSpec()));
            //can also use transformer: 
            //ontModels.add(URL2OntModelTransformer.transformToOntModel(url, p));
        }
        Alignment parsedInputAlignment = inputAlignment == null ? new Alignment(): AlignmentParser.parse(inputAlignment);
        Alignment returnedAlignment = this.match(ontModels, parsedInputAlignment, p);
        return Alignment2URLTransformer.serializeAlignmentToTmpDir(returnedAlignment);
        //File alignmentFile = File.createTempFile(FILE_PREFIX, FILE_SUFFIX);
        //AlignmentSerializer.serialize(alignment, alignmentFile);
        //return alignmentFile.toURI().toURL();
    }
    
    @Override
    public abstract Alignment match(List<OntModel> graphs, Alignment inpuAlignment, Properties parameters) throws Exception;
    
    
    protected OntModelSpec getModelSpec(){
        return OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC;
    }
    
    /**
     * Default implementation to load an ontology from an url with jena.
     * Uses the cache.
     * It can be changed by subclasses.
     * @param url the url pointing to an ontology
     * @param spec the spec which should be used
     * @return ont model
     */
    protected OntModel readOntology(URL url, OntModelSpec spec){
        return OntologyCacheJena.get(url, spec);
    }
    
    protected OntModel createOntology(){
        return ModelFactory.createOntologyModel(getModelSpec());
    }
}
