package de.uni_mannheim.informatik.dws.melt.matching_assembly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    
    private static final String NEWLINE = System.getProperty("line.separator");

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

        try {
            archiver.addFile(createDummyFile("Start"), "bin/start.bat");
            archiver.addFile(createDummyFile("Stop"), "bin/stop.bat");
            archiver.addFile(createDummyFile("Deploy"), "bin/deploy.bat");
            archiver.addFile(createDummyFile("Undeploy"), "bin/undeploy.bat");
            LibsAndBaseVersion libs = new LibsAndBaseVersion(archiver);
            archiver.addFile(createDescriptor(
                    libs.getLibList(), 
                    "de.uni_mannheim.informatik.dws.melt.matching_base.receiver.SealsWrapper", 
                    libs.getMatchingBaseFullFileName()
            ),"descriptor.xml");
            archiver.addFile(getFileFromText(this.mainclass), "conf/external/main_class.txt");
        } catch (IOException ex) {
            throw new ArchiverException("Could not create SEALS archive", ex);
        }
    }
    
    @Override
    public List<String> getVirtualFiles() {
        return Arrays.asList(
                "bin/start.bat", 
                "bin/stop.bat", 
                "bin/deploy.bat", 
                "bin/undeploy.bat",
                "descriptor.xml",
                "bin/matching-base.jar",
                "conf/external/main_class.txt"
        );
    }
    
    @Override
    public void finalizeArchiveExtraction(UnArchiver ua) throws ArchiverException { }

    @Override
    public boolean isSelected(FileInfo fi) throws IOException { return true; }
   

    protected File createDescriptor(List<String> libs, String descriptorMainclass, String descriptorProjectjar) throws IOException {
        File f = File.createTempFile("descriptor.xml", ".tmp");
        f.deleteOnExit();
        
        StringBuilder libsXml= new StringBuilder();
        if(libs.isEmpty() == false){
            libsXml.append(NEWLINE);
            libsXml.append("			<ns:dependencies>").append(NEWLINE);
            for(String lib : libs){
                libsXml.append("				<ns:lib>").append(lib).append("</ns:lib>").append(NEWLINE);
            }
            libsXml.append("			</ns:dependencies>").append(NEWLINE);
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write(String.join(NEWLINE, Arrays.asList(
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
"			<ns:jar>" + descriptorProjectjar + "</ns:jar>" + libsXml.toString(),
"		</ns:bridge>",
"	</ns:wrapper>",
"</ns:package>",
"",
"")));
        }
        return f;
    }
    
    protected String getDescPart(String type){
        return String.join(NEWLINE, Arrays.asList(
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
    
    
    protected File createDummyFile(String type) throws IOException {
        String content = String.join(NEWLINE, Arrays.asList(
            "@echo off",
            "REM Add '" + type + "' business logic.",
            "echo Executing the '" + type + "' capability shell script.",
            "echo  - Deployment directory: %1",
            "exit 1",
            "")
        );
        return getFileFromText(content);
    }
    
    protected File getFileFromText(String content) throws IOException{
        File f = File.createTempFile("matching_file", ".txt");
        f.deleteOnExit();
        try (PrintWriter out = new PrintWriter(f)) {
            out.print(content);
        }
        return f;
    }
}
