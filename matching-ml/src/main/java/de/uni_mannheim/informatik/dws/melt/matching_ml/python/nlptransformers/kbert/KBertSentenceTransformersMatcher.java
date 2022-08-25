package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.kbert.TextExtractorKbert;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class KBertSentenceTransformersMatcher extends SentenceTransformersMatcher {
    public static final Map<Boolean, String> NORMALIZED_MAP = Map.of(true, "normalized", false, "raw");
    public static final Map<Boolean, String> ALL_TARGETS_MAP = Map.of(true, "all_targets", false, "one_target");

    protected TextExtractorKbert extractor;

    public KBertSentenceTransformersMatcher(TextExtractorKbert extractor, String modelName) {
        super(extractor, modelName);
        this.extractor = extractor;
        this.fileSuffix = ".csv";
    }

    // Function to get the Spliterator
    public static <T> Iterable<T> getIterable(Iterator<T> iterator) {
        return () -> iterator;
    }

    public static <T> Stream<T> streamFromIterator(Iterator<T> iterator) {
        return StreamSupport.stream(getIterable(iterator).spliterator(), false);
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {

        if (inputAlignment == null)
            inputAlignment = new Alignment();
        for (ResourcesExtractor resExtractor : resourcesExtractor) {
            File corpus = FileUtil.createFileWithRandomNumber("corpus", ".csv");
            File queries = FileUtil.createFileWithRandomNumber("queries", ".csv");
            try {
                int linesWrittenSource = createTextFile(source, corpus, resExtractor, parameters);
                int linesWrittenTarget = createTextFile(target, queries, resExtractor, parameters);
                if (linesWrittenSource == 0 || linesWrittenTarget == 0) {
                    continue; // nothing to match. skip it.
                }
                LOGGER.info("Written {} source and {} target text representations", linesWrittenSource, linesWrittenTarget);

                //run python
                Alignment alignment = PythonServer.getInstance().sentenceTransformersPrediction(this, corpus, queries);
                //add correspondences
                for (Correspondence c : alignment) {
                    c.addAdditionalConfidence(this.getClass(), c.getConfidence());
                    inputAlignment.addOrModify(c);
                }
            } finally {
                corpus.delete();
                queries.delete();
            }
        }
        return inputAlignment;
    }

    public TextExtractorKbert getExtractor() {
        return extractor;
    }

    @Override
    public int createTextFile(OntModel model, File file, ResourcesExtractor extractor, Properties parameters)
            throws IOException {
        //LOGGER.info("Write text to file {}", file);
        AtomicInteger linesWritten = new AtomicInteger();
        TextExtractorKbert textExtractorKbert = this.getExtractor();
        File indexOutputFile = new File(file.getParentFile(), "index_" + file.getName());
        Files.createDirectories(indexOutputFile.getParentFile().toPath());
        try (PrintWriter printWriter = new PrintWriter(indexOutputFile)) {
            textExtractorKbert.getIndexStream(extractor.extract(model, parameters)).forEach(printWriter::println);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            streamFromIterator(extractor.extract(model, parameters))
                    .filter(RDFNode::isURIResource)
                    .forEach(r -> textExtractorKbert.extract(r)
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
        return linesWritten.get();
    }


}
