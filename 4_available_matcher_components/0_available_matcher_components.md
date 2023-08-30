---
layout: default
title: Available Matchers
has_children: true
nav_order: 4
permalink: /matcher-components
---

# Available Matchers and Filters
MELT offers a wide range of-out-of-the-box matchers and filters. Below, we list the most significant matchers and filters. 
The remaining pages in this section list all available matchers and filters in MELT.

A **filter** is a matcher that does not add new correspondences to the alignment but instead
further processes the given alignment by (1) removing correspondences and/or (2) adding new feature weights to
existing correspondences. MELT filters implement the [`Filter`](/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/Filter.java) interface.

**List of Matchers (Selection)**
- [`BaselineStringMatcher`](/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/BaselineStringMatcher.java)<br/>
Default String-matcher, used as default-baseline in evaluators.
- [`ScalableStringProcessingMatcher`](/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/scale/ScalableStringProcessingMatcher.java)<br/>
Configurable String-matcher that scales well.
- [`ParisMatcher`](/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/wrapper/ParisMatcher.java)<br/>
Wrapper of the Paris matching system.

**List of Filters (Selection)** 
- [`MachineLearningScikitFilter`](/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/MachineLearningScikitFilter.java)<br/>
This filter learns and applies a classifier given a training sample and an existing alignment. You can refer to
our article *Supervised Ontology and Instance Matching with MELT* for a more detailed
description and application examples. In the [example](/examples) directory, you can find the implementations of the 
matchers described in the article.
- [`NaiveDescendingExtractor`](/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveDescendingExtractor.java)<br/>
It iterates over the sorted (descending) correspondences and uses the correspondence with the highest confidence.
Afterwards removes every other correspondence with the same source or target.
- [`MaxWeightBipartiteExtractor`](/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/MaxWeightBipartiteExtractor.java)<br/>
Faster implementation than the HungarianExtractor to generate a one-to-one alignment.
- [`HungarianExtractor`](/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/HungarianExtractor.java)<br/> 
Implementation of the Hungarian algorithm to find a one-to-one mapping.
- [`ConfidenceFilter`](/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ConfidenceFilter.java)<br/>
Simple filter that removes correspondences with a confidence lower than a predefined threshold.
Thresholds can be set per type.