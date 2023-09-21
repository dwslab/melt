package de.uni_mannheim.informatik.dws.melt.examples.llm_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackNameLookup;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorForTransformers;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorLabelAndDirectSuperclass;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorOnlyLabel;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorResourceDescriptionInRDF;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorSet;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorShortAndLongTexts;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorVerbalizedRDF;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.TextExtractorMapSet;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles pasring the options and providing useful data structures.
 */
public class CLIOptions {
    private static final Logger LOGGER = LoggerFactory.getLogger(CLIOptions.class);
    
    public static final List<String> PREDEFINED_PROMPTS = createPredefinedPrompts();
    private static List<String> createPredefinedPrompts(){
        List<String> prompts = new ArrayList<>();
        
        /********************
         * zero shot
         ********************/        
        //0
        prompts.add("Classify if the following two concepts are the same.\n### First concept:\n{left}\n### Second concept:\n{right}\n### Answer:\n");
        //1 - adding more context for anatomy
        prompts.add("Classify if two concepts refer to the same real word entiy. This is an ontology matching task between the anatomy of human and mouse. \n"
                + "First concept: {left}\n"
                + "Second concept: {right}\n"
                + "Answer:");
        //2 - very simple
        prompts.add("Is {left} and {right} the same? The answer which can be yes or no is ");
        //3 - more context in general
        prompts.add("The task is ontology matching. Given two concepts, the task is to classify if they are the same or not.\n "
                + "The first concept is: {left}\n"
                + "The second concept is: {right}\n"
                + "The answer which can be yes or no is:");
        //4 - another test
        prompts.add("Given two concepts decide if they match or not.\n"
                + "First concept: {left}\n"
                + "Second concept: {right}\n"
                + "Answer(yes or no):");
        
        
        /********************
         * Few shot
         ********************/
        // 5 - 2 shot
        prompts.add("### Concept one: endocrine pancreas secretion ### Concept two: Pancreatic Endocrine Secretion ### Answer: yes\n"
                + "### Concept one: urinary bladder urothelium ### Concept two: Transitional Epithelium ### Answer: no\n"
                + "### Concept one: {left} ### Concept two: {right} ### Answer: ");
        
        // 6 - 6 shot        
        prompts.add("### Concept one: endocrine pancreas secretion ### Concept two: Pancreatic Endocrine Secretion ### Answer: yes\n"
                + "### Concept one: urinary bladder urothelium ### Concept two: Transitional Epithelium ### Answer: no\n"
                + "### Concept one: trigeminal V nerve ophthalmic division ### Concept two: Ophthalmic Nerve ### Answer: yes\n"
                + "### Concept one: foot digit 1 phalanx ### Concept two: Foot Digit 2 Phalanx ### Answer: no\n"
                + "### Concept one: large intestine ### Concept two: Colon ### Answer: no\n"
                + "### Concept one: ocular refractive media ### Concept two: Refractile Media ### Answer: yes\n"
                + "### Concept one: {left} ### Concept two: {right} ### Answer: ");
        
        
        // 7 - 6 shot with   
        prompts.add("Classify if two descriptions refer to the same real world entity (ontology matching).\n" 
                + "### Concept one: endocrine pancreas secretion ### Concept two: Pancreatic Endocrine Secretion ### Answer: yes\n"
                + "### Concept one: urinary bladder urothelium ### Concept two: Transitional Epithelium ### Answer: no\n"
                + "### Concept one: trigeminal V nerve ophthalmic division ### Concept two: Ophthalmic Nerve ### Answer: yes\n"
                + "### Concept one: foot digit 1 phalanx ### Concept two: Foot Digit 2 Phalanx ### Answer: no\n"
                + "### Concept one: large intestine ### Concept two: Colon ### Answer: no\n"
                + "### Concept one: ocular refractive media ### Concept two: Refractile Media ### Answer: yes\n"
                + "### Concept one: {left} ### Concept two: {right} ### Answer: ");
        
        // 8 - zero shot chain of thought
        prompts.add("Classify if two descriptions refer to the same real world entity (ontology matching).\n"
                + "First concept: {left}\n"
                + "Second concept: {right}\n"
                + "Answer can be yes or no. Let's think step by step.\n");
        
        // 9 - few shot chain of thought
        prompts.add("Classify if two descriptions refer to the same real world entity (ontology matching).\n"
                + "### Concept one: endocrine pancreas secretion ### Concept two: Pancreatic Endocrine Secretion ### Explanation: Both describe the process of the pancreas releasing hormones into the bloodstream ### Answer: yes\n"
                + "### Concept one: foot digit 1 phalanx ### Concept two: Foot Digit 2 Phalanx ### Explanation: The concepts refer to different bones in the toes of the foot ### Answer: no\n"
                + "### Concept one: {left} ### Concept two: {right} ### Explanation: "
        );
        
        //10 - which is number 7 plus rdf info
        prompts.add("Classify if two descriptions (given as RDF) refer to the same real world entity (ontology matching).\n" 
                + "### Concept one: endocrine pancreas secretion ### Concept two: Pancreatic Endocrine Secretion ### Answer: yes\n"
                + "### Concept one: urinary bladder urothelium ### Concept two: Transitional Epithelium ### Answer: no\n"
                + "### Concept one: trigeminal V nerve ophthalmic division ### Concept two: Ophthalmic Nerve ### Answer: yes\n"
                + "### Concept one: foot digit 1 phalanx ### Concept two: Foot Digit 2 Phalanx ### Answer: no\n"
                + "### Concept one: large intestine ### Concept two: Colon ### Answer: no\n"
                + "### Concept one: ocular refractive media ### Concept two: Refractile Media ### Answer: yes\n"
                + "### Concept one: {left} ### Concept two: {right} ### Answer: ");
        
        
        /********************************
         * used for chooser
         * {left} is the single entity and {right} is the text which contains all possible entities by number
         ******************************/
        
        //zero shot 
        
        //11 - 
        prompts.add("The task is ontology matching (find the description which refer to the same real world entity). "
                + "Which of the following descriptions fits best to this description: {left}?\n" 
                + "{right}"
                + "Answer with the corresponding letter or \"none\" if no description fits. Answer: ");
        
        //few shot
        //12
        prompts.add("The task is ontology matching and to find the description which refer to the same real world entity. "
                + "Which of the following descriptions fits best to this description: endocrine pancreas secretion?\n" 
                + "\t a) Islet of Langerhans\n"
                + "\t b) Pancreatic Secretion\n"
                + "\t c) Pancreatic Endocrine Secretion\n"
                + "\t d) Delta Cell of the Pancreas\n"
                + "Answer with the corresponding letter or \"none\" if no description fits. Answer: c\n"
                + "Which of the following descriptions fits best to this description: {left}?\n"
                + "{right}\n"
                + "Answer with the corresponding letter or \"none\" if no description fits. Answer:" );
        return prompts;
    }
    
    
    
