package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for {@link MaxWeightBipartiteExtractor}.
 * The node of a graph.
 */
class MwbNode implements Comparable<MwbNode>{


    /**
     * The graph structure (modeled only as successor).
     */
    private Set<MwbEdge> successor;
    /**
     * The potential as given in the algorithm.
     */
    private int potential;
    /**
     * Shortest path property for distance.  
     */
    private int distance;
    /**
     * Is the node already matched.
     */
    private boolean free;
    /**
     * Shortest path property for restoring the path.
     */
    private MwbEdge predecessor;

    public MwbNode() {
        this.successor = new HashSet<>();
        this.potential = 0;
        this.distance = 0;
        this.free = true;
        this.predecessor = null;
    }

    public void addSuccesor(MwbEdge e){
        this.successor.add(e);
    }

    public void removeSuccesor(MwbEdge e){
        this.successor.remove(e);
    }    

    public int getPotential() {
        return potential;
    }

    public void setPotential(int potential) {
        this.potential = potential;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public MwbEdge getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(MwbEdge predecessor) {
        this.predecessor = predecessor;
    }

    public Set<MwbEdge> getSuccessor() {
        return successor;
    }

    public void setSuccessor(Set<MwbEdge> successor) {
        this.successor = successor;
    }

    @Override
    public int compareTo(MwbNode o) {
        return Integer.compare(distance, o.distance);
    }
}
