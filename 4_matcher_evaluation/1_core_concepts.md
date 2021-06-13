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
You can run any matcher class (your own implemenation) but also packaged matchers by wrapping them as matchers (for details see the sections on [Matcher Packaging](https://dwslab.github.io/melt/matcher-packaging))

## ExecutionResultSet
An `ExecutionResultSet` is the result of an `Executor`. It represents a collection of individual `ExecutionResult`s.

##  Evaluator
An evaluator is an abstract class for creating any evaluation based on an ExecutionResultSet.

## GoldStandardCompletness
TODO