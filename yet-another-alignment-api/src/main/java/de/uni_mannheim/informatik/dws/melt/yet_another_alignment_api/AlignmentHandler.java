package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The AlignmentHandler manages the parsing of alignment files.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
class AlignmentHandler extends DefaultHandler {


    private static final Logger LOGGER = LoggerFactory.getLogger(AlignmentHandler.class );
    
    private static final String ALIGNMENT = "http://knowledgeweb.semanticweb.org/heterogeneity/alignment";
    private static final String SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String EDOAL = "http://ns.inria.org/edoal/1.0/#";
    
    private static final String RULE_RELATION = "relation";    
    private static final String MEASURE = "measure";
    private static final String ENTITY2 = "entity2";
    private static final String ENTITY1 = "entity1";
    private static final String CELL = "Cell";
    private static final String RDF_RESOURCE = "rdf:resource";
    private static final String Formalism = "Formalism";
    private static final String RDF_ID = "rdf:ID";
    private static final String RDF_ABOUT = "rdf:about";
    private static final String URI = "uri";
    private static final String NAME = "name";
    private static final String ONTOLOGY = "Ontology";
    private static final String ONTO2 = "onto2";
    private static final String ONTO1 = "onto1";
    private static final String ALIGNMENT_NAME = "Alignment";
    private static final String URI1 = "uri1";
    private static final String URI2 = "uri2";
    private static final String LOCATION = "location";
    private static final String TYPE = "type";
    private static final String LEVEL = "level"; 
    private static final String EXTENSION = "alignapilocalns:extensionLabel";

    private StringBuilder content;
    private Alignment alignment;
    private Correspondence cell;
    private OntoInfo currentOntoInfo;

    /**
     * Indicator whether the parser is currently within a cell or not.
     * This is used to determine whether extensions are made on alignment level or on correspondence level.
     * When starting the parsing process, the initial state is false.
     */
    private boolean inCorrespondence = false;

    /**
     * Constructor
     * @param alignment Alignment which will hold parsed elements.
     */
    public AlignmentHandler(Alignment alignment){
        this.content = null;    
        this.alignment = alignment;
        this.cell = new Correspondence();
        this.currentOntoInfo = new OntoInfo();
    }

    @Override
    public void startElement( String namespaceURI, String pName, String qName, Attributes atts ) throws SAXException {
        //LOGGER.trace( "startElement XMLParser : {}", pName );
        if(namespaceURI.equals(ALIGNMENT+"#") || namespaceURI.equals(ALIGNMENT)){
            if (pName.equals( ENTITY2 )) {
                cell.setEntityTwo(atts.getValue(RDF_RESOURCE));
            } else if (pName.equals(ENTITY1)) {
                cell.setEntityOne(atts.getValue(RDF_RESOURCE));
            } else if (pName.equals(CELL)) {
                this.inCorrespondence = true;
                this.cell = new Correspondence();
                if ( atts.getValue(RDF_ID) != null ){
                    this.cell.setIdentifier(atts.getValue(RDF_ID));
                } else if ( atts.getValue(RDF_ABOUT) != null ){
                    this.cell.setIdentifier(atts.getValue(RDF_ABOUT));
                }
            } else if (pName.equals(Formalism)) {
                if ( atts.getValue(URI) != null ){
                    this.currentOntoInfo.setFormalismURI(atts.getValue(URI));
                } else if ( atts.getValue(NAME) != null ){
                    this.currentOntoInfo.setFormalism(atts.getValue(NAME));
                }
            } else if (pName.equals(ONTOLOGY)) {
                if ( atts.getValue(RDF_ABOUT) != null ){
                    this.currentOntoInfo.setOntoID(atts.getValue(RDF_ABOUT));
                }
            } else if (pName.equals(ONTO2)) {
                this.currentOntoInfo = this.alignment.getOnto2();
            } else if (pName.equals(ONTO1)) {
                this.currentOntoInfo = this.alignment.getOnto1();
            } else if (pName.equals(ALIGNMENT_NAME)) {
                //this.alignment = new Alignment(); //already initialized
            }
        } else if ( namespaceURI.equals(SOAP_ENV)) {
            // Ignore SOAP namespace
            if ( !pName.equals("Envelope") && !pName.equals("Body") ) {
                throw new SAXException("[XMLParser] unknown element name: "+pName); };
        } else if (namespaceURI.equals(RDF)) {
            if ( !pName.equals("RDF") ) {
                throw new SAXException("[XMLParser] unknown element name: "+pName); };
        } else if (namespaceURI.equals(EDOAL)) { 
            throw new SAXException("[XMLParser] EDOAL alignment must have type EDOAL: "+pName);
        }
        content = new StringBuilder();
    }

