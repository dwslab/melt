---
layout: home
title: Home
nav_order: 1
permalink: /
---

# Matching EvaLuation Toolkit (MELT) <br/>User Guide
{: .fs-9 }

A powerful framework for ontology, instance, and knowledge graph matching.
{: .fs-6 .fw-300 }

[Get started now](#overview){: .btn .btn-primary .fs-5 .mb-4 .mb-md-0 .mr-2 } [View it on GitHub](https://github.com/dwslab/melt){: .btn .fs-5 .mb-4 .mb-md-0 }

---

## Overview
MELT is a powerful maven framework for developing, tuning, evaluating, and packaging ontology matching systems.
It is optimized to be used in [OAEI](http://oaei.ontologymatching.org/) campaigns and allows to submit matchers to the SEALS and HOBBIT evaluation platform easily. 
MELT can also be used for non OAEI-related matching tasks and evaluation.

Found a bug? Don't hesitate to <a href="https://github.com/dwslab/melt/issues">open an issue</a>.

## Code Examples
The [examples folder](https://github.com/dwslab/melt/tree/master/examples) contains reference examples that you can use to better understand how MELT can be used for different tasks and that can be used as barebone project for specific applications. The examples are also referenced in the user guide here.

## Code Documentation / JavaDoc
The MELT code is extensively documented using JavaDoc which is also published online:
- [Latest Commit / Development Version](/javadoc_latest/index.html)
- [Releases](https://javadoc.io/doc/de.uni-mannheim.informatik.dws.melt)

## About this User Guide
This guide is written for newcomers and practitioners in the matching domain. You can read this guide like a book chapter by chapter using the navigation pane on the left or by jumping directly to the section of your interest.

Note that you can also easily search the entire user guide: 

![image](/media/search_screenshot.png)

## Code search
You can use the search below to search the complete code base of MELT.
<form>
<input type="search" class="form-control" id="searchText" placeholder="Search">
<button type="submit" name="button" class="btn btn-primary" onclick="window.open('https://github.com/search?q=' + encodeURIComponent(document.getElementById('searchText').value) + '+repo%3Adwslab%2Fmelt&type=Code','_blank');">Search with Github</button>
</form>

## Installation
MELT is [distributed through maven central](https://mvnrepository.com/artifact/de.uni-mannheim.informatik.dws.melt).
You can directly include MELT dependencies in your maven project from there.
If you want to use the latest MELT development version (e.g. to try out new features or to contribute to the MELT project), clone the github repository and install it locally (`mvn clean install`).

### Special Installation Instructions for Module `matching-ml`
The ML module of MELT executes python code in the background. Before installing/using the dependency locally, you need to prepare your machine (once):
- make sure python 3 is installed
- install the requirements from the [`requirements.txt` file](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/resources/requirements.txt)

## Modules Overview
MELT is grouped into multiple [maven](https://maven.apache.org/what-is-maven.html) modules which are described below.

### hobbit-wrapper
Contains a wrapper for HOBBIT platform (implements the interface used in HOBBIT and transforms the calls to MatcherURL interface).

### hobbit-maven-plugin
Maven Plugin for creating a container for the [HOBBIT platform](https://project-hobbit.eu/outcomes/hobbit-platform/).

### matching-base
Contains the basic interfaces to implement a matcher e.g. MatcherURL.

### matching-data
Contains the MELT track repository implementation and data structures such as `Track`, or `TestCase`.

### matching-eval
Contains various tools to evaluate the performance of matchers and to analyze their result.

### matching-jena
Contains [Jena-based](https://jena.apache.org/) classes related to matcher development as well as additional services such as caching of source and target ontologies.  

### matching-jena-matchers
Contains modularized matchers that can be used to quickly assemble matching systems. Note that it is possible to easily chain those matchers building a matching pipeline.

### matching-ml
The machine learning extension for MELT. The ML extension allows communicating with a Python backend.
Currently, [gensim](https://radimrehurek.com/gensim/) is supported. The module also contains a client to consume
<a href="http://kgvec2go.org/">KGvec2go</a> vectors.

### matching-owlapi
Contains OWL-API-based classes related to matcher development as well as additional services such as caching of source and target ontologies.  

### matching-validation
Contains various validation services to validate new tracks and test cases. Validation includes parseability by multiple libraries using different releases and further checks.

### receivers (receiver-hobbit, receiver-cli, receiver-http)
Contains utilities to wrap a matcher to a specific interface such as HTTP, SEALS, HOBBIT etc.

### yet-another-alignment-api
MELT's alignment API containing the `Alignment` data structure and related functionality such as parsing or serializing of alignments.

