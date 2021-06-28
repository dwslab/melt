package de.uni_mannheim.informatik.dws.melt.matching_base.external.docker;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MatcherDockerFileTest {

    @Test
    void getImageNameFromFile(){
        // without latest
        String sampleFilePath = "./my-image.tar.gz";
        File sampleFile = new File(sampleFilePath);
        assertEquals("my-image", MatcherDockerFile.getImageNameFromFile(sampleFile));
        assertEquals("my-image", MatcherDockerFile.getImageNameFromFile(sampleFilePath));

        String sampleFilePath2 = "./my-image-latest.tar.gz";
        File sampleFile2 = new File(sampleFilePath2);
        assertEquals("my-image", MatcherDockerFile.getImageNameFromFile(sampleFile2));
        assertEquals("my-image", MatcherDockerFile.getImageNameFromFile(sampleFilePath2));
    }

}