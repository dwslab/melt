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
line and outputs the results via the standard output of the process - similar to many Unix command line tools. All external resources have to be placed in a directory named `oaei-resources`.

## Wrapping a Python Matcher

### Project Overview
The example project [can be found on GitHub](https://github.com/dwslab/melt/tree/master/examples/externalPythonMatcherWeb).

The matcher is completely implemented in Python. All python resources can be found in directory `oaei-resources`. For parsing the ontologies, rdflib is used. Alignment services like serialization are handled in file `AlignmentFormat.py`. Note that MELT does not offer comprehensive matcher development tooling in other programming languages than Java.

Java class `DemoPythonMatcher` is used to wrap the python matcher. At runtime, this wrapper will call the python python matcher.

### Wrapping as Docker Web Package
As discussed above, the whole matching process is implemented in Python. The project is configured to be packaged as docker web package. You can find *all* the information conderning the packaging in the `pom.xml` file. 

If any dependencies need to be installed in the docker container, you can add them in the `pom.xml` (plugin: `docker-maven-plugin`).
In the demo project, you can see that we install the `rdflib` dependency (`<run>pip install rdflib</run>`). 

When you run `mvn clean install`, MELT will wrap a server around your matcher and put everything neatly in a docker container that you can share. In the `target` directory of your project, you will find a sharable docker container (file ending with `.tar.gz`). Others can then execute your matcher without installing any dependencies etc.

### Wrapping as SEALS/HOBBIT Package
You can also find an example project for [SEALS packaging](https://github.com/dwslab/melt/tree/master/examples/externalPythonMatcherSeals) as well as for [HOBBIT](https://github.com/dwslab/melt/tree/master/examples/externalPythonMatcherHobbit) packaging.