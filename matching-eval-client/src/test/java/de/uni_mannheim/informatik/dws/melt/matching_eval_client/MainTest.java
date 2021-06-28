package de.uni_mannheim.informatik.dws.melt.matching_eval_client;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    /**
     * Quickly show the help / make sure no exceptions are thrown.
     */
    @Test
    void showHelp() {
        Main.main(new String[]{"-h"});
    }

    /**
     * Make sure not exceptions are thrown.
     */
    @Test
    void noParameters() {
        Main.main(null);
    }

    /**
     * Simple SEALS evaluation.
     */
    @Test
    @Disabled
    void evaluateSingleSealsFile() {
        Main.main(new String[]{"-s",
                loadFile("simpleSealsMatcher-1.0-seals_external.zip").getAbsolutePath(),
                loadFile("simplewebmatcher-1.0-web-latest.tar.gz").getAbsolutePath(),
                "-t", "http://oaei.webdatacommons.org/tdrs/", "conference", "conference-v1",
                "-j", "/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/bin/java"});
    }

    /**
     * Helper function to load files in class path that contain spaces.
     *
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName) {
        try {
            File result = FileUtils.toFile(this.getClass().getClassLoader().getResource(fileName).toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception) {
            exception.printStackTrace();
            fail("Could not load file.", exception);
            return null;
        }
    }


}