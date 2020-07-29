package de.uni_mannheim.informatik.dws.melt.matching_external;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Read the file "external/external_command.txt" and start an external process.
 * The file can contain the following texts which are replaced.
 * <ul>
 * <li>"{File.separator}" with the os dependent file separator.On UNIX systems the value is <code>'/'</code>; on Microsoft Windows systems it is <code>'\\'</code>.</li>
 * <li>"{File.pathSeparator}" with the os dependent path separator.It is used to separate filenames in a sequence of files given as a <em>path list</em>.On UNIX systems, this character is <code>':'</code>; on Microsoft Windows systems it is <code>';'</code>.</li>
 * <li>"{xmx}" with the value xmx value the java process is started with (e.g. "{xmx}" is replaced with "-Xmx100G")</li>
 * <li>"{xms}" with the value xms value the java process is started with (e.g. "{xms}" is replaced with "-Xms10G")</li>
 * <li>"{source}" with the URL of the source ontology</li>
 * <li>"{target}" with the URL of the target ontology</li>
 * <li>"{inputAlignment}" with the URL of the source ontology</li>
 * </ul>
 */
public class MatcherExternalCompleteCommandFromFile extends MatcherExternalCommandFromFile {
    
    @Override
    protected List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception {
        String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        List<String> command = new ArrayList(Arrays.asList(replaceString(fileContent, source, target, inputAlignment).split(" ")));
        command.add(source.toString());
        command.add(target.toString());
        if(inputAlignment != null)
            command.add(inputAlignment.toString());
        return command;
    }    
}
