package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.jrdf2vec.RDF2Vec;
import de.uni_mannheim.informatik.dws.jrdf2vec.training.Word2VecConfiguration;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;

import java.io.File;
import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.Gensim;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;

/**
 * A simple string matcher using String equivalence as matching criterion.
 */
public class RDF2VecMatcher extends MatcherYAAAJena {

    public Alignment initialAlignment = new Alignment();

    public RDF2VecMatcher(){
    }

    public RDF2VecMatcher(Alignment initialAlignment){
        this.initialAlignment = initialAlignment;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        Alignment usedAlignment;
        if(inputAlignment != null && inputAlignment.size() > 0){
            usedAlignment = inputAlignment;
        } else usedAlignment = this.initialAlignment;

        // create directory in which the walks for the source will be generated into (and also the model)
        File sourceWalkDirectory = new File("./sourceDirectory");
        sourceWalkDirectory.mkdir();

        // create directory in which the walks for the target will be generated into (and also the model); must be different than source directory (!)
        File targetWalks = new File("./targetDirectory");
        targetWalks.mkdir();

        // RDF2vec Instance with ontMOdel and directory for walks
        RDF2Vec rdf2Vec = new RDF2Vec(source, sourceWalkDirectory);
        Word2VecConfiguration config = new Word2VecConfiguration();
        config.setVectorDimension(100);
        rdf2Vec.setConfiguration(config);

        // trigger source training
        String sourceModel = rdf2Vec.train();

        // trigger target training
        String targetModel = rdf2Vec.trainNew(target, targetWalks);

        de.uni_mannheim.informatik.dws.jrdf2vec.training.Gensim.shutDown();

        Alignment alignment = Gensim.getInstance().alignModel("./sourceDirectory/model.kv", "./targetDirectory/model.kv", "linear_projection", usedAlignment);
        
        FileUtils.deleteDirectory(sourceWalkDirectory);
        FileUtils.deleteDirectory(targetWalks);
        Gensim.shutDown();

        return alignment;
    }
}
