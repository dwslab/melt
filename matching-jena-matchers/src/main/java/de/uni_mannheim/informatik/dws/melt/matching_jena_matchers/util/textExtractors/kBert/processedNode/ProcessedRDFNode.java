package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;

import static org.apache.commons.text.StringEscapeUtils.escapeCsv;

public abstract class ProcessedRDFNode {

    /**
     * todo
     *
     * @return todo
     */
    public String getNormalized() {
        return escapeCsv(StringProcessing.normalizeJoining(getRaw()));
    }

    protected abstract String getRaw();
}