    private static final List<Entry<String, TextExtractorMap>> TEXT_EXTRACTORS = createTextExtractors();
    private static List<Entry<String, TextExtractorMap>> createTextExtractors(){
        List<Entry<String, TextExtractorMap>> extractors = new ArrayList<>();
        extractors.add(new SimpleEntry<>("TE0MapSet", new TextExtractorMapSet())); // 0
        extractors.add(new SimpleEntry<>("TE1Set", wrap(true, new TextExtractorSet()))); // 1
        extractors.add(new SimpleEntry<>("TE2ShortAndLongTexts", wrap(true, new TextExtractorShortAndLongTexts()))); // 2
        extractors.add(new SimpleEntry<>("TE3ForTransformers", wrap(true, new TextExtractorForTransformers()))); // 3
        extractors.add(new SimpleEntry<>("TE4OnlyLabel", wrap(false, new TextExtractorOnlyLabel()))); // 4
        
        extractors.add(new SimpleEntry<>("TE65abelSuperclassWhichTrue", 
                wrap(false, new TextExtractorLabelAndDirectSuperclass("which is subclass of", true))));//5
        extractors.add(new SimpleEntry<>("TE6LabelSuperclassWhichFalse", 
                wrap(false, new TextExtractorLabelAndDirectSuperclass("which is subclass of", false))));//6
        extractors.add(new SimpleEntry<>("TE7LabelSuperclassSubTrue", 
                wrap(false, new TextExtractorLabelAndDirectSuperclass("subclass of", true))));//7
        extractors.add(new SimpleEntry<>("TE8LabelSuperclassSubFalse", 
                wrap(false, new TextExtractorLabelAndDirectSuperclass("subclass of", false))));//8
        
        //rdf serialization variants:
        
        extractors.add(new SimpleEntry<>("TE9ResourceDescriptionInRDF", 
                wrap(false, new TextExtractorResourceDescriptionInRDF()))); //9
        TextExtractorResourceDescriptionInRDF extractor = new TextExtractorResourceDescriptionInRDF();
        extractor.setRemovePrefixDefition(false);
         extractors.add(new SimpleEntry<>("TE10ResourceDescriptionInRDFwithprefix", 
                 wrap(false, extractor))); // 10
        extractors.add(new SimpleEntry<>("TE11ResourceDescriptionInRDFTrueNT", 
                wrap(false, new TextExtractorResourceDescriptionInRDF(true, RDFFormat.NT)))); //11
        extractors.add(new SimpleEntry<>("TE12ResourceDescriptionInRDFFalseNT", 
                wrap(false, new TextExtractorResourceDescriptionInRDF(false, RDFFormat.NT)))); //12
        
        
        //text translation all
        extractors.add(new SimpleEntry<>("TE13VerbalizedRDFfalsefalse", 
                wrap(false, new TextExtractorVerbalizedRDF(false, false)))); //13
        extractors.add(new SimpleEntry<>("TE14VerbalizedRDFfalsetrue", 
                wrap(false, new TextExtractorVerbalizedRDF(false, true)))); //14
        extractors.add(new SimpleEntry<>("TE15VerbalizedRDFtruefalse", 
                wrap(false, new TextExtractorVerbalizedRDF(true, false)))); //15
        extractors.add(new SimpleEntry<>("TE16VerbalizedRDFtruetrue", 
                wrap(false, new TextExtractorVerbalizedRDF(true, true)))); //16
        
        
        //more "resource description" text extractors:
        extractors.add(new SimpleEntry<>("TE17", //17
                wrap(false, new TextExtractorResourceDescriptionInRDF()
                        .setStatementProcessor(TextExtractorResourceDescriptionInRDF.SKIP_DEFINITIONS)))); 
        extractors.add(new SimpleEntry<>("TE18", //18
                wrap(false, new TextExtractorResourceDescriptionInRDF()
                        .setStatementProcessor(TextExtractorResourceDescriptionInRDF.SKIP_DEFINITIONS_AND_LONG_LITERALS)))); 
        extractors.add(new SimpleEntry<>("TE19", //19
                wrap(false, new TextExtractorResourceDescriptionInRDF()
                        .setStatementProcessor(TextExtractorResourceDescriptionInRDF.SKIP_DEFINITIONS_AND_SHORTEN_LONG_LITERALS)))); 
        
        extractors.add(new SimpleEntry<>("TE20", //20
                wrap(false, new TextExtractorResourceDescriptionInRDF(true, RDFFormat.TURTLE)
                        .setStatementProcessor(TextExtractorResourceDescriptionInRDF.SKIP_DEFINITIONS)))); 
        extractors.add(new SimpleEntry<>("TE21", //21
                wrap(false, new TextExtractorResourceDescriptionInRDF(true, RDFFormat.TURTLE)
                        .setStatementProcessor(TextExtractorResourceDescriptionInRDF.SKIP_DEFINITIONS_AND_LONG_LITERALS)))); 
        extractors.add(new SimpleEntry<>("TE22", //22
                wrap(false, new TextExtractorResourceDescriptionInRDF(true, RDFFormat.TURTLE)
                        .setStatementProcessor(TextExtractorResourceDescriptionInRDF.SKIP_DEFINITIONS_AND_SHORTEN_LONG_LITERALS)))); 
        
        
        return extractors;
    }
    
