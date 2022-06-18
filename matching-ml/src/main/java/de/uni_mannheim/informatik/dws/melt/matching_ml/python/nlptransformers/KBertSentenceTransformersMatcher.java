package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.TextExtractorKBert;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.kBert.constant.InputTypes;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import static org.apache.commons.text.StringEscapeUtils.escapeCsv;

public class KBertSentenceTransformersMatcher extends SentenceTransformersMatcher {
    public KBertSentenceTransformersMatcher(String modelName) {
        super(new TextExtractorKBert(), modelName);
    }

    // Function to get the Spliterator
    public static <T> Iterable<T> getIterable(Iterator<T> iterator) {
        return () -> iterator;
    }

    @Override
    protected int createTextFile(OntModel model, File file, ResourcesExtractor extractor, Properties parameters)
            throws IOException {
        //LOGGER.info("Write text to file {}", file);
        AtomicInteger linesWritten = new AtomicInteger();
        TextExtractorMap simpleTextExtractor = this.getExtractorMap();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            if (this.multipleTextsToMultipleExamples) {
                throw (new NotImplementedException(
                        "K-BERT Sentence Transformer currently only supports generating one example per set of texts"));
            } else {
                StreamSupport.stream(getIterable(extractor.extract(model, parameters)).spliterator(), false)
                        .filter(RDFNode::isURIResource)
                        .forEach(r -> simpleTextExtractor.extract(r).entrySet().stream()
                                .sorted(Comparator.comparing(e -> InputTypes.fromName(e.getKey())))
                                .flatMap(e -> e.getValue().stream().map(v -> escapeCsv(e.getKey()) + "," + escapeCsv(v) + NEWLINE))
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
