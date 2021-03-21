package de.uni_mannheim.informatik.dws.melt.matching_jena;

import de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.JenaTransformerHelper;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * 
 */
public class JenaHelper {


    public static OntModel createNewOntModel(){
        return createNewOntModel(new Properties());
    }
    
    public static OntModel createNewOntModel(Properties parameters){
        return ModelFactory.createOntologyModel(JenaTransformerHelper.getSpec(parameters));
    }
    
    public static Model createNewModel(){
        return createNewModel(new Properties());
    }
    
    public static Model createNewModel(Properties parameters){
        //TODO: make TDB if parameters says so
        return ModelFactory.createDefaultModel();
    }
    
}
