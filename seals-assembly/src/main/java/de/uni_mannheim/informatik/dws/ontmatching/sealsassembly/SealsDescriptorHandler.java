package de.uni_mannheim.informatik.dws.ontmatching.sealsassembly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.maven.plugins.assembly.filter.ContainerDescriptorHandler;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.components.io.fileselectors.FileInfo;

@Component(role = ContainerDescriptorHandler.class, hint = "sealsdescriptorhandler")
public class SealsDescriptorHandler implements ContainerDescriptorHandler {
    
    protected static final String newline = System.getProperty("line.separator");

    protected String id;
    protected String version;
    protected String description;
    protected String copyright;
    protected String license;
    protected String mainclass;
    protected String projectjar;

    @Override
    public void finalizeArchiveCreation(Archiver archiver) throws ArchiverException {
        archiver.getResources().forEachRemaining(a -> {}); // necessary to prompt the isSelected() call

        String libEntries = getLibEntries(archiver);        
        try {
            archiver.addFile(createStartStopFile("Start"), "bin/start.bat");
            archiver.addFile(createStartStopFile("Stop"), "bin/stop.bat");
            archiver.addFile(createStartStopFile("Deploy"), "bin/deploy.bat");
            archiver.addFile(createStartStopFile("Undeploy"), "bin/undeploy.bat");            
            archiver.addFile(createDescriptor(libEntries, mainclass, projectjar), "descriptor.xml");
        } catch (IOException ex) {
            System.out.println("Couldn't create start stop scripts or descriptor for seals packaging");
        }
    }
    
    @Override
    public List getVirtualFiles() {
        return Arrays.asList(
                "bin/start.bat", 
                "bin/stop.bat", 
                "bin/deploy.bat", 
                "bin/undeploy.bat",
                "descriptor.xml");
    }
    
    protected String getLibEntries(Archiver archiver){
        List<String> libList = new ArrayList<>();
        ResourceIterator ri = archiver.getResources();
        while (ri.hasNext()) {
            String normalisedName = normalise(ri.next().getName());
            if(normalisedName.startsWith("bin/lib/")){                
                libList.add("				<ns:lib>" + normalisedName.substring(4) + "</ns:lib>");
            }
        }
        Collections.sort(libList);
        return String.join(newline, libList);
    }
    
    protected String normalise(String path){
        return path.replace( '\\', '/' ).replace( File.separatorChar, '/' );
    }
    
    @Override
    public void finalizeArchiveExtraction(UnArchiver ua) throws ArchiverException { }

    @Override
    public boolean isSelected(FileInfo fi) throws IOException { return true; }
   

    protected File createStartStopFile(String type) throws IOException {
        File f = File.createTempFile(type.toLowerCase() + ".bat", ".tmp");
        f.deleteOnExit();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write(String.join(newline, Arrays.asList(
            "@echo off",
            "REM Add '" + type + "' business logic.",
            "echo Executing the '" + type + "' capability shell script.",
            "echo  - Deployment directory: %1",
            "exit 1",
            "")));
        }
        return f;
    }
    
    protected File createDescriptor(String libs, String descriptorMainclass, String descriptorProjectjar) throws IOException {
        File f = File.createTempFile("descriptor.xml", ".tmp");
        f.deleteOnExit();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write(String.join(newline, Arrays.asList(
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
"<!--",
"old schema: http://www.seals-project.eu/schemas/2010-08-15/ToolPackageDescriptor.xsd",
"new schema: http://www.seals-project.eu/resources/res/tools/bundle/v1",
"-->",
"",
"<ns:package 	  ",
"        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ",
"	xmlns:ns=\"http://www.seals-project.eu/resources/res/tools/bundle/v1\"",
"	id=\"" + id + "\" ",
"	version=\"" + version + "\">",
"	<ns:description>" + description + "</ns:description>",
"	<ns:endorsement>",
"		<ns:copyright>" + copyright + "</ns:copyright>",
"		<ns:license>" + license + "</ns:license>",
"	</ns:endorsement>",
"	<ns:wrapper>",
"		<ns:management>",
getDescPart("deploy"),
getDescPart("start"),
getDescPart("stop"),
getDescPart("undeploy"),
"		</ns:management>",
"		<ns:bridge>",
"			<!-- references relative to bin folder -->",
"			<ns:class>" + descriptorMainclass + "</ns:class>",
"			<ns:jar>" + descriptorProjectjar + "</ns:jar>",
"			<ns:dependencies>",
libs,
"			</ns:dependencies>",
"		</ns:bridge>",
"	</ns:wrapper>",
"</ns:package>",
"",
"")));
        }
        return f;
    }
    
    protected String getDescPart(String type){
        return String.join(newline, Arrays.asList(
"			<ns:"+ type + ">",
"				<ns:executable xsi:type=\"ns:ShellScript\">",
"					<ns:script>"+ type + ".bat</ns:script>",
"					<ns:error-log>"+ type + "-error.log</ns:error-log>",
"				</ns:executable>",
"			</ns:"+ type + ">"));
    }
    

    public void setId(String id) { this.id = id; }
    public void setVersion(String version) { this.version = version; }
    public void setDescription(String description) { this.description = description; }
    public void setCopyright(String copyright) { this.copyright = copyright; }
    public void setLicense(String license) { this.license = license; }
    public void setMainclass(String mainclass) { this.mainclass = mainclass; }
    public void setProjectjar(String projectjar) { this.projectjar = projectjar; }

}