    private static TextExtractorMap wrap(boolean appendPostProcessing, TextExtractor ex){
        if(appendPostProcessing){
            return TextExtractorMap.appendStringPostProcessing(
                    TextExtractorMap.wrapTextExtractor(ex), StringProcessing::normalizeOnlyCamelCaseAndUnderscore
            );
        }else{
            return TextExtractorMap.wrapTextExtractor(ex);
        }
    }
    
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
        
        options.addOption(Option.builder("c")
                .longOpt("cache")
                .hasArg()
                .argName("path")
                .desc("The path to the cache folder for ontologies.")
                .build());
        
        options.addOption(Option.builder("co")
                .longOpt("choose")
                .desc("Activate the choose filter.")
                .build());
        
        options.addOption(Option.builder("d")
                .longOpt("debug")
                .hasArg(false)
                .desc("If given, all debug options are activated (especially a file for each run is written)")
                .build());
        
        options.addOption(Option.builder("g")
                .longOpt("gpu")
                .hasArg()
                .desc("Which GPUs to use. This can be comma separated. Eg. 0,1 which uses GPU zero and one.")
                .build());
        
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Print this help message.")
                .build());
        
        options.addOption(Option.builder("isp")
                .longOpt("includesystemprompt")
                .desc("Include the systemprompt for the specific models.")
                .build());
        
        options.addOption(Option.builder("ila")
                .longOpt("includeloadingarguments")
                .desc("Include the loading arguments defined for the model.")
                .build());
        
        options.addOption(Option.builder("k")
                .longOpt("kneighbours")
                .hasArg()
                .desc("how many candidates should be generated during candidate search")
                .build());
        
        options.addOption(Option.builder("m")
                .longOpt("maxtokens")
                .hasArg()
                .desc("How many token should be generated at maximum. Default is 10.")
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
        
        options.addOption(Option.builder("p")
                .longOpt("python")
                .hasArg()
                .desc("The python command to use.")
                .build());
        
        options.addOption(Option.builder("pr")
                .longOpt("prompt")
                .required()
                .hasArgs()
                .desc("The prompts to use - the texts of the resources are inserted by replacing the text {left} and {right}."
                    + "Can also be a number which then uses a predefined prompt. The number can range from 0 to " + (PREDEFINED_PROMPTS.size() - 1))
                .build());
        
        options.addOption(Option.builder("r")
                .longOpt("replace")
                .desc("Replace the user prompt")
                .build()
        );
        
        options.addOption(Option.builder("rec")
                .longOpt("recall")
                .hasArg()
                .desc("How the recall alignment should be generated. Possible values: smalldummy, dummy, only, normal. Default is: normal")
                .build()
        );
        
        options.addOption(Option.builder("sp")
                .longOpt("serverport")
                .hasArg()
                .desc("The port of the python server.")
                .build()
        );
        
        options.addOption(Option.builder("tc")
                .longOpt("transformerscache")
                .hasArg()
                .desc("The file path to the transformers cache.")
                .build());

        options.addOption(Option.builder("tm")
                .longOpt("transformermodels")
                .hasArgs()
                .required()
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

        // configure the extractor options here...

        StringBuilder extractorDescription = new StringBuilder();
        extractorDescription.append("The extractor to be used. Reference by number. Options:\n");
        int i = 0;
        for (Entry<String, TextExtractorMap> te : TEXT_EXTRACTORS) {
            extractorDescription.append("(")
                    .append(i++)
                    .append(") ")
                    .append(te.getKey())
                    .append("\n");
        }

        options.addOption(Option.builder("te")
                .longOpt("textextractor")
                .desc(extractorDescription.toString() + "Multiple values are separated by space.")
                .hasArgs()
                .required()
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
    
    public boolean isIncludeSystemPrompt(){
        return cmd.hasOption("isp");
    }
    
    public boolean isChoose(){
        return cmd.hasOption("co");
    }
    
    public boolean isIncludeLoadingArguments(){
        return cmd.hasOption("ila");
    }
    
    public boolean isReplacePrompt(){
        return cmd.hasOption("r");
    }
    
    
    public int getMaxTokens(){
        if(cmd.hasOption("m") == false){
            return 10; // default
        }
        String maxToken = cmd.getOptionValue("m");
        
        try{
            return Integer.parseInt(maxToken);
        } catch(NumberFormatException e){
            LOGGER.warn("Maximum tokens (-m) is not a number. Aborting.");
            System.exit(1);
            return 10;
        }
    }
    
    public int getKNeighbours(){
        if(cmd.hasOption("k") == false){
            return 5; // default is 5
        }
        String k = cmd.getOptionValue("k");
        
        try{
            return Integer.parseInt(k);
        } catch(NumberFormatException e){
            LOGGER.warn("Kneighburs (-k) is not a number. Aborting.");
            System.exit(1);
            return 10;
        }
    }
    
    public List<Entry<String, String>> getPrompts(TextExtractorMap textExtractorMap){
        List<String> prompts = Arrays.asList(cmd.getOptionValues("pr"));
        if (prompts.isEmpty()) {
            LOGGER.warn("No prompts specified. ABORTING program.");
            System.exit(1);
        }
        //process prompts
        List<Entry<String, String>> finalPrompts = new ArrayList<>();
        for(String prompt : prompts){
            if(prompt.equals("7auto")){
                finalPrompts.add(new SimpleEntry<>("7auto", getAutoPrompt(textExtractorMap, "Classify if two descriptions refer to the same real world entity (ontology matching).\n"))); 
                continue;
            }
            if(prompt.equals("9auto")){
                finalPrompts.add(new SimpleEntry<>("9auto", getAutoPrompt(textExtractorMap, "Classify if two descriptions (given as RDF) refer to the same real world entity (ontology matching).\n"))); 
                continue;
            }
            if(prompt.equals("12auto")){
                finalPrompts.add(new SimpleEntry<>("12auto", getAutoPromptChooser(textExtractorMap))); 
                continue;
            }
            try{
                int promptNumber = Integer.parseInt(prompt);
                //range check
                if(promptNumber < 0 || promptNumber >= PREDEFINED_PROMPTS.size()){
                    LOGGER.warn("Argument prompts (-pr) which is set to \"{}\" is not in the range 0-{}.", prompt, PREDEFINED_PROMPTS.size()-1);
                    System.exit(1);
                    return null;
                }
                finalPrompts.add(new SimpleEntry<>(Integer.toString(promptNumber), PREDEFINED_PROMPTS.get(promptNumber))); 
            } catch(NumberFormatException e){
                finalPrompts.add(new SimpleEntry<>(getPromptIdentification(prompt), prompt)); 
            }
        }
        
        return finalPrompts;
    }
    
    private String getAutoPrompt(TextExtractorMap extractor, String initialText){
        List<Correspondence> list = new ArrayList<>();
        list.add(new Correspondence("http://mouse.owl#MA_0002517", "http://human.owl#NCI_C33255", 1.0, CorrespondenceRelation.EQUIVALENCE));
        list.add(new Correspondence("http://mouse.owl#MA_0001693", "http://human.owl#NCI_C13318", 1.0, CorrespondenceRelation.INCOMPAT));
        list.add(new Correspondence("http://mouse.owl#MA_0001104", "http://human.owl#NCI_C33215", 1.0, CorrespondenceRelation.EQUIVALENCE));
        list.add(new Correspondence("http://mouse.owl#MA_0001381", "http://human.owl#NCI_C52779", 1.0, CorrespondenceRelation.INCOMPAT));
        list.add(new Correspondence("http://mouse.owl#MA_0000333", "http://human.owl#NCI_C12382", 1.0, CorrespondenceRelation.INCOMPAT));
        list.add(new Correspondence("http://mouse.owl#MA_0001911", "http://human.owl#NCI_C33452", 1.0, CorrespondenceRelation.EQUIVALENCE));
        
        TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();
        OntModel source = tc.getSourceOntology(OntModel.class);
        OntModel target = tc.getTargetOntology(OntModel.class);
        StringBuilder sb = new StringBuilder();
        sb.append(initialText);
        for(Correspondence c : list){
            String sourceText = getText(source, c.getEntityOne(), extractor);
            String targetText = getText(target, c.getEntityTwo(), extractor);
            String yesNo = c.getRelation().equals(CorrespondenceRelation.EQUIVALENCE) ? "yes" : "no";
            sb.append("### Concept one: ").append(sourceText)
              .append(" ### Concept two: ").append(targetText)
              .append(" ### Answer: ").append(yesNo).append("\n");
        }
        sb.append("### Concept one: {left} ### Concept two: {right} ### Answer: ");
        return sb.toString();
    }
    
    private String getAutoPromptChooser(TextExtractorMap extractor){
        TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();
        OntModel source = tc.getSourceOntology(OntModel.class);
        OntModel target = tc.getTargetOntology(OntModel.class);
        
        return "The task is ontology matching and to find the description which refer to the same real world entity. "
                + "Which of the following descriptions fits best to this description: " + getText(source, "http://mouse.owl#MA_0002517", extractor) + "?\n" 
                + "\t 0) " + getText(target, "http://human.owl#NCI_C12608", extractor) + "\n"
                + "\t 1) " + getText(target, "http://human.owl#NCI_C13270", extractor) + "\n"
                + "\t 2) " + getText(target, "http://human.owl#NCI_C33255", extractor) + "\n"
                + "\t 3) " + getText(target, "http://human.owl#NCI_C38639", extractor) + "\n"
                + "Answer with the corresponding number or \"None\" if no description fits. Answer: 2\n"
                + "Which of the following descriptions fits best to this description: {left}?\n"
                + "{right}\n"
                + "Answer with the corresponding number or \"None\" if no description fits. Answer:";
    }
    
    private String getText(OntModel m, String url, TextExtractorMap extractor){
        Map<String, Set<String>> texts = extractor.extract(m.getResource(url));
        return texts.entrySet().iterator().next().getValue().iterator().next();
        //return StringProcessing.normalizeOnlyCamelCaseAndUnderscore(oneValue);
    }
    
    private String getPromptIdentification(String prompt){
        //prompt identification is the first word plus a short hash of the prompt.
        int i = prompt.indexOf(' ');
        String firstWord = prompt;
        if(i >= 0){
            firstWord = firstWord.substring(0, i);
        }
        //at max 20 characters
        if(firstWord.length() > 15){
            firstWord = firstWord.substring(0, 15);
        }
        
        return firstWord+DigestUtils.sha256Hex(prompt).substring(0, 7);
    }
    
    
    public boolean isDebug(){
        return cmd.hasOption("d");
    }
    
    public String getRecallGeneration(){
        return cmd.getOptionValue("rec", "normal");
    }
    
    public List<Entry<String, TextExtractorMap>> getTextExtractors(){
        List<Entry<String, TextExtractorMap>> textExtractors = new ArrayList<>();
        if(cmd.hasOption("te")) {
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
                    textExtractors.add(TEXT_EXTRACTORS.get(parseTeOption));
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Argument textextractor (-te) is set to {} but the index does not correspong to a text extractor.",teOption);
                    System.exit(1);
                    return null;
                }
            }
        }
        if(textExtractors.isEmpty()){
            LOGGER.warn("Text extractor (Option -te) not specified. ABORTING program.");
            System.exit(1);
        }
        return textExtractors;
    }
        
    private List<Track> getTracks(){
        List<Track> tracks = new ArrayList<>();        
        if (cmd.hasOption("tracks")) {
            for (String trackString : cmd.getOptionValues("tracks")) {
                trackString = trackString.toLowerCase(Locale.ROOT).trim();
                Track track = TrackNameLookup.getTrackByString(trackString);
                if(track == null){
                    LOGGER.warn("Could not map track: " + trackString);
                    System.exit(1);
                }else{
                    tracks.add(track);
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
