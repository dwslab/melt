package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConceptType;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.TypeFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.NLPTransformersFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.TrainingAlignmentGenerator;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorProperty;
import java.io.File;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class SupervisedMatcher extends MatcherYAAAJena {

    private static final Property abstractProp = ResourceFactory.createProperty("http://dbkwik.webdatacommons.org/ontology/abstract");
    private static final TextExtractor extractor = new TextExtractorProperty(abstractProp);
    
    private String gpu;
    private File transformersCache;

    public SupervisedMatcher(String gpu, File transformersCache) {
        this.gpu = gpu;
        this.transformersCache = transformersCache;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {

        Alignment recallAlignment = new BaseMatcher().match(source, target, new Alignment(), properties);
        
        //here we are just interested in instance matches.
        recallAlignment = new TypeFilter(ConceptType.INSTANCE).match(source, target, recallAlignment, properties);
        
        //generate the training examples
        Alignment trainingAlignment = TrainingAlignmentGenerator.getTrainingAlignment(recallAlignment, inputAlignment);        
        
        NLPTransformersFilter filter = new NLPTransformersFilter(extractor, "bert-base-cased-finetuned-mrpc");
        filter.setCudaVisibleDevices(this.gpu);
        filter.setTransformersCache(this.transformersCache);
        //filter.createPredictionFile(source, target, trainingAlignment);
        Alignment alignmentWithConfidences = filter.match(source, target, trainingAlignment, properties);
        
        return alignmentWithConfidences;
    }
}
