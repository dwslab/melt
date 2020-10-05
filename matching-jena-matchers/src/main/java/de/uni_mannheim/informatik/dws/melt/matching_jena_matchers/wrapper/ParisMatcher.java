package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.wrapper;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Call the paris matcher.
 */
public class ParisMatcher extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParisMatcher.class);
    
    private static URL PARIS_WEB_LOCATION = createURL("http://webdam.inria.fr/paris/releases/paris_0_3.jar");
    
    /**
     * Path to the Paris matcher jar file.
     */
    private File pathToParisJar;
    
    /**
     * Folder in which the ontologies and output file of PARIS are stored.
     * It is removed after a run.
     */
    private File tmpFolderForResults;
    
    /**
     * The java command which is usually just "java" but can also be a fully qualified path to a java runtime.
     */
    private String javaCommand;
    
    /**
     * A list of java runtime arguments like "-Xmx2g" or the like
     */
    private List<String> javaRuntimeArguments;

    /**
     * Constructor with all attributes.
     * Be careful with the second parameter because the provided folder is deleted after a run.
     * Thus specify a non existent folder which is created and then removed.
     * @param pathToParisJar Path to the Paris matcher jar file. If file is not existent then version 0.3 it will be downloaded.
     * @param tmpFolderForResultsDeleted Folder in which the ontologies and output file of PARIS are stored. It is removed after a run. BE CAREFUL!!!
     * @param javaCommand The java command which is usually just "java" but can also be a fully qualified path to a java runtime.
     * @param javaRuntimeArguments  A list of java runtime arguments like "-Xmx2g" or the like
     */
    public ParisMatcher(File pathToParisJar, File tmpFolderForResultsDeleted, String javaCommand, List<String> javaRuntimeArguments) {
        this.pathToParisJar = pathToParisJar;
        this.tmpFolderForResults = tmpFolderForResultsDeleted;
        this.javaCommand = javaCommand;
        this.javaRuntimeArguments = javaRuntimeArguments;
        
        if(pathToParisJar.exists() == false){
            LOGGER.info("Download PARIS matcher because file {} does not exist.", pathToParisJar);
            try {
                FileUtils.copyURLToFile(PARIS_WEB_LOCATION, pathToParisJar);
            } catch (IOException ex) {
                LOGGER.warn("Could not download Paris matcher.", ex);
            }
        }
    }
    
    /**
     * Constructor which uses a temp directory as a storage for files produced by PARIS.
     * @param pathToParisJar Path to the Paris matcher jar file. If file is not existent then version 0.3 it will be downloaded.
     * @param javaCommand The java command which is usually just "java" but can also be a fully qualified path to a java runtime.
     * @param javaRuntimeArguments  A list of java runtime arguments like "-Xmx2g" or the like
     */
    public ParisMatcher(File pathToParisJar, String javaCommand, List<String> javaRuntimeArguments) {
        this(pathToParisJar, createTempDir("paris_matcher"), javaCommand, javaRuntimeArguments);
    }
    
    /**
     * Constructor which uses the default java executable (available in PATH) and maximum heap size of 10G.
     * A temp directory as a storage for files produced by PARIS is used.
     * @param pathToParisJar Path to the Paris matcher jar file. If file is not existent then version 0.3 it will be downloaded.
     */
    public ParisMatcher(File pathToParisJar) {
        this(pathToParisJar, "java", Arrays.asList("-Xmx10g"));
    }
    
    /**
     * Constructor which expects the Paris matcher to be in the working directory and called "paris.jar".
     * It uses the default java executable (available in PATH) and maximum heap size of 10G.
     * A temp directory as a storage for files produced by PARIS is used.
     */
    public ParisMatcher(){
        this(new File("paris.jar"));
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        File outputFolder = new File(tmpFolderForResults, "outputfolder");
        outputFolder.mkdirs();
        
        //1. save ontology in correct format
        File sourceOntology = new File(tmpFolderForResults, "source.nt");
        File targetOntology = new File(tmpFolderForResults, "target.nt");
        saveOntologyToNTripleFile(source, sourceOntology);
        saveOntologyToNTripleFile(target, targetOntology);
        
        //2. call external jar file
        //do not load it in this JVM because the classpath can collide (just start it as external process)
        try {
            runProcess(tmpFolderForResults);
        } catch (IOException ex) {
            LOGGER.error("Could not start external PARIS matcher", ex);
        } catch (InterruptedException ex) {
            LOGGER.error("External process is interrupted", ex);
        }
        
        //3. load the results from the output folder
        Alignment alignment = getAlignment(outputFolder);
        
        //4. delete the created folder and files
        FileUtils.deleteDirectory(tmpFolderForResults);
        
        return alignment;
    }
    
    private void runProcess(File tmpFolderForResults) throws IOException, InterruptedException{
        List<String> command = Arrays.asList(
                javaCommand, "-jar", pathToParisJar.getCanonicalPath(), 
                "source.nt", "target.nt", "outputfolder" //the folder and files are created in the match method (PARIS only accepts relative folder)
        );
        LOGGER.info("Start external process (in folder {}) with following command: {}", tmpFolderForResults.getCanonicalPath(), String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(tmpFolderForResults);
        
        //pb.redirectInput(Redirect.INHERIT); // no need because the process gets no further input than the process parameters
        pb.redirectOutput(Redirect.INHERIT); // redirect out pipe because of all logging etc
        pb.redirectError(Redirect.INHERIT); // redirect err pipe because of all logging etc
        Process process = pb.start();
        int errCode = process.waitFor(); // wait for the matcher to finish
        
        if(errCode != 0){
            LOGGER.warn("External process returned error code {}", errCode);
        }
    }
    
    private Alignment getAlignment(File outputFolder){
        List<File> equivalenceFiles = new ArrayList();
        //List<File> superRelationOneFiles = new ArrayList();
        //List<File> superRelationTwoFiles = new ArrayList();
        //List<File> superClassOneFiles = new ArrayList();
        //List<File> superClassTwoFiles = new ArrayList();
        for (File f : outputFolder.listFiles()) {
            if(f.getName().endsWith("eqv.tsv")) {
                equivalenceFiles.add(f);
            }
            /*
            else if(f.getName().endsWith("superrelations1.tsv")) {
                superRelationOneFiles.add(f);
            }else if(f.getName().endsWith("superrelations2.tsv")) {
                superRelationTwoFiles.add(f);
            }else if(f.getName().endsWith("superclasses1.tsv")) {
                superClassOneFiles.add(f);
            }else if(f.getName().endsWith("superclasses2.tsv")) {
                superClassTwoFiles.add(f);
            }
            */
        }
        Alignment equivalence = getAlignmentHighestIteration(equivalenceFiles);
        /*
        Alignment superRelationOne = getAlignmentHighestIteration(superRelationOneFiles);
        Alignment superRelationTwo = getAlignmentHighestIteration(superRelationTwoFiles);
        Alignment superClassOne = getAlignmentHighestIteration(superClassOneFiles);
        Alignment superClassTwo = getAlignmentHighestIteration(superClassTwoFiles);
        
        List<Correspondence> l = equivalence.getConfidenceOrderedMapping();
        List<Correspondence> l1 = superRelationOne.getConfidenceOrderedMapping();
        List<Correspondence> l2 = superRelationTwo.getConfidenceOrderedMapping();
        List<Correspondence> l3 = superClassOne.getConfidenceOrderedMapping();
        List<Correspondence> l4 = superClassTwo.getConfidenceOrderedMapping();
        
        Alignment relations = findEquivalence(superRelationOne, superRelationTwo);
        Alignment classes = findEquivalence(superClassOne, superClassTwo);
        */
        return equivalence;
    }
    
    private Alignment findEquivalence(Alignment directionOne, Alignment directionTwo){
        //directionOne
        Alignment equivalence = new Alignment();
        for(Correspondence one : directionOne){
            //ask if in direction two conatins the reverse correspondence
            Correspondence two = directionTwo.getCorrespondence(one.getEntityTwo(), one.getEntityOne(), CorrespondenceRelation.EQUIVALENCE);
            if(two != null){
                //add the correspondence with the mean of both
                equivalence.add(one.getEntityOne(), one.getEntityTwo(), (one.getConfidence() + two.getConfidence()) / 2.0, CorrespondenceRelation.EQUIVALENCE);
            }
        }
        return equivalence;  
    }
    
    private Alignment getAlignmentHighestIteration(List<File> files){
        files.sort(new FileNumberDescendingComparator());
        for(File f : files){
            try {
                Alignment alignment = AlignmentParser.parseTSV(f);
                if(alignment.size() > 0)
                    return alignment;
            } catch (IOException ex) {
                LOGGER.warn("File generated by PARIS matcher is not parsable", ex);
            }
        }
        return new Alignment();
    }
    
    
    protected void saveOntologyToNTripleFile(OntModel m, File f){
        try(OutputStream writer = new BufferedOutputStream(new FileOutputStream(f))){
            RDFDataMgr.write(writer, m, RDFFormat.NTRIPLES);
        } catch (IOException ex) {
            LOGGER.error("Could not write OntModel to file.", ex);
        }
    }
    
    private static URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }
    
    private static File createTempDir(String prefix){
        try {
            return Files.createTempDirectory(prefix).toFile();
        } catch (IOException ex) {
            LOGGER.warn("Could not create a temp directory (use one in current working directory)", ex);
            return new File(prefix);
        }
    }
    
    /**
     * Compare file names by containing numbers.
     * File names have to separated by undersore like 100_xyz.ext and 1_abx.ext
     */
    class FileNumberDescendingComparator implements Comparator<File>{
        @Override
        public int compare(File o1, File o2) {
            return Integer.compare(extractNumber(o2), extractNumber(o1)); //change o1 with o2 to get descending
        }
        private int extractNumber(File f){
            return Integer.parseInt(f.getName().split("_")[0]);
        }
    }
}
