package de.uni_mannheim.informatik.dws.melt.seals_assembly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.maven.plugins.assembly.filter.ContainerDescriptorHandler;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.component.annotations.Component;


@Component(role = ContainerDescriptorHandler.class, hint = "sealsexternaldescriptorhandler")
public class SealsExternalDescriptorHandler extends SealsDescriptorHandler {
    
    //dummy matcher
    protected String dummyMatcherMainclass = "de.uni_mannheim.informatik.dws.melt.matching_external.MatcherExternalCommandFromFile";
    protected String dummyMatcherJarMain = "matching-external.jar";
    protected String dummyMatcherJarDep = "matching-base.jar";
    
    //wrapper
    protected String wrapperJar = "seals-external.jar";
    protected String wrapperMainclass = "de.uni_mannheim.informatik.dws.melt.matching_seals_external.Main";
    
    protected String externalCommand;

    @Override
    public void finalizeArchiveCreation(Archiver archiver) throws ArchiverException {
        archiver.getResources().forEachRemaining(a -> {}); // necessary to prompt the isSelected() call
        
        try {
            archiver.addFile(createStartStopFile("Start"), "bin/start.bat");
            archiver.addFile(createStartStopFile("Stop"), "bin/stop.bat");
            archiver.addFile(createStartStopFile("Deploy"), "bin/deploy.bat");
            archiver.addFile(createStartStopFile("Undeploy"), "bin/undeploy.bat");            
            archiver.addFile(createDescriptor(getLibEntries(), dummyMatcherMainclass, dummyMatcherJarMain), "descriptor.xml");
            archiver.addFile(getFileFromResource(dummyMatcherJarMain), "bin/" + dummyMatcherJarMain);
            archiver.addFile(getFileFromResource(dummyMatcherJarDep), "bin/lib/" + dummyMatcherJarDep);
                       
            if(existsProperty(this.externalCommand)){
                archiver.addFile(getFileFromText(this.externalCommand), "conf/external/external_command.txt");
            }else if(existsProperty(this.mainclass)){
                //the wrapper accepts as first argument the class which implements the matching interface
                String wrapperexternalCommand = "java {xmx} -cp external{File.separator}" + wrapperJar + "{File.pathSeparator}external{File.separator}lib{File.separator}* " + this.wrapperMainclass + " " + this.mainclass;
                archiver.addFile(getFileFromText(wrapperexternalCommand), "conf/external/external_command.txt");
                archiver.addFile(getFileFromResource(wrapperJar), "conf/external/" + wrapperJar);
            }else{
                throw new ArchiverException("You choose the external seals package but not providing external command nor mainclass.");
            }
        } catch (IOException ex) {
            System.out.println("Couldn't create start stop scripts or descriptor for seals packaging");
        }
    }
    
    @Override
    public List getVirtualFiles() {
        List<String> virtualFiles = new LinkedList<>(Arrays.asList(
                "bin/start.bat", 
                "bin/stop.bat", 
                "bin/deploy.bat", 
                "bin/undeploy.bat",
                "descriptor.xml",
                "bin/" + dummyMatcherJarMain,
                "bin/lib/" + dummyMatcherJarDep,
                "conf/external/external_command.txt"));
        if(existsProperty(this.mainclass)){
            virtualFiles.add("conf/external/lib/" + wrapperJar);
        }
        return virtualFiles;
    }
    
    protected String getLibEntries(){
        List<String> libList = new ArrayList<>();
        libList.add("				<ns:lib>lib/" + dummyMatcherJarDep + "</ns:lib>");
        return String.join(newline, libList);
    }
    
    
    protected File getFileFromText(String content) throws IOException{
        File f = File.createTempFile("matching_file", ".txt");
        f.deleteOnExit();
        try (PrintWriter out = new PrintWriter(f)) {
            out.println(content);
        }
        return f;
    }
    
    
    protected File getFileFromResource(String path) throws IOException{
        File f = File.createTempFile("matching_file", ".jar");
        f.deleteOnExit();
        try (InputStream s = getClass().getClassLoader().getResourceAsStream(path)){
            try (OutputStream outStream = new FileOutputStream(f)) {
                IOUtils.copy(s, outStream);
            }
            return f;
        }
    }
    
    protected boolean existsProperty(String prop){
        if(prop == null)
            return false;
        if(prop.startsWith("$"))
            return false;
        return true;
    }
    
    public void setExternalcommand(String c) { this.externalCommand = c; }
}
