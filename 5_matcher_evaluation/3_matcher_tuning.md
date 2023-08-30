---
layout: default
title: Matcher Tuning
parent: Matcher Evaluation
nav_order: 4
permalink: /matcher-evaluation/matcher-tuning
---


# Optimal Confidence Determination
Many matching systems use varying confidences for each correspondence, typically in the range [0, 1].
Removing low-confidence matches can significantly improve precision and F1. 

In MELT, [`ConfidenceFinder`](https://github.com/dwslab/melt/blob/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/paramtuning/ConfidenceFinder.java) can be used to determine the optimal threshold given any `ExecutionResult`.

Note that it is much better performance-wise to optimize a matcher execution result that contains no removed correspondences rather than running a matcher multiple times with different cut-off points. Therefore, `ConfidenceFinder` works with an `ExecutionResult` instance rather than with a matcher instance. If you want to fine-tune parameters of an actual matching instance, use class [`GridSearch`](https://github.com/dwslab/melt/blob/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/paramtuning/GridSearch.java).

*Example:*
```java
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.ConfidenceFinder;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;

public class ConfidenceFinderExample {

    
    public static void main(String[] args) {

        // let's run a default matcher on the OAEI anatomy track:
        ExecutionResultSet ers = Executor.run(TrackRepository.Anatomy.Default, new SimpleStringMatcher());
        for (ExecutionResult e : ers) {

            // the actual optimization:
            double bestConfidence = ConfidenceFinder.getBestConfidenceForFmeasure(e);

            // just some meaningful output:
            System.out.println("Best confidence for matcher " + e.getMatcherName() +
                    " on " + e.getTrack().getName() + " (" + e.getTestCase().getName() + "): " +
                    bestConfidence);
        }
    }

}
```

All correspondences with a confidence *LOWER* than the result should be discarded. You can do this by applying a filter in a matching pipeline. MELT provides [`ConfidenceFilter`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ConfidenceFilter.java) for exactly this case: 

*Example:*
```java
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;

public class ConfidenceFilterExample {


    public static void main(String[] args) {

        // assume that we determined an optimal confidence as outlined above
        double bestConfidence = 0.8;

        // build a matcher pipeline with the filter at the end:
        MatcherYAAAJena matcher = new MatcherPipelineYAAAJenaConstructor(
                new SimpleStringMatcher(), // some matcher
                new ConfidenceFilter(bestConfidence)); // let's filter the result using ConfidenceFilter

        // do something with the matcher :)
    }

}
```




