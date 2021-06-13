---
layout: default
title: Core Concepts
parent: Matcher Evaluation
nav_order: 1
permalink: /matcher-evaluation/core-concepts
---

# Matcher Evaluation Core Concepts

## Executor
An `Executor` runs a matcher or a list of matchers on a single test case or a list of test cases.
You can run any matcher class (your own implemenation) but also packaged matchers by wrapping them as matchers (for details see the sections on [Matcher Packaging](https://dwslab.github.io/melt/matcher-packaging)).

## ExecutionResultSet
An `ExecutionResultSet` is the result of an `Executor`. It represents a collection of individual `ExecutionResult`s.

##  Evaluator
An evaluator is an abstract class for creating any evaluation based on an ExecutionResultSet.

## Track and TrackRepository
A track is a collection of multiple TestCases. A track may be available online via the MELT `TrackRepository`. 

## TestCase
A `TestCase` is an individual matching task that may belong to a `Track`. A test case consists of two URIs for source and target ontology/graph as well as an URI to the reference (also known as *solution* or *gold standard* of the matching task). 

## GoldStandardCompletness
Most test cases are complete, i.e. correspondences that are not in the gold standard are regarded as wrong. However, if you have a (local) test case that is not complete, you can provide the level gold standard completeness via enum `GoldStandardCompleteness`. The default evaluators will later on correctly evaluate system alignments on incomplete or partially complete test cases. Please refer to the [API documentation](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_data/GoldStandardCompleteness.html) to learn about the individual levels of completeness that are supported in MELT.