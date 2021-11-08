package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

/**
 * A task which contains the two knowledge graphs to be merged and the new position of the merged kg.
 */
public class MergeTaskPos {
    private int clusterOnePos;
    private int clusterTwoPos;    
    private int clusterResultPos;

    public MergeTaskPos(int clusterOnePos, int clusterTwoPos, int clusterResultPos) {
        this.clusterOnePos = clusterOnePos;
        this.clusterTwoPos = clusterTwoPos;
        this.clusterResultPos = clusterResultPos;
    }

    public int getClusterOnePos() {
        return clusterOnePos;
    }

    public int getClusterTwoPos() {
        return clusterTwoPos;
    }

    public int getClusterResultPos() {
        return clusterResultPos;
    }
}
