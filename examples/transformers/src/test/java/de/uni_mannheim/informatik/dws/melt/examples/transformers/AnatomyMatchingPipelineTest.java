package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import org.junit.jupiter.api.Test;

import java.io.File;


class AnatomyMatchingPipelineTest {


    @Test
    void evaluateZeroShotTransformer(){
        String gpu = "";
        String transformerModel = null;
        File transformersCache = null;
        ExecutionResultSet ers = new ExecutionResultSet();

        /*
        transformerModel = "gpt2";
        ers.addAll(Executor.run(TrackRepository.Anatomy.Default, new AnatomyMatchingPipeline(gpu,
                transformerModel, transformersCache), transformerModel));

        transformerModel = "bert-base-cased";
        ers.addAll(Executor.run(TrackRepository.Anatomy.Default, new AnatomyMatchingPipeline(gpu,
                transformerModel, transformersCache), transformerModel));
         */

        transformerModel = "bert-base-cased-finetuned-mrpc";
        ers.addAll(Executor.run(TrackRepository.Anatomy.Default, new AnatomyMatchingPipeline(gpu,
                transformerModel, transformersCache), transformerModel));


        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(ers);
        evaluatorCSV.writeToDirectory();
    }

}