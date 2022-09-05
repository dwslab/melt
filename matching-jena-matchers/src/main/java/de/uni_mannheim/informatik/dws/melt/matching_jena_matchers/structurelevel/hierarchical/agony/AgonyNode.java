package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

/**
 * Helper class for {@link Agony}.
 */
class AgonyNode <E> {
    private int id;
    private E label;
    
    private int rank;
    private int newrank;
    private int diff;
    
    private AgonyNode<E> parent;
    private int parentEdge;
    
    private int count;

    public AgonyNode(int id) {
        this.id = id;
    }
    
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public E getLabel() {
        return label;
    }

    public void setLabel(E label) {
        this.label = label;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getNewrank() {
        return newrank;
    }

    public void setNewrank(int newrank) {
        this.newrank = newrank;
    }
    
    public void reduceNewrank(int shift) {
        this.newrank -= shift;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public AgonyNode<E> getParent() {
        return parent;
    }

    public void setParent(AgonyNode<E> parent) {
        this.parent = parent;
    }

    public int getParentEdge() {
        return parentEdge;
    }

    public void setParentEdge(int parentEdge) {
        this.parentEdge = parentEdge;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    public void decreaseCount(){
        this.count--;
    }

    @Override
    public String toString() {
        return "AgonyNode{" + "id=" + id + ", label=" + label + '}';
    }
    
    //hashcode and equals are based on the id:
    //important for the agonyqueue to fastly generate the hash
    //for the hashset attribute called remove

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AgonyNode<?> other = (AgonyNode<?>) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
    
}
