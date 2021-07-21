package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher.RecallMatcherAnatomy;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.MaxWeightBipartiteExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors.LiteralExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors.LiteralExtractorFallback;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalExtractors.LiteralExtractorUrlFragment;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorFallback;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorUrlFragment;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is also the main class that will be run when executing the JAR.
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) throws Exception{
        //CLI setup:
        Options options = new Options();

        options.addOption(Option.builder("g")
                .longOpt("gpu")
                .hasArg()
                .desc("Which GPUs to use. This can be comma separated. Eg. 0,1 which uses GPU zero and one.")
                .build());
        
        options.addOption(Option.builder("tc")
                .longOpt("transformerscache")
                .hasArg()
                .desc("The file path to the transformers cache.")
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

        options.addOption(Option.builder("tm")
                .longOpt("transformermodel")
                .hasArg()
                .desc("The transformer model to be used.")
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
            Track.setCacheFolder(cacheFile);
        }
        
        String gpu = cmd.getOptionValue("g", "");
        File transformersCache = null;
        if(cmd.hasOption("tc")){
            transformersCache = new File(cmd.getOptionValue("tc"));
        }

        // "bert-base-cased-finetuned-mrpc"
        String transformerModel = cmd.getOptionValue("tm", "paraphrase-TinyBERT-L6-v2");
        
        anatomy(gpu, transformerModel, transformersCache);
    }
    
    static void anatomy(String gpu, String transformerModel, File transformersCache) throws Exception{
        ExecutionResultSet ers = Executor.run(TrackRepository.Anatomy.Default, new AnatomyMatchingPipeline(gpu,
                transformerModel, transformersCache));
        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }
    
}
