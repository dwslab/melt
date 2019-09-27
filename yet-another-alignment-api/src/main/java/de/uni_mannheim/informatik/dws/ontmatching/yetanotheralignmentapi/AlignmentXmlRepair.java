package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
    
    private static final Pattern resourceAttributePattern = Pattern.compile("rdf:resource\\s*=\\s*\"(.+?)\"");
    
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
     */ 
    public static Alignment loadRepairedAlignment(File alignmentFile) throws IOException, SAXException{
        File tmpFile = new File(alignmentFile.getParentFile(), alignmentFile.getName() + ".tmp");
        repair(alignmentFile, tmpFile);
        Alignment a = new Alignment(tmpFile);
        tmpFile.delete();
        return a;
    }
    
    /**
     * Repairs an alignment file.
     * @param inFile The alignment file
     * @return the repaired alignment as string.
     */ 
    public static String repair(File inFile){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            repair(new FileInputStream(inFile),out);
        } catch (FileNotFoundException ex) {
            LOGGER.error("File to repair not found", ex);
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
                    Matcher m = resourceAttributePattern.matcher(line);
                    boolean found = m.find();
                    if (found) {
                        StringBuffer sb = new StringBuffer();
                        do{
                            m.appendReplacement(sb, repairXMLAttribute(m.group(1)));
                            found = m.find();
                        }while(found);
                        m.appendTail(sb);
                        writer.write(sb.toString());
                    }else{
                        writer.write(line);
                    }
                    writer.newLine();
                }
                reader.close();
        } catch (IOException ex) {
            LOGGER.error("Can not repair xml of alignment file:", ex);
        }
    }
    
    private static String repairXMLAttribute(String attribute){
        return new StringBuilder()
            .append("rdf:resource=\"")
            .append(StringEscapeUtils.ESCAPE_XML10.translate(attribute))
            .append("\"").toString();
    }
}
