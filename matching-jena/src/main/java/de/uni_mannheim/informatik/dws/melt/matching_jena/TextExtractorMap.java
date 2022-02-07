package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

/**
 * Given a Jena resource, a ValueExtractor can derive zero or more String representations.
 * The returned value is a map between the key (e.g. where it is extracted from like label or fragment etc) and the corresponding texts.
 * 
 * Developer remark:
 * If you implement a new extractor: For a good design, you may want to implement a {@link LiteralExtractorMap} and use
 * this interface to wrap it.
 */
public interface TextExtractorMap {

    /**
     * Given a Jena resource this method extracts textual/string representations from it.
     * @param r the jena resource which also allows to traverse the whole rdf graph
     * @return a set of textual representations of the given resource.
     */
    Map<String, Set<String>> extract(Resource r);
    
    public static TextExtractorMap wrapLiteralExtractorMap(LiteralExtractorMap e){
        return (Resource r) -> {
            Map<String, Set<String>> returnMap = new HashMap<>();
            for(Entry<String, Set<Literal>> entry : e.extract(r).entrySet()){
                returnMap.put(
                        entry.getKey(),
                        entry.getValue().stream().map(Literal::getLexicalForm).filter(x -> !x.trim().equals("")).collect(Collectors.toSet())
                );
            }
            return returnMap;
        };
    }
    
    public static TextExtractorMap wrapTextExtractor(TextExtractor e){
        return (Resource r) -> {
            Map<String, Set<String>> returnMap = new HashMap<>();
            returnMap.put("TextExtractor", e.extract(r));
            return returnMap;
        };
    }
    
    public static TextExtractorMap appendStringPostProcessing(TextExtractorMap e, Function<String, String> postprocessing){
        return (Resource r) -> {
            Map<String, Set<String>> returnMap = new HashMap<>();
            for(Entry<String, Set<String>> entry : e.extract(r).entrySet()){
                returnMap.put(
                        entry.getKey(),
                        entry.getValue().stream().map(postprocessing).filter(x -> !x.trim().equals("")).collect(Collectors.toSet())
                );
            }
            return returnMap;
        };
    }
}
