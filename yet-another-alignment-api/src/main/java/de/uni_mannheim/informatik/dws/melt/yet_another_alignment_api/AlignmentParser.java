package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.io.File;
import java.io.FileInputStream;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The AlignmentParser can parse XML files following the convention described in the
 * <a href="http://alignapi.gforge.inria.fr/format.html">Alignment Format</a>. Note that currently EDOAL is not
 * supported.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class AlignmentParser {

    /**
     * Default logger.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(AlignmentParser.class);


    private static final ThreadLocal<SAXParser> threadLocal = new ThreadLocal<SAXParser>(){
        @Override
        protected SAXParser initialValue() {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating( false );
            parserFactory.setNamespaceAware( true );
            SAXParser parser = null;
            try {
                parser = parserFactory.newSAXParser();
            } catch (ParserConfigurationException | SAXException ex) {
                LOGGER.warn("Could not create Sax Parser", ex);
            }
            return parser;
        }
    };

    /**
     * Reference the alignment file to be parsed using a String URI.
     * @param uri URI as String. Alternatively a file path can be specified.
     * @return Parsed alignment instance.
     * @throws SAXException Parsing exception.
     * @throws IOException IO exception.
     */
    public static Alignment parse(String uri) throws SAXException, IOException{
        try {
            return parse(new URL(uri));
        } catch (MalformedURLException mue){
            LOGGER.warn("Malformed URL exception. Trying to parse URI as File Path...");
            return parse(new File(uri));
        }
    }

    /**
     * Reference the alignment file to be parsed using a String URI.
     * @param uri URI as String.
     * @return Parsed alignment instance.
     * @throws SAXException Parsing exception.
     * @throws IOException IO exception.
     */
    public static Alignment parse(URI uri) throws SAXException, IOException{
        return parse(uri.toURL());
    }
    
    public static Alignment parse(URL url) throws SAXException, IOException{
        return parse(getInputStreamFromURL(url));
    }

    /**
     * Parses the given file as alignment.
     * @param fileToBeParsed The file that shall be parsed.
     * @return Parsed alignment instance.
     * @throws SAXException A SAXException.
     * @throws IOException An IOException.
     */
    public static Alignment parse(File fileToBeParsed) throws SAXException, IOException {
	return parse(new FileInputStream(fileToBeParsed));
    }
    
    public static Alignment parse(InputStream s) throws SAXException, IOException {
        Alignment m = new Alignment();
        AlignmentHandler p = new AlignmentHandler(m);
        threadLocal.get().parse(s, p);
        return p.getAlignment();
    }
    
    public static void parse(InputStream s, Alignment m) throws SAXException, IOException {
        AlignmentHandler p = new AlignmentHandler(m);
        threadLocal.get().parse(s, p);
    }
    
    
    public static InputStream getInputStreamFromURL(URL url) throws IOException{
        URLConnection connection = url.openConnection();
        connection.setRequestProperty( "Accept", "text/xml, application/rdf+xml" );
        return connection.getInputStream();
    }
   
    /**
     * Parse alignment from CSV (comma separated file) with header (source, target, confidence, relation).
     * The extensions are not parsed.
     * @param file the file to read from
     * @return the parsed alignment
     * @throws java.io.IOException thrown if some io error occurs.
     */
    public static Alignment parseCSV(File file) throws IOException{
        Alignment a = new Alignment();
        try(CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            for (CSVRecord record : csvParser) {
                Correspondence correspondence = new Correspondence(record.get("source").trim(), record.get("target").trim());
                String confidence = getRecordEntry(record, "confidence");
                if(!confidence.isEmpty()){
                    correspondence.setConfidence(Double.parseDouble(confidence));
                }
                String relation = getRecordEntry(record, "relation");
                if(!relation.isEmpty()){
                    correspondence.setRelation(CorrespondenceRelation.parse(relation));
                }
                a.add(correspondence);
            }
        }
        return a;
    }
    
    private static String getRecordEntry(CSVRecord record, String name){
        if(record.isSet(name)){
            return record.get(name).trim();
        }
        return "";
    }
    
    private static String getRecordEntry(CSVRecord record, int index){
        if(record.isSet(index)){
            return record.get(index).trim();
        }
        return "";
    }
    
    /**
     * Parse alignment from CSV (comma separated file) without header. The order is: (source, target, confidence, relation).
     * The extensions are not parsed.
     * @param file the file to read from
     * @return the parsed alignment
     * @throws java.io.IOException thrown if some io error occurs.
     */
    public static Alignment parseCSVWithoutHeader(File file) throws IOException{
        return parseCSVWithoutHeader(file, ',');
    }
    
    /**
     * Parse alignment from CSV (comma separated file) without header. The order is: (source, target, confidence, relation).
     * The extensions are not parsed.
     * @param file the file to read from
     * @param delimiter the delimiter to use
     * @return the parsed alignment
     * @throws java.io.IOException thrown if some io error occurs.
     */
    public static Alignment parseCSVWithoutHeader(File file, char delimiter) throws IOException{
        Alignment a = new Alignment();
        try(CSVParser csvParser = CSVFormat.DEFAULT.withDelimiter(delimiter).parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            for (CSVRecord record : csvParser) {
                Correspondence correspondence = new Correspondence(record.get(0).trim(), record.get(1).trim());
                String confidence = getRecordEntry(record, 2);
                if(!confidence.isEmpty()){
                    correspondence.setConfidence(Double.parseDouble(confidence));
                }
                String relation = getRecordEntry(record, 3);
                if(!relation.isEmpty()){
                    correspondence.setRelation(CorrespondenceRelation.parse(relation));
                }
                a.add(correspondence);
            }
        }
        return a;
    }
    
    /**
     * Parse alignment from TSV (tab separated file) without header.The columns are source \t target \t confidence \t relation.
     * The relation and confidence can be left out as long as the position stays the same (e.g. relation is always on index 3).
     * The files are usually generated by PARIS matcher.
     * @param file the file to read from
     * @return the parsed alignment
     * @throws java.io.IOException thrown if some io error occurs.
     */
    public static Alignment parseTSV(File file) throws IOException{
        return parseCSVWithoutHeader(file, '\t');
    }
    
}
    
