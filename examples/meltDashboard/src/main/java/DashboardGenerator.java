import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.visualization.dashboard.DashboardBuilder;

import java.io.File;

public class DashboardGenerator {

    public static void main(String[] args) {

        // set the path to the unzipped anatomy results folder here
        // you can download the 2019 results here: http://oaei.ontologymatching.org/2019/results/anatomy/
        String pathToAnatomyResultsFolder = "...";

        // set the path to the unzipped conference results folder here
        // you can download the 2019 results here: http://oaei.ontologymatching.org/2019/results/conference/
        String pathToConferenceResultsFolder = "...";

        // ------------------------------------------------
        // Generate Page for Anatomy and Conference
        // ------------------------------------------------
        ExecutionResultSet executionResultSet = Executor.loadFromAnatomyResultsFolder(pathToAnatomyResultsFolder);
        executionResultSet.addAll(Executor.loadFromConferenceResultsFolder(pathToConferenceResultsFolder));
        DashboardBuilder pb = new DashboardBuilder(executionResultSet, "MELT Dashboard: OAEI 2019 Anatomy/Conference", 
                "This (beta) dashboard is rendered using MELT. You can click on the diagrams (such as the pie/bar charts) to create selections. "
                        + "The dashboard will be updated instantly. If you make selections, reset buttons will appear that allow you to undo your"
                        + "selection.");
        pb.addDefaultDashboard();
        pb.writeToFile(new File("anatomy_conference_dashboard.html"));
        System.out.println("Anatomy/Conference Dashboard written.");

    }

}
