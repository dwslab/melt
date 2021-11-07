package de.uni_mannheim.informatik.dws.melt.examples.sentence_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives.AddNegativesRandomlyAbsolute;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives.AddNegativesViaMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersFineTuner;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * This training pipeline writes the training file.
 */
public class TrainingPipeline extends MatcherYAAAJena {


    private final SentenceTransformersFineTuner fineTuner;

    
    public TrainingPipeline( SentenceTransformersFineTuner fineTuner) {
        this.fineTuner = fineTuner;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        
        
        //add negatives with a matcher:
        //Alignment recallAlignment = this.recallMatcher.match(source, target, new Alignment(), properties);
        //Alignment trainingAlignment = AddNegativesViaMatcher.addNegatives(recallAlignment, inputAlignment);
        
        //add negatives randomly
        AddNegativesRandomlyAbsolute addnegativesRandomly = new AddNegativesRandomlyAbsolute(4, true, false);
        Alignment trainingAlignment = addnegativesRandomly.match(source, target, inputAlignment, new Properties());

        //append to training file
        fineTuner.match(source, target, trainingAlignment, properties);
        
        return inputAlignment;
    }

    public SentenceTransformersFineTuner getFineTuner() {
        return fineTuner;
    }
}
