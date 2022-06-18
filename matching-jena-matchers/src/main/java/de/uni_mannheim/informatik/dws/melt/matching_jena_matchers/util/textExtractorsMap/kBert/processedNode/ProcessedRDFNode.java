package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.processedNode;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;

public abstract class ProcessedRDFNode {

    /**
     * todo
     *
     * @return todo
     */
    public String getNormalized() {
        return StringProcessing.normalizeJoining(getRaw());
    }

    protected abstract String getRaw();
}
