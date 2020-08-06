package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the confidence of already matched resources.
 * It writes a textual representation of each resource to a csv file (text generation can be modified by subclassing and overriding getResourceText method).
 */
public class Doc2vecModelMatcher extends DocumentSimilarityBase {
    private final static Logger LOGGER = LoggerFactory.getLogger(Doc2vecModelMatcher.class);
    
    protected Word2VecConfiguration configuration;
    
    public Doc2vecModelMatcher(){
        super();
        this.configuration = new Word2VecConfiguration();
    }
    

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        createCorpusFileIfNecessary(source, target);
        String modelName = "corpora";
        Gensim gensim = Gensim.getInstance();
        gensim.trainDoc2VecModel(modelName, this.corpusFile.getCanonicalPath(), this.configuration);
        updateConfidences(gensim, modelName, inputAlignment);
        Gensim.shutDown();
        return inputAlignment;
    }
    
    private void updateConfidences(Gensim gensim, String modelName, Alignment inputAlignment){
        //make the order explicit
        List<Correspondence> list = new ArrayList<>(inputAlignment);
        List<Double> confidences = null;
        try {
            confidences = gensim.queryDoc2VecModel(modelName, list);
        } catch (Exception ex) {
            LOGGER.error("Server failure in queryVectorSpaceModel. No confidences are updated.", ex);
            return;
        }
        if(confidences.size() != list.size()){
            LOGGER.error("Size of result list of confidences is not equal to initial list of correspondences. No confidences are updated.");
            return;            
        }     
        for(int i=0; i < list.size(); i++){
            Correspondence c = list.get(i);
            Double conf = confidences.get(i);            
            if(conf <= 1.1 && conf >= -1.1){ // between -1.0 and 1.0 (.1 because of rounding)
                c.setConfidence(conf);
            }
        }
    }

    public Word2VecConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Word2VecConfiguration configuration) {
        this.configuration = configuration;
    }
    
    
    
    
}
