package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

/**
 * Helper class for {@link Agony}.
 */
class AgonyEdge {
    
    private boolean eulerian;
    private int id;
    private int slack;

    public AgonyEdge(int id) {
        this.id = id;
    }
    
    public boolean isEulerian() {
        return eulerian;
    }

    public void setEulerian(boolean eulerian) {
        this.eulerian = eulerian;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSlack() {
        return slack;
    }

    public void setSlack(int slack) {
        this.slack = slack;
    }
}
