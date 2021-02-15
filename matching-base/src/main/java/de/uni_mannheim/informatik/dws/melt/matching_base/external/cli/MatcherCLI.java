package de.uni_mannheim.informatik.dws.melt.matching_base.external.cli;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process.ExternalProcess;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.process.ProcessOutputAlignmentCollector;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Matcher for running external matchers (require the subclass to create a command to execute).
 */
public abstract class MatcherCLI extends MatcherURL implements IMatcher<URL, URL, URL> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherCLI.class);
    
    /**
     * if set to true, all logging should go to stderr and the result of the process (url or alignment api format) should go to stdout.
     * if set to false, all logging should go to stdout and the result of the process (url or alignment api format) should go to stderr.
     * @return true, all logging should go to stderr and the result of the process (url or alignment api format) should go to stdout, false otherwise
     */
    protected boolean isUsingStdOut(){
        return true;
    }

    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        return match(source, target, inputAlignment, null);
    }
    
     /**
     * The command which should be executed as one string (containing potentially multiple arguments).The line is splitted by whitespace but quotations are respected.
     * An argument line can contain scopes (scopes are only printed if all variables in a scope can be replaced).
     * The following varaibles are replaced:
     * <ul>
     * <li>${source} with source URI</li>
     * <li>${target} with target URI</li>
     * <li>${inputAlignment} with inputAlignment URI</li>
     * <li>${parameters} with parameters URI</li>
     * <li>system properties like ${line.separator} or ${file.separator} ${java.io.tmpdir}</li>
     * <li>environment variables like ${PATH}</li>
     * <li>JVM arguments like ${Xmx} which is replaced by e.g. -Xmx10G</li>
     * </ul>
     * For more see {@link ExternalProcess#addSubstitutionForSystemProperties() }, 
     * {@link ExternalProcess#addSubstitutionForEnvironmentVariables() } and
     * {@link ExternalProcess#addSubstitutionForJVMArguments() }.
     * 
     * The String can also contain scopes which are created with $[...].
     * The scope is only printed if all variables in the scope can be replaced. This is good for named arguments like:
     * $[-i ${inputAlignment}] then only -i is printed if the input alignment is set.
     * @return the string which represents the command to execute.
     * @throws java.lang.Exception in case something goes from when generating the command
     */
    protected abstract String getCommand() throws Exception;
    
    private Map<String,Object> getSubsitiutionMap(URL source, URL target, URL inputAlignment, URL parameters){
        Map<String,Object> map = new HashMap<>();
        map.put("source", source);
        map.put("target", target);
        map.put("inputAlignment", inputAlignment);
        map.put("parameters", parameters);
        return map;
    }

    @Override
    public URL match(URL source, URL target, URL inputAlignment, URL parameters) throws Exception {
        ExternalProcess p = new ExternalProcess();
        p.addArgumentLine(getCommand());
        p.addSubstitutionMap(getSubsitiutionMap(source, target, inputAlignment, parameters));
        p.addSubstitutionDefaultLookups();
        ProcessOutputAlignmentCollector alignmentCollector = new ProcessOutputAlignmentCollector();
        p.addStdErrConsumer(l -> LOGGER.info("External (ERR): {}",l));
        p.addStdOutConsumer(l -> LOGGER.info("External (OUT): {}",l));
        if(isUsingStdOut()){
            p.addStdOutConsumer(alignmentCollector);
        }else{
            p.addStdErrConsumer(alignmentCollector);
        }
        p.run();
        URL detectedURL = alignmentCollector.getURL(); 
        if(detectedURL == null){
            LOGGER.warn("Did not find an URL in the output of the external process. Return input alignment");
            return inputAlignment;
        }
        return detectedURL;
    }
}
