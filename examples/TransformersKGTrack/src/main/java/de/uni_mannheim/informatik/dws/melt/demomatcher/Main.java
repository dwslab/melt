package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceType;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.BaselineStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.TrainingAlignmentGenerator;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllStringLiterals;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.NLPTransformersFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
        //CLI setup:
        Options options = new Options();

        options.addOption(Option.builder("g")
                .longOpt("gpu")
                .hasArg()
                .desc("which GPUs to use. This can be comma separated. Eg. 0,1 which uses GPU zero and one.")
                .build());
        
        options.addOption(Option.builder("t")
                .longOpt("transformerscache")
                .hasArg()
                .desc("the file path to the transformers cache.")
                .build());
        
        options.addOption(Option.builder("p")
                .longOpt("python")
                .hasArg()
                .desc("The python command to use.")
                .build());
        
        options.addOption(Option.builder("c")
                .longOpt("cache")
                .hasArg()
                .argName("path")                
                .desc("The path to the cache folder for ontologies")
                .build());
        options.addOption(Option.builder("h")
                .longOpt("help")           
                .desc("Print this help message.")
                .build());
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar ", options);
            System.exit(1);
        }
        
        if(cmd.hasOption("h")){
            formatter.printHelp("java -jar ", options);
            System.exit(1);
        }
        if(cmd.hasOption("p")){
            String p = cmd.getOptionValue("p");
            LOGGER.info("Setting python command to {}", p);
            PythonServer.setPythonCommandBackup(p);
        }
        if(cmd.hasOption("c")){
            File cacheFile = new File(cmd.getOptionValue("c"));
            LOGGER.info("Setting cache to {}", cacheFile);
            Track.setCacheFolder(cacheFile);
        }
        
        String gpu = "";
        if(cmd.hasOption("g")){
            gpu = cmd.getOptionValue("g");
        }
        
        File transformersCache = null;
        if(cmd.hasOption("t")){
            transformersCache = new File(cmd.getOptionValue("t"));
        }
        
        PythonServer.setOverridePythonFiles(false);
        
        analyzeSupervisedLearningMatcher(0.3, gpu, transformersCache);
        //writeConference();
    }
        
    private static void analyzeSupervisedLearningMatcher(double fraction, String gpu, File transformersCache){
        List<TestCase> testCases = new ArrayList<>();
        for(TestCase tc : TrackRepository.Knowledgegraph.V3.getTestCases().subList(1, 2)){
            testCases.add(TrackRepository.generateTestCaseWithSampledReferenceAlignment(tc, fraction, 1324567));
        }
        
        ExecutionResultSet results = Executor.run(testCases, new SupervisedMatcher(gpu, transformersCache));

        results.addAll(Executor.run(testCases, new BaseMatcher()));
        EvaluatorCSV e = new EvaluatorCSV(results);
        e.setBaselineMatcher(new ForwardMatcher());
        e.setResourceExplainers(Arrays.asList(new ExplainerResourceProperty(RDFS.label, SKOS.altLabel), new ExplainerResourceType()));
        e.writeToDirectory();
    }
    
    private static void writeConference() {
        List<TestCase> testCases = new ArrayList<>();
        for(TestCase tc : TrackRepository.Conference.V1.getTestCases()){
            testCases.add(TrackRepository.generateTestCaseWithSampledReferenceAlignment(tc, 0.3, 1324567));
        }
        
        Executor.run(testCases, new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                Alignment recallAlignment = new BaselineStringMatcher().match(source, target, new Alignment(), properties);

                //generate the training examples
                Alignment trainingAlignment = TrainingAlignmentGenerator.getTrainingAlignment(recallAlignment, inputAlignment);

                NLPTransformersFilter filter = new NLPTransformersFilter(new TextExtractorAllStringLiterals(), "bert-base-uncased");//"bert-base-cased-finetuned-mrpc"
                File predictionFile = filter.createPredictionFile(source, target, trainingAlignment);
                LOGGER.info("Wrote prediction file to {}", predictionFile);
                return inputAlignment;
            }
        });
    }
}
