package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;

/**
 * This component adds negative samples to the alignment.
 * The number of negative samples is defined by an absolute number.
 */
public class AddNegativesRandomlyAbsolute extends AddNegativesRandomly {


    private final int numberOfNegatives;

    /**
     * This component adds {@code numberOfNegatives} negative samples to the alignment
     * @param numberOfNegatives the absolute number of negatives to add.
     */
    public AddNegativesRandomlyAbsolute(int numberOfNegatives) {
        super(false, false);
        this.numberOfNegatives = numberOfNegatives;
    }

    public AddNegativesRandomlyAbsolute(int numberOfNegatives, boolean homogenousDraw, boolean withRepetitions) {
        super(homogenousDraw, withRepetitions);
        this.numberOfNegatives = numberOfNegatives;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {
        
        RandomSampleOntModel sourceRandomSample = new RandomSampleOntModel(source);
        RandomSampleOntModel targetRandomSample = new RandomSampleOntModel(target);
        
        int left = (int)Math.ceil(this.numberOfNegatives/(double)2);
        int right = (int)Math.floor(this.numberOfNegatives/(double)2);
        if(this.withRepetitions){            
            for(String entityOne : sourceRandomSample.getGlobalSampler().getRandomElementsWithRepetition(left)){
                inputAlignment.add(
                        entityOne, 
                        sampleResource(entityOne, source, targetRandomSample),
                        CorrespondenceRelation.INCOMPAT);
            }
            
            for(String entityTwo : targetRandomSample.getGlobalSampler().getRandomElementsWithRepetition(right)){
                inputAlignment.add(
                        sampleResource(entityTwo, target, sourceRandomSample),
                        entityTwo,
                        CorrespondenceRelation.INCOMPAT);
            }
        }else{
            Set<String> excludeSources = inputAlignment.getDistinctSourcesAsSet();
            Set<String> excludeTargets = inputAlignment.getDistinctTargetsAsSet();
            //TODO: improve because the random sampled elements are not excluded from being sampled once again (only elements from the alignment are excluded).
            
            for(String entityOne : sourceRandomSample.getGlobalSampler().getRandomElementsWithoutRepetition(left, excludeSources)){
                inputAlignment.add(
                        entityOne, 
                        sampleResource(entityOne, source, targetRandomSample, excludeTargets),
                        CorrespondenceRelation.INCOMPAT);
            }
            
            for(String entityTwo : targetRandomSample.getGlobalSampler().getRandomElementsWithoutRepetition(right, excludeTargets)){
                inputAlignment.add(
                        sampleResource(entityTwo, target, sourceRandomSample, excludeSources),
                        entityTwo,
                        CorrespondenceRelation.INCOMPAT);
            }
        }
        
        
        /*
        if(this.homogenousDraw){
            int left = (int)Math.ceil(this.numberOfNegatives/(double)2);
            int right = (int)Math.floor(this.numberOfNegatives/(double)2);
            
            if(this.withRepetitions){
                for(String leftElement : sourceRandomSample.getGlobalSampler().getRandomElementsWithRepetition(left)){
                    inputAlignment.add(
                        leftElement,
                        this.getSampler(leftElement, source, targetRandomSample).getRandomElement(),
                        CorrespondenceRelation.INCOMPAT);
                }

                for(String rightElement : targetRandomSample.getGlobalSampler().getRandomElementsWithRepetition(right)){
                    inputAlignment.add(
                        this.getSampler(rightElement, target, sourceRandomSample).getRandomElement(),
                        rightElement,
                        CorrespondenceRelation.INCOMPAT);
                }
            }else{
                //TODO: improve because the random sampled elements are not excluded from being sampled once again.
                for(String leftElement : sourceRandomSample.getGlobalSampler().getRandomElementsWithoutRepetition(left, inputAlignment.getDistinctSourcesAsSet())){
                    inputAlignment.add(
                        leftElement,
                        this.getSampler(leftElement, source, targetRandomSample).getRandomElement(),
                        CorrespondenceRelation.INCOMPAT);
                }
                for(String rightElement : targetRandomSample.getGlobalSampler().getRandomElementsWithoutRepetition(right, inputAlignment.getDistinctTargetsAsSet())){
                    inputAlignment.add(
                        this.getSampler(rightElement, target, sourceRandomSample).getRandomElement(),
                        rightElement,
                        CorrespondenceRelation.INCOMPAT);
                }
            }
        }else{            
            if(this.withRepetitions){
                for(int i = 0; i < this.numberOfNegatives; i++){
                    inputAlignment.add(
                            sourceRandomSample.getRandomElement(),
                            targetRandomSample.getRandomElement(),
                            CorrespondenceRelation.INCOMPAT);
                }
            }else{
                List<String> sourceElements = sourceRandomSample.getGlobalSampler()
                        .getRandomElementsWithoutRepetition(this.numberOfNegatives, inputAlignment.getDistinctSourcesAsSet());
                List<String> targetElements = targetRandomSample.getGlobalSampler()
                        .getRandomElementsWithoutRepetition(this.numberOfNegatives, inputAlignment.getDistinctTargetsAsSet());
                if(sourceElements.size() != targetElements.size()){
                    throw new IllegalArgumentException("Random elements of source and target are not of the same size.");
                }
                for(int i=0; i < sourceElements.size(); i++){
                    inputAlignment.add(
                            sourceElements.get(i),
                            targetElements.get(i),
                            CorrespondenceRelation.INCOMPAT);
                }
            }
        }
        */
        return inputAlignment;
    }
}
