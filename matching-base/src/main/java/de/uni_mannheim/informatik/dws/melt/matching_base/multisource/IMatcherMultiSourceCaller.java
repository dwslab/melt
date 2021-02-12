package de.uni_mannheim.informatik.dws.melt.matching_base.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import java.util.List;
import java.util.Set;

/**
 * Generic matcher interface for matching multiple ontologies / knowledge graphs which calls other matchers itself.
 * It gets multiple ontologies / knowledge graphs, an input alignment and additional parameters.
 */
public interface IMatcherMultiSourceCaller {
    
    /**
     * Matches multiple ontologies / knowledge graphs together.
     * @param models this is a list of sets of objects where each sets contains different representations of the dame ontologies/ knowledge graph.
     * @param inputAlignment this object represents the input alignment.
     * @param parameters object representing additional parameters.
     *      Only add to this object and do not create a new Object like <code>parameters= new ...()</code> 
     *      because otherwise the parameters are lost (<a href="https://stackoverflow.com/questions/40480/is-java-pass-by-reference-or-pass-by-value">java ist call by value</a>).
     *      Sensible classes are {@link java.util.Properties}, {@link java.util.Map Map&lt;String, Object&gt;} or any similar data structure.
     *      Some already specified keys (strings) can be found at {@link ParameterConfigKeys}.
     * @return the resulting alignment of the matching process.
     * @throws java.lang.Exception in case of any errors
     */
    AlignmentAndParameters match(List<Set<Object>> models, Object inputAlignment, Object parameters) throws Exception;
    
    /**
     * Returns a boolean value if the matcher needs a transitive closure for evaluation.
     * E.g. some matchers match only A-B-C and the testcase asks for A-C then this is only true,
     * if the transitive closure is computed
     * @return true if the transitive closure is need, false otherwise
     */
    default boolean needsTransitiveClosureForEvaluation() { return false; }
    
}
