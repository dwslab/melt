package de.uni_mannheim.informatik.dws.melt.demomatcher;

import com.googlecode.cqengine.query.QueryFactory;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConceptType;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.TypeFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.MachineLearningScikitFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

public class SupervisedMatcher extends MatcherYAAAJena{

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        
        List<MatcherYAAAJena> matchers = new ArrayList<>();
        matchers.add(new BaseMatcher());
        matchers.addAll(Main.FEATURE_GENERATORS);

        Alignment recallAlignment = new Alignment();
        for(MatcherYAAAJena y : matchers){
            recallAlignment = y.match(source, target, recallAlignment, properties);
        }
        
        //here we are just interest in instance matches.
        recallAlignment = new TypeFilter(ConceptType.INSTANCE).match(source, target, recallAlignment, properties);
        
        //generate the training examples
        Iterable<Correspondence> alternatives = recallAlignment.retrieve(QueryFactory.or(
            QueryFactory.in(Correspondence.SOURCE, inputAlignment.getDistinctSourcesAsSet()),
            QueryFactory.in(Correspondence.TARGET, inputAlignment.getDistinctTargetsAsSet())
        ));
        
        Alignment trainingAlignment = new Alignment();
        for(Correspondence c : alternatives){
            if(inputAlignment.contains(c)){
                trainingAlignment.add(
                        new Correspondence(c.getEntityOne(), c.getEntityTwo(), c.getConfidence(), CorrespondenceRelation.EQUIVALENCE, c.getExtensions())
                );
            }else{
                trainingAlignment.add(
                        new Correspondence(c.getEntityOne(), c.getEntityTwo(), c.getConfidence(), CorrespondenceRelation.INCOMPAT, c.getExtensions())
                );
            }
        }
        MachineLearningScikitFilter filter = new MachineLearningScikitFilter(trainingAlignment);
        return filter.match(source, target, recallAlignment, properties);
    }
}
