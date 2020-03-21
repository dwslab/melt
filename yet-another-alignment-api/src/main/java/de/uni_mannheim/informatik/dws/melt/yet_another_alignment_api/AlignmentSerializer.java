package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;


/**
 * The AlignmentSerializer writes a {@link Alignment} to a file.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class AlignmentSerializer {

    /**
     * Constant for UTF-8 encoding.
     */
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    
    private static final String newline = System.getProperty("line.separator");

    /**
     * Method to write the specified alignment to the specified file
     * @param alignment The alignment that shall be written.
     * @param file The file to which the alignment shall be written.
     * @throws IOException Exception that occurred while serializing the alignment.
     */
    public static void serialize(Alignment alignment, File file) throws IOException {
        
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(getXmlIntro(alignment).getBytes(ENCODING));
            for(Correspondence cell : alignment){
                out.write(getXmlMappingCellMultiLine(cell).getBytes(ENCODING));
            }
            out.write(getXmlOutro().getBytes(ENCODING));
        }        
    }

    /**
     * Serializes an alignment as String.
     * @param alignment The alignment to be serialized.
     * @return Alignment as String.
     */
    public static String serialize(Alignment alignment) {
        StringBuilder sb = new StringBuilder();
        sb.append(getXmlIntro(alignment));
        for(Correspondence cell : alignment){
            sb.append(getXmlMappingCellMultiLine(cell));
        }
        sb.append(getXmlOutro());
        return sb.toString();
    }

    /**
     * XML header.
     * @param alignment Alignment to be serialized.
     * @return XML header as String.
     */
    private static String getXmlIntro(Alignment alignment){
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<rdf:RDF xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment\"\n");
        sb.append("  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
        sb.append("  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n");
        sb.append("<Alignment>\n");
        sb.append("  <xml>yes</xml>\n");
        sb.append("  <level>0</level>\n");
        sb.append("  <type>??</type>\n");

        if(alignment.getExtensions() != null) {
            for (HashMap.Entry<String, String> extension : alignment.getExtensions().entrySet()) {
                String extensionLabel = getExtensionLabel(extension.getKey());
                sb.append("      <alignapilocalns:" + extensionLabel + " xmlns:alignapilocalns=\"" + getExtensionBaseUri(extension.getKey()) + "\">")
                        .append(extension.getValue() + "</alignapilocalns:" + extensionLabel + ">\n");
            }
        }

        serializeOntoInfo(sb, alignment.getOnto1(), "onto1");
        serializeOntoInfo(sb, alignment.getOnto2(), "onto2");
        return sb.toString();
    }
    
    private static String getXmlMappingCellMultiLine(Correspondence cell){
        StringBuilder sb = new StringBuilder();
        sb.append("  <map>\n");
        if(isBlank(cell.getIdentifier())) sb.append("    <Cell>\n");
        else sb.append("    <Cell rdf:about=\"").append(StringEscapeUtils.ESCAPE_XML10.translate(cell.getIdentifier())).append(">\n");
        sb.append("      <entity1 rdf:resource=\"").append(StringEscapeUtils.ESCAPE_XML10.translate(cell.getEntityOne())).append("\"/>\n");
        sb.append("      <entity2 rdf:resource=\"").append(StringEscapeUtils.ESCAPE_XML10.translate(cell.getEntityTwo())).append("\"/>\n");
        sb.append("      <relation>").append(StringEscapeUtils.ESCAPE_XML10.translate(cell.getRelation().toString())).append("</relation>\n");
        sb.append("      <measure rdf:datatype=\"xsd:float\">").append(cell.getConfidence()).append("</measure>\n");
        if(cell.getExtensions() != null){
            for(HashMap.Entry<String, Object> extension : cell.getExtensions().entrySet()){
                String extensionLabel = getExtensionLabel(extension.getKey());
                sb.append("      <alignapilocalns:" + extensionLabel + " xmlns:alignapilocalns=\"" + getExtensionBaseUri(extension.getKey()) + "\">")
                        .append(StringEscapeUtils.ESCAPE_XML10.translate(extension.getValue().toString()) + "</alignapilocalns:" + extensionLabel + ">\n");
            }
        }
        sb.append("    </Cell>\n");
        sb.append("  </map>\n");
        return sb.toString();
    }


    /**
     * Returns the XML footer.
     * @return XML footer as string.
     */
    private static String getXmlOutro(){
        return "</Alignment>\n</rdf:RDF>\n";
    }
    
    private static void serializeOntoInfo(StringBuilder sb, OntoInfo o, String name) {
        if (isBlank(o.getOntoID()) == false) {
            sb.append("  <").append(name).append(">\n");
            sb.append("    <Ontology rdf:about=\"").append(StringEscapeUtils.ESCAPE_XML10.translate(o.getOntoID())).append("\">\n");
            if (isBlank(o.getOntoLocation()) == false) {
                sb.append("      <location>").append(StringEscapeUtils.ESCAPE_XML10.translate(o.getOntoLocation())).append("</location>\n");
            }
            if (isBlank(o.getFormalism()) == false && isBlank(o.getFormalismURI()) == false) {
                sb.append("      <formalism>\n");
                sb.append("        <Formalism align:name=\"").append(StringEscapeUtils.ESCAPE_XML10.translate(o.getFormalism())).append("\" align:uri=\"").append(StringEscapeUtils.ESCAPE_XML10.translate(o.getFormalismURI())).append("\"/>\n");
                sb.append("      </formalism>\n");
            }
            sb.append("    </Ontology>\n");
            sb.append("  </").append(name).append(">\n");
        }
    }

    /**
     * Returns the extension label of a full URI.
     * @param uri Extension URI with base URI and appended extension label.
     *            Example: http://purl.org/dc/elements/1.1/creator or http://exmo.inrialpes.fr/align/ext/1.0/#pretty
     * @return Only the extension label.
     *         Example: creator or pretty
     */
    public static String getExtensionLabel(String uri){
        int lastIndex = uri.lastIndexOf("#");
        if(lastIndex >= 0){
            return uri.substring(lastIndex + 1);
        }
        lastIndex = uri.lastIndexOf("/");
        if(lastIndex >= 0){
            return uri.substring(lastIndex + 1);
        }
        throw new IllegalArgumentException("Key of correspondence extension is not a URI. Please use ExtensionMap to ensure every key is a URI.");
    }

    /**
     * Returns the extension label of a full URI.
     * @param uri Extension URI with base URI and appended extension label.
     *            Example: http://purl.org/dc/elements/1.1/creator or http://exmo.inrialpes.fr/align/ext/1.0/#pretty
     * @return Only the extension label.
     *         Example: http://purl.org/dc/elements/1.1/ or http://exmo.inrialpes.fr/align/ext/1.0/#
     */
    public static String getExtensionBaseUri(String uri){
        int lastIndex = uri.lastIndexOf("#");
        if(lastIndex >= 0){
            return uri.substring(0, lastIndex + 1);
        }
        lastIndex = uri.lastIndexOf("/");
        if(lastIndex >= 0){
            return uri.substring(0, lastIndex + 1);
        }
        throw new IllegalArgumentException("Key of correspondence extension is not a URI. Please use ExtensionMap to ensure every key is a URI.");
    }


    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method to write the specified alignment to the specified file (in CSV format).
     * @param alignment The alignment that shall be written.
     * @param file The file to which the alignment shall be written.
     * @throws IOException Exception that occurred while serializing the alignment.
     */
    public static void serializeToCSV(Alignment alignment, File file) throws IOException {
        
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        //TODO: add also correspondence extensions
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(("source,target,confidence,relation" + newline).getBytes(ENCODING));
            for(Correspondence cell : alignment){
                StringBuilder sb = new StringBuilder();
                sb.append(StringEscapeUtils.escapeCsv(cell.getEntityOne())).append(",");
                sb.append(StringEscapeUtils.escapeCsv(cell.getEntityTwo())).append(",");
                sb.append(Double.toString(cell.confidence)).append(",");
                sb.append(cell.getRelation().toString()).append(newline);
                out.write(sb.toString().getBytes(ENCODING));
            }
        }        
    }
}
