---
layout: default
title: Evaluators
parent: Matcher Evaluation
nav_order: 2
permalink: /matcher-evaluation/evaluators
---

# Overview: Available Evaluators
- [**EvaluatorCSV**](#evaluatorcsv): Default evaluator for an in-depth analysis of alignments. Multiple CSV files are generated that can be analyzed using a spreadsheet program such as <a href="https://www.libreoffice.org/download/download/">LibreOffice Calc</a>.
- **EvaluatorBasic**: A basic evaluator that is easy on memory. Use this evaluator when you run into
memory issues with `EvaluatorCSV` on very large evaluation problems. Note that this evaluator offers less
functionality than the default evaluator.
- **EvaluatorMcNemarSignificance**: An evaluator for statistical significance tests.
 This evaluator allows checking whether multiple alignments are significantly different.
- **DashboardBuilder**: This evaluator generates an interactive Web UI (*MELT Dashboard*) to analyze alignments
in a self-service BI fashion. You can find an exemplary dashboard for the OAEI 2019
<a href="http://oaei.ontologymatching.org/2019/anatomy/index.html">Anatomy</a> and <a href="http://oaei.ontologymatching.org/2019/conference/index.html">Conference</a> track <a href="https://dwslab.github.io/melt/anatomy_conference_dashboard.html">here</a>.

*Note that it is possible to build your own evaluator and call functions from the existing evaluators.*

# EvaluatorCSV
[`EvaluatorCSV`](https://github.com/dwslab/melt/blob/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/evaluator/EvaluatorCSV.java) is the default evaluator. Like all evaluators, it is called using the `void writeResultsToDirectory(File baseDirectory)` method. The evaluator will create the specified `baseDirectory` and generate files according to a spcific hierarchy:

```
root
  |
  --- alignmentCube.csv
  |
  --- testCasePerformanceCube.csv
  |
  --- trackPerformanceCube.csv
  |
  --- <track_directory>/
       |
       --- aggregated/
       |   |
       |   --- <matcher_directory>/
       |       |
       |       --- aggregatedPerformance.csv
       |
       --- <test_case_directory>/
           |
           --- <matcher_directory>/
               |
               --- performance.csv
               |
               --- systemAlignment.rdf
```

`alignmentCube.csv`
The alignment cube contains every single correspondence of all matching systems on all tracks together with additional information e.g. whether the correspondence is a true positive or a false positive match. We recommend opening all generated CSV files in [LibreOffice Calc](https://www.libreoffice.org/), and using the [AutoFilter](https://help.libreoffice.org/7.1/en-US/text/scalc/guide/autofilter.html?&DbPAR=WRITER&System=MAC). This way, you can slice the data according to your desire, e.g. showing only false positive class-class matches of a specific matcher on a specific test case.

`testCasePerformanceCube.csv`
This file contains all the aggregated performance figures (precision, recall, F1, residual recall) per *test case*. When using the AutoFilter, you can slide the data according to your desire, e.g. showing the precision, recall, and F1 of property matches of a specific matcher on a specific test case.

`trackPerformanceCube.csv`
This file contains all the aggregated performance figures ([micro and macro] precision, recall, F1, residual recall) per *track*. When using the AutoFilter, you can slide the data according to your desire, e.g. showing the micro precision, micro recall, and micro F1 of instance matches of all matchers on a specific track.

`<track_directory>/` For each track in the `ExecutionResultSet`, the evaluator will create such a directory carrying the name of the track.
In the track directory, you will find a directory fore each test case of the track (`<test_case_directory>/`). 
In here, each matcher in the `ExecutionResultSet` that was run on this track will have a `<matcher_directory>/`.
A `<matcher_directory>/` contains the performance of the system on the particular test case (`performance.csv`) as well as the actual system alignment (`systemAlignment.rdf`).
You will also find an `aggregated/<matcher_directory>/aggregatedPerformance.csv` in the `<track_directory>/` containing the (aggregated micro/macro) performance of the particular matcher on the track.