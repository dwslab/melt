package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Helper class for {@link Agony}.
 */
class AgonyGraphNode {
    private int id;
    private Queue<AgonyGraphEdge> out;
    private Queue<AgonyGraphEdge> in;
    private int ind;
    private int outd;
    
    public AgonyGraphNode(int id){
        this.id = id;
        this.out = new LinkedList<>();
        this.in = new LinkedList<>();
        this.ind = 0;
        this.outd = 0;        
    }
    
    public void addOut(AgonyGraphEdge e){
        this.out.add(e);
        this.outd++;
    }
    public void removeOut(AgonyGraphEdge e){
        this.out.remove(e);
        this.outd--;
    }
    public void removeAllOut(){
        this.out.clear();
        this.outd = 0;
    }
    
    
    public void addIn(AgonyGraphEdge e){
        this.in.add(e);
        this.ind++;
    }    
    public void removeIn(AgonyGraphEdge e){
        this.in.remove(e);
        this.ind--;
    }
    public void removeAllIn(){
        this.in.clear();
        this.ind = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Queue<AgonyGraphEdge> getOut() {
        return out;
    }
    
    public Queue<AgonyGraphEdge> getIn() {
        return in;
    }

    public int getInd() {
        return ind;
    }
    
    public int getOutd() {
        return outd;
    }

    @Override
    public String toString() {
        return "AgonyGraphNode(" + id + ')';
    }
}
