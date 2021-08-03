package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;

/**
 * This component adds negative samples to the alignment.
 * Negatives are added as long as the share of negatives is not fullfilled.
 */
public class AddNegativesRandomlyShare extends AddNegativesRandomly{
    
    private final double shareOfNegatives;

    public AddNegativesRandomlyShare(double shareOfNegatives) {
        super(false, false);
        this.shareOfNegatives = shareOfNegatives;
    }

    public AddNegativesRandomlyShare(double shareOfNegatives, boolean homogenousDraw, boolean withRepetitions) {
        super(homogenousDraw, withRepetitions);
        this.shareOfNegatives = shareOfNegatives;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {
        
        RandomSampleOntModel sourceRandomSample = new RandomSampleOntModel(source);
        RandomSampleOntModel targetRandomSample = new RandomSampleOntModel(target);
        
        
        long negatives = Alignment.iterableSize(inputAlignment.getCorrespondencesRelation(CorrespondenceRelation.INCOMPAT));
        long positives = Alignment.iterableSize(inputAlignment.getCorrespondencesRelation(CorrespondenceRelation.EQUIVALENCE));
        boolean samplefromSource = true;
        if(this.withRepetitions){
            while(((double)negatives/(negatives + positives)) < this.shareOfNegatives){
                if(samplefromSource){
                    String entityOne = sourceRandomSample.getGlobalSampler().getRandomElement();
                    inputAlignment.add(
                        entityOne,
                        sampleResource(entityOne, source, targetRandomSample),
                        CorrespondenceRelation.INCOMPAT);
                    samplefromSource = false;
                }else{
                    String entityTwo = targetRandomSample.getGlobalSampler().getRandomElement();
                    inputAlignment.add(
                        sampleResource(entityTwo, target, sourceRandomSample),
                        entityTwo,
                        CorrespondenceRelation.INCOMPAT);
                    samplefromSource = true;
                }
                negatives++;
            }
        }else{
            Set<String> excludeSources = inputAlignment.getDistinctSourcesAsSet();
            Set<String> excludeTargets = inputAlignment.getDistinctTargetsAsSet();
            
            while(((double)negatives/(negatives + positives)) < this.shareOfNegatives){
                if(samplefromSource){
                    String entityOne = sourceRandomSample.getGlobalSampler().getRandomElement(excludeSources);
                    String entityTwo = sampleResource(entityOne, source, targetRandomSample, excludeTargets);
                    excludeSources.add(entityOne);
                    excludeTargets.add(entityTwo);
                    inputAlignment.add(
                        entityOne,
                        entityTwo,
                        CorrespondenceRelation.INCOMPAT);
                    samplefromSource = false;
                }else{
                    String entityTwo = targetRandomSample.getGlobalSampler().getRandomElement(excludeTargets);
                    String entityOne = sampleResource(entityTwo, target, sourceRandomSample, excludeSources);
                    excludeSources.add(entityOne);
                    excludeTargets.add(entityTwo);
                    inputAlignment.add(
                        entityOne,
                        entityTwo,
                        CorrespondenceRelation.INCOMPAT);
                    samplefromSource = true;
                }
                negatives++;
            }
        }
        return inputAlignment;
    }
}
