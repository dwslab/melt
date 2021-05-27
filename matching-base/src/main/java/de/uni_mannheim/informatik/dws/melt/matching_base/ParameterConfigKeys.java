package de.uni_mannheim.informatik.dws.melt.matching_base;

/**
 * List all the keys (URLs) which can be used as matching parameters.
 * Matchers itself can also define keys (which are just strings).
 */
public class ParameterConfigKeys {


    /**
     * Boolean value (true/false) if matching classes is required (true) or not (false)
     */
    public static final String MATCHING_CLASSES = "http://oaei.ontologymatching.org/matchingClasses";
    
    /**
     * Boolean value (true/false) if matching data properties is required (true) or not (false)
     */
    public static final String MATCHING_DATA_PROPERTIES = "http://oaei.ontologymatching.org/matchingDataProperties";
    
    /**
     * Boolean value (true/false) if matching object properties is required (true) or not (false)
     */
    public static final String MATCHING_OBJECT_PROPERTIES = "http://oaei.ontologymatching.org/matchingObjectProperties";
    
    /**
     * Boolean value (true/false) if matching rdf properties is required (true) or not (false)
     */
    public static final String MATCHING_RDF_PROPERTIES = "http://oaei.ontologymatching.org/matchingRDFProperties";
    
    /**
     * Boolean value (true/false) if matching instances is required (true) or not (false)
     */
    public static final String MATCHING_INSTANCES = "http://oaei.ontologymatching.org/matchingInstances";
    
    /**
     * List of URIs (String) which represent classes. All instances of these classes should
     * be matched (allowlist)
     */
    public static final String MATCHING_INSTANCE_TYPES = "http://oaei.ontologymatching.org/matchingInstanceTypes";
    
    /**
     * List of URIs (String) which represent classes. All instances of these classes should
     * not be matched (blocklist)
     */
    public static final String NON_MATCHING_INSTANCE_TYPES = "http://oaei.ontologymatching.org/nonMatchingInstanceTypes";
    
    /**
     * The main language of the source ontology / knowledge graph.
     * It is a string containing either the <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">ISO 639-1 (also called alpha-2)</a> language code which is default or
     * the <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes">ISO 639-2 (also called alpha-3)</a> language code 
     */
    public static final String SOURCE_LANGUAGE = "http://oaei.ontologymatching.org/sourceLanguage";
    
    /**
     * The main language of the target ontology / knowledge graph.
     * It is a string containing either the <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">ISO 639-1 (also called alpha-2)</a> language code which is default or
     * the <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes">ISO 639-2 (also called alpha-3)</a> language code 
     */
    public static final String TARGET_LANGUAGE = "http://oaei.ontologymatching.org/targetLanguage";
    
    /**
     * The default serialization format for parameters. The value is a string and can contain currently only "json" or "yaml" (caseinsensitive).
     * It is used when the parameters should be convert to e.g. a URL (by writing a file).
     */
    public static final String DEFAULT_PARAMETERS_SERIALIZATION_FORMAT = "http://oaei.ontologymatching.org/defaultParametersSerializationFormat";

    /**
     * The default serialization format for ontologies / knowledge graphs. The value is a string and can contain values <a href="https://jena.apache.org/documentation/io/rdf-output.html">used by jena</a> 
     * e.g. RDF/XML, Turtle, N-Triples, N3, N-Quads, CSV, TSV etc. Usually the method 
     * <a href="http://loopasam.github.io/jena-doc/documentation/javadoc/arq/org/apache/jena/riot/RDFLanguages.html#nameToLang(java.lang.String)">RDFLanguages.html.nameToLang(String)</a> is called.
     * It is used when the ontology should be convert to e.g. a URL (by writing a file). Used in OntModel2URLTransformer.
     */
    public static final String DEFAULT_ONTOLOGY_SERIALIZATION_FORMAT = "http://oaei.ontologymatching.org/defaultOntologySerializationFormat";
    
    /**
     * The folder in which all alignments and properties files are stored. It is a string (path to the folder).
     * The folder has to exist already.
     * The default is the temp directory of the system.
     * It is used in the Alignment2URITransformer in YAAA as wella s in the Properties2URITransformer in the base project.
     */
    public static final String SERIALIZATION_FOLDER = "http://oaei.ontologymatching.org/serializationFolder";

    /**
     * A boolean value (true/false and not a string!) if the ontology cache should be used or not.
     * True means that the ontologies are kept in memory and not directly removed.
     * It is used in URL2OntModelTransformer (and thus as parameter in OntologyCacheJena).
     * If not given, it defaults to true (mean caching is enabled).
     */
    public static final String USE_ONTOLOGY_CACHE = "http://oaei.ontologymatching.org/useOntologyCache";
    
    /**
     * A string value indicating the OntModelSpec for reading a OntModel in Jena.
     * This also contains if reasoning should be applied or not.
     * Possible value are all constants in jena's OntModelSpec class (case sensitive) e.g. OWL_MEM or OWL_DL_MEM_RDFS_INF etc
     * It is used in URL2OntModelTransformer (and thus as parameter in OntologyCacheJena).
     */
    public static final String JENA_ONTMODEL_SPEC = "http://oaei.ontologymatching.org/jenaOntModelSpec";
    
    
    /**
     * A boolean value indicating if an unparsable alignment file should be automatically repaired.
     * This defaults to true.
     * It is used in URL2AlignmentTransformer.
     */
    public static final String ALLOW_ALIGNMENT_REPAIR = "http://oaei.ontologymatching.org/allowAlignmentRepair";
    
    
    /**
     * A string decribing the format of the input files. This parameter is mainly used by HOBBIT.
     */
    public static final String FORMAT = "http://oaei.ontologymatching.org/format";
    
    /**
     * A string decribing the RDF serialization format which is a hint for the reading code.
     * Values can be: RDFXML, TTL, NTriple, NQuad etc (see also the documentation in <a href="https://jena.apache.org/documentation/io/rdf-input.html">jena</a>).
     */
    public static final String HINT_LANG = "http://oaei.ontologymatching.org/format";
}
