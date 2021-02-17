package de.uni_mannheim.informatik.dws.melt.seals_assembly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugins.assembly.filter.ContainerDescriptorHandler;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.component.annotations.Component;


@Component(role = ContainerDescriptorHandler.class, hint = "sealsexternaldescriptorhandler")
public class SealsExternalDescriptorHandler extends SealsDescriptorHandler {
    
    protected String wrapperMainclass = "";
    
    protected String externalCommand;

    @Override
    public void finalizeArchiveCreation(Archiver archiver) throws ArchiverException {
        archiver.getResources().forEachRemaining(a -> {}); // necessary to prompt the isSelected() call
        
        try {
            archiver.addFile(createStartStopFile("Start"), "bin/start.bat");
            archiver.addFile(createStartStopFile("Stop"), "bin/stop.bat");
            archiver.addFile(createStartStopFile("Deploy"), "bin/deploy.bat");
            archiver.addFile(createStartStopFile("Undeploy"), "bin/undeploy.bat");            
            archiver.addFile(createDescriptor(new ArrayList<>(), "de.uni_mannheim.informatik.dws.melt.matching_base.external.cli.MatcherCLIFromFile", "matching-base.jar"), "descriptor.xml");
            archiver.addFile(getFileFromResource("matching-base.jar"), "bin/matching-base.jar");
            
            if(existsProperty(this.externalCommand)){
                archiver.addFile(getFileFromText(this.externalCommand), "conf/external/external_command.txt");
            }else if(existsProperty(this.mainclass)){
                //the wrapper accepts as first argument the class which implements the matching interface
                String wrapperExternalCommand = "java ${Xmx} -cp external${file.separator}cli-receiver.jar${path.separator}external${file.separator}lib${file.separator}* de.uni_mannheim.informatik.dws.melt.receiver.cli.Main" +
                        " -m " + this.mainclass + " -s ${source} -t ${target} $[-i ${inputAlignment}] $[-p ${parameters}]"; //arguments
                archiver.addFile(getFileFromText(wrapperExternalCommand), "conf/external/external_command.txt");
                archiver.addFile(getFileFromResource("cli-receiver.jar"), "conf/external/cli-receiver.jar");
            }else{
                throw new ArchiverException("You choose the external seals package but not providing external command nor mainclass.");
            }
        } catch (IOException ex) {
            System.out.println("Couldn't create start stop scripts or descriptor for seals packaging");
        }
    }
    
    @Override
    public List<String> getVirtualFiles() {
        List<String> virtualFiles = new LinkedList<>(Arrays.asList(
                "bin/start.bat", 
                "bin/stop.bat", 
                "bin/deploy.bat", 
                "bin/undeploy.bat",
                "descriptor.xml",
                "bin/matching-base.jar",
                "conf/external/external_command.txt"));
        if(existsProperty(this.mainclass)){
            virtualFiles.add("conf/external/lib/cli-receiver.jar");
        }
        return virtualFiles;
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
