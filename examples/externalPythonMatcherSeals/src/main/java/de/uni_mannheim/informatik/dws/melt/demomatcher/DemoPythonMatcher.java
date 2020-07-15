package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_external.MatcherExternal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for the external Python matcher.
 * RDFlib is required in python environment.
 */
public class DemoPythonMatcher extends MatcherExternal {

    @Override
    protected List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(getPythonCommand());
        command.add("oaei-resources" + File.separator + "pythonMatcher.py");
        command.add(source.toString());
        command.add(target.toString());
        if(inputAlignment != null)
            command.add(inputAlignment.toString());
        return command;
        //return new ArrayList(Arrays.asList("python", "oaei-resources" + File.separator + "pythonMatcher.py", source.toString(), target.toString()))
    }
    
    /**
     * Returns the python command which is extracted from {@code file oaei-resources/python_command.txt}.
     * @return The python executable path.
     */
    protected String getPythonCommand(){
        Path filePath = Paths.get("oaei-resources", "python_command.txt");
        if(Files.exists(filePath)){
            try {
                String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                return fileContent.replace("\r", "").replace("\n", "")
                        .replace("{File.pathSeparator}", File.pathSeparator)
                        .replace("{File.separator}", File.separator)
                        .trim();
            } catch (IOException ex) { return "python"; }
        } else{
            return "python";
        }
    }
}
