package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.Properties;

/**
 * TypeTransformer interface. Classes implementing this interface can convert one java type to another.
 * TypeTransformers are loaded with the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">ServiceLoader</a> of java.
 * Thus a so called provider-configuration file has to be placed in the resource directory META-INF/services/. 
 * The name of this file needs to be de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformer
 * and the content should be a list of fully-qualified binary names of concrete classes which 
 * implement TypeTransformer interface, one per line.
 * @param <S> The source type
 * @param <T> The target type
 */
public interface TypeTransformer <S,T>{
    /**
     * Transforms the given value to the new type.
     * There is only one instance of this TypeTransformer. So any attribute of the class is used in multiple transformations.
     * @param value the given value to convert
     * @param parameters the parameters can contain any further hints for the transformation e.g. create OntModel with or without reasoning / in memory or in file etc. 
     * @return the transformed value
     * @throws TypeTransformationException in case the transformation did not work
     */
    T transform(S value, Properties parameters) throws TypeTransformationException;
    
    /**
     * Returns the source type as a class value.
     * It is necessary because the generics are removed after compilation. 
     * @return the source type as a class value
     */
    Class<S> getSourceType();
    
    /**
     * Returns the target type as a class value.
     * It is necessary because the generics are removed after compilation. 
     * @return the target type as a class value
     */
    Class<T> getTargetType();
    
    /**
     * Returns the transformation cost given the parameter.
     * It is usually a good idea to reduce the tranformation cost, if more parameter can be used during the conversion.
     * @param parameters the parameters can contain any further hints for the transformation e.g. create OntModel with or without reasoning / in memory or in file etc. 
     * @return a positive number which represents the transformation cost.
     */
    int getTransformationCost(Properties parameters);
}
