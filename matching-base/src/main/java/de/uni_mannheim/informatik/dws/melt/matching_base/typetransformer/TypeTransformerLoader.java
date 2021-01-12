package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

/**
 * Implement this interface to register multiple TypeTransformers.
 * They are loaded with the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">ServiceLoader</a> of java.
 * Thus a so called provider-configuration file has to be placed in the resource directory META-INF/services/. 
 * The name of this file needs to be  * de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerLoader
 * and the content should be a list of fully-qualified binary names of concrete classes which 
 * implement TypeTransformer interface, one per line.
 */
public interface TypeTransformerLoader {
    
    /**
     * Call the addTransformer method of the static TypeTransformerRegistry
     * to add multiple TypeTransformers at the same time.
     */
    void registerTypeTransformers();
}
