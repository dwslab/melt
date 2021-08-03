package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives;

import de.uni_mannheim.informatik.dws.melt.matching_base.AddNegatives;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConceptType;
import java.util.Set;
import org.apache.jena.ontology.OntModel;

/**
 * Abstract class which is the base class for all {@link AddNegatives} which are based on random sampling.
 */
public abstract class AddNegativesRandomly extends MatcherYAAAJena implements AddNegatives {


    /**
     * If true, then all negatives are of the same type e.g. class, property, instance.
     */
    protected boolean homogenousDraw;
    
    /**
     * If true, then it will samples random elements which can also appear multiple times in different correspondences.
     * If false, then all sampled entities for e.g. the target ontology will not contain
     * any entities which are already used in the alignment or are randomly sampled before.
     */
    protected boolean withRepetitions;

    public AddNegativesRandomly(boolean homogenousDraw, boolean withRepetitions) {
        this.homogenousDraw = homogenousDraw;
        this.withRepetitions = withRepetitions;
    }

    /**
     * Returns the correct sampler for the given resource contained in given model.
     * In case homogenousDraw is set to true, it will sample from same same entity e.g. sample a class for a class.
     * @param resource the resource to check which concept type should be sampled
     * @param m the model corresponding to resource
     * @param sampleModel the RandomSampleOntModel.
     * @return the random sampler.
     */
    protected RandomSampleSet<String> getSampler(String resource, OntModel m, RandomSampleOntModel sampleModel){
        if(homogenousDraw){
            return sampleModel.getSampler(ConceptType.analyzeWithJena(m, resource));
        }else{
            return sampleModel.getGlobalSampler();
        }
    }
    
    /**
     * Sample a resource from {@code sampleModel} given the resource in model m.
     * In case homogenousDraw is set to true, it will sample from same same entity e.g. sample a class for a class.
     * @param resource the resource to check which concept type should be sampled
     * @param m the model corresponding to resource
     * @param sampleModel the model to sample from.
     * @return the random element.
     */
    protected String sampleResource(String resource, OntModel m, RandomSampleOntModel sampleModel){
        if(homogenousDraw){
            return sampleModel.getSampler(ConceptType.analyzeWithJena(m, resource)).getRandomElement();
        }else{
            return sampleModel.getGlobalSampler().getRandomElement();
        }
    }

    /**
     * Sample a resource from {@code sampleModel} given the resource in model m.
     * In case homogenousDraw is set to true, it will sample from same same entity e.g. sample a class for a class.
     * It will exclude 
     * @param resource the resource to check which concept type should be sampled
     * @param m the model corresponding to resource
     * @param sampleModel the model to sample from.
     * @param excludes the set contains elements which should not be chosen as a random sample.
     * @return the random element.
     */
    protected String sampleResource(String resource, OntModel m, RandomSampleOntModel sampleModel, Set<String> excludes){
        if(homogenousDraw){
            return sampleModel.getSampler(ConceptType.analyzeWithJena(m, resource)).getRandomElement(excludes);
        }else{
            return sampleModel.getGlobalSampler().getRandomElement(excludes);
        }
    }

    /**
     * If homogenousDraw is true, then all negatives are of the same type e.g. class, property, instance.
     * @return true, if all negatives are of the same type e.g. class, property, instance.
     */
    public boolean isHomogenousDraw() {
        return homogenousDraw;
    }

    /**
     * Sets the homogenousDraw value. If true, all negatives are of the same type e.g. class, property, instance.
     * @param homogenousDraw true, if all negatives are of the same type e.g. class, property, instance.
     */
    public void setHomogenousDraw(boolean homogenousDraw) {
        this.homogenousDraw = homogenousDraw;
    }

    /**
     * If true, then it will samples random elements which can also appear multiple times in different correspondences.
     * If false, then all sampled entities for e.g. the target ontology will not contain
     * any entities which are already used in the alignment or are randomly sampled before.
     * @return true, if with repetitions
     */
    public boolean isWithRepetitions() {
        return withRepetitions;
    }

    /**
     * If set to true, then it will samples random elements which can also appear multiple times in different correspondences.
     * If false, then all sampled entities for e.g. the target ontology will not contain
     * any entities which are already used in the alignment or are randomly sampled before.
     * @param withRepetitions true, if with repetitions
     */
    public void setWithRepetitions(boolean withRepetitions) {
        this.withRepetitions = withRepetitions;
    }
}