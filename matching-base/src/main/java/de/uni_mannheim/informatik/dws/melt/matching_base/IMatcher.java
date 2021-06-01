package de.uni_mannheim.informatik.dws.melt.matching_base;

/**
 * Generic matcher interface which just implements one method called match.
 * It gets a source and a target ontology / knowledge graph, an input alignment and additional parameters.
 * @param <ModelClass> The specific type which can store an ontology / knowledge graph / ABox and TBox examples are OntModel or Model from Jena.
 * @param <AlignmentClass> The specific type which can store an alignment e.g. (Alignment from YAAA)
 * @param <ParameterClass> The specific type which can store parameters. This is somewhat a map where the keys are string and the values are any objects e.g. java.util.Properties
 */
public interface IMatcher <ModelClass, AlignmentClass, ParameterClass> {


    /**
     * Aligns two ontologies/knowledge graphs given as the first and second parameter.
     * An additional input alignment can be given as well as parameters which further define how and what to match.
     * In case inputAlignment or parameters is not used, making them to the Object class, 
     * will required no additional transformations of these objects (no overhead).
     * @param source this object represents the source ontology/knowledge graph
     * @param target this object represents the target ontology/knowledge graph
     * @param inputAlignment this object represents the input alignment.
     * @param parameters object representing additional parameters.
     *      Only add to this object and do not create a new Object like <code>parameters= new ...()</code> 
     *      because otherwise the parameters are lost (<a href="https://stackoverflow.com/questions/40480/is-java-pass-by-reference-or-pass-by-value">java ist call by value</a>).
     *      Sensible classes are {@link java.util.Properties} (preferred), {@link java.util.Map Map&lt;String, Object&gt;} or any similar data structure.
     *      Some already specified keys (strings) can be found at {@link ParameterConfigKeys}.
     * @return the resulting alignment of the matching process.
     * @throws Exception any exception which occurs during matching
     */
    AlignmentClass match(ModelClass source, ModelClass target, AlignmentClass inputAlignment, ParameterClass parameters) throws Exception;
}