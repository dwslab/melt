package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

/**
 * Helper class for {@link Agony}.
 */
class AgonyGraphEdge {
    private AgonyGraphNode parent;
    private AgonyGraphNode child;
    private int id;
    private boolean bound;
    
    public AgonyGraphEdge(int id){
        this.parent = null;
        this.child = null;
        this.id = id;
        this.bound = false;
    }

    public AgonyGraphNode getParent() {
        return parent;
    }

    public void setParent(AgonyGraphNode parent) {
        this.parent = parent;
    }

    public AgonyGraphNode getChild() {
        return child;
    }

    public void setChild(AgonyGraphNode child) {
        this.child = child;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isBound() {
        return bound;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
    }
}
