package de.uni_mannheim.informatik.dws.melt.matching_external;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import static java.nio.file.Files.newBufferedReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the file "external/external_command.txt" and start an external process. Each line in the file represents an argument of the command line.
 * The file can contain the following texts which are replaced.
 * <ul>
 * <li>"{File.separator}" with the os dependent file separator.On UNIX systems the value is <code>'/'</code>; on Microsoft Windows systems it is <code>'\\'</code>.</li>
 * <li>"{File.pathSeparator}" with the os dependent path separator.It is used to separate filenames in a sequence of files given as a <em>path list</em>.On UNIX systems, this character is <code>':'</code>; on Microsoft Windows systems it is <code>';'</code>.</li>
 * <li>"{xmx}" with the value xmx value the java process is started with (e.g. "{xmx}" is replaced with "-Xmx100G")</li>
 * <li>"{xms}" with the value xms value the java process is started with (e.g. "{xms}" is replaced with "-Xms10G")</li>
 * </ul>
 * At the end of the command the URL of the source and target ontology are appended and optionally also the URL of the input alignment.
 * The called process should access the arguments by index and not by name.
 */
public class MatcherExternalCommandFromLineFile extends MatcherExternalCommandFromFile {
    
    @Override
    protected List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception {
        System.err.println("If you want to modify the following external run command, change file " + filePath.toAbsolutePath().toString() +" or in the SEALS package conf/external/external_command.txt");
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