    @Override
    public void characters( char ch[], int start, int length ) {
        if (content != null)
            content.append(new String( ch, start, length ));
        //use all charcters and reset it in start element.
        //because it may happen that the parser call charcters method multiple times.
        //when having the corenlp dependency in the environment, then there are multiple calls.
        //https://gforge.inria.fr/scm/viewvc.php/alignapi/trunk/src/fr/inrialpes/exmo/align/parser/XMLParser.java?view=markup#l403
        
        //this was from procalign
        //String newContent = new String( ch, start, length );
        //if ( content != null && content.indexOf('.',0) != -1 && newContent != null && !newContent.startsWith("\n ")) {
        //    content += newContent;
        //} else {
        //    content = newContent; 
        //}
        //LOGGER.trace( "content XMLParser : {}", content );
    }

    @Override
    public  void endElement( String namespaceURI, String pName, String qName ) throws SAXException {
        //LOGGER.trace( "endElement XMLParser : {}", pName );
        if(namespaceURI.equals(ALIGNMENT+"#") || namespaceURI.equals(ALIGNMENT)){
            if (pName.equals(RULE_RELATION)) {
                this.cell.setRelation(CorrespondenceRelation.parse(content.toString()));
            } else if (pName.equals(MEASURE)) {
                this.cell.setConfidence(Double.parseDouble(content.toString()));
            } else if (pName.equals(CELL)) {
                if ( this.cell.getEntityOne() == null || this.cell.getEntityTwo() == null) {
                    LOGGER.warn( "(cell voided), missing entity {} {}", this.cell.getEntityOne(), this.cell.getEntityTwo() );
                } else{
                    this.alignment.add(cell);
                }
                this.inCorrespondence = false;
            } else if (pName.equals(URI1)) {                
                this.alignment.getOnto1().setOntoLocation(content.toString());
            } else if (pName.equals(URI2)) {
                this.alignment.getOnto2().setOntoLocation(content.toString());
            } else if (pName.equals(LOCATION)) {
                this.currentOntoInfo.setOntoLocation(content.toString());
            } else if (pName.equals( ONTO1 ) || pName.equals(ONTO2)) {
                if (this.currentOntoInfo.getOntoLocation().equals("") && content != null && !content.toString().equals("")) {
                    this.currentOntoInfo.setOntoLocation(content.toString());
                    if(this.currentOntoInfo.getOntoID().equals(""))
                        this.currentOntoInfo.setOntoID(content.toString());
                };
                this.currentOntoInfo = null;
            } else if (pName.equals(TYPE)) {
                this.alignment.setType(content.toString());
            } else if (pName.equals(LEVEL)) {
                if ( content.toString().startsWith("2") ) { // Maybe !startsWith("0") would be better
                    throw new SAXException("Cannot parse Level 2 alignments (so far).");
                } else {
                    this.alignment.setLevel(content.toString());
                }
            }
        }  else if ( namespaceURI.equals(SOAP_ENV)) {
            // Ignore SOAP namespace
            if ( !pName.equals("Envelope") && !pName.equals("Body") ) {
                throw new SAXException("[XMLParser] unknown element name: "+pName); };
        } else if (namespaceURI.equals(RDF)) {
            if ( !pName.equals("RDF") ) {
                throw new SAXException("[XMLParser] unknown element name: "+pName); };
        } else if (namespaceURI.equals(EDOAL)) { 
            throw new SAXException("[XMLParser] EDOAL alignment must have type EDOAL: "+pName);
        } else {
            // we are parsing an extension
            if(inCorrespondence) {
                this.cell.addExtensionValue(namespaceURI + pName, content.toString());
            } else {
                this.alignment.addExtensionValue(namespaceURI + pName, content.toString());
            }
        }
        //uncommented because it is initialised in start element.
        //content = null; // set it for the character patch
    }
    
    public Alignment getAlignment(){
        return this.alignment;
    }
}
