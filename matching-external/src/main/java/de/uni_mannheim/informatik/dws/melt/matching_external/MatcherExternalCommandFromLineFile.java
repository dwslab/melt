package de.uni_mannheim.informatik.dws.melt.matching_external;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import static java.nio.file.Files.newBufferedReader;
import java.util.ArrayList;
import java.util.List;

public class MatcherExternalCommandFromLineFile extends MatcherExternalCommandFromFile {
    
    @Override
    protected List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception {
        List<String> command = new ArrayList<>();
        try (BufferedReader reader = newBufferedReader(filePath, StandardCharsets.UTF_8)) {            
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                command.add(replaceString(line));
            }
        }
        command.add(source.toString());
        command.add(target.toString());
        if(inputAlignment != null)
            command.add(inputAlignment.toString());
        return command;
    }    
}
