package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceType;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance.BagOfWordsSetSimilarityFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance.CommonPropertiesFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance.SimilarHierarchyFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance.SimilarHierarchyFilterApproach;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance.SimilarNeighboursFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance.SimilarTypeFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.SetSimilarity;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

/**
 * This class is also the main class that will be run when executing the JAR.
 */
public class Main {
    private static Property abstractProp = ModelFactory.createDefaultModel().createProperty("http://dbkwik.webdatacommons.org/ontology/abstract");
    
    public static List<MatcherYAAAJena> FEATURE_GENERATORS = Arrays.asList(
            new SimilarNeighboursFilter(l -> l.getLexicalForm(), 0.0, SetSimilarity.ABSOLUTE),
            new CommonPropertiesFilter(0.0, SetSimilarity.ABSOLUTE),
            new SimilarHierarchyFilter(DCTerms.subject, SKOS.broader, new CategoryMatcher(), SimilarHierarchyFilterApproach.ABSOLUTE_MATCHES, 0.0),
            new BagOfWordsSetSimilarityFilter(abstractProp),                
            new SimilarTypeFilter()
    );
    
    public static void main(String[] args){                
        //CacheInit.populateKGTrack("E:\\tmp_tdb\\");
        //analyzeIsolatedFeatures();
        analyzeSupervisedLearningMatcher(0.2);
        //analyzeSupervisedLearningMatcher(0.4);
        //analyzeSupervisedLearningMatcher(0.6);
    }
    
    
    private static void analyzeIsolatedFeatures(){
        List<TestCase> testCases = TrackRepository.Knowledgegraph.V3.getTestCases();
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap();
        for(MatcherYAAAJena featureGenerator : FEATURE_GENERATORS){
            matchers.put(featureGenerator.getClass().getSimpleName(), new OneFeatureMatcher(featureGenerator));
        }
        matchers.put("BaseMatcher", new BaseMatcher());
        ExecutionResultSet results = Executor.run(testCases, matchers);
        EvaluatorCSV e = new EvaluatorCSV(results);
        e.setResourceExplainers(Arrays.asList(new ExplainerResourceProperty(RDFS.label, SKOS.altLabel), new ExplainerResourceType()));
        e.writeToDirectory();
    }
    
    
    private static void analyzeSupervisedLearningMatcher(double fraction){
        List<TestCase> testCases = new ArrayList();
        for(TestCase tc : TrackRepository.Knowledgegraph.V3.getTestCases()){
            testCases.add(TrackRepository.generateTestCaseWithSampledReferenceAlignment(tc, fraction, 1324567));
        }
        ExecutionResultSet results = Executor.run(testCases, new SupervisedMatcher());
        results.addAll(Executor.run(testCases, new BaseMatcher()));
        EvaluatorCSV e = new EvaluatorCSV(results);//, new ConfusionMatrixMetric(true, true));
        e.setBaselineMatcher(new ForwardMatcher());
        e.setResourceExplainers(Arrays.asList(new ExplainerResourceProperty(RDFS.label, SKOS.altLabel), new ExplainerResourceType()));
        e.writeToDirectory();
    }
    
}
