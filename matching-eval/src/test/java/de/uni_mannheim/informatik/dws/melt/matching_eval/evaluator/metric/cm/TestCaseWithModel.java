package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

public class TestCaseWithModel extends TestCase {
    private OntModel sourceModel;
    private OntModel targetModel;
    private Alignment referenceAlignment;
    public TestCaseWithModel(String name, OntModel source, OntModel target, Alignment reference, Track track, GoldStandardCompleteness goldStandardCompleteness) {
        super(name, null, null, null, track, null, goldStandardCompleteness, null, null);
        this.sourceModel = source;
        this.targetModel = target;
        this.referenceAlignment = reference;
    }
    
    @Override
    public <T> T getSourceOntology(Class<T> clazz){
        return getSourceOntology(clazz, null);
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSourceOntology(Class<T> clazz, Properties parameters){
        if(clazz.equals(OntModel.class)){
            return (T) sourceModel;
        }else{
            throw new IllegalArgumentException("Wrong ontology type");
        }
    }
    
    
    @Override
    public <T> T getTargetOntology(Class<T> clazz){
        return getTargetOntology(clazz, null);
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getTargetOntology(Class<T> clazz, Properties parameters){
        if(clazz.equals(OntModel.class)){
            return (T) targetModel;
        }else{
            throw new IllegalArgumentException("Wrong ontology type");
        }
    }
    
    @Override
    public Alignment getParsedReferenceAlignment() {
        return referenceAlignment;
    }
    
}
