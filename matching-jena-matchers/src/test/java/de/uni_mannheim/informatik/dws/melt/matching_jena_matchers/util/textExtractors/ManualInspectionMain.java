package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllLiterals;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllStringLiterals;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.StringUtil;
import java.io.File;
import java.util.List;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.LoggerFactory;

/**
 * This class allows to manually inspect the output of a {@link TextExtractor} by writing the results to a file or stdout.
 */
public class ManualInspectionMain {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ManualInspectionMain.class);
    public static void main(String[] args){
        
        //TextExtractor extractor = new TextExtractorAllStringLiterals();
        //TextExtractor extractor = new TextExtractorAllLiterals();
        //TextExtractor extractor = new TextExtractorAllAnnotationProperties();
        //TextExtractor extractor = new TextExtractorForTransformers();
        TextExtractor extractor = new TextExtractorShortAndLongTexts();
        //TextExtractor extractor = new  TextExtractorSet();
        
        ManualInspection inspector = new ManualInspection(extractor);
        File baseDir = new File("./textExtractorManualInspection");
        baseDir.mkdirs();
        
        
        //do anatomy
        //inspector.writeTestCaseSourceToFile(TrackRepository.Anatomy.Default.getFirstTestCase(), new File(baseDir, "anatomy-mouse.txt"));
        //inspector.writeTestCaseTargetToFile(TrackRepository.Anatomy.Default.getFirstTestCase(), new File(baseDir, "anatomy-human.txt"));
        
        //conference
        //inspector.writeTrackToFolder(TrackRepository.Conference.V1, baseDir);
        
        //inspector.writeTestCaseSourceToFile(TrackRepository.Knowledgegraph.V3.getFirstTestCase(), new File("kg-one.txt", baseDir));
        //inspector.writeTrackToFolder(TrackRepository.Knowledgegraph.V3, baseDir);
        
        //======================================================
        
        //inspector.describeResourcesWithExtractor(TrackRepository.Anatomy.Default.getFirstTestCase(), 10, new File(baseDir, "describe-anatomy-mouse.txt"), true, true, true);
        //inspector.describeResourcesWithExtractor(TrackRepository.Anatomy.Default.getFirstTestCase(), 10, new File(baseDir, "describe-anatomy-human.txt"), false, true, true);
        /*
        inspector.describeResourcesWithExtractor(TrackRepository.Conference.V1.getFirstTestCase(), 10, new File(baseDir, "describe-conference.txt"), true);
        
        inspector.describeResourcesWithExtractor(TrackRepository.Largebio.V2016.FMA_NCI_SMALL.getFirstTestCase(), 10, new File(baseDir, "describe-largebio-fma.txt"), true);
        */
        
        TestCase tc = TrackRepository.Knowledgegraph.V3.getTestCase("memoryalpha-memorybeta");
        inspector.describeResourcesWithExtractor(tc, 30, new File(baseDir, "describe-kg-memoryalpha.txt"), true);
        inspector.describeResourcesWithExtractor(tc, 30, new File(baseDir, "describe-kg-memorybeta.txt"), false);

    }
    
    
    
}
