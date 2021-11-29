package de.uni_mannheim.informatik.dws.melt.examples.sentence_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorForTransformers;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorSet;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorShortAndLongTexts;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersLoss;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles pasring the options and providing useful data structures.
 */
public class CLIOptions {
    private static final Logger LOGGER = LoggerFactory.getLogger(CLIOptions.class);
    
    private static final List<TextExtractor> TEXT_EXTRACTORS = Arrays.asList(
        new TextExtractorSet(),
        new TextExtractorShortAndLongTexts(),
        new TextExtractorForTransformers()
    );
    
    private CommandLine cmd;
    private Options options;

    public CLIOptions(String[] args) {
        this.options = createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(150);
        try {
            this.cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar ", options);
            System.exit(1);
        }

        if (this.cmd.hasOption("h")) {
            formatter.printHelp("java -jar ", options);
            System.exit(1);
        }
    }
        
    private Options createOptions(){
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
                .desc("The path to the cache folder for ontologies.")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Print this help message.")
                .build());

        options.addOption(Option.builder("tm")
                .longOpt("transformermodels")
                .hasArgs()
                .valueSeparator(' ')
                .desc("The transformer models to be used, separated by space.")
                .build());

        options.addOption(Option.builder("tracks")
                .longOpt("tracks")
                .required()
                .hasArgs()
                .valueSeparator(' ')
                .desc("The tracks to be used, separated by spaces.")
                .build()
        );
        
        options.addOption(Option.builder("testcases")
                .longOpt("testcases")
                .hasArgs()
                .valueSeparator(' ')
                .desc("The testcases to use (only those which are mentioned in this option are used"
                        + " even if other tracks are provided.")
                .build()
        );

        options.addOption(Option.builder("pos")
                .longOpt("positives")
                .hasArgs()
                .valueSeparator(' ')
                .desc("How the positives should be generated. If it is a float point number, then the reference will be sampled. "
                        + "If it is a string, then it is assumed to be a class pointing to a matcher class. Example:\n" +
                        "--positives 0.1 0.2 de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.HighPrecisionMatcher")
                .build()
        );
        
        
        options.addOption(Option.builder("m")
                .longOpt("mode")
                .hasArg()
                .desc("Available modes: BASELINE, BASELINE_LIGHT, ZEROSHOT, TC_FINETUNE, TRACK_FINETUNE, " +
                        "TRACK_FINETUNE_HP, GLOBAL_FINETUNE")
                .required()
                .build()
        );

        options.addOption(Option.builder("mt")
                .longOpt("multitext")
                .hasArg()
                .desc("Require a value which can be 'true', 'false', or 'both'. "
                        + "If set to true, multiple text parts for one element will lead to multiple examples." +
                        "If set to 'both', then true and false are tried out.")
                .build()
        );

        options.addOption(Option.builder("sp")
                .longOpt("serverport")
                .hasArg()
                .desc("The port of the python server.")
                .build()
        );
        
        options.addOption(Option.builder("s")
                .longOpt("switch")
                .hasArg()
                .desc("Additionally switch the source and target for training. Can be used with and without argument: can be 'true', 'false', or 'both'.")
                .build()
        );
        
        

        // configure the extractor options here...

        StringBuilder extractorDescription = new StringBuilder();
        extractorDescription.append("The extractor to be used. Reference by number. Options:\n");
        int i = 0;
        for (TextExtractor te : TEXT_EXTRACTORS) {
            extractorDescription.append("(")
                    .append(i++)
                    .append(") ")
                    .append(te.getClass().getSimpleName())
                    .append("\n");
        }

        options.addOption(Option.builder("te")
                .longOpt("textextractor")
                .desc(extractorDescription.toString() + "Multiple values are separated by space.")
                .hasArgs()
                .valueSeparator(' ')
                .build());
        
