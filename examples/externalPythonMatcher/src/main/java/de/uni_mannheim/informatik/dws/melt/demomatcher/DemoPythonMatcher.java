package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_external.MatcherExternal;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for the external Python matcher.
 * @author Sven Hertling
 */
public class DemoPythonMatcher extends MatcherExternal {

    @Override
    protected List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("python");
        command.add("oaei-resources" + File.separator + "pythonMatcher.py");
        command.add(source.toString());
        command.add(target.toString());
        if(inputAlignment != null)
            command.add(inputAlignment.toString());
        return command;
        //return new ArrayList(Arrays.asList("python", "oaei-resources" + File.separator + "pythonMatcher.py", source.toString(), target.toString()))
    }
    
}
