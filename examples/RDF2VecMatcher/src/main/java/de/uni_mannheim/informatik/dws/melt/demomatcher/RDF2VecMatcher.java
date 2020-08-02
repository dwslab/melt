package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;

import java.io.File;
import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * A simple string matcher using String equivalence as matching criterion.
 */
public class RDF2VecMatcher extends MatcherYAAAJena {
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        Alignment alignment = new Alignment();

        // create directory in which the walks for the source will be generated into (and also the model)
        File sourceWalkDirectory = new File("./sourceDirectory");
        sourceWalkDirectory.mkdir();

        // create directory in which the walks for the target will be generated into (and also the model); must be different than source directory (!)
        File targetWalks = new File("./targetDirectory");
        targetWalks.mkdir();

        // RDF2vec Instance with ontMOdel and directory for walks
        RDF2Vec rdf2Vec = new RDF2Vec(source, sourceWalkDirectory);

        // trigger source training
        String sourceModel = rdf2Vec.train();

        // trigger target training
        String targetModel = rdf2Vec.trainNew(target, targetWalks);
        return alignment;
    }
    
    private void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment) {


    }
    
    private String getStringRepresentation(OntResource resource) {
        String arbitraryLabel = resource.getLabel(null);
        if(arbitraryLabel != null)
            return arbitraryLabel;
        return resource.getLocalName();
    }


}
