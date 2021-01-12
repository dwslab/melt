package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.List;
import java.util.Properties;

public class TransformationRoute {
    protected Class<?> source;
    protected Class<?> target;    
    protected List<TypeTransformer> transformations;
    protected int cost;

    public TransformationRoute(Class<?> source, Class<?> target, List<TypeTransformer> transformations, int cost) {
        this.source = source;
        this.target = target;
        this.transformations = transformations;
        this.cost = cost;
    }

    /**
     * Copy constructor used in ObjectTransformationRoute 
     * @param route the old route
     */
    public TransformationRoute(TransformationRoute route) {
        this.source = route.source;
        this.target = route.target;
        this.transformations = route.transformations;
        this.cost = route.cost;
    }
    
    public Object getTransformedObject(Object object) throws Exception{
        return getTransformedObject(object, new Properties());
    }

    public Object getTransformedObject(Object object, Properties params) throws Exception{
        Object tmp = object;
        for(TypeTransformer transformer : transformations){
            tmp = transformer.transform(tmp, params);
        }
        return tmp;
    }

    public List<TypeTransformer> getTransformations() {
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