package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.Properties;

public class ObjectTransformationRoute extends TransformationRoute {
    private final Object initialObject;
    
    public ObjectTransformationRoute(TransformationRoute route, Object object) {
        super(route);
        this.initialObject = object;
    }
        
    public Object getTransformedObject() throws Exception{
        return super.getTransformedObject(this.initialObject);
    }

    public Object getTransformedObject(Properties params) throws Exception{
        return super.getTransformedObject(this.initialObject, params);
    }

    public Object getInitialObject() {
        return initialObject;
    }
}