        // configure the loss options here...
        StringBuilder lossDescription = new StringBuilder();
        lossDescription.append("The loss to be used for training. Reference by number. Options:\n");
        i = 0;
        //wil iterate over the enum values in the order they're declared
        for (SentenceTransformersLoss loss :  SentenceTransformersLoss.values()) {
            lossDescription.append("(")
                    .append(i++)
                    .append(") ")
                    .append(loss.toString())
                    .append("\n");
        }
        
        options.addOption(Option.builder("l")
                .longOpt("loss")
                .desc(lossDescription.toString() + "Multiple values are separated by space.")
                .hasArgs()
                .valueSeparator(' ')
                .build());
        
        return options;
    }
    
    public void initializeStaticCmdParameters(){
        if (cmd.hasOption("p")) {
            String p = cmd.getOptionValue("p");
            LOGGER.info("Setting python command to {}", p);
            PythonServer.setPythonCommandBackup(p);
        }
        if (cmd.hasOption("sp")) {
            String p = cmd.getOptionValue("sp");
            try {
                int port = Integer.parseInt(p);
                LOGGER.info("Setting python port to {}", port);
                PythonServer.setPort(port);
            } catch (NumberFormatException ex) {
                LOGGER.warn("Argument serverport (sp) which is set to \"{}\" is not a number.", p);
                System.exit(1);
            }
        }
        if (cmd.hasOption("c")) {
            File cacheFile = new File(cmd.getOptionValue("c"));
            LOGGER.info("Setting OAEI cache to {}", cacheFile);
            Track.setCacheFolder(cacheFile);
        }
    }
    
    /**
     * Returns the lowercased mode.
     * @return the lowercased mode.
     */
    public String getMode() {
        return this.cmd.getOptionValue("m").toLowerCase(Locale.ROOT).trim();
    }
    
    /**
     * This helper method simply parses the provided positives generators.
     * @param testCase the testcase for which the input alignment should be generated.
     * @return Float array. Will default to a value if no fractions parameter is provided.
     */
    public List<Entry<String, TestCase>> getTestCaseWithPositives(TestCase testCase) {
        
        List<Entry<String, TestCase>> result = new ArrayList<>();
        if (!cmd.hasOption("pos")) {
            LOGGER.error("It is not provided how positives examples should be generated. Please provide them space-separated via the -p option, e.g.:\n" +
                    "-pos 0.2 0.4\n" +
                    "Using default: 0.2.");
            TestCase trainingCase = TrackRepository.generateTestCaseWithSampledReferenceAlignment(
                        testCase, 0.2, 41, false);
            result.add(new SimpleEntry<>("ref0.2", trainingCase));
            return result;
        }
        
        for(String f : cmd.getOptionValues("pos")){
            float parsedValue = 0;
            try {
                parsedValue = Float.parseFloat(f);
                TestCase tc = TrackRepository.generateTestCaseWithSampledReferenceAlignment(testCase, parsedValue, 41, false);
                result.add(new SimpleEntry<>("posref" + Float.toString(parsedValue), tc));
            } catch (NumberFormatException ex) {
                try {
                    Class<?> matcherclass = Class.forName(f);
                    Object matcherInstance = null;
                    try {
                        matcherInstance = matcherclass.newInstance();
                    } catch (InstantiationException | IllegalAccessException instEx) {
                        LOGGER.error("Could not instantiate the class {}. The matcher will not be called.", f, instEx);
                        System.exit(1);
                        return null;
                    }
                    ExecutionResult r = Executor.runSingle(testCase, matcherInstance);
                    URL matcherPosURL = r.getOriginalSystemAlignment();
                    if(matcherPosURL == null){
                        LOGGER.warn("Could not run matcher class {} on testcase {}", matcherclass.getSimpleName(), testCase.getName());
                        System.exit(1);
                        return null;
                    }
                    URI positives = null;
                    try{
                        positives = matcherPosURL.toURI();
                    }catch (URISyntaxException ex1) {
                        LOGGER.warn("Could not convert URL to URI: {}", matcherPosURL, ex);
                        System.exit(1);
                        return null;
                    }
                    TestCase tc = new TestCase(
                            testCase.getName(), testCase.getSource(), testCase.getTarget(), 
                            testCase.getReference(), testCase.getTrack(), 
                            positives, testCase.getGoldStandardCompleteness(), testCase.getParameters());
                    result.add(new SimpleEntry<>("pos" + matcherclass.getSimpleName(), tc));
                } catch (ClassNotFoundException ex1) {
                    LOGGER.warn("Argument positives (-pos) which is set to \"{}\" is not a floating point number or a string pointing to a class.", f);
                    System.exit(1);
                    return null;
                }
            }
        }
        return result;
    }
    
    
    
    
    public List<TextExtractor> getTextExtractors(){
        List<TextExtractor> textExtractors = new ArrayList<>();
        if (cmd.hasOption("te")) {
            for(String teOption : cmd.getOptionValues("te")){
                int parseTeOption = 0;
                try {
                    parseTeOption = Integer.parseInt(teOption);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Argument textextractor (-te) which is set to \"{}\" is not a number.", teOption);
                    System.exit(1);
                    return null;
                }
                //range check
                if(parseTeOption < 0 || parseTeOption >= TEXT_EXTRACTORS.size()){
                    LOGGER.warn("Argument textextractor (-te) which is set to \"{}\" is not in the range 0-{}.", teOption, TEXT_EXTRACTORS.size()-1);
                    System.exit(1);
                    return null;
                }
                
                try {
                    TextExtractor textExtractor = TEXT_EXTRACTORS.get(parseTeOption);
                    LOGGER.info("Using text extractor: " + textExtractor.getClass().getSimpleName());
                    textExtractors.add(textExtractor);
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Argument textextractor (-te) is set to {} but the index does not correspong to a text extractor.",teOption);
                    System.exit(1);
                    return null;
                }
            }
        }
        if(textExtractors.isEmpty()){
            TextExtractor textExtractor = TEXT_EXTRACTORS.get(0);
            LOGGER.info("Text extractor (Option -te) not specified. Using default: " +
                    textExtractor.getClass().getSimpleName());
            textExtractors.add(textExtractor);
        }
        return textExtractors;
    }
    
    public List<SentenceTransformersLoss> getLoss(){
        List<SentenceTransformersLoss> losses = new ArrayList<>();
        if (cmd.hasOption("l")) {
            for(String lOption : cmd.getOptionValues("l")){
                int parselOption = 0;
                try {
                    parselOption = Integer.parseInt(lOption);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Argument loss (-l) which is set to \"{}\" is not a number.", parselOption);
                    System.exit(1);
                    return null;
                }
                //range check
                if(parselOption < 0 || parselOption >= SentenceTransformersLoss.values().length){
                    LOGGER.warn("Argument loss (-l) which is set to \"{}\" is not in the range 0-{}.", lOption, SentenceTransformersLoss.values().length - 1);
                    System.exit(1);
                    return null;
                }
                
                try {
                    SentenceTransformersLoss loss = SentenceTransformersLoss.values()[parselOption];
                    LOGGER.info("Using loss: " + loss.toString());
                    losses.add(loss);
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Argument loss (-l) is set to {} but the index does not correspong to a loss.",lOption);
                    System.exit(1);
                    return null;
                }
            }
        }
        if(losses.isEmpty()){
            SentenceTransformersLoss loss = SentenceTransformersLoss.CosineSimilarityLoss;
            LOGGER.info("Loss (Option -l) not specified. Using default: " +
                    loss.toString());
            losses.add(loss);
        }
        return losses;
    }
    
    private List<Track> getTracks(){
        List<Track> tracks = new ArrayList<>();        
        if (cmd.hasOption("tracks")) {
            for (String trackString : cmd.getOptionValues("tracks")) {
                trackString = trackString.toLowerCase(Locale.ROOT).trim();
                switch (trackString) {
                    case "conference":
                        tracks.add(TrackRepository.Conference.V1);
                        break;
                    case "anatomy":
                        tracks.add(TrackRepository.Anatomy.Default);
                        break;
                    case "kg":
                    case "knowledge-graphs":
                    case "knowledgegraphs":
                    case "knowledgegraph":
                        tracks.add(TrackRepository.Knowledgegraph.V4);
                        break;
                    default:
                        LOGGER.warn("Could not map track: " + trackString);
                        System.exit(1);
                }
            }
        }
        if (tracks.isEmpty()) {
            LOGGER.warn("No tracks specified. ABORTING program.");
            System.exit(1);
        }        
        return tracks;
    }
    
    public List<TestCase> getTestCases(){
        List<TestCase> testcases = new ArrayList<>();
        for(Track track : getTracks()){
            testcases.addAll(track.getTestCases());
        }
        if (testcases.isEmpty()) {
            LOGGER.warn("No testcase can be retrived for all specified tracks. ABORTING program.");
            System.exit(1);
        }
        if(this.cmd.hasOption("testcases")){
            List<TestCase> selectedTestcases = new ArrayList<>();
            for(String tc : this.cmd.getOptionValues("testcases")){
                TestCase testcase = getTestCaseFromList(testcases, tc);
                if(testcase == null){
                    LOGGER.warn("Did not find test case specified by \"{}\". Skipping this testcase.", tc);
                }else{
                    selectedTestcases.add(testcase);
                }                
            }
            if (selectedTestcases.isEmpty()) {
                LOGGER.warn("No testcase can be retrived for all specified tracks and testcase. ABORTING program.");
                System.exit(1);
            }
            return selectedTestcases;
        }else{
            return testcases;
        }
        
    }
    
    private TestCase getTestCaseFromList(List<TestCase> testcases, String name){
        for(TestCase tc : testcases){
            if(tc.getName().equals(name)){
                return tc;
            }
        }
        return null;
    }
    
    public List<String> getTransformerModels(){
        String[] transformerModels = cmd.getOptionValues("tm");
        if (transformerModels == null) {
            LOGGER.warn("No transformer model specified. ABORTING program.");
            System.exit(1);
            return new ArrayList<>();
        }
        List<String> models = new ArrayList<>(transformerModels.length);
        for(String m : transformerModels){
            String processed = m.trim();
            if(processed.length() > 0){
                models.add(processed);
            }
        }
        if(models.isEmpty()){
            LOGGER.warn("No transformer model specified. ABORTING program.");
            System.exit(1);
            return new ArrayList<>();
        }
        return models;
    }
    
    public List<Boolean> getMultiText(){
        return getListBoolean("mt");
    }
    
    public List<Boolean> getAdditionallySwitchSourceTarget(){
        return getListBoolean("s");
    }
    
    private List<Boolean> getListBoolean(String option){
        if (!cmd.hasOption(option)) {
            //use it as a switch -> is multitext is not provided, then set it to false
            return Arrays.asList(false);
        }
        String value = cmd.getOptionValue(option);
        if(value == null){
            //use it as a switch -> then set it to true if available.
            return Arrays.asList(true);
        }
        value = value.trim().toLowerCase(Locale.ENGLISH);
        if(value.equals("true")){
            return Arrays.asList(true);
        }else if(value.equals("false")){
            return Arrays.asList(false);
        }else if(value.equals("both")){
            return Arrays.asList(true, false);
        }else{
            LOGGER.warn("Unkown value for {} was provided: {}. ABORTING program.", option, value);
            System.exit(1);
            return null;
        }
    }
    
    
    public String getGPU(){
        return cmd.getOptionValue("g", "");
    }
    
    public File getTransformersCache(){
        File transformersCache = null;
        if (cmd.hasOption("tc")) {
            transformersCache = new File(cmd.getOptionValue("tc"));
        }
        return transformersCache;
    }
    
    
    public File getTargetDirectoryForModels(){
        File targetDirForModels = new File("./models");
        if (!targetDirForModels.exists()) {
            targetDirForModels.mkdirs();
        }
        return targetDirForModels;
    }
}
