package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.NormalizedLiteral;

public abstract class ProcessedRDFNode {

    /**
     * todo
     *
     * @return todo
     */
    public NormalizedLiteral getNormalizedLiteral() {
        return new NormalizedLiteral(getRaw());
    }

    public abstract String getRaw();
}
