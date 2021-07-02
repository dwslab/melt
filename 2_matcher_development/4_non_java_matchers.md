---
layout: default
title: Non-Java Matchers
parent: Matcher Development
nav_order: 4
permalink: /matcher-development/non-java-matchers
---

# Non-Java Matchers / External Matcher Development
MELT allows to develop a matcher in any other programming language and wrap it as a SEALS or HOBBIT package. 
Therefore, class `MatcherExternal` has to be extended. The interface for the external process is simple. It receives the input variables via the command 
line and outputs the results via the standard output of the process - similar to many Unix command line tools. All external resources have to be placed in a directory named `oaei-resources`. An example project for a 
Python matcher can be found [here](/examples/externalPythonMatcher).

## Wrapping a Python Matcher
The example project [can be found on GitHub](https://github.com/dwslab/melt/tree/master/examples/externalPythonMatcherWeb).

The matcher is completely implemented in Python. All python resources can be found in directory `oaei-resources`. For parsing the ontologies, rdflib is used. Alignment services like serialization are handled in file `AlignmentFormat.py`. Note that MELT does not offer comprehensive matcher development tooling in other programming languages than Java.

Java class `DemoPythonMatcher` is used to wrap the python matcher. At runtime, this wrapper will call the python python matcher.

Make sure that the executing systems has all dependencies correctly installed.