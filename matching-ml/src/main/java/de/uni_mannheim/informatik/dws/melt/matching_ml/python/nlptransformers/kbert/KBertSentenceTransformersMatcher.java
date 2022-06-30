package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert;

import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher;
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
