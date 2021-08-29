package de.uni_mannheim.informatik.dws.melt.receiver_cli;

import de.uni_mannheim.informatik.dws.melt.matching_base.receiver.MainMatcherClassExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * This class is used as wrapper for the SEALS external matcher build process.
 * It follows the MELT ExternalMatcher communication protocol.
 */
public class Main {


    public static void main(String[] args){
        Options options = new Options();

        options.addOption(Option.builder("s")
                .longOpt("source")
                .hasArg()
                .argName("URL")
                .required()
                .desc("The source ontology URI (usually a file uri pointing to a file containing an ontology).")
                .build());
        
        options.addOption(Option.builder("t")
                .longOpt("target")
                .hasArg()
                .argName("URL")
                .required()
                .desc("The target ontology URI (usually a file uri pointing to a file containing an ontology).")
                .build());
        
        options.addOption(Option.builder("i")
                .longOpt("inputAlignment")
                .hasArg()
                .argName("URL")
                .desc("The input alignment URI (usually a file uri pointing to a file containing the alignment in the alignment api format).")
                .build());
        
        options.addOption(Option.builder("p")
                .longOpt("parameters")
                .hasArg()
                .argName("URL")
                .desc("The parameters URI (usually a file uri pointing to a file containing parameters formatted usually as json or YAML).")
                .build());
        
        options.addOption("help", "print this message");
                
        CommandLine cmd = null;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("java -jar cli-receiver.jar", options);
            System.exit(1);
        }
        
        if(cmd.hasOption("help")){
            new HelpFormatter().printHelp("java -jar cli-receiver.jar", options);
            System.exit(1);
        }
        
        String mainClass;
        try {
            mainClass = MainMatcherClassExtractor.extractMainClass();
        } catch (IOException ex) {
            System.err.println("Could not extract Main class name. Do nothing." + ex.getMessage());
            return;
        }
        
        AlignmentAndParameters result = null;
        try {
            result = GenericMatcherCaller.runMatcher(mainClass, getURL(cmd, "s"), getURL(cmd, "t"), getURL(cmd, "i"), getURL(cmd, "p"));
        } catch (Exception ex) {
            System.err.println("Could not call the matcher. " + ex.getMessage());
            ex.printStackTrace();
            return;
        }        
        
        if(result.getAlignment() == null){
            System.err.println("The resulting alignment of the matcher is null.");
            return;
        }
        try {
            System.out.println(TypeTransformerRegistry.getTransformedObject(result.getAlignment(), URL.class));
        } catch (TypeTransformationException ex) {
            System.err.println("Cannot transform the alignment to a URL" + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private static URL getURL(CommandLine cmd, String parameter){
        if(cmd.hasOption(parameter)){
            try {
                return new URL(cmd.getOptionValue(parameter));
            } catch (MalformedURLException ex) {
                System.err.println("argument " + parameter + " is not a valid url. The argument will not be used.");
            }
        }
        return null;
    }
}