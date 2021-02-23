---
layout: default
title: FAQ
nav_order: 8
---

## Frequently Asked Questions (FAQs)

**I have a multiple SEALS packages and I want to use MELT's group evaluation functionalities. What is the simplest way 
to do so?**<br/>
SEALS packages were wrapped for the SEALS platform. If the matchers were not developed using MELT or you are not sure 
whether they were developed with MELT, one option is to create the alignment files by executing the matchers 
using the SEALS client. Afterwards, you can read the alignment files (e.g. method `loadFromFolder` of class 
[`Executor`](/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/Executor.java)).<br/>
Alternatively (and more easily), you can install the SEALS client and run the SEALS packages from within MELT using 
[`ExecutorSeals`](/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/ExecutorSeals.java). This executor 
will start the evaluation in SEALS directly from the framework and can be used to conveniently evaluate one or more
matchers. Like the default `Executor`, `ExecutorSeals` will return an `ExecutionResultSet` that can then be further processed by 
any evaluator. When calling `run()`, system alignment files and any output will also be stored on disk and can be reused at 
a later point in time. You can also set the maximum time you want MELT to allocate to a particular matcher. If the matcher
does not finish within the given time limit, MELT will stop the process and proceed with the next test case or matcher.
`ExecutorSeals` can read zipped, unzipped (or a mix of both) SEALS packages.<br/>

**I am running a SEALS matcher that was packaged with MELT and uses some python component. On my system, the
default python command does not refer to Python 3. How can this situation be resolved?**<br/>
A folder `melt-resouces` in the working directory (perhaps `$SEALS_HOME`) has to be created. In there a file `python_command.txt` containing your full 
python path should be placed. This applies to all MELT packaged matchers that use the ML module. 
In other cases, you can also try to create a directory `oaei-resources` rather than `melt-resources`
and place the python_command.txt` there.

**Is there more documentation?**<br/>
MELT is far more powerful than documented here. This `README` is intended to give an overview of the framework.
For specific code snippets, have a look at the examples. Note that classes, interfaces, and methods are extensively 
documented using <a href="https://dwslab.github.io/melt/">JavaDoc</a>.
