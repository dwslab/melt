package de.uni_mannheim.informatik.dws.melt.matching_base.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import java.util.List;

/**
 * Generic matcher interface for matching multiple ontologies / knowledge graphs.
 * It gets multiple ontologies / knowledge graphs, an input alignment and additional parameters.
 * @param <ModelClass> The specific type which can store an ontology / knowledge graph / ABox and TBox. Examples are URI, OntModel or Model from Jena.
 * @param <AlignmentClass> The specifc type which can store an alignment e.g. (Alignment from YAAA)
 * @param <ParameterClass> The specific type which can store parameters. This is somewhat a map where the keys are string and the values are any objects e.g. java.util.Properties
 */
public interface IMatcherMultiSource <ModelClass, AlignmentClass, ParameterClass>{
    
    /**
     * Matches multiple ontologies / knowledge graphs together.
     * @param models a list of ontologies / knowledge graphs in the desired format.
     * @param inputAlignment this object represents the input alignment.
     * @param parameters object representing additional parameters.
     *      Only add to this object and do not create a new Object like <code>parameters= new ...()</code> 
     *      because otherwise the parameters are lost (<a href="https://stackoverflow.com/questions/40480/is-java-pass-by-reference-or-pass-by-value">java ist call by value</a>).
     *      Sensible classes are {@link java.util.Properties}, {@link java.util.Map Map&lt;String, Object&gt;} or any similar data structure.
     *      Some already specified keys (strings) can be found at {@link ParameterConfigKeys}.
     * @return the resulting alignment of the matching process.
     * @throws java.lang.Exception in case of any errors
     */
    AlignmentClass match(List<ModelClass> models, AlignmentClass inputAlignment, ParameterClass parameters) throws Exception;
    
    /**
     * Returns a boolean value if the matcher needs a transitive closure for evaluation.
     * E.g. some matchers match only A-B-C and the testcase asks for A-C then this is only true,
     * if the transitive closure is computed
     * @return true if the transitive closure is need, false otherwise
     */
    default boolean needsTransitiveClosureForEvaluation() { return false; }
    
}
