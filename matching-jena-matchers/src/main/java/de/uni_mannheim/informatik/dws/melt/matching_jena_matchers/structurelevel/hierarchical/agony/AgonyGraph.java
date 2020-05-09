package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for {@link Agony}.
 */
class AgonyGraph {
    
    private List<AgonyGraphNode> nodes;
    private List<AgonyGraphEdge> edges;
    
    public AgonyGraph(){}

    public AgonyGraph(AgonyGraph g){
        copy(g);
    }
    
    public void reset(int n, int m){
        this.nodes = new ArrayList<>(n);
        for(int i = 0; i < n; i++){
            this.nodes.add(new AgonyGraphNode(i));
        }
        
        this.edges = new ArrayList<>(m);
        for(int i = 0; i < m; i++){
            this.edges.add(new AgonyGraphEdge(i));
        }
    }
    
    public void copy(AgonyGraph g){
        reset(g.nodes.size(), g.edges.size());
        for(int i = 0; i < g.edges.size(); i++){
            if(g.edges.get(i).isBound()){
                bind(i, g.edges.get(i).getParent().getId(), g.edges.get(i).getChild().getId());
            }
        }
    }
    
    public void bind(int k, int n, int m){
        bind(getEdge(k), getNode(n), getNode(m));        
    }
    
    public void bind(AgonyGraphEdge e, AgonyGraphNode n, AgonyGraphNode m){
        n.addOut(e);
        m.addIn(e);
        e.setBound(true);
        e.setParent(n);
        e.setChild(m);
    }
    
    public void unbind(AgonyGraphEdge e){
        e.getParent().removeOut(e);
        e.getChild().removeIn(e);
        e.setBound(false);
    }
    
    public void unbind(AgonyGraphNode n){
        while(!n.getOut().isEmpty())
            unbind(n.getOut().peek());
        
        while(!n.getIn().isEmpty())
            unbind(n.getIn().peek());
    }
    
    public AgonyGraphNode getNode(int i){
        return this.nodes.get(i);
    }
    
    public AgonyGraphEdge getEdge(int i){
        return this.edges.get(i);
    }
}
