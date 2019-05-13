package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.OntologyCacheJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * A TestCase is an individual matching task that may be a component of a {@link Track}.
 */
public class TestCase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCase.class);

    private URI source;
    private URI target;
    private URI reference;
    /**this is the identifier for the track*/
    private String name;
    private Track track;
    
    private Alignment parsedReference;
    

    public TestCase(String name, URI source, URI target, URI reference, Track track) {
        this.name = name;
        this.source = source;
        this.target = target;
        this.reference = reference;
        this.track = track;
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

    public String getName() {
        return name;
    }
    
    public Track getTrack() {
        return track;
    }
    
    @Override
    public String toString() {
        return "Testcase " + name + " of " + track.toString() + "(src: " + source + " dst: " + target + " ref: " + reference + ")";
    }
    
    //convenient methods for retriving parsed ontologies
    
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
    
    
    
    //override hascode and equals: a testcase is equal when name and track is equal
    
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
