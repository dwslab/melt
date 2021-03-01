package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.List;
import java.util.Properties;

public class TransformationRoute<T> {
    protected Class<?> source;
    protected Class<T> target;    
    protected List<TypeTransformer<?,?>> transformations;
    protected int cost;

    public TransformationRoute(Class<?> source, Class<T> target, List<TypeTransformer<?,?>> transformations, int cost) {
        this.source = source;
        this.target = target;
        this.transformations = transformations;
        this.cost = cost;
    }

    /**
     * Copy constructor used in ObjectTransformationRoute 
     * @param route the old route
     */
    public TransformationRoute(TransformationRoute<T> route) {
        this.source = route.source;
        this.target = route.target;
        this.transformations = route.transformations;
        this.cost = route.cost;
    }
    
    public T getTransformedObject(Object object) throws TypeTransformationException{
        return getTransformedObject(object, new Properties());
    }
    
    
    @SuppressWarnings("unchecked")
    public T getTransformedObject(Object object, Properties params) throws TypeTransformationException{
        Object tmp = object;
        for(TypeTransformer transformer : transformations){
            tmp = transformer.transform(tmp, params);
        }
        return target.cast(tmp);
    }

    public List<TypeTransformer<?,?>> getTransformations() {
        return transformations;
    }

    public int getCost() {
        return cost;
    }

    public Class<?> getSource() {
        return source;
    }

    public Class<?> getTarget() {
        return target;
    }
}