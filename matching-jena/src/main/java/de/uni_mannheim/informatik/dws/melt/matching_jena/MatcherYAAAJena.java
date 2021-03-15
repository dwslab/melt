package de.uni_mannheim.informatik.dws.melt.matching_jena;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.OntoInfo;

import java.net.URL;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;


/**
 * A matcher template for matchers that are based on Apache Jena.
 */
public abstract class MatcherYAAAJena extends MatcherYAAA implements IMatcher<OntModel, Alignment, Properties>{


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
         
    @Override
    public Alignment match(URL source, URL target, Alignment inputAlignment, Properties properties) throws Exception {
        OntModel jena_source = readOntology(source, getModelSpec());
        OntModel jena_target = readOntology(target, getModelSpec());
        
        inputAlignment.setOnto1(new OntoInfo(jena_source.getNsPrefixURI(""), source.toString()));
        inputAlignment.setOnto2(new OntoInfo(jena_target.getNsPrefixURI(""), target.toString()));
               
        return this.match(jena_source, jena_target, inputAlignment, properties);
    }

     /**
     * Aligns two ontologies specified via a Jena OntModel, with an input alignment
     * as Alignment object, and returns the mapping of the resulting alignment.
     *
     * Note: This method might be called multiple times in a row when using the evaluation framework.
     * Make sure to return a mapping which is specific to the given inputs.
     *
     * @param source This OntModel represents the source ontology.
     * @param target This OntModel represents the target ontology.
     * @param inputAlignment This mapping represents the input alignment.
     * @param properties Additional properties.
     * @return The resulting alignment of the matching process.
     * @throws Exception Any exception which occurs during matching.
     */
    @Override
    public abstract Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception ;
}
