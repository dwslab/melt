package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Properties;

public class KBertSentenceTransformersMatcher extends SentenceTransformersMatcher {
    public KBertSentenceTransformersMatcher(TextExtractorMap extractor, String modelName) {
        super(extractor, modelName);
    }

    public KBertSentenceTransformersMatcher(TextExtractor extractor, String modelName) {
        super(extractor, modelName);
    }

    @Override
    protected int createTextFile(OntModel model, File file, ResourcesExtractor extractor, Properties parameters)
            throws IOException {
        //LOGGER.info("Write text to file {}", file);
        int linesWritten = 0;
        TextExtractor simpleTextExtractor = this.getExtractor();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            Iterator<? extends OntResource> resourceIterator = extractor.extract(model, parameters);
            if (this.multipleTextsToMultipleExamples) {
                while (resourceIterator.hasNext()) {
                    OntResource r = resourceIterator.next();
                    if (!r.isURIResource())
                        continue;
                    for (String text : simpleTextExtractor.extract(r)) {
                        text = text.trim();
                        if (text.isEmpty())
                            continue;
                        writer.write(StringEscapeUtils.escapeCsv(r.getURI()) + "," + StringEscapeUtils.escapeCsv(text) + NEWLINE);
                        linesWritten++;
                    }
                }
            } else {
                while (resourceIterator.hasNext()) {
                    OntResource r = resourceIterator.next();
                    if (!r.isURIResource())
                        continue;
                    StringBuilder sb = new StringBuilder();
                    for (String text : simpleTextExtractor.extract(r)) {
                        sb.append(text.trim()).append(" ");
                    }
                    String text = sb.toString().trim();
                    if (text.isEmpty())
                        continue;
                    writer.write(StringEscapeUtils.escapeCsv(r.getURI()) + "," + StringEscapeUtils.escapeCsv(text) + NEWLINE);
                    linesWritten++;
                }
            }
        }
        return linesWritten;
    }

}
