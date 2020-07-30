package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just saves the ontologies in a specific format.
 */
public class FileSaveMatcher extends MatcherYAAAJena{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSaveMatcher.class);
    
    private File resultFolder;
    private RDFFormat format;
    private String fileExtension;

    /**
     * Choose in which folder the OntModel are stored and in which format.
     * @param resultFolder the result folder in which the OntModels are stored.
     * @param format the RDFFormat in which the OntModels are stored.
     */
    public FileSaveMatcher(File resultFolder, RDFFormat format) {
        this.resultFolder = resultFolder;
        this.format = format;
        this.fileExtension = format.getLang().getFileExtensions().get(0);
    }

    /**
     * Stores the OntModel in Ntriple format in folder "fileSaveMatcher" in current working directory.
     */
    public FileSaveMatcher() {
        this(new File("fileSaveMatcher"), RDFFormat.NTRIPLES);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        writeModel(source, new File(resultFolder, "source." + fileExtension));
        writeModel(target, new File(resultFolder, "target." + fileExtension));
        return inputAlignment;
    }
    
    public void writeModel(OntModel m, File f){
        f.getParentFile().mkdirs();
        try(OutputStream writer = new BufferedOutputStream(new FileOutputStream(f))){
            RDFDataMgr.write(writer, m, format);
        } catch (IOException ex) {
            LOGGER.error("Could not write OntModel to file.", ex);
        }
    }
}
