package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_owlapi.OntologyCacheOwlApi;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import org.apache.jena.ontology.OntModel;
import org.semanticweb.owlapi.model.OWLOntology;
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
     * How complete is the gold standard for this test case.
     */
    private GoldStandardCompleteness goldStandardCompleteness;

    /**
     * Constructor
     * @param name Name of the test case.
     * @param source URI to the source ontology.
     * @param target URI to the target ontology.
     * @param reference URI to the alignment reference file.
     * @param track The track to which the test case belongs.
     * @param inputAlignment The input alignment for the matcher.
     * @param goldStandardCompleteness How complete is the gold standard for this test case.
     */
    public TestCase(String name, URI source, URI target, URI reference, Track track, URI inputAlignment, GoldStandardCompleteness goldStandardCompleteness) {
        this.name = name;
        this.track = track;
        this.source = source;
        this.target = target;
        this.reference = reference;
        this.inputAlignment = inputAlignment;
        this.goldStandardCompleteness = goldStandardCompleteness;
    }

    /**
     * Constructor with a complete gold standard and not input alignment.
     * @param name Name of the test case.
     * @param source URI to the source ontology.
     * @param target URI to the target ontology.
     * @param reference URI to the alignment reference file.
     * @param track The track to which the test case belongs.
     */
    public TestCase(String name, URI source, URI target, URI reference, Track track) {
        this(name, source, target, reference, track, null, GoldStandardCompleteness.COMPLETE);
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
    
    @Override
    public String toString() {
        return "Testcase " + name + " of " + track.toString() + "(src: " + source + " dst: " + target + " ref: " + reference + ")";
    }
    
    //convenient methods for retrieving parsed ontologies
    
    /**
     * Get the source ontology using a buffer and the default OntModelSpec.
     * @param clazz The result type that is expected.
     * @param <T> Type of the ontology class e.g. OntModel
     * @return Source ontology in the specified format.
     */
    public <T> T getSourceOntology(Class<T> clazz){
        if(clazz == OntModel.class){
            // return ontology model using default specification and cache
            return (T) OntologyCacheJena.get(getSource().toString());
        }else if(clazz == OWLOntology.class){
            return (T) OntologyCacheOwlApi.get(getSource().toString());
        } else {
            LOGGER.error("Cannot get source ontology for the class type provided.");
            return null;
        }
    }


    /**
     * Get the target ontology using a buffer and the default OntModelSpec.
     * @param clazz The result type that is expected.
     * @param <T> Type of the ontology class e.g. OntModel
     * @return Target ontology in the specified format.
     */
    public <T> T getTargetOntology(Class<T> clazz){
        if(clazz == OntModel.class){
            // return ontology model using default specification and cache
            return (T) OntologyCacheJena.get(getTarget().toString());
        } else {
            LOGGER.error("Cannot get source ontology for the class type provided.");
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
