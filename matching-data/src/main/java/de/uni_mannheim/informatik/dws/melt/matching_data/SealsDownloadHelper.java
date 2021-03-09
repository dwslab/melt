package de.uni_mannheim.informatik.dws.melt.matching_data;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SealsDownloadHelper {

    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SealsDownloadHelper.class);

    private static final String HAS_SUITE_ITEM = "http://www.seals-project.eu/ontologies/SEALSMetadata.owl#hasSuiteItem";
    private static final String IDENTIFIER = "http://purl.org/dc/terms/identifier";

    private String testDataRepositoryUrl;
    private String persistentRepositoryUrl;
    private String testDataCollectionName;
    private String testDataVersionNumber;

    public SealsDownloadHelper(String testDataRepositoryUrl, String testDataCollectionName, String testDataVersionNumber) {
        this.testDataRepositoryUrl = testDataRepositoryUrl;
        this.testDataCollectionName = testDataCollectionName;
        this.testDataVersionNumber = testDataVersionNumber;
        this.persistentRepositoryUrl = createLocation(testDataRepositoryUrl, new String[]{"testdata", "persistent"});
    }

    public List<String> getTestCases() {
        String url = createLocation(
                this.persistentRepositoryUrl,
                encode(this.testDataCollectionName),
                encode(this.testDataVersionNumber),
                "suite");
        Model m = ModelFactory.createDefaultModel();
        LOGGER.info("Load from url {}", url);
        m.read(url);
        String queryString = "SELECT ?suiteItem ?suiteItemName WHERE { ?x <" + HAS_SUITE_ITEM + "> ?suiteItem . ?suiteItem <" + IDENTIFIER + "> ?suiteItemName . } ORDER BY ?suiteItemName";
        List<String> names = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, m)) {
            ResultSet results = qexec.execSelect();
            //ResultSetFormatter.out(System.out, results) ;
            while(results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal l = soln.getLiteral("suiteItemName");
                names.add(l.getString());
            }
        }
        return names;
    }

    public URL getDataItem(String testCaseId, String componentType) {
        String url = createLocation(this.testDataRepositoryUrl,
                "testdata",
                "persistent",
                encode(this.testDataCollectionName),
                encode(this.testDataVersionNumber),
                "suite",
                encode(testCaseId),
                "component",
                encode(componentType));
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed URL.", e);
        }
        return null;
    }

    /**
     * Create an URL encoding for the given String.
     * @param stringToEncode String to be encoded.
     * @return Encoded String.
     */
    private static String encode(String stringToEncode) {
        if (stringToEncode == null) {
            return null;
        }
        try {
            return URLEncoder.encode(stringToEncode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Encoding error occurred.", e);
        }
        return stringToEncode;
    }

    private static String createLocation(String originalUrl, String... parts) {
        StringBuilder builder = new StringBuilder();

        builder.append(originalUrl.trim());
        if (!originalUrl.endsWith("/")) {
            builder.append("/");
        }
        if (parts != null) {
            for (String part : parts) {
                if (part != null) {
                    builder.append(part);
                    if (!part.endsWith("/")) {
                        builder.append("/");
                    }
                }
            }
        }
        return builder.toString();
    }

}
