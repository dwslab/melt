# MELT - Matching EvaLuation Toolkit

[![Java CI](https://github.com/janothan/kgvec2go-walks/workflows/Java%20CI/badge.svg)](https://github.com/dwslab/melt/actions)
[![Coverage Status](https://coveralls.io/repos/github/dwslab/melt/badge.svg?branch=master)](https://coveralls.io/github/dwslab/melt?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/de.uni-mannheim.informatik.dws.melt/matching-eval)](https://mvnrepository.com/artifact/de.uni-mannheim.informatik.dws.melt)
[![License](https://img.shields.io/github/license/dwslab/melt)](https://github.com/dwslab/melt/blob/master/LICENSE)

MELT is a helpful maven framework for developing, tuning, evaluating, and packaging ontology matching systems.
It is optimized to be used in [OAEI](http://oaei.ontologymatching.org/) campaigns and allows to submit matchers to the SEALS and HOBBIT evaluation platform easily. MELT can also be used for non OAEI-related matching tasks and evaluation.

**How to Cite?** <br/>

*Main Paper*
```
Hertling, Sven; Portisch, Jan; Paulheim, Heiko. MELT - Matching EvaLuation Toolkit. SEMANTICS. Karlsruhe, Germany. 2019.
```
An open-access version of the paper is available <a href="https://link.springer.com/content/pdf/10.1007%2F978-3-030-33220-4_17.pdf">here</a>.
The accompanying presentation can be found in the <a href="https://github.com/dwslab/melt/blob/master/documentation/MELT_presentation_semantics.pdf">documentation directory</a>.

*Demo Paper (MELT Dashboard)*
```
Portisch, Jan; Hertling, Sven; Paulheim, Heiko. Visual Analysis of Ontology Matching Results with the MELT Dashboard. ESWC 2020 - Posters and Demos. Heraklion, Greece. 2020.
``` 
An open-access version of the paper is available <a href="https://arxiv.org/pdf/2004.12628.pdf">here</a>.<br/>
The poster can be found in the <a href="https://github.com/dwslab/melt/blob/master/documentation/eswc_2020_melt_dashboard_poster.pdf">documentation directory</a>.<br/>
A simple demo for the <a href="http://oaei.ontologymatching.org/2019/anatomy/index.html">OAEI 2019 Anatomy</a> and <a href="http://oaei.ontologymatching.org/2019/conference/index.html">OAEI 2019 Conference</a> tracks can be found <a href="https://dwslab.github.io/melt/anatomy_conference_dashboard.html">here</a>.

## Code Examples
The [examples folder](/examples/) contains reference examples that you can use to better understand how MELT can be used for different tasks and that can be used as barebone project for specific applications. 

## Code Documentation / JavaDoc
- [Latest Commit / Development Version](https://dwslab.github.io/melt/)
- [Releases](https://javadoc.io/doc/de.uni-mannheim.informatik.dws.melt)

## Matcher Development in Java

MELT is now available in [maven central](https://repo1.maven.org/maven2/de/uni-mannheim/informatik/dws/melt/) and can be added as a dependency with e.g.:
```
<dependency>
 <groupId>de.uni-mannheim.informatik.dws.melt</groupId>
 <artifactId>matching-eval</artifactId>
 <version>2.4</version>
</dependency>
```

### TL;DR
1. Pick a class to start with depending on your needs. If you start from scratch `MatcherYAAAJena` or `MatcherYAAAOwlApi` are the best fit depending on whether your prefer [Jena](https://jena.apache.org) or the [OWL API](http://owlcs.github.io/owlapi/). 
Classes that can be extended for matcher implementation:
   * `MatcherURL`
   * `MatcherYAA`
   * `MatcherYAAAJena`
   * `MatcherYAAAOwlApi`
2. Implement the `match()` method.

### In More Detail

#### Yet Another Alignment API (YAAA)
MELT introduces a simple API for matcher development. In the following, the most important classes are
explained: 
- `Correspondence`<br/>A Correspondence contains a relation (`CorrespondenceRelation`) that holds between two elements from two different ontologies. 
In the literature, it is also known as "Mapping Cell" or "Cell". Optionally, a correspondence might have a confidence value,
and an identifier. Note that a correspondence can be extended with further attributes. For usability, class `DefaultExtensions`
contains the most common extensions. The correspondence is uniquely identified by the two matching elements as well as the relation.
- `Alignment`<br/>An alignment is a set (no duplicates, no ordering) of multiple `Correspondence` instances. In the literature, it is also 
known as "mapping" or "mappings". 

Class `AlignmentSerializer` can be used to persist an alignment to a file and class `AlignmentParser` can parse an alignment
file directly into a Java object. 

#### Development Options
In order to develop a matcher in Java with MELT, the first step is to decide which matching interface to implement.
The most general interface is encapsulated in class`MatcherURL` which receives two URLs of the ontologies to be matched 
together with a URL referencing an input alignment. The return value should be a URL representing a file with 
correspondences in the alignment format. Since this interface is not very convenient, we also provide more specialized 
classes.
In the `matching-yaaa` package we set the alignment library to YAAA. All matchers implementing interfaces from this 
package have to use the library and get at the same time an easier to handle interface of correspondences.
In further specializations we also set the semantic framework which is used to represent the ontologies.
For a better usability, the two  most well-known frameworks are integrated into MELT: [Apache Jena](https://jena.apache.org) (`MatcherYAAAJena`) 
and the [OWL API](http://owlcs.github.io/owlapi/) (`MatcherYAAAOwlApi`). 
As the latter two classes are organized as separate maven projects, only the libraries which are actually 
required for the matcher are loaded. In addition, further services were implemented such as an ontology cache 
which ensures that ontologies are parsed only once. This is helpful, for instance, when the matcher accesses an 
ontology multiple times, when multiple matchers work together in a pipeline, or when multiple matchers shall be evaluated.
The different levels at which a matcher can be developed as well as how the classes presented in this section work together, 
are displayed in the figure below.

![image](documentation/matcher_hierarchy.png)


## External Matcher Development
MELT allows to develop a matcher in any other programming language and wrap it as a SEALS or HOBBIT package. 
Therefore, class [`MatcherExternal`](/matching-external/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_external/MatcherExternal.java) 
has to be extended. The interface for the external process is simple. It receives the input variables via the command 
line and outputs the results via the standard output of the process - similar to many Unix command line tools. 
All external resources have to be placed in a directory named `oaei-resources`. An example project for a 
Python matcher can be found [here](/examples/externalPythonMatcher).


## Matcher Evaluation
For a local evaluation within MELT, multiple Metrics and Evaluators are available.

### TL;DR

* MELT defines a simple work flow: After you implemented your matcher, hand it over to an `Executor` and call `run()`.
* If you want to evaluate multiple matchers, you can also hand those over to the `Executor`.
* The resulting `ExecutionResultSet` can be given to an evaluator. The default evaluator is `EvaluatorCSV`.
* If you want to implement your own evaluator, extend class `Evaluator` and have a look at our metrics before implementing
your own metric - it might already be there.
* If you know the OAEI and want to use its data: Good. You will never have to download anything from the Web site or fiddle around with file paths. 
MELT can manage all the data. Just have a look at the `TrackRepository`, you will find everything you need there.

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

#### Available Evaluators
- `EvaluatorCSV`: Default evaluator for an in-depth analysis of alignments. Multiple CSV files are generated that can be 
analyzed using a spreadsheet program such as <a href="https://www.libreoffice.org/download/download/">LibreOffice Calc</a>.
- `EvaluatorBasic`: A basic evaluator that is easy on memory. Use this evaluator when you run into
memory issues with `EvaluatorCSV` on very large evaluation problems. Note that this evaluator offers less
functionality than the default evaluator.
- `EvaluatorMcNemarSignificance`: An evaluator for statistical significance tests.
 This evaluator allows to check whether multiple alignments
are significantly different.
- `DashboardBuilder`: This evaluator generates an interactive Web UI (*MELT Dashboard*) to analyze alignments
in a self-service BI fashion. You can find an exemplary dashboard for the OAEI 2019
<a href="http://oaei.ontologymatching.org/2019/anatomy/index.html">anatomy</a> and <a href="http://oaei.ontologymatching.org/2019/conference/index.html">conference</a> track <a href="https://dwslab.github.io/melt/anatomy_conference_dashboard.html">here</a>.

*Note that it is possible to build your own evaluator and call functions from the existing evaluators.*


## Packaging Matchers for SEALS and HOBBIT

### TL;DR
1. Have a look at [examples/simpleJavaMatcher](/examples/simpleJavaMatcher)
1. Create hobbit account and gitlab access token
1. Adjust settings in pom.xml to your needs
1. Implement your matcher (see Matcher development)
1. Execute ```mvn deploy``` to create seals zip and deploy docker image to hobbit server
   - if you only execute ```mvn install``` it will create seals zip and hobbit docker image locally
   - if you execute ```mvn package``` only seals zip will be created
1. The seals zip can be found in the target folder and the hobbit docker image in the local docker repository

### In More Detail
- for Hobbit submission
  - Prerequisites for Hobbit is a working docker installation ([download docker](https://www.docker.com/get-docker))
  - create a user account
      - open [http://master.project-hobbit.eu/](http://master.project-hobbit.eu/)  and click on ```Register```
 - user name should be the first part (local part - everything before the @) of your mail address
      - mail: `max.power@example.org` then user name should be `max.power`
 - more information at [the  hobbit wiki page](https://hobbit-project.github.io/master.html#user-registration)
  - update settings in gitlab (in Hobbit every matcher corresponds to a gitlab project)
      - go to page [http://git.project-hobbit.eu](http://git.project-hobbit.eu) and log in (same account as for the platform itself)
      - click on the upper right user icon and choose `settings`
  - create a Personal Access Token (click on `Access Tokens` give it a name and choose only the `api` scope)
      - use this access token and your username and password to create the settings file (see the pom.xml)
- adjust pom.xml to your needs
  - definitely change the following:
      - `groupId` and `artifactId` (only artifactId is used to identify the matcher -> make it unique)
      - `oaei.mainClass`: set it to the fully qualified path to the matcher (the class implementing ```IOntologyMatchingToolBridge``` or any subclass like ```MatcherURL``` or ```MatcherYAAAJena```)
      - benchmarks: change the benchmarks to the ones your system can deal with
      - create a settings file with username, password and access_token (see an example at the bottom of the [simpleJavaMatcher pom file](/examples/simpleJavaMatcher/pom.xml))
- implement your matcher (see Matcher development)
- build your matcher
  - execute maven goals from command line or from any IDE
  - ```mvn package``` will only build seals zip
  - ```mvn install``` will create seals zip and hobbit docker image locally
      - On MacOS, you have to run ```export DOCKER_HOST=unix:///var/run/docker.sock``` (see [issue of docker-maven-plugin](https://github.com/spotify/docker-maven-plugin/issues/218)) in order to allow maven to communicate with docker.
  - ```mvn deploy``` will create seals zip and deploy docker image to hobbit server
- submit your matcher
  - for SEALS upload the generated seals file ```{artifactId}-{version}-seals.zip``` in the target folder
  - for Hobbit call ```mvn deploy```

### Evaluate your matcher in HOBBIT

- you can start an experiment in hobbit online platform
  - go to page [http://master.project-hobbit.eu/](http://master.project-hobbit.eu/), log in and choose `Benchmarks`
 - select the benchmark you want to use
  - select the system you want to use
  - (optionally) specify configuration parameters and click on `submit`
 - click on the Hobbit ID in the pop up to see the results (reload the page if it is not finished)
  - more information at the  hobbit wiki page ['Benchmarking'](https://hobbit-project.github.io/benchmarking) and ['Browsing Results'](https://hobbit-project.github.io/browsing_results.html).


## Further Services

### OAEI Track Repository
The [`TrackRepository`](/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/tracks/TrackRepository.java) 
checks whether the required ontologies and alignments are available in the cache folder (~/oaei_track_cache); if data is missing, it is automatically downloading and 
caching it for the next access.

Exemplary call using the `TrackRepository`:

```
// access the Anatomy track
TrackRepository.Anatomy.Default;

// access all Conference test cases
TrackRepository.Conference.V1.getTestCases();
```

The resulting instances can be directly used by the Executor or any other MELT functionality that requires tracks or
test cases.

#### Available tracks as SEALS Repository
MELT also provides a server which mocks the SEALS repository and hosts the following tracks:

Name | Repository | Suite-ID | Version-ID 
---- | ---------- | -------- | ----------
[anatomy](http://oaei.ontologymatching.org/2019/anatomy/index.html) | `http://oaei.webdatacommons.org/tdrs/` | `anatomy_track` | `anatomy_track-default`
[conference](http://oaei.ontologymatching.org/2019/conference/index.html) | `http://oaei.webdatacommons.org/tdrs/` | `conference` | `conference-v1`
[knowledgegraph](http://oaei.ontologymatching.org/2019/knowledgegraph/index.html) | `http://oaei.webdatacommons.org/tdrs/` | `knowledgegraph` | `v3`
[iimb](http://islab.di.unimi.it/content/im_oaei/2018/) | `http://oaei.webdatacommons.org/tdrs/` | `iimb` | `v1`
[biodiv](http://oaei.ontologymatching.org/2018/biodiv/index.html) | `http://oaei.webdatacommons.org/tdrs/` | `biodiv` | `2018`
[link](https://project-hobbit.eu/challenges/om2019/) | `http://oaei.webdatacommons.org/tdrs/` | `link` | `2017`
[phenotype](https://sws.ifi.uio.no/oaei/phenotype/) | `http://oaei.webdatacommons.org/tdrs/` | `phenotype` | <ul><li>`phenotype-hp-mp-2017-bioportal`</li><li>`phenotype-doid-ordo-2017-bioportal`</li></ul>
[multifarm](http://oaei.ontologymatching.org/2018/multifarm/index.html) | `http://oaei.webdatacommons.org/tdrs/` | `multifarm` | `<language_pair>-v2`
[largebio](http://www.cs.ox.ac.uk/isg/projects/SEALS/oaei/) | `http://oaei.webdatacommons.org/tdrs/` | `largebio` |  <ul><li>`largebio-all_tasks_2016` </li><li>`largebio-fma_nci_small_2016`</li><li>`largebio-fma_nci_whole_2016`</li><li>`largebio-fma_snomed_small_2016`</li><li>`largebio-fma_snomed_whole_2016`</li><li>`largebio-snomed_nci_small_2016`</li><li>`largebio-snomed_nci_whole_2016`</li></ul>
[complex](http://oaei.ontologymatching.org/2019/complex/index.html) | `http://oaei.webdatacommons.org/tdrs/` | `geolink`</br> `hydrography`</br>`popgeolink`</br>`popenslaved`</br>`popconference`|  `geolink-v1`</br>`hydrography-v1`</br>`popgeolink-v1`</br>`popenslaved-v1`</br>`popconference-[0-20-40-60-80-100]-v1`

Available multifarm language pairs:<br/>
`ar-cn`, `ar-cz`, `ar-de`, `ar-en`, `ar-es`, `ar-fr`, `ar-nl`, `ar-pt`, `ar-ru`, `cn-cz`, `cn-de`, `cn-en`, `cn-es`, 
`cn-fr`, `cn-nl`, `cn-pt`, `cn-ru`, `cz-de`, `cz-en`, `cz-es`, `cz-fr`, `cz-nl`, `cz-pt`, `cz-ru`, `de-en`, `de-es`, 
`de-fr`, `de-nl`, `de-pt`, `de-ru`, `en-es`, `en-fr`, `en-nl`, `en-pt`, `en-ru`, `es-fr`, `es-nl`, `es-pt`, `es-ru`, 
`fr-nl`, `fr-pt`, `fr-ru`, `nl-pt`, `nl-ru`, `pt-ru`


### TestCase/Track Validation Service
Creating new tracks and test case can be very cumbersome. The MELT validation service allows you to check whether your 
test cases:
1. Contain parseable ontologies.
2. Contain a parseable reference alignment.
3. Mention only URIs in the reference alignment that also appear in the corresponding source and target ontologies.

Exemplary call using the [`TestCaseValidationService`](/matching-validation/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_validation/TestCaseValidationService.java):
```
URI sourceUri = new File("<path to source ontology file>").toURI();
URI targetUri = new File("<path to target ontology file>").toURI();
URI referenceUri = new File("<path to reference alignment file>").toURI();
TestCase testCase = new TestCase("FSDM", sourceUri, targetUri, referenceUri, null);
TestCaseValidationService validator = new TestCaseValidationService(testCase)
System.out.println(validator);
```
You can also test your track on different versions of Jena and the OWL API automatically
by adapting the [`TestLocalFile`](/matching-validation/src/test/java/de/uni_mannheim/informatik/dws/melt/matching_validation/local/TestLocalFile.java) 
and running `runAll.cmd` in the Windows shell. The release versions to be tested can be edited in the corresponding
[`pom.xml`](/matching-validation/pom.xml).

### Gensim Integration
The MELT-ML module exposes some machine learning functionality that is implemented in python. This is achieved
through the start of a python process within java. The communication is performed through local HTTP calls. This
is also shown in the following figure. 

![image](documentation/melt_ml_architecture.png)

The program will use the default `python` command of your system path.
Note that Python 3 is required together with the dependencies listed 
in [/matching-ml/melt-resources/requirements.txt](/matching-ml/src/main/resources/requirements.txt).

If you want to use a special python environment, you can create a file named `python_command.txt`
in your `melt-resources` directory (create if not existing) containing the path to your python executable. You can, for example,
use the executable of a certain Anaconda environment. 

*Example*:
```
C:\Users\myUser\Anaconda3\envs\matching\python.exe
```
Here, an Anaconda environment, named `matching` will be used.

## Modules Overview
The ontology matching framework is grouped into multiple maven modules which are described below.

### matching-yaaa 
Simple alignment API (Yet Another Alignment API, YAAA) offering data structures for Ontology Alignments as well as additional alignment-related services.

### matching-base
Contains the basic interfaces to implement a matcher e.g. MatcherURL.

### matching-eval
Contains various tools to evaluate the performance of matchers and to analyze their result.

### matching-jena
Contains [Jena-based](https://jena.apache.org/) classes related to matcher development as well as additional services such as caching of source and target ontologies.  

### matching-jena-matchers
Contains modularized matchers that can be used to quickly assemble matching systems. Note that it is possible to easily chain those matchers building a matching pipeline.

### matching-ml
The machine learning extension for MELT. Currently, [gensim](https://radimrehurek.com/gensim/) is supported. The ML extension allows to communicate with a Python backend.

### matching-owlapi
Contains OWL-API-based classes related to matcher development as well as additional services such as caching of source and target ontologies.  

### matching-validation
Contains various validation services to validate new tracks and test cases. Validation includes parseability by multiple libraries using different releases and further checks.

### seals-assembly
Maven Plugin for creating a ZIP-file for the [SEALS platform](http://www.seals-project.eu/).

### hobbit-assembly
Maven Plugin for defining which files the docker image should contain (for [HOBBIT platform](https://project-hobbit.eu)).

### hobbit-wrapper
Contains a wrapper for HOBBIT platform (implements the interface used in HOBBIT and transforms the calls to MatcherURL interface).

### hobbit-maven-plugin
Maven Plugin for creating a container for the [HOBBIT platform](https://project-hobbit.eu/outcomes/hobbit-platform/).

### matching-external
Contains matcher classes for matchers that are implemented in another environment than Java (such as a python matcher).

### demo-benchmark
Tool for submitting a Track/Testcase in HOBBIT (only interesting for OAEI track organizers).
  

## Frequently Asked Questions (FAQs)

**I have a muliple SEALS packages and I want to use MELT's group evaluation functionalities. What is the simplest way 
to do so?**<br/>
SEALS packages were wrapped for the SEALS platform. If the matchers were not developed using MELT or you are not sure 
whether they were developed with MELT, one option is to create the alignment files by executing the matchers 
using the SEALS client. Afterwards, you can read the alignment files (e.g. method `loadFromFolder` of class 
[`Executor`](/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/Executor.java)).<br/>
Alternatively (and more easily), you can install the SEALS client and run the SEALS packages from within MELT using 
[`ExecutorSeals`]((/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/ExecutorSeals.java)). This executor 
will start the evaluation in SEALS directly from the framework and can be used to conveniently evaluate one or more
matchers. Like the default `Executor`, `ExecutorSeals` will return an `ExecutionResultSet` that can then be further processed by 
any evaluator. When calling `run()`, system alignment files and any output will also be stored on disk and can be reused at 
a later point in time. You can also set the maximum time you want MELT to allocate to a particular matcher. If the matcher
does not finish within the given time limit, MELT will stop the process and proceed with the next test case or matcher.
`ExecutorSeals` can read zipped, unzipped (or a mix of both) SEALS packages.<br/>

**Is there more documentation?**<br/>
MELT is far more powerful than documented here. This `README` is intended to give an overview of the framework.
For specific code snippets, have a look at the examples. Note that classes, interfaces, and methods are extensively 
documented using <a href="https://dwslab.github.io/melt/">JavaDoc</a>.
