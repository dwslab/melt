---
layout: default
title: Evaluators
parent: Matcher Evaluation
nav_order: 2
permalink: /matcher-evaluation/evaluators
---

# Available Evaluators
- `EvaluatorCSV`: Default evaluator for an in-depth analysis of alignments. Multiple CSV files are generated that can be 
analyzed using a spreadsheet program such as <a href="https://www.libreoffice.org/download/download/">LibreOffice Calc</a>.
- `EvaluatorBasic`: A basic evaluator that is easy on memory. Use this evaluator when you run into
memory issues with `EvaluatorCSV` on very large evaluation problems. Note that this evaluator offers less
functionality than the default evaluator.
- `EvaluatorMcNemarSignificance`: An evaluator for statistical significance tests.
 This evaluator allows checking whether multiple alignments are significantly different.
- `DashboardBuilder`: This evaluator generates an interactive Web UI (*MELT Dashboard*) to analyze alignments
in a self-service BI fashion. You can find an exemplary dashboard for the OAEI 2019
<a href="http://oaei.ontologymatching.org/2019/anatomy/index.html">Anatomy</a> and <a href="http://oaei.ontologymatching.org/2019/conference/index.html">Conference</a> track <a href="https://dwslab.github.io/melt/anatomy_conference_dashboard.html">here</a>.

*Note that it is possible to build your own evaluator and call functions from the existing evaluators.*