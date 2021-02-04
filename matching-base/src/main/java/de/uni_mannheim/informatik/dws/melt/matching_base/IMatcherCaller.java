package de.uni_mannheim.informatik.dws.melt.matching_base;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import java.util.Set;

/**
 * A matcher interface which allows the matcher to call other matchers as well.
 */
public interface IMatcherCaller{
    
    /**
     * Aligns two ontologies/knowledge graphs given as the first and second parameter.
     * An additional input alignment can be given as well as parameters which further define how and what to match.
     * In case the matcher needs the objects in a specific type, call
     * <pre>
     * {@code
     * TODO
     * }
     * </pre>
     * @param sourceRespresentations a set of objects which all represents the source ontology/knowledge graph
     * @param targetRespresentations a set of objects which all represents the target ontology/knowledge graph
     * @param inputAlignment this object represents the input alignment
     * @param parameters object representing additional parameters. Sensible classes are {@link java.util.Properties}, Map&lt;String, Object&gt; or any similar data structure.
     * Some already specified keys (strings) can be found at {@link ParameterConfigKeys}.
     * @return the resulting alignment and updated parameters of the matching process.
     * @throws Exception any exception which occurs during matching
     */
    AlignmentAndParameters match(Set<Object> sourceRespresentations, Set<Object> targetRespresentations, Object inputAlignment, Object parameters) throws Exception;
    
}