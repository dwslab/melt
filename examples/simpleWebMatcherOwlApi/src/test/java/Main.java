import de.uni_mannheim.informatik.dws.melt.demomatcher.SimpleStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;


public class Main {
    public static void main(String[] args){
        ExecutionResultSet results = Executor.run(TrackRepository.Anatomy.Default, new SimpleStringMatcher());
        EvaluatorCSV eval = new EvaluatorCSV(results);
        eval.writeToDirectory();
    }
}
