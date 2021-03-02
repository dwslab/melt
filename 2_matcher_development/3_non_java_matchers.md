---
layout: default
title: Non-Java Matchers
parent: Matcher Development
nav_order: 3
permalink: /matcher-development/non-java-matchers
---

# Non-Java Matchers / External Matcher Development
MELT allows to develop a matcher in any other programming language and wrap it as a SEALS or HOBBIT package. 
Therefore, class `MatcherExternal` has to be extended. The interface for the external process is simple. It receives the input variables via the command 
line and outputs the results via the standard output of the process - similar to many Unix command line tools. All external resources have to be placed in a directory named `oaei-resources`. An example project for a 
Python matcher can be found [here](/examples/externalPythonMatcher).
