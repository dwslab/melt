package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Class which helps to repair alignment files which are for example not correctly encoded.
 * Thus it is not parsable by a stream parser.
 */
public class AlignmentXmlRepair {


    private static final Logger LOGGER = LoggerFactory.getLogger(AlignmentXmlRepair.class);
    
    private static final Pattern RESOURCE_ATTRIBUTE_PATTERN = Pattern.compile("rdf:resource\\s*=\\s*\"(.+?)\"");
    
     /**
     * Repairs an alignment file inplace.
     * @param alignmentFile The alignment file which should be repaired in place
     */     
    public static void repairInPlace(String alignmentFile){
        repairInPlace(new File(alignmentFile));
    }
    
    /**
     * Repairs an alignment file inplace.
     * @param alignmentFile The alignment file which should be repaired in place
     */     
    public static void repairInPlace(File alignmentFile){
        File tmpFile = new File(alignmentFile.getParentFile(), alignmentFile.getName() + ".tmp");
        repair(alignmentFile, tmpFile);
        alignmentFile.delete();
        tmpFile.renameTo(alignmentFile);
    }
    
    /**
     * Loads a repaired alignment.
     * @param alignmentFile The alignment file
     * @return the repaired alignment
     * @throws IOException An IOException.
     * @throws SAXException A SAXException.
     */ 
    public static Alignment loadRepairedAlignment(File alignmentFile) throws IOException, SAXException{
        File tmpFile = new File(alignmentFile.getParentFile(), alignmentFile.getName() + "-" + Long.toString(FileUtil.getRandomPositiveNumber()) + ".tmp");
        try{
            repair(alignmentFile, tmpFile);
            return new Alignment(tmpFile);
        }finally{
            tmpFile.delete();
        }
    }
    
    /**
     * Repairs an alignment file.
     * @param inFile The alignment file
     * @return the repaired alignment as string.
     */ 
    public static String repair(File inFile){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try(FileInputStream in = new FileInputStream(inFile)) {
            repair(in,out);
        } catch (IOException ex) {
            LOGGER.error("IOException for file to repair", ex);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
    
    /**
     * Repairs an alignment file.
     * @param filePathAlignment The alignment file
     * @param filePathOut The repaired alignment File
     */    
    public static void repair(String filePathAlignment, String filePathOut){
        repair(new File(filePathAlignment), new File(filePathOut));
    }
    
    public static String repair(String text){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        repair(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), stream);
        return new String(stream.toByteArray(), StandardCharsets.UTF_8);
    }
    
    /**
     * Repairs an alignment file.
     * @param inFile The alignment file
     * @param outFile The repaired alignment File
     */
    public static void repair(File inFile, File outFile){
        try {
            repair(new FileInputStream(inFile),new FileOutputStream(outFile));
        } catch (FileNotFoundException ex) {
            LOGGER.error("File to repair not found", ex);
        }
    }    
    
    /**
     * Repairs an alignment file.
     * @param in The alignment stream
     * @param out The repaired alignment stream
     */
    public static void repair(InputStream in, OutputStream out){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))){
                String line;
                while ((line=reader.readLine()) != null) {
                    Matcher m = RESOURCE_ATTRIBUTE_PATTERN.matcher(line);
                    boolean found = m.find();
                    if (found) {
                        StringBuffer sb = new StringBuffer();
                        do{
                            m.appendReplacement(sb, Matcher.quoteReplacement(repairXMLAttribute(m.group(1))));
                            found = m.find();
                        }while(found);
                        m.appendTail(sb);
                        writer.write(sb.toString());
                    }else{
                        writer.write(line);
                    }
                    writer.newLine();
                }
        } catch (IOException ex) {
            LOGGER.error("Can not repair xml of alignment file:", ex);
        }
    }
    
    private static Pattern ESCAPED_AMPERSAND = Pattern.compile("&amp;");
            
    private static String repairXMLAttribute(String attribute){
        return new StringBuilder()
            .append("rdf:resource=\"")
            .append(StringEscapeUtils.ESCAPE_XML10.translate(ESCAPED_AMPERSAND.matcher(attribute).replaceAll("&")))
            .append("\"").toString();
    }
    
    
    
    /**
     * Copy all file from source to destionation directory.
     * If a file within those directories ends with ".rdf", then this file is repaired with the repair function.
     * @param srcDir a source directory which should contain some alignment files (for repairing)
     * @param destDir a destination directory where all files will be copied to
     */
    public static void repairAlignmentFolder(File srcDir, File destDir){
        if (!srcDir.isDirectory()) {
            LOGGER.error("Source '" + srcDir + "' exists but is not a directory");
            return;
        }
        
        File[] srcFiles = srcDir.listFiles();
        if (srcFiles == null){
            LOGGER.error("Failed to list contents of " + srcDir);
            return;
        }
        if (destDir.exists()) {
            if (destDir.isDirectory() == false) {
                LOGGER.error("Destination '" + destDir + "' exists but is not a directory");
                return;
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                LOGGER.error("Destination '" + destDir + "' directory cannot be created");
                return;
            }
        }
        if (destDir.canWrite() == false) {
            LOGGER.error("Destination '" + destDir + "' cannot be written to");
            return;
        }
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());

            if (srcFile.isDirectory()) {
                repairAlignmentFolder(srcFile, dstFile);
            } else {
                if(srcFile.getName().endsWith(".rdf")){
                    repair(srcFile, dstFile);
                }else{
                    try {
                        Files.copy(srcFile.toPath(), dstFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        LOGGER.error("Could not copy a file from source to destination.", ex);
                    }
                }
            }
        }
    }
}
