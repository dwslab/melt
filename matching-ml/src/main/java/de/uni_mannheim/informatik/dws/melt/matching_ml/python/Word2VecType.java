package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import java.util.Locale;

/**
 * Type of word2vec model/approach like CBOW or SG.
 */
public enum Word2VecType {
    
    /**
     * Continuous bag-of-words model for word2vec.
     * In case of Doc2Vec this represents PV-DM.
     */
    CBOW,
    
    /**
     * Skip-gram model for word2vec.
     * In case of Doc2Vec this represents PV-DBOW.
     */
    SG;
    
    @Override
    public String toString(){
        return this.name().toLowerCase(Locale.ENGLISH);
    }
}
