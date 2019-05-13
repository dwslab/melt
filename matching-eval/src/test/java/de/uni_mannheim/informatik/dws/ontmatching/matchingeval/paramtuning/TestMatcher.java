package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.paramtuning;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.MatcherFile;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.AlignmentSerializer;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;


public class TestMatcher extends MatcherFile {

    private String one;
    private int two;
    private double threshold;

    private static Map<URL, OntModel> cache;

    public TestMatcher() {
        cache = new HashMap<>();
        this.threshold = 1.0;
    }

    private OntModel getOntModel(URL url) {
        OntModel model = cache.get(url);
        if (model == null) {
            model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
            model.read(url.toString());
            cache.put(url, model);
            return model;
        } else {
            return model;
        }
    }

    @Override
    public void match(URL source, URL target, URL inputAlignment, File alignmentFile) throws Exception {
        OntModel src = getOntModel(source);
        OntModel tgt = getOntModel(target);
        Alignment m = new Alignment();
        iterateOver(src.listClasses().toList(), tgt.listClasses().toList(), m);
        iterateOver(src.listDatatypeProperties().toList(), tgt.listDatatypeProperties().toList(), m);
        iterateOver(src.listObjectProperties().toList(), tgt.listObjectProperties().toList(), m);
        AlignmentSerializer.serialize(m, alignmentFile);
    }

    private void iterateOver(List<? extends OntResource> listOne, List<? extends OntResource> listTwo, Alignment m) {
        Iterator<? extends OntResource> iteratorOne = listOne.iterator();
        while (iteratorOne.hasNext()) {
            OntResource one = iteratorOne.next();
            String labelOne = one.getLocalName();
            if(labelOne == null || labelOne.isEmpty())
                continue;
            Iterator<? extends OntResource> iteratorTwo = listTwo.iterator();
            while (iteratorTwo.hasNext()) {
                OntResource two = iteratorTwo.next();
                String labelTwo = two.getLocalName();
                if(labelTwo == null || labelTwo.isEmpty())
                    continue;
                double conf = damerauLevenshteinNormalised(labelOne, labelTwo);
                if (conf >= threshold) {
                    m.add(one.getURI(), two.getURI(), conf);
                }
            }
        }
    }

    private static double damerauLevenshteinNormalised(String a, String b) {
        double edit = (double) damerauLevenshtein(a, b);
        double maxLength = getMaxLength(a, b);
        return getNormalised(edit, maxLength);
    }

    private static double getNormalised(double editDistance, double maxLength) {
        return 1.0d - (editDistance / maxLength);
    }

    private static double getMaxLength(String a, String b) {
        if (a.length() > b.length()) {
            return (double) a.length();
        } else {
            return (double) b.length();
        }
    }

    private static int damerauLevenshtein(String compOne, String compTwo) {
        //System.out.println("one: " + compOne + "\ttwo: " + compTwo);
        int res = -1;
        int INF = compOne.length() + compTwo.length();
        int maxLength = 0;
        if (compOne.length() > compTwo.length()) {
            maxLength = compOne.length();
        } else {
            maxLength = compTwo.length();
        }

        int[][] matrix = new int[compOne.length() + 1][compTwo.length() + 1];

        for (int i = 0; i < compOne.length(); i++) {
            matrix[i + 1][1] = i;
            matrix[i + 1][0] = INF;
        }

        for (int i = 0; i < compTwo.length(); i++) {
            matrix[1][i + 1] = i;
            matrix[0][i + 1] = INF;
        }

        int[] DA = new int[maxLength];

        for (int i = 0; i < maxLength; i++) {
            DA[i] = 0;
        }

        for (int i = 1; i < compOne.length(); i++) {
            int db = 0;

            for (int j = 1; j < compTwo.length(); j++) {

                int i1 = DA[compTwo.indexOf(compTwo.charAt(j - 1))];
                int j1 = db;
                int d = ((compOne.charAt(i - 1) == compTwo.charAt(j - 1)) ? 0 : 1);
                if (d == 0) {
                    db = j;
                }

                matrix[i + 1][j + 1] = Math.min(Math.min(matrix[i][j] + d, matrix[i + 1][j] + 1), Math.min(matrix[i][j + 1] + 1, matrix[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1)));
            }
            DA[compOne.indexOf(compOne.charAt(i - 1))] = i;
        }

        return matrix[compOne.length()][compTwo.length()];
    }

    public String getOne() {
        return one;
    }

    public void setOne(String one) {
        this.one = one;
    }

    public int getTwo() {
        return two;
    }

    public void setTwo(int two) {
        this.two = two;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
