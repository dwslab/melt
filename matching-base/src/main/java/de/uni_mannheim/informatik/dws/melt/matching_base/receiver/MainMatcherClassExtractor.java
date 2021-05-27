package de.uni_mannheim.informatik.dws.melt.matching_base.receiver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Extracts the main matcher class as a string from a file.
 */
public class MainMatcherClassExtractor {
    public static final Path MAIN_CLASS_PATH = Paths.get("external", "main_class.txt");
    
    public static String extractMainClass() throws IOException{
        String mainClass = new String(Files.readAllBytes(MAIN_CLASS_PATH), StandardCharsets.UTF_8);
        return mainClass.replace("\r", "").replace("\n", "").trim();
    }
}
