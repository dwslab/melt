package de.uni_mannheim.informatik.dws.melt.mldataset;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorSet;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;


public class SBertOnly extends MatcherYAAAJena{

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        SentenceTransformersMatcher biEncoder = new SentenceTransformersMatcher(
            TextExtractor.appendStringPostProcessing(new TextExtractorSet(), StringProcessing::normalizeOnlyCamelCaseAndUnderscore),
            "multi-qa-mpnet-base-dot-v1"//"all-MiniLM-L6-v2"
        );
        biEncoder.setMultipleTextsToMultipleExamples(true);
        //biEncoder.setCudaVisibleDevices(gpu);
        biEncoder.setTopK(5); 
        //biEncoder.setTransformersCache(new File("C:\\dev\\transformer_cache"));
        return biEncoder.match(source, target, inputAlignment, properties);
    }
}
