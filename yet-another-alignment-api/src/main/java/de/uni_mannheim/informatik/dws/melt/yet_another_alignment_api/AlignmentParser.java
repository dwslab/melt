package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.io.File;
import java.io.FileInputStream;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

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
}
    
