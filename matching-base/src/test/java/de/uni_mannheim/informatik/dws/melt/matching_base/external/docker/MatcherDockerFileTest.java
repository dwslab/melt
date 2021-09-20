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
        assertEquals("my-image", MatcherDockerFile.getImageNameFromFileName(sampleFile));

        String sampleFilePath2 = "./my-image-latest.tar.gz";
        File sampleFile2 = new File(sampleFilePath2);
        assertEquals("my-image", MatcherDockerFile.getImageNameFromFileName(sampleFile2));
    }
    
    
    @Test
    void getImageNameFromFileContent(){
        //just tar file
        File file = new File(getClass().getClassLoader().getResource("getImageNameTest.tar").getFile());
        assertEquals("simplewebmatcher-1.0-web", MatcherDockerFile.getImageNameFromFileContent(file));
        
        //tar gz file
        File fileTarGz = new File(getClass().getClassLoader().getResource("getImageNameTest.tar.gz").getFile());
        assertEquals("simplewebmatcher-1.0-web", MatcherDockerFile.getImageNameFromFileContent(fileTarGz));
        
        //no fiel ending but tar gz
        File noFileExtension = new File(getClass().getClassLoader().getResource("getImageNameTest.foo_bar").getFile());
        assertEquals("simplewebmatcher-1.0-web", MatcherDockerFile.getImageNameFromFileContent(noFileExtension));
    }

}