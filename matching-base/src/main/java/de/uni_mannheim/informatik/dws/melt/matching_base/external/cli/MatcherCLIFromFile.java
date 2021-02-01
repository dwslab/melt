package de.uni_mannheim.informatik.dws.melt.matching_base.external.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read the file "external/external_command.txt" and start an external process. The whole content of the file is used and newlines are ignored.
 * For replacements in this string have a look at {@link MatcherCLI#getCommand() }
 */
public class MatcherCLIFromFile extends MatcherCLI{
    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherCLIFromFile.class);
    
    protected Path filePath = Paths.get("external", "external_command.txt");
    
    @Override
    protected String getCommand() throws IOException{
        LOGGER.info("If you want to modify the following external run command, change file " + filePath.toAbsolutePath().toString() +" or in the SEALS package conf/external/external_command.txt");
        return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
    }
}
