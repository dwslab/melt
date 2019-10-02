import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer.NamePropertyTuple;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.visualization.DashboardBuilder;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.File;
import java.util.ArrayList;

public class DashboardGenerator {

    public static void main(String[] args) {

        // set the path to the unzipped anatomy results folder here
        // you can download the 2019 results here: http://oaei.ontologymatching.org/2019/results/anatomy/
        String pathToAnatomyResultsFolder = "C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\OAEI\\2019\\Results\\Anatomy\\oaei2019-anatomy-alignments";

        // set the path to the unzipped conference results folder here
        // you can download the 2019 results here: http://oaei.ontologymatching.org/2019/results/conference/
        String pathToConferenceResultsFolder = "C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\OAEI\\2019\\Results\\Conference\\conference2019results";

        // set the path to the unzipped KG track results folder here
        // you can download the 2019 results here: oaei.ontologymatching.org/2019/results/knowledgegraph/oaei2019-knowledgegraph-alignments.zip
        String pathToKgResultsFolder = "C:\\Users\\D060249\\OneDrive - SAP SE\\Documents\\PhD\\OAEI\\2019\\Results\\Knowledge_Graph\\oaei2019-knowledgegraph-alignments";


        // ------------------------------------------------
        // Part 1: Generate Page for Anatomy and Conference
        // ------------------------------------------------
        ExecutionResultSet executionResultSet = Executor.loadFromAnatomyResultsFolder(pathToAnatomyResultsFolder);
        executionResultSet.addAll(Executor.loadFromConferenceResultsFolder(pathToConferenceResultsFolder));
        DashboardBuilder pb = new DashboardBuilder(executionResultSet);
        pb.addDefaultDashboard();
        pb.writeToFile(new File("anatomy_conference_dashboard.html"));
        System.out.println("Anatomy/Conference Dashboard written.");

        // ------------------------------------------------
        // Part 2: Generate Page for KG Track
        // ------------------------------------------------
        executionResultSet = Executor.loadFromKnowledgeGraphResultsFolder(pathToKgResultsFolder);
        pb = new DashboardBuilder(executionResultSet);
        pb.addDefaultDashboard();
        pb.writeToFile(new File("knowledge_graph_dashboard.html"));
        System.out.println("Knowledge Graph Dashboard written.");
    }

}
