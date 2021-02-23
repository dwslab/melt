---
layout: default
title: Build Your First Matcher
parent: Matcher Development
nav_order: 1
---

# Build Your First Java Matcher with MELT

## Matcher Development in Java

1. Pick a class to start with depending on your needs. If you start from scratch `MatcherYAAAJena` or `MatcherYAAAOwlApi` are the best fit depending on whether your prefer [Jena](https://jena.apache.org) or the [OWL API](http://owlcs.github.io/owlapi/). 
Classes that can be extended for matcher implementation:
   * `MatcherURL`
   * `MatcherYAA`
   * `MatcherYAAAJena`
   * `MatcherYAAAOwlApi`
2. Implement the `match()` method.
