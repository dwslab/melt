package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.classic;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.WebIsAlodSPARQLservice;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import org.apache.jena.atlas.lib.NotImplemented;

import java.util.Set;

/**
 * WebIsAlod Knowledge source. All requests are made through the {@link WebIsAlodSPARQLservice}.
 */
public class WebIsAlodClassicKnowledgeSource extends SemanticWordRelationDictionary {


    /**
     * The linker that links input strings to terms.
     */
    WebIsAlodClassicLinker linker;

    /**
     * SPARQL service instance required for requests.
     */
    WebIsAlodSPARQLservice sparqlService;

    /**
     * minimum confidence for synonymy queries.
     */
    public double minimumConfidence = - 1.0;

    /**
     * Constructor
     * @param minimumConfidence The minimum required confidence for synonymy.
     */
    public WebIsAlodClassicKnowledgeSource(double minimumConfidence){
        this();
        this.minimumConfidence = minimumConfidence;
    }

    /**
     * Default constructor.
     * A minimum required confidence of 0.0 is used as default.
     */
    public WebIsAlodClassicKnowledgeSource(){
        sparqlService = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT);
        linker = new WebIsAlodClassicLinker();
    }

    public boolean isInDictionary(String word) {
        // NOTE: Compared to the other background sources, this dictionary uses the linker.
        // However, as the linker does not use the isInDictionary() method, this has no effect and is rather
        // semantic sugar.
        if(linker.linkToSingleConcept(word) != null){
            return true;
        } else return false;
    }

    /**
     * Not handled this way.
     * @param linkedConcept The concept (already linked).
     * @return A set of synonyms.
     */
    @Override
    public Set<String> getSynonymsLexical(String linkedConcept) {
        throw new NotImplemented();
    }

    @Override
    public boolean isSynonymous(String link1, String link2) {
        if(this.minimumConfidence <= 0) {
            return sparqlService.isSynonymous(link1, link2);
        } else {
            return sparqlService.isSynonymous(link1, link2, minimumConfidence);
        }
    }

    @Override
    public boolean isStrongFormSynonymous(String link1, String link2) {
        if(this.minimumConfidence <= 0) {
            // no distinction here
            return sparqlService.isSynonymous(link1, link2);
        } else {
            // no distinction here
            return sparqlService.isSynonymous(link1, link2, minimumConfidence);
        }
    }

    @Override
    public Set<String> getHypernyms(String linkedConcept) {
        return sparqlService.getHypernyms(linkedConcept, minimumConfidence);
    }

    @Override
    public boolean isHypernymous(String linkedConcept_1, String linkedConcept_2) {
        if (this.minimumConfidence <= 0) {
            // no distinction here
            return sparqlService.isHypernymous(linkedConcept_1, linkedConcept_2);
        } else {
            // no distinction here
            return sparqlService.isHypernymous(linkedConcept_1, linkedConcept_2, minimumConfidence);
        }
    }

    //----------------------------------------------------------------------------
    // Getters and Setters
    //----------------------------------------------------------------------------

    @Override
    public void close() {
        sparqlService.close();
    }

    @Override
    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName(){
        if(minimumConfidence > 0.0){
            return "AlodClassic_" + "MinConf_" + minimumConfidence;
        } else {
            return "AlodClassic" + "MinConf_00";
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
