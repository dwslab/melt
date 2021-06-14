---
layout: default
title: Matcher Evaluation
has_children: true
nav_order: 4
permalink: /matcher-evaluation
---



# Matcher Evaluation
For a local evaluation within MELT, multiple Metrics and Evaluators are available.

### TL;DR

* MELT defines a simple work flow: After you implemented your matcher, hand it over to an `Executor` and call `run()`.
* If you want to evaluate multiple matchers, you can also hand those over to the `Executor`.
* The resulting `ExecutionResultSet` can be given to an evaluator. The default evaluator is `EvaluatorCSV`.
* If you want to implement your own evaluator, extend class `Evaluator` and have a look at our metrics before implementing
your own metric - it might already be there.
* If you know the OAEI and want to use its data: Good. You will never have to download anything from the Web site or fiddle around with file paths. 
MELT can manage all the data. Just have a look at the [`TrackRepository`](https://dwslab.github.io/melt/track-repository), you will find everything you need there.
* If you want to use some data that is not available on the public repository: No problem. Just create a [`LocalTrack`](https://dwslab.github.io/melt/matcher-evaluation/local-track).

### In More Detail
MELT defines a workflow for matcher execution and evaluation. Therefore, it utilizes the vocabulary used by the OAEI: A
matcher can be evaluated on a `TestCase`, i.e. a single ontology matching task. One or more test cases are summarized in
a `Track`. MELT contains a built-in `TrackRepository` which allows to access all OAEI tracks and test cases at design time
without actually downloading them from the OAEI Web page. At runtime `TrackRepository` (see *Further Services* for details) checks whether the required
ontologies and alignments are available in the internal buffer; if data is missing, it is automatically downloading and
caching it for the next access. The caching mechanism is an advantage over the SEALS platform which downloads all ontologies
again at runtime which slows down the evaluation process if run multiple times in a row. If a local data set shall be
evaluated, class `LocalTrack` can be instantiated.

One or more matchers are given, together with the track or test case on which they shall be run, to an `Executor`. The 
Executor runs a matcher or a list of matchers on a single test case, a list of test cases, or a track. The `run()` method 
of the executor returns an `ExecutionResultSet`. The latter is a set of `ExecutionResult` instances which 
represent individual matching results on a particular test case. Lastly, an `Evaluator` accepts an `ExecutionResultSet` and performs an evaluation. Therefore, it may use one or more `Metric` objects. MELT contains various metrics, such as a 
`ConfusionMatrixMeric`, and evaluators. Nonetheless, the framework is designed to allow for the further implementation 
of evaluators and metrics.

After the `Executor` ran, an `ExecutionResult` can be refined by a `Refiner`. A refiner takes an individual `ExecutionResult` and makes it smaller. An example is the `TypeRefiner` which creates additional execution results depending on the type of 
the alignment (classes, properties, datatype properties, object properties, instances). Another example for an implemented 
refiner is the `ResidualRefiner` which only keeps non-trivial correspondences. Refiners can be combined. This means that 
MELT can calculate very specific evaluation statistics such as the residual precision of datatype property correspondences. 

The default evaluator is `EvaluatorCSV`.

#### Minimal Evaluation Example
The following code example will execute the `SimpleStringMatcher` on the `Anatomy` track
and run the default evaluation using `EvaluatorCSV`. A `results` directory will be generated containing
among others:
- `trackPerformanceCube.csv`<br/>Track evaluation KPIs such as (macro/micro) Precision, Recall, or F1 for the track.
- `testCasePerformanceCube.csv`<br/>Test case evaluation KPIs such as Precision, Recall, or F1. 
- `alignmentCube.csv`<br/>Detailed evaluation per correspondence. You can use a spreadsheet program to filter, for example, for only true positives.


```java
// imports...
public class EvaluationPlayground {
    public static void main(String[] args) {
        ExecutionResultSet result = Executor.run(TrackRepository.Anatomy.Default, new SimpleStringMatcher());
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(result);
        evaluatorCSV.writeToDirectory();
    }
}
```

#### More Complex Evaluation Example
MELT allows for much more complex evaluation scenarios. The following example [can be found on github](https://github.com/dwslab/melt/tree/master/examples/evaluationExample) (including an example SEALS package and an example Docker package). 

Here, three matchers are run: (1) A matcher which is directly instantiated (`classMatcher`), (2) a SEALS matcher where the SEALS zip is wrapped to be a re-usable matcher (`sealsMatcher`), (3) a Docker matcher where the docker tar.gz file is wrapped to be a re-usable matcher (`dockerMatcher`).

Note that we can execute all matchers using one evaluation protocol. Here, we use `EvaluatorCSV` to execute the systems on two tracks: OAEI Anatomy and OAEI Conference.

If you want to learn more about packaging matchers as [SEALS package](https://dwslab.github.io/melt/matcher-packaging/seals) / [Docker Web package](https://dwslab.github.io/melt/matcher-packaging/web), read the corresponding sections on matcher packaging.

```java
public static void main(String[] args) {

    // STEP 1: Let's initialize the matchers we want to evaluate

    // CASE 1: A matcher we can directly instantiate:
    SimpleStringMatcher classMatcher = new SimpleStringMatcher();

    // CASE 2: SEALS Package
    // If you have problems with your java version, have a look at our user guide on how to manually set
    // a path to JAVA 8 for SEALS: https://dwslab.github.io/melt/matcher-packaging/seals#evaluate-and-re-use-a-seals-package-with-melt
    File sealsFile = loadFile("simpleSealsMatcher-1.0-seals_external.zip");
    MatcherSeals sealsMatcher = new MatcherSeals(sealsFile);

    // CASE 3: Web Docker Package
    File dockerFile = loadFile("simplewebmatcher-1.0-web-latest.tar.gz");
    MatcherDockerFile dockerMatcher = new MatcherDockerFile("simplewebmatcher-1.0-web", dockerFile);


    // STEP 2: Run (execute) the 3 matchers to obtain an ExecutionResultSet instance

    // Let's run the matchers on two tracks:
    List<Track> tracks = new ArrayList<>();
    tracks.add(TrackRepository.Conference.V1);
    tracks.add(TrackRepository.Anatomy.Default);

    // Let's add all matchers to a map (key: matcher name, value: matcher instance)
    Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
    matchers.put("Class Matcher", classMatcher);
    matchers.put("SEALS Matcher", sealsMatcher);
    matchers.put("Docker Matcher", dockerMatcher);

    ExecutionResultSet result = Executor.run(TrackRepository.Conference.V1, matchers);


    // Step 3: Use your favorite evaluator to interpret the result
    EvaluatorCSV evaluatorCSV = new EvaluatorCSV(result);
    evaluatorCSV.writeToDirectory();
}
```