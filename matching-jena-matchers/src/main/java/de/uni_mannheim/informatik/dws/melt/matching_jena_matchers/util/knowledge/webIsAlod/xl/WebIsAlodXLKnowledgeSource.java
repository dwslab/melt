package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.webIsAlod.xl;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.KnowledgeSource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.webIsAlod.WebIsAlodSPARQLservice;
import org.apache.jena.atlas.lib.NotImplemented;

import java.util.HashSet;

public class WebIsAlodXLKnowledgeSource extends KnowledgeSource {


    /**
     * The linker that links input strings to terms.
     */
    WebIsAlodXLLinker linker;

    /**
     * SPARQL service instance required for requests.
     */
    WebIsAlodSPARQLservice sparqLservice;

    /**
     * minimum confidence for synonymy queries.
     */
    public double minimumConfidence;


    public WebIsAlodXLKnowledgeSource(double minimumConfidence){
        sparqLservice = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_XL_NO_PROXY);
        linker = new WebIsAlodXLLinker();
        this.minimumConfidence = minimumConfidence;
    }

    /**
     * Default constructor.
     * A minimum required confidence of 0.0 is used as default.
     */
    public WebIsAlodXLKnowledgeSource(){
        this(-1.0);
    }


    @Override
    public boolean isInDictionary(String word) {
        // NOTE: Compared to the other background sources, this dictionary uses the linker.
        // However, as the linker does not use the isInDictionary() method, this has no effect and is rather
        // semantic sugar.
        if(linker.linkToSingleConcept(word) != null){
            return true;
        } else return false;
    }


    public boolean isURIinDictionary(String uri){
        throw new NotImplemented();
    }


    @Override
    public boolean isSynonymous(String word1, String word2) {
        if(this.minimumConfidence <= 0) {
            return sparqLservice.isSynonymous(word1, word2);
        } else {
            return sparqLservice.isSynonymous(word1, word2, minimumConfidence);
        }
    }

    @Override
    public boolean isStrongFormSynonymous(String word1, String word2) {
        if(this.minimumConfidence <= 0) {
            // no distinction here
            return sparqLservice.isSynonymous(word1, word2);
        } else {
            // no distinction here
            return sparqLservice.isSynonymous(word1, word2, minimumConfidence);
        }
    }

    @Override
    public HashSet<String> getSynonyms(String linkedConcept) {
        throw new NotImplemented();
    }

    @Override
    public void close() {
        // intentionally left blanc
    }

    @Override
    public HashSet<String> getHypernyms(String linkedConcept) {
        return null;
    }

    //----------------------------------------------------------------------------
    // Getters and Setters
    //----------------------------------------------------------------------------

    @Override
    public LabelToConceptLinker getLinker() {
        return null;
    }

    @Override
    public String getName() {
        if(minimumConfidence > 0.0){
            return "AlodXL_" + "MinConf_" + minimumConfidence;
        } else {
            return "AlodXL" + "MinConf_00";
        }
    }

    public void setMinimumConfidence(double minimumConfidence) {
        // cannot be larger than 1.0
        this.minimumConfidence = Math.min(minimumConfidence, 1.0);
    }

    public double getMinimumConfidence() {
        return minimumConfidence;
    }
}