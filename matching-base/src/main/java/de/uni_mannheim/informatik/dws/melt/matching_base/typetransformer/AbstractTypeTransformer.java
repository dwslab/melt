package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.Properties;

public abstract class AbstractTypeTransformer<S,T> implements TypeTransformer<S,T>{
    protected Class<S> sourceClass;
    protected Class<T> targetClass;

    public AbstractTypeTransformer(Class<S> sourceClass, Class<T> targetClass) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }
    
    @Override
    public Class<S> getSourceType() { return sourceClass; }

    @Override
    public Class<T> getTargetType() { return targetClass; }

    @Override
    public int getTransformationCost(Properties parameters) {
        return 30; //default transformation cost of 30 (arbitrary value)
    }
}
