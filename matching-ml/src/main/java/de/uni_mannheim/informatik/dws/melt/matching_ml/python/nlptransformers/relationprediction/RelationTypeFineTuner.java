package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.relationprediction;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This matcher predicts the relation type given a transformer model.
 * This component do not create new correspondences but refine the relation of given class correspondences.
 */
public class RelationTypeFineTuner extends TransformersFineTuner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelationTypeFineTuner.class);
    private static final String NEWLINE = System.getProperty("line.separator");
    
    public RelationTypeFineTuner(TextExtractor extractor, String modelName, File resultingModelLocation) {
        super(extractor, modelName,resultingModelLocation);
    }
    
    public RelationTypeFineTuner(TextExtractorMap extractor, String modelName, File resultingModelLocation) {
        super(extractor, modelName,resultingModelLocation);
    }
    
    
    @Override
    public int writeTrainingFile(OntModel source, OntModel target, Alignment trainingAlignment, File trainFile, boolean append) throws IOException{
        int examples = 0;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile, append), StandardCharsets.UTF_8))){
            examples += writeRelationPredictionFormat(source, writer);
            examples += writeRelationPredictionFormat(target, writer);
        }
        return examples;
    }
    
    protected int writeRelationPredictionFormat(Model model, Writer writer) throws IOException{
        int subclassOfExamples = 0;
        int superclassExamples = 0;
        StmtIterator stmts = model.listStatements(null, RDFS.subClassOf, (RDFNode) null);
        while(stmts.hasNext()){
            Statement s = stmts.next();            
            if(s.getObject().isResource() == false){
                continue;
            }
            
            Map<String, Set<String>> subjectMap = this.extractor.extract(s.getSubject());
            Map<String, Set<String>> objectMap = this.extractor.extract(s.getObject().asResource());
            
            for(Entry<String, Set<String>> textLeftGroup : subjectMap.entrySet()){
                for(String textRight : objectMap.get(textLeftGroup.getKey())){
                    for(String textLeft : textLeftGroup.getValue()){
                        writer.write(StringEscapeUtils.escapeCsv(textLeft) + "," + StringEscapeUtils.escapeCsv(textRight) +  ",1" + NEWLINE);
                        subclassOfExamples++;
                        
                        writer.write(StringEscapeUtils.escapeCsv(textRight) + "," + StringEscapeUtils.escapeCsv(textLeft) +  ",2" + NEWLINE);
                        superclassExamples++;
                    }
                }
            }
        }
        
        //equivalence
        
        //part of
        
        
        LOGGER.info("Wrote {} subclass, {} superclass training examples.", subclassOfExamples, superclassExamples);
        
        return subclassOfExamples + superclassExamples;
    }
}
