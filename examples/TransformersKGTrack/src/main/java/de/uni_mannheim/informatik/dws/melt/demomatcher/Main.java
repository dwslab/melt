package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceType;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.BaselineStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConceptType;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.TypeFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.TrainingAlignmentGenerator;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllStringLiterals;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.NLPTransformersFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is also the main class that will be run when executing the JAR.
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args){                
        //analyzeSupervisedLearningMatcher(0.3);
        writeConference();
    }
        
    private static void analyzeSupervisedLearningMatcher(double fraction){
        List<TestCase> testCases = new ArrayList<>();
        for(TestCase tc : TrackRepository.Knowledgegraph.V3.getTestCases().subList(1, 2)){
            testCases.add(TrackRepository.generateTestCaseWithSampledReferenceAlignment(tc, fraction, 1324567));
        }
        
        ExecutionResultSet results = Executor.run(testCases, new SupervisedMatcher());

        results.addAll(Executor.run(testCases, new BaseMatcher()));
        EvaluatorCSV e = new EvaluatorCSV(results);
        e.setBaselineMatcher(new ForwardMatcher());
        e.setResourceExplainers(Arrays.asList(new ExplainerResourceProperty(RDFS.label, SKOS.altLabel), new ExplainerResourceType()));
        e.writeToDirectory();
    }
    
    private static void writeConference(){
        List<TestCase> testCases = new ArrayList<>();
        for(TestCase tc : TrackRepository.Conference.V1.getTestCases().subList(1, 2)){
            testCases.add(TrackRepository.generateTestCaseWithSampledReferenceAlignment(tc, 0.3, 1324567));
        }
        
        Executor.run(testCases, new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                Alignment recallAlignment = new BaselineStringMatcher().match(source, target, new Alignment(), properties);

                //generate the training examples
                Alignment trainingAlignment = TrainingAlignmentGenerator.getTrainingAlignment(recallAlignment, inputAlignment);

                NLPTransformersFilter filter = new NLPTransformersFilter(new TextExtractorAllStringLiterals(), "bert-base");
                File predictionFile = filter.createPredictionFile(source, target, trainingAlignment);
                LOGGER.info("Wrote prediction file to {}", predictionFile);
                return inputAlignment;
            }
        });
    }
}
