package de.uni_mannheim.informatik.dws.melt.matching_assembly;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.plugins.assembly.filter.ContainerDescriptorHandler;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;

@Component(role = ContainerDescriptorHandler.class, hint = "writemainmatcherclasstofile")
public class WriteMainMatcherClassToFile implements ContainerDescriptorHandler {
    
    protected String mainclass;

    @Override
    public void finalizeArchiveCreation(Archiver archiver) throws ArchiverException {
        archiver.getResources().forEachRemaining(a -> {}); // necessary to prompt the isSelected() call

        try {
            archiver.addFile(getFileFromText(this.mainclass), "external/main_class.txt");
        } catch (IOException ex) {
            throw new ArchiverException("Could not create SEALS archive", ex);
        }
    }
    
    @Override
    public List<String> getVirtualFiles() {
        return Arrays.asList("external/main_class.txt");
    }
    
    @Override
    public void finalizeArchiveExtraction(UnArchiver ua) throws ArchiverException { }

    @Override
    public boolean isSelected(FileInfo fi) throws IOException { return true; }    

    public void setMainclass(String mainclass) { this.mainclass = mainclass; }

    protected File getFileFromText(String content) throws IOException{
        File f = File.createTempFile("matching_file", ".txt");
        f.deleteOnExit();
        try (PrintWriter out = new PrintWriter(f)) {
            out.print(content);
        }
        return f;
    }
}
