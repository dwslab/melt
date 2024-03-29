package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives;

import com.googlecode.cqengine.query.QueryFactory;
import de.uni_mannheim.informatik.dws.melt.matching_base.AddNegatives;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * This component adds negative correspondences to the input alignment via an alignment (generated by a recall optimized matcher).
 * This matcher is only suitable if the whole matching system is executed only for one test case.
 * Why? Because the alignment (which is test case specific) need to be passed in the constructor.
 * Thus it will only give useful results if only used for the correct test case.
 * 
 * The input alignment should contain positive correspondences.
 * The negatives are generated by looking at the provided alignment in the constructor.
 * All correspondences which are contained in the input alignment (ground truth) and in the given alignment
 * (which has a high recall - meaning many correspondences) are assumned to be positives and all
 * others correspondences which maps a concept of the positive correspondence  to another concept are assumed to be negatives.
 * 
 * The returned alignment consists of positives (= relation) and negatives (% incompat relation) which can be used to 
 * train supervised matchers. 
 */
public class AddNegativesViaAlignment extends MatcherYAAAJena implements AddNegatives {

    private Alignment recallAlignment;
    
    /**
     * Constructor with requires the recall alignment.
     * @param recallAlignment the recall alignment should contain many correpondences (high recall).
     */
    public AddNegativesViaAlignment(Alignment recallAlignment){
        this.recallAlignment = recallAlignment;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {
        return addNegatives(recallAlignment, inputAlignment);
    }

    public Alignment getRecallAlignment() {
        return recallAlignment;
    }

    public void setRecallAlignment(Alignment recallAlignment) {
        this.recallAlignment = recallAlignment;
    }
    
    /**
     * This method returns a training alignment based on a recall alignment and a reference alignment.
     * The training alignment is generated by getting all correspondences in the recall alignment where at least one
     * part is also contained in the reference alignment.
     * If the correspondence is directly in the reference alignment, then it is assumed to be a positive example.
     * If only one part of the correspondence is in the reference alignment, then is is assumed to be a negative
     * correspondence.
     * The result contains the correspondences from the recall alignment, where positive examples have the equivalence
     * relation and negative examples have INCOMPAT relation.
     * @param recallAlignment recall alignment
     * @param referenceAlignment reference alignment which does not need to really be the reference alignment of a track.
     * @return the correspondences from the recall alignment, where positive examples have the equivalence relation and negative examples have INCOMPAT relation
     */
    public static Alignment addNegatives(Alignment recallAlignment, Alignment referenceAlignment){
        
        //generate the training examples
        Iterable<Correspondence> alternatives = recallAlignment.retrieve(
                QueryFactory.and(
                    QueryFactory.or(
                        QueryFactory.in(Correspondence.SOURCE, referenceAlignment.getDistinctSourcesAsSet()),
                        QueryFactory.in(Correspondence.TARGET, referenceAlignment.getDistinctTargetsAsSet())
                    ),
                    QueryFactory.equal(Correspondence.RELATION, CorrespondenceRelation.EQUIVALENCE)
                )
        );
        
        Alignment trainingAlignment = new Alignment();
        for(Correspondence c : alternatives) {
            if(referenceAlignment.contains(c)) {
                trainingAlignment.add(
                        new Correspondence(c.getEntityOne(), c.getEntityTwo(), c.getConfidence(), CorrespondenceRelation.EQUIVALENCE, c.getExtensions())
                );
            } else {
                trainingAlignment.add(
                        new Correspondence(c.getEntityOne(), c.getEntityTwo(), c.getConfidence(), CorrespondenceRelation.INCOMPAT, c.getExtensions())
                );
            }
        }
        return trainingAlignment;
    }
}
