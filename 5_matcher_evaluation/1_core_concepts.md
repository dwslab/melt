---
layout: default
title: Core Concepts
parent: Matcher Evaluation
nav_order: 1
permalink: /matcher-evaluation/core-concepts
---

# Matcher Evaluation Core Concepts

##  Evaluator
An evaluator is an abstract class for creating any evaluation based on an `ExecutionResultSet`.

## ExecutionResultSet
An `ExecutionResultSet` is the result of an `Executor`. It represents a collection of individual `ExecutionResult`s.

## Executor
An `Executor` runs a matcher or a list of matchers on a single test case or a list of test cases.
You can run any matcher class (your own implemenation) but also packaged matchers by wrapping them as matchers (for details see the sections on [Matcher Packaging](https://dwslab.github.io/melt/matcher-packaging)).

## Filter
A filter is a matcher that does not add new correspondences to the alignment but instead
further processes the given alignment by (1) removing correspondences and/or (2) adding new feature weights to
existing correspondences. MELT filters implement the [`Filter`](/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/Filter.java) interface. Filters are intended to be used in a matcher pipeline.

## GoldStandardCompletness
Most test cases are complete, i.e. correspondences that are not in the gold standard are regarded as wrong. However, if you have a (local) test case that is not complete, you can provide the level of gold standard completeness via enum `GoldStandardCompleteness`. The default evaluators will later on correctly evaluate system alignments for incomplete or partially complete test cases. Please refer to the [API documentation](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_data/GoldStandardCompleteness.html) to learn about the individual levels of completeness that are supported in MELT.

## Matcher
A matcher is a process which, given two ontologies and an alignment, returns an alignment. Matchers can be used on their own or can be combined in a modular way through a matching pipeline. MELT offers various abstract matcher classes that can be extended such as `MatcherYAAA`, `MatcherYAAAJena`, or `MatcherYAAAOwlApi`.

## TestCase
A `TestCase` is an individual matching task that may belong to a `Track`. A test case consists of two URIs for source and target ontology/graph as well as an URI to the reference (also known as *solution* or *gold standard* of the matching task). 

## Track and TrackRepository
A track is a collection of multiple TestCases. A track may be available online via the MELT `TrackRepository`. 