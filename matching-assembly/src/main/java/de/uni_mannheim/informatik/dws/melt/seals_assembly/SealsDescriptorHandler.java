package de.uni_mannheim.informatik.dws.melt.seals_assembly;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.compress.utils.IOUtils;
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

        try {
            archiver.addFile(createStartStopFile("Start"), "bin/start.bat");
            archiver.addFile(createStartStopFile("Stop"), "bin/stop.bat");
            archiver.addFile(createStartStopFile("Deploy"), "bin/deploy.bat");
            archiver.addFile(createStartStopFile("Undeploy"), "bin/undeploy.bat");            
            archiver.addFile(createDescriptor(getLibEntries(archiver), "de.uni_mannheim.informatik.dws.melt.matching_base.GenericMatcherCallerFromFile", "matching-base.jar"), "descriptor.xml");
            archiver.addFile(getFileFromResource("matching-base.jar"), "bin/matching-base.jar");
            archiver.addFile(getFileFromText(this.mainclass), "conf/external/main_class.txt");
        } catch (IOException ex) {
            System.out.println("Couldn't create start stop scripts or descriptor for seals packaging");
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
    
    protected List<String> getLibEntries(Archiver archiver){
        List<String> libList = new ArrayList<>();
        ResourceIterator ri = archiver.getResources();
        while (ri.hasNext()) {
            String normalisedName = normalise(ri.next().getName());
            if(normalisedName.startsWith("bin/lib/")){                
                libList.add(normalisedName.substring(4));
            }
        }
        Collections.sort(libList);
        return libList;
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
    
    protected File createDescriptor(List<String> libs, String descriptorMainclass, String descriptorProjectjar) throws IOException {
        File f = File.createTempFile("descriptor.xml", ".tmp");
        f.deleteOnExit();
        
        StringBuilder libsXml= new StringBuilder();
        if(libs.isEmpty() == false){
            libsXml.append("			<ns:dependencies>").append(newline);
            for(String lib : libs){
                libsXml.append("				<ns:lib>").append(lib).append("</ns:lib>").append(newline);
            }
            libsXml.append("			</ns:dependencies>").append(newline);
        }
        
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
libsXml.toString(),
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
    
    
    
    protected File getFileFromText(String content) throws IOException{
        File f = File.createTempFile("matching_file", ".txt");
        f.deleteOnExit();
        try (PrintWriter out = new PrintWriter(f)) {
            out.print(content);
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
}
