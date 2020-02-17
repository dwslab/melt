package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.melt.MatcherURL;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * This stub demonstrates how resources such as configuration files can be accessed in a matcher.
 */
public class LoadResourceMatcher extends MatcherURL{

    /** Accessing the resources:
    - all files in "oaei-resources" folder are stored in the current working directory and can be accessed with 
          Files.readAllLines(Paths.get("oaei-resources", "configuration_oaei.txt"))  
    - all files in "src/main/resources" folder are compiled to the resulting jar and can be accessed with
          getClass().getClassLoader().getResourceAsStream("configuration_jar.txt");
    **/
    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        //oaei resource
        System.out.println(Files.readAllLines(Paths.get("oaei-resources", "configuration_oaei.txt")));
        
        //jar resource
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("configuration_jar.txt");
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            String fileContent = scanner.useDelimiter("\\A").next();
            System.out.println(fileContent);
        }
        return null;
    }
    
}
