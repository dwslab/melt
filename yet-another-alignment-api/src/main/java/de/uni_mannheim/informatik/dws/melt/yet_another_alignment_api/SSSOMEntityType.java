package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum SSSOMEntityType {
    OWL_CLASS,
    OWL_OBJECT_PROPERTY,
    OWL_DATA_PROPERTY,
    OWL_ANNOTATION_PROPERTY,
    OWL_NAMED_INDIVIDUAL,
    SKOS_CONCEPT,
    RDFS_RESOURCE,
    RDFS_CLASS,
    RDFS_LITERAL,
    RDFS_DATATYPE,
    RDF_PROPERTY;

    private final static Map<String, SSSOMEntityType> MAP;

    static {
        Map<String, SSSOMEntityType> map = new HashMap<>();
        for ( SSSOMEntityType value : SSSOMEntityType.values() ) {
            map.put(value.toString(), value);
        }

        MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return name().toLowerCase().replace('_', ' ');
    }

    public static SSSOMEntityType fromString(String v) {
        return MAP.get(v);
    }
}
