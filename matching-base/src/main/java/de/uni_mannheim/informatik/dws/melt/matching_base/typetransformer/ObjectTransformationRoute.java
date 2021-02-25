package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.Properties;

public class ObjectTransformationRoute <T> extends TransformationRoute<T> {
    private final Object initialObject;
    
    public ObjectTransformationRoute(TransformationRoute<T> route, Object object) {
        super(route);
        this.initialObject = object;
    }
        
    public T getTransformedObject() throws TypeTransformationException{
        return super.getTransformedObject(this.initialObject);
    }

    public T getTransformedObject(Properties params) throws TypeTransformationException{
        return super.getTransformedObject(this.initialObject, params);
    }

    public Object getInitialObject() {
        return initialObject;
    }
}