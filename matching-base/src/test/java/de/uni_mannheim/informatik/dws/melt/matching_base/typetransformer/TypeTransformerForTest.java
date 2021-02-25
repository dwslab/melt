package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.Properties;

/**
 * Simple type transformer for the tests.
 * @param <S> source type
 * @param <T> target type
 */
public class TypeTransformerForTest <S,T> extends AbstractTypeTransformer<S,T>{
    private final int weight;
    public TypeTransformerForTest(Class<S> sourceClass, Class<T> targetClass) {
        super(sourceClass, targetClass);
        this.weight = 30;
    }
    
    public TypeTransformerForTest(Class<S> sourceClass, Class<T> targetClass, int weight) {
        super(sourceClass, targetClass);
        this.weight = weight;
    }

    @Override
    public T transform(S value, Properties parameters) throws TypeTransformationException {
        if(this.sourceClass.isInstance(value)){
            try {
                return targetClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new TypeTransformationException("Could not transform", ex);
            }
        }
        return null; //OR throw new exception
    }
    
    @Override
    public int getTransformationCost(Properties parameters) {
        return weight;
    }
}