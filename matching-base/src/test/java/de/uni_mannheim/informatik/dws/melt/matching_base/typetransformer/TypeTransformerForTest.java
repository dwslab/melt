package de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer;

import java.util.Properties;

/**
 * Simple type transformer for the tests.
 */
public class TypeTransformerForTest extends AbstractTypeTransformer{
    private int weight;
    public TypeTransformerForTest(Class sourceClass, Class targetClass) {
        super(sourceClass, targetClass);
        this.weight = 30;
    }
    
    public TypeTransformerForTest(Class sourceClass, Class targetClass, int weight) {
        super(sourceClass, targetClass);
        this.weight = weight;
    }

    @Override
    public Object transform(Object value, Properties parameters) throws Exception {
        if(this.sourceClass.isInstance(value)){
            return targetClass.newInstance();
        }
        return null; //OR throw new exception
    }
    
    @Override
    public int getTransformationCost(Properties parameters) {
        return weight;
    }
}