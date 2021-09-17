package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.Iterator;
import java.util.Set;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;

/**
 * This matcher uses the <a href="https://github.com/UKPLab/sentence-transformers">Sentence Transformers library</a> to build an embedding space for each resource given a textual representation of it.
 * Thus this matcher does not filter anything but generates matching candidates based on the text.
 */
public class SentenceTransformersMatcher extends TransformersBase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceTransformersMatcher.class);
    private static final String NEWLINE = System.getProperty("line.separator");
    
    private List<ResourcesExtractor> resourcesExtractor;
    private int queryChunkSize;
    private int corpusChunkSize;
    private int topK;
    private boolean bothDirections;
    
    public SentenceTransformersMatcher(TextExtractor extractor, String modelName){
        super(extractor, modelName);
        initialiseResourceExtractor();
        this.queryChunkSize = 100;
        this.corpusChunkSize = 500000;
        this.topK = 10;
        this.bothDirections = true;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {
        
        if(inputAlignment == null)
            inputAlignment = new Alignment();
        for(ResourcesExtractor resExtractor : resourcesExtractor){            
            File corpus = FileUtil.createFileWithRandomNumber(this.tmpDir, "corpus", ".txt");
            File queries = FileUtil.createFileWithRandomNumber(this.tmpDir, "queries", ".txt");
            try{
                int linesWrittenSource = createTextFile(source, corpus, resExtractor, parameters);
                int linesWrittenTarget = createTextFile(target, queries, resExtractor, parameters);
                if(linesWrittenSource == 0 || linesWrittenTarget == 0){
                    continue; // nothing to match. skip it.
                }
                LOGGER.info("Written {} source and {} target text representations", linesWrittenSource, linesWrittenTarget);

                //run python
                Alignment alignment = PythonServer.getInstance().sentenceTransformersPrediction(this, corpus, queries);
                //add correspondences
                for(Correspondence c : alignment){
                    c.addAdditionalConfidence(this.getClass(), c.getConfidence());
                    inputAlignment.addOrModify(c);
                }
            }
            finally{
                corpus.delete();
                queries.delete();
            }
        }
        return inputAlignment;
    }
    
    
    private int createTextFile(OntModel model, File file, ResourcesExtractor extractor, Properties parameters) throws IOException {
        //LOGGER.info("Write text to file {}", file);
        int linesWritten = 0;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))){
            Iterator<? extends OntResource> resourceIterator = extractor.extract(model, parameters);
            if(this.multipleTextsToMultipleExamples){
                while(resourceIterator.hasNext()){
                    OntResource r = resourceIterator.next();
                    if(!r.isURIResource())
                        continue;
                    for(String text : this.extractor.extract(r)){
                        text = text.trim();
                        if(text.isEmpty())
                            continue;
                        writer.write(StringEscapeUtils.escapeCsv(r.getURI()) + "," + StringEscapeUtils.escapeCsv(text) + NEWLINE);
                        linesWritten++;
                    }
                }
            }else{
                while(resourceIterator.hasNext()){
                    OntResource r = resourceIterator.next();
                    if(!r.isURIResource())
                        continue;
                    StringBuilder sb = new StringBuilder();
                    for(String text : this.extractor.extract(r)){
                        sb.append(text.trim()).append(" ");
                    }
                    String text = sb.toString().trim();
                    if(text.isEmpty())
                        continue;
                    writer.write(StringEscapeUtils.escapeCsv(r.getURI()) + "," + StringEscapeUtils.escapeCsv(text) + NEWLINE);
                    linesWritten++;
                }
            }
        }
        return linesWritten;
    }
    
    
    //getter setter
    /**
     * Initilaises the resource extractor such that classes, datatypeproperties, objectproperties, all other properties,
     * and instances are matched if the properties suggests to do so.
     */
    public void initialiseResourceExtractor() {
        this.resourcesExtractor = new ArrayList<>();
        //TODO: use parameters to determine if classes whould be matched etc
        this.resourcesExtractor.add((model, parameters) -> model.listClasses());
        this.resourcesExtractor.add((model, parameters) -> model.listDatatypeProperties());
        this.resourcesExtractor.add((model, parameters) -> model.listObjectProperties());
        this.resourcesExtractor.add((model, parameters) -> {
            Set<OntProperty> allProperties = model.listAllOntProperties().toSet();
            allProperties.removeAll(model.listObjectProperties().toSet());
            allProperties.removeAll(model.listDatatypeProperties().toSet());
            return allProperties.iterator();
       });
       this.resourcesExtractor.add((model, parameters) -> model.listIndividuals());
    }

    public List<ResourcesExtractor> getResourcesExtractor() {
        return resourcesExtractor;
    }

    public void setResourcesExtractor(List<ResourcesExtractor> resourcesExtractor) {
        this.resourcesExtractor = resourcesExtractor;
    }

    /**
     * Returns the number of queries which are processed simultaneously.
     * @return the number of queries which are processed simultaneously
     */
    public int getQueryChunkSize() {
        return queryChunkSize;
    }

    /**
     * Sets the number of queries which are processed simultaneously.
     * Increasing that value increases the speed, but requires more memory.
     * The default value is 100.
     * @param queryChunkSize number of queries which are processed simultaneously
     */
    public void setQueryChunkSize(int queryChunkSize) {
        this.queryChunkSize = queryChunkSize;
    }

    /**
     * Returns the number of enties which are scaned at a time.
     * Increasing that value increases the speed, but requires more memory.
     * The default value is 500000.
     * @return the number of enties which are scaned at a time
     */
    public int getCorpusChunkSize() {
        return corpusChunkSize;
    }

    /**
     * Sets the number of enties which are scaned at a time.
     * Increasing that value increases the speed, but requires more memory.
     * The default value is 500000.
     * @param corpusChunkSize the number of enties which are scaned at a time
     */
    public void setCorpusChunkSize(int corpusChunkSize) {
        this.corpusChunkSize = corpusChunkSize;
    }

    /**
     * Returns the number which represents how many correspondences should be created per resource.
     * @return the number which represents how many correspondences should be created per resource
     */
    public int getTopK() {
        return topK;
    }

    /**
     * Sets the number which represents how many correspondences should be created per resource.
     * The default is 10
     * @param topK the number which represents how many correspondences should be created per resource
     */
    public void setTopK(int topK) {
        this.topK = topK;
    }

    /**
     * Returns true if both directions are enabled. This means the left ontology is once the query and once the corpus.
     * Thus each element from the source AND target ontologies has at least number of topK corresponding entities.
     * @return true, if source and target ontology are both query and corpus.
     */
    public boolean isBothDirections() {
        return bothDirections;
    }

    /**
     * Sets the value if both directions are enabled. If true (the default value), the source and target ontology is once the query and once the corpus.
     * Thus each element from the source AND target ontologies has at least number of topK corresponding entities.
     * If false, only source elements has at least topK corresponding entities.
     * The default is true.
     * @param bothDirections true if both directions are enabled
     */
    public void setBothDirections(boolean bothDirections) {
        this.bothDirections = bothDirections;
    }
    
    //override setters which are not needed

    @Override
    public void setTrainingArguments(TransformersTrainerArguments trainingArguments) {
        throw new IllegalArgumentException("Training arguments are not used in SentenceTransformersMatcher.");
    }

    @Override
    public void setUsingTensorflow(boolean usingTensorflow) {
        if(usingTensorflow){
            throw new IllegalArgumentException("SentenceTransformersMatcher only work with Pytorch. Do not set usingTensorflow to true.");
        }
    }
}
