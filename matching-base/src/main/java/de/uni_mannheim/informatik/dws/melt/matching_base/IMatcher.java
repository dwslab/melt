package de.uni_mannheim.informatik.dws.melt.matching_base;

/**
 * Generic matcher interface which just implements one method called match.
 * It gets a source and a target ontology / knowledge graph, an input alignment and additional parameters.
 * If you do not need any of the last parameters, have a look at
 * @param <ModelClass> The specific type which can store an ontology / knowledge graph / ABox and TBox examples are OntModel or Model from Jena.
 * @param <AlignmentClass> The specifc type which can store an alignment e.g. (Alignment from YAAA)
 * @param <ParameterClass> The specific type which can store parameters. This is somewhat a map where the keys are string and the values are any objects e.g. java.util.Properties
 */
public interface IMatcher <ModelClass, AlignmentClass, ParameterClass>{
    
    /**
     * Aligns two ontologies/knowledge graphs given as the first and second parameter.
     * An additional input alignment can be given as well as parameters which further define how and what to match.
     * @param source this object represents the source ontology/knowledge graph
     * @param target this object represents the target ontology/knowledge graph
     * @param inputAlignment this object represents the input alignment
     * @param parameter object representing additional properties
     * @return the resulting mapping of the matching process.
     * @throws Exception any exception which occurs during matching
     */
    AlignmentClass match(ModelClass source, ModelClass target, AlignmentClass inputAlignment, ParameterClass parameter) throws Exception;
    
}