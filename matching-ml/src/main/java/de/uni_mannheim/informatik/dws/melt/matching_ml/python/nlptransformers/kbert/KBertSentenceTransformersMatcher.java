package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

public class KBertSentenceTransformersMatcher extends SentenceTransformersMatcher {
    public KBertSentenceTransformersMatcher(TextExtractor extractor, String modelName) {
        super(extractor, modelName);
        this.fileSuffix = ".csv";
    }

    // Function to get the Spliterator
    public static <T> Iterable<T> getIterable(Iterator<T> iterator) {
        return () -> iterator;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {

        if(inputAlignment == null)
            inputAlignment = new Alignment();
        for(ResourcesExtractor resExtractor : resourcesExtractor){
            File corpus = FileUtil.createFileWithRandomNumber("corpus", ".csv");
            File queries = FileUtil.createFileWithRandomNumber("queries", ".csv");
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

    @Override
    public int createTextFile(OntModel model, File file, ResourcesExtractor extractor, Properties parameters)
            throws IOException {
        //LOGGER.info("Write text to file {}", file);
        AtomicInteger linesWritten = new AtomicInteger();
        TextExtractorMap simpleTextExtractor = this.getExtractorMap();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            if (this.multipleTextsToMultipleExamples) {
                throw (new NotImplementedException(
                        "K-BERT Sentence Transformer currently only supports generating one example per set of texts"));
            } else {
                StreamSupport.stream(getIterable(extractor.extract(model, parameters)).spliterator(), false)
                        .filter(RDFNode::isURIResource)
                        .forEach(r -> simpleTextExtractor.extract(r).entrySet().stream()
                                .sorted(Comparator.comparing(e -> InputTypes.fromName(e.getKey())))
                                .flatMap(e -> e.getValue().stream())
                                .forEach(line -> {
                                    try {
                                        writer.write(line);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    linesWritten.getAndIncrement();
                                })
                        );
            }
        }
        return linesWritten.get();
    }

}
