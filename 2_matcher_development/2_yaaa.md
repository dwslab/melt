---
layout: default
title: YAAA
parent: Matcher Development
nav_order: 2
---

# Yet Another Alignment API (YAAA)

## Core Concepts
MELT introduces a simple API for matcher development, referred to as YAAA. In the following, the most important classes are explained: 
- `Correspondence`<br/>A Correspondence contains a relation (`CorrespondenceRelation`) that holds between two elements from two different ontologies. 
In the literature, it is also known as "Mapping Cell" or "Cell". Optionally, a correspondence might have a confidence value,
and an identifier. Note that a correspondence can be extended with further attributes. For usability, class `DefaultExtensions`
contains the most common extensions. The correspondence is uniquely identified by the two matching elements as well as the relation.
- `Alignment`<br/>An alignment is a set (no duplicates, no ordering) of multiple `Correspondence` instances. In the literature, it is also 
known as "mapping" or "mappings". 

Class `AlignmentSerializer` can be used to persist an alignment to a file and class `AlignmentParser` can parse an alignment
file directly into a Java object. 

## Development Options
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

![image](media/matcher_hierarchy.png)