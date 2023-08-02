package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum SSSOMMappingCardinality {
    ONE_TO_ONE("1:1"),
    ONE_TO_MANY("1:n"),
    MANY_TO_ONE("n:1"),
    ONE_TO_NONE("1:0"),
    NONE_TO_ONE("0:1"),
    MANY_TO_MANY("n:n");

    private final static Map<String, SSSOMMappingCardinality> MAP;

    static {
        Map<String, SSSOMMappingCardinality> map = new HashMap<>();
        for ( SSSOMMappingCardinality value : SSSOMMappingCardinality.values() ) {
            String text = value.toString().toLowerCase(Locale.ENGLISH);
            map.put(text, value);
            // for alignment api format - see format description attribute type
            //https://moex.gitlabpages.inria.fr/alignapi/format.html
            map.put(text.replace(":", ""), value);            
            map.put(text.replace("n", "*"), value); 
            map.put(text.replace(":", "").replace("n", "*"), value);
        }
        MAP = Collections.unmodifiableMap(map);
    }

    private final String repr;

    SSSOMMappingCardinality(String repr) {
        this.repr = repr;
    }

    @Override
    public String toString() {
        return repr;
    }

    public static SSSOMMappingCardinality fromString(String v) {
        return MAP.getOrDefault(v.toLowerCase(Locale.ENGLISH), ONE_TO_ONE);
    }
    
    public String toAlignmentFormat() {
        return repr.replace("n", "*");
    }
}
