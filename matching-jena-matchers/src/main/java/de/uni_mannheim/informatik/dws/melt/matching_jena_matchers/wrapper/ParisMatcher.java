package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.wrapper;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.OntoInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a wrapper for <a href="http://webdam.inria.fr/paris/">PARIS matching system</a> by Fabian Suchanek et al.
 * The corresponding paper is called <a href="https://arxiv.org/abs/1111.7164">PARIS: Probabilistic Alignment of Relations, Instances, and Schema</a>.
 * It will download the matcher if not already done and execute it as an external process. The equivalence files of the last iteration 
 * are then read into a YAAA aligment. It is tested to run with java 1.7 and 1.8.
 */
public class ParisMatcher extends MatcherYAAAJena{
    private static final Logger LOGGER = LoggerFactory.getLogger(ParisMatcher.class);
    
    private static final URL PARIS_WEB_LOCATION = createURL("http://webdam.inria.fr/paris/releases/paris_0_3.jar");
    private static final File SYSTEM_TMP_DIR = new File(System.getProperty("java.io.tmpdir"));
    
    /**
     * Path to the Paris matcher jar file.
     */
    private File pathToParisJar;
    
    /**
     * Folder in which the ontologies and output file of PARIS are stored.
     * The created folders in it are removed after a run.
     */
    private File tmpFolder;
    
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
     * @param tmpFolder Folder in which the ontologies and output file of PARIS are stored. The created folders in it are removed after a run.
     * @param javaCommand The java command which is usually just "java" but can also be a fully qualified path to a java runtime.
     * @param javaRuntimeArguments  A list of java runtime arguments like "-Xmx2g" or the like
     */
    public ParisMatcher(File pathToParisJar, File tmpFolder, String javaCommand, List<String> javaRuntimeArguments) {
        this.pathToParisJar = pathToParisJar;
        this.tmpFolder = tmpFolder;
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
        this(pathToParisJar, SYSTEM_TMP_DIR, javaCommand, javaRuntimeArguments);
    }
    
    /**
     * Constructor which uses the default java executable (available in PATH) and no java parameters are used.
     * A temp directory as a storage for files produced by PARIS is used.
     * @param pathToParisJar Path to the Paris matcher jar file. If file is not existent then version 0.3 it will be downloaded.
     */
    public ParisMatcher(File pathToParisJar) {
        this(pathToParisJar, "java", new ArrayList<>());
    }
    
    /**
     * Constructor which only needs the additional java runtime arguments.
     * A temp directory as a storage for files produced by PARIS is used.
     * @param javaRuntimeArguments A list of java runtime arguments like "-Xmx2g" or the like
     */
    public ParisMatcher(List<String> javaRuntimeArguments) {
        this(new File("paris.jar"), "java", javaRuntimeArguments);
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
    public Alignment match(URL source, URL target, Alignment inputAlignment, Properties properties) throws Exception {
        File runFolder = createFolderWithRandomNumberInDirectory(this.tmpFolder, "paris_run");
        runFolder.mkdirs();
        
        File outputFolder = new File(runFolder, "outputfolder");
        outputFolder.mkdirs();
        
        //1. save ontology in correct format
        if(source.getPath().endsWith(".nt") && target.getPath().endsWith(".nt")){
            LOGGER.info("Run Paris by coping source and target URLs");
            FileUtils.copyURLToFile(source, new File(runFolder, "source.nt"));
            FileUtils.copyURLToFile(target, new File(runFolder, "target.nt"));
        }else if(source.getPath().endsWith(".nt")){
            //only source is nt file:
            LOGGER.info("Run Paris by copying source and transforming target to N-TRIPLE");
            FileUtils.copyURLToFile(source, new File(runFolder, "source.nt"));
            urlToNTripleFile(target, new File(runFolder, "target.nt"));
        }else if(target.getPath().endsWith(".nt")){
            //only target is nt file
            LOGGER.info("Run Paris by transforming source to N-TRIPLE and copying target");
            urlToNTripleFile(source, new File(runFolder, "source.nt"));
            FileUtils.copyURLToFile(target, new File(runFolder, "target.nt"));
        }else{
            //none is nt file
            LOGGER.info("Run Paris by transforming source and target to N-TRIPLE");
            urlToNTripleFile(source, new File(runFolder, "source.nt"));
            urlToNTripleFile(target, new File(runFolder, "target.nt"));
        }
        
        //2. call external jar file
        //do not load it in this JVM because the classpath can collide (just start it as external process)
        try {
            runProcess(runFolder);
        } catch (IOException ex) {
            LOGGER.error("Could not start external PARIS matcher", ex);
        } catch (InterruptedException ex) {
            LOGGER.error("External process is interrupted", ex);
        }
        
        //3. load the results from the output folder
        Alignment alignment = getAlignment(outputFolder);
        //4. delete the created folder and files
        FileUtils.deleteDirectory(runFolder);
        return alignment;
    }
    
    
            
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        LOGGER.info("Run Paris based on OntModels.");
        File runFolder = createFolderWithRandomNumberInDirectory(this.tmpFolder, "paris_run");
        runFolder.mkdirs();
        
        File outputFolder = new File(runFolder, "outputfolder");
        outputFolder.mkdirs();
        
        //1. save ontology in correct format
        modelToNTripleFile(source, new File(runFolder, "source.nt"));
        modelToNTripleFile(target, new File(runFolder, "target.nt"));
                
        //2. call external jar file
        //do not load it in this JVM because the classpath can collide (just start it as external process)
        try {
            runProcess(runFolder);
        } catch (IOException ex) {
            LOGGER.error("Could not start external PARIS matcher", ex);
        } catch (InterruptedException ex) {
            LOGGER.error("External process is interrupted", ex);
        }
        
        //3. load the results from the output folder
        Alignment alignment = getAlignment(outputFolder);
        
        //4. delete the created folder and files
        FileUtils.deleteDirectory(runFolder);
        
        return alignment;
    }
    
    private void runProcess(File tmpFolderForResults) throws IOException, InterruptedException{
        List<String> command = new ArrayList<>();
        command.add(javaCommand);
        command.addAll(javaRuntimeArguments);
        command.addAll(Arrays.asList("-jar", pathToParisJar.getCanonicalPath(), 
                "source.nt", "target.nt", "outputfolder" //the folder and files are created in the match method (PARIS only accepts relative folder)
        ));
        
        LOGGER.info("Start external process (in folder {}) with following command: {}", tmpFolderForResults.getAbsolutePath(), String.join(" ", command));

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
        List<File> equivalenceFiles = new ArrayList<>();
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
    
    
    private void urlToNTripleFile(URL url, File file){
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, url.toString());
        modelToNTripleFile(model, file);
    }
    
    private void modelToNTripleFile(Model m, File file){
        try(OutputStream writer = new BufferedOutputStream(new FileOutputStream(file))){
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
        
    private static final SecureRandom random = new SecureRandom();
    private static File createFolderWithRandomNumberInDirectory(File folder, String prefix){
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);
        return new File(folder, prefix + "-" + n);
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
