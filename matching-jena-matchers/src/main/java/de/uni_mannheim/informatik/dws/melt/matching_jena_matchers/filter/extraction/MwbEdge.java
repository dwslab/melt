package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

/**
 * Helper class for {@link MaxWeightBipartiteExtractor}.
 * The edge of a graph.
 */
class MwbEdge{
    /**
     * Attribute for storing the graph : source node of the edge
     */
    private MwbNode source;
    /**
     * Attribute for storing the graph : target node of the edge
     */
    private MwbNode target;
    /**
     * Payload of the edge: this is just used for later extraction of the correct alignment and for the weight of the edge.
     */
    private Correspondence correspondence;
    
    private int weigth;
    
    public MwbEdge(MwbNode source, MwbNode target, Correspondence correspondence, int weight) {
        this.source = source;
        this.target = target;
        this.correspondence = correspondence;
        this.weigth = weight;
    }
    
    public MwbNode getSource() {
        return source;
    }
    
    public MwbNode getTarget() {
        return target;
    }
    
    public int getWeight() {
        return weigth;
    }
    
    public Correspondence getCorrespondence(){
        return this.correspondence;
    }
    
    public void reverse(){
        this.getSource().removeSuccesor(this);
        
        MwbNode tmpTarget = target;
        this.target = this.source;
        this.source = tmpTarget;
        
        this.getSource().addSuccesor(this);
    }
}