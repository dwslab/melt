package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;

/**
 * This component adds negative samples to the alignment.
 * For each positive correspondences a configurable number of negative correspondences are added.
 */
public class AddNegativesRandomlyOneOneAssumption extends AddNegativesRandomly {


    private final int numberOfNegativesPerPositiveCorrespondence;

    public AddNegativesRandomlyOneOneAssumption(int numberOfNegativesPerPositiveCorrespondence) {
        super(false, false);
        this.numberOfNegativesPerPositiveCorrespondence = numberOfNegativesPerPositiveCorrespondence;
    }

    public AddNegativesRandomlyOneOneAssumption(int numberOfNegativesPerPositiveCorrespondence, boolean homogenousDraw, boolean withRepetitions) {
        super(homogenousDraw, withRepetitions);
        this.numberOfNegativesPerPositiveCorrespondence = numberOfNegativesPerPositiveCorrespondence;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {
        
        RandomSampleOntModel sourceRandomSample = new RandomSampleOntModel(source);
        RandomSampleOntModel targetRandomSample = new RandomSampleOntModel(target);
        
        if(this.withRepetitions){
            for(Correspondence correspondence : inputAlignment.getCorrespondencesRelation(CorrespondenceRelation.EQUIVALENCE)){
                for(int i = 0; i < numberOfNegativesPerPositiveCorrespondence; i++){
                    inputAlignment.add(
                            correspondence.getEntityOne(), 
                            sampleResource(correspondence.getEntityOne(), source, targetRandomSample),
                            CorrespondenceRelation.INCOMPAT);
                    inputAlignment.add(
                            sampleResource(correspondence.getEntityTwo(), target, sourceRandomSample),
                            correspondence.getEntityTwo(),
                            CorrespondenceRelation.INCOMPAT);
                }
            }
        }else{
            Set<String> sourceExclude = inputAlignment.getDistinctSourcesAsSet();
            Set<String> targetExclude = inputAlignment.getDistinctTargetsAsSet();
            for(Correspondence correspondence : inputAlignment.getCorrespondencesRelation(CorrespondenceRelation.EQUIVALENCE)){
                for(int i = 0; i < numberOfNegativesPerPositiveCorrespondence; i++){
                    String entityOne = sampleResource(correspondence.getEntityTwo(), target, sourceRandomSample);
                    String entityTwo = sampleResource(correspondence.getEntityOne(), source, targetRandomSample);
                    sourceExclude.add(entityOne);
                    targetExclude.add(entityTwo);
                    inputAlignment.add(
                            correspondence.getEntityOne(), 
                            entityTwo,
                            CorrespondenceRelation.INCOMPAT);
                    inputAlignment.add(
                            entityOne,
                            correspondence.getEntityTwo(),
                            CorrespondenceRelation.INCOMPAT);
                }
            }
        }
        
        return inputAlignment;
    }
}
