package de.uni_mannheim.informatik.dws.melt.matching_data;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * A TestCase is an individual matching task that may be a component of a {@link Track}.
 */
public class TestCase {


    private static final Logger LOGGER = LoggerFactory.getLogger(TestCase.class);

    /** 
     * This is the identifier for the test case 
     */
    private final String name;

    /** 
     * A test case always belongs to a track. 
     */
    private final Track track;
    
    /**
     * URI pointing to the source file for matching.
     */
    private final URI source;

    /**
     * URI pointing to the target file for matching.
     */
    private final URI target;

    /**
     * URI pointing to the reference file.
     */
    private final URI reference;
    
    /**
     * The parsed reference which is initialized lazily.
     */
    private Alignment parsedReference;
        
    /**
     * Input alignment for the matcher. It is null in case there is no inputAlignment.
     */
    private URI inputAlignment;
    
    /**
     * The parsed input alignment which is initialized lazily.
     */
    private Alignment parsedInputAlignment;
    
    /**
     * How complete is the gold standard for this test case.
     */
    private GoldStandardCompleteness goldStandardCompleteness;
    
    /**
     * URI pointing to the parameter file. Can be null (which means no parameters at all).
     * The format can currently be JSON and YAML. The available keys are listed in {@link ParameterConfigKeys}
     */
    private URI parameters;

    /**
     * Constructor
     * @param name Name of the test case.
     * @param source URI to the source ontology.
     * @param target URI to the target ontology.
     * @param reference URI to the alignment reference file.
     * @param track The track to which the test case belongs.
     * @param inputAlignment The input alignment for the matcher.
     * @param goldStandardCompleteness How complete is the gold standard for this test case.
     * @param parameters the parameters which the matcher get.
     */
    public TestCase(String name, URI source, URI target, URI reference, Track track, URI inputAlignment, GoldStandardCompleteness goldStandardCompleteness, URI parameters) {
        this.name = name;
        this.track = track;
        this.source = source;
        this.target = target;
        this.reference = reference;
        this.inputAlignment = inputAlignment;
        this.goldStandardCompleteness = goldStandardCompleteness;
        this.parameters = parameters;
    }

    /**
     * Constructor with a complete gold standard and no input alignment.
     * @param name Name of the test case.
     * @param source URI to the source ontology.
     * @param target URI to the target ontology.
     * @param reference URI to the alignment reference file.
     * @param track The track to which the test case belongs.
     */
    public TestCase(String name, URI source, URI target, URI reference, Track track) {
        this(name, source, target, reference, track, null, GoldStandardCompleteness.COMPLETE, null);
    }

    
    public String getName() {
        return name;
    }
    
    public Track getTrack() {
        return track;
    }
    
    public URI getSource() {
        return source;
    }

    public URI getTarget() {
        return target;
    }

    public URI getReference() {
        return reference;
    }

    public URI getInputAlignment() {
        return inputAlignment;
    }

    public GoldStandardCompleteness getGoldStandardCompleteness() {
        return goldStandardCompleteness;
    }

    public URI getParameters() {
        return parameters;
    }
    
    @Override
    public String toString() {
        return "Testcase " + name + " of " + track.toString() + "(src: " + source + " dst: " + target + " input: " + inputAlignment + " ref: " + reference +  ")";
    }
    
    //convenient methods for retrieving parsed ontologies
    
    /**
     * Get the source ontology.
     * @param clazz The result type that is expected.
     * @param <T> Type of the ontology class e.g. OntModel
     * @return Source ontology in the specified format.
     */
    public <T> T getSourceOntology(Class<T> clazz){
        return getSourceOntology(clazz, new Properties());
    }
    
    /**
     * Get the source ontology.
     * @param clazz The result type that is expected.
     * @param parameters parameters for the transformation of URL to corresponding model type.
     * @param <T> Type of the ontology class e.g. OntModel
     * @return Source ontology in the specified format.
     */
    public <T> T getSourceOntology(Class<T> clazz, Properties parameters){
        try {
            return TypeTransformerRegistry.getTransformedObject(getSource().toURL(), clazz, parameters);
        } catch (MalformedURLException | TypeTransformationException ex) {
            LOGGER.error("Could not return the parsed source ontology.", ex);
            return null;
        }
    }
    
    /**
     * Get the target ontology.
     * @param clazz The result type that is expected.
     * @param <T> Type of the ontology class e.g. OntModel
     * @return Target ontology in the specified format.
     */
    public <T> T getTargetOntology(Class<T> clazz){
        return getTargetOntology(clazz, new Properties());
    }
    
    /**
     * Get the target ontology.
     * @param clazz The result type that is expected.
     * @param parameters parameters for the transformation of URL to corresponding model type.
     * @param <T> Type of the ontology class e.g. OntModel
     * @return Target ontology in the specified format.
     */
    public <T> T getTargetOntology(Class<T> clazz, Properties parameters){
        try {
            return TypeTransformerRegistry.getTransformedObject(getTarget().toURL(), clazz, parameters);
        } catch (MalformedURLException | TypeTransformationException ex) {
            LOGGER.error("Could not return the parsed source ontology.", ex);
            return null;
        }
    }

    /**
     * This method parses the reference alignment and returns it.
     * If called again, a cached parsed instance will be returned.
     * @return Parsed reference {@link Alignment}.
     */
    public Alignment getParsedReferenceAlignment() {
        if(parsedReference == null){
            try {
                parsedReference = new Alignment(getReference().toURL());
            } catch (SAXException | IOException ex) {
                LOGGER.error("Could not parse reference alignment file. Return null.", ex);
            }
        }
        return parsedReference;
    }
    
    /**
     * This method parses the input alignment and returns it.
     * If called again, a cached parsed instance will be returned.
     * @return Parsed input {@link Alignment}.
     */
    public Alignment getParsedInputAlignment() {
        if(parsedInputAlignment == null){
            if(getInputAlignment() == null){
                parsedInputAlignment =  new Alignment();
            }else{
                try {
                    parsedInputAlignment = new Alignment(getInputAlignment().toURL());
                } catch (SAXException | IOException ex) {
                    LOGGER.error("Could not parse reference alignment file. Return null.", ex);
                }
            }
        }
        return parsedInputAlignment;
    }
    
    
    /**
     * This method parses the parameters and returns it in the given class type.
     * If no parameters are given, then a new instance of the required class is returned (if possible).
     * @param <T> the type of parameter class
     * @param type the requested type
     * @return Parsed parameters.
     */
    public <T> T getParsedParameters(Class<T> type) {
        return TypeTransformerRegistry.getTransformedObjectOrNewInstance(this.parameters, type);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.name);
        hash = 23 * hash + Objects.hashCode(this.track);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCase other = (TestCase) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.track, other.track)) {
            return false;
        }
        return true;
    }
}
