package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


/**
 *
 * @author Sven Hertling
 */
public abstract class MatcherString extends MatcherFile {

    private static final String newline = System.getProperty("line.separator");

    private String readStringFromURL(URL url) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(newline);
            }
            return stringBuilder.toString();
        }
    }

    @Override
    public void match(URL source, URL target, URL inputAlignment, File alignmentResult) throws Exception {
        String result = match(readStringFromURL(source), readStringFromURL(target), readStringFromURL(inputAlignment));
        try (BufferedWriter out = new BufferedWriter(new FileWriter(alignmentResult))) {
            out.write(result);
        }
    }

    public abstract String match(String source, String target, String inputAlignment) throws Exception;
}
