package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.kBert.processedNode;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.NormalizedLiteral;

import java.util.Objects;

public abstract class ProcessedRDFNode {

    /**
     * todo
     *
     * @return todo
     */
    public NormalizedLiteral getNormalizedLiteral() {
        return new NormalizedLiteral(getRaw());
    }

    public String getNormalized() {
        return getNormalizedLiteral().getNormalized();
    }

    public abstract String getRaw();

    public String getKey() {
        return String.valueOf(hashCode());
    };

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(getNormalized());
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
        Class<? extends ProcessedRDFNode> clazz = getClass();
        Class<?> objClass = obj.getClass();
        if (clazz != objClass && clazz.getGenericSuperclass() != objClass.getGenericSuperclass()) {
            return false;
        }
        return Objects.equals(getNormalized(), ((ProcessedRDFNode) obj).getNormalized());
    }
}
