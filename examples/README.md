# MELT Examples
This directory contains various sample projects using MELT. The overall documentation of MELT can be found in the 
[main README](/README.md).

## analyzingMatcherSimilarity
This project has been used to derive the statistics and figures of the original MELT paper.

## externalPythonMatcherHobbit
This project shows exemplary how a matcher can be implemented in Python and packed for HOBBIT using MELT.
You should add your python libraries in the POM file `<run>pip install rdflib</run><!-- install all your python dependencies here -->`.

## externalPythonMatcherSeals
This project shows exemplary how a matcher can be implemented in Python and packed for SEALS using MELT.

## meltDashboard
This project shows exemplary how to generate dashboards for the OAEI 2019 results.

## RDF2VecMatcher
This project uses <a href="https://github.com/dwslab/jRDF2Vec">jRDF2Vec</a> to train RDF2Vec Embeddings for the ontologies to be matched
and projects them in the same embedding space. 

## simpleJavaMatcher
This project contains different matchers that are implemented in Java and use the MELT framework.