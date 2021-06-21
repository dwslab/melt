package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.MatcherCLI;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the external Python matcher.
 * RDFlib is required in python environment.
 */
public class DemoPythonMatcher extends MatcherCLI {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoPythonMatcher.class);
   
    @Override
    protected String getCommand() throws Exception {
        return getPythonCommand() + " oaei-resources${file.separator}pythonMatcher.py ${source} ${target} $[${inputAlignment}] $[${parameters}]";
    }
    
    /**
     * Returns the python command which is extracted from {@code file oaei-resources/python_command.txt}.
     * @return The python executable path.
     */
    protected String getPythonCommand(){
        Path filePath = Paths.get("oaei-resources", "python_command.txt");
        if(Files.exists(filePath)){
            try {
                return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8).replace("\r", "").replace("\n", "").trim();
            } catch (IOException ex) { LOGGER.warn("Could not read python command file", ex);}
        }
        return "python";
    }
}
