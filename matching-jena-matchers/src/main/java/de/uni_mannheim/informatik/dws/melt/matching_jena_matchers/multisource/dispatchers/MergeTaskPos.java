package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

/**
 * A task which contains the two knowledge graphs to be merged and the new position of the merged kg.
 */
public class MergeTaskPos {
    private final int clusterOnePos;
    private final int clusterTwoPos;    
    private final int clusterResultPos;
    private final double distance;
    private final double distanceNormalized;

    public MergeTaskPos(int clusterOnePos, int clusterTwoPos, int clusterResultPos, double distance, double distanceNormalized) {
        this.clusterOnePos = clusterOnePos;
        this.clusterTwoPos = clusterTwoPos;
        this.clusterResultPos = clusterResultPos;
        this.distance = distance;
        this.distanceNormalized = distanceNormalized;
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

    public double getDistance() {
        return distance;
    }

    public double getDistanceNormalized() {
        return distanceNormalized;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.clusterOnePos;
        hash = 17 * hash + this.clusterTwoPos;
        hash = 17 * hash + this.clusterResultPos;
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
        final MergeTaskPos other = (MergeTaskPos) obj;
        if (this.clusterOnePos != other.clusterOnePos) {
            return false;
        }
        if (this.clusterTwoPos != other.clusterTwoPos) {
            return false;
        }
        if (this.clusterResultPos != other.clusterResultPos) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {        
        return "merge(" + clusterOnePos + "," + clusterTwoPos + ") to " + clusterResultPos + "(dist:" + distance + " norm:" + distanceNormalized + ')';
    }
    
    
}
