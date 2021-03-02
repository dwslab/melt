---
layout: default
title: Matching with Background Knowledge
parent: Matcher Development
nav_order: 4
permalink: /matcher-development/with-background-knowledge

---

# Matching with Background Knowledge
MELT supports multiple external sources of background knowledge for matching:
1. WorNet
2. BabelNet
3. DBpedia
4. Wikidata
5. Wiktionary

## Core Concepts
The related classes/implementations can be found in [de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.](https://github.com/dwslab/melt/tree/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external).

Any external background knowledge source implements `ExternalResource` and, therefore, has a name (`getName()`) and an associated linker (`getLinker()`). A `LabelToConceptLinker` is responsible for linking natural language Strings, such as "European Union" to concepts in the background knowledge source, such as [https://www.wikidata.org/wiki/Q458](https://www.wikidata.org/wiki/Q458). Throughout the implementation, there is a distinction between a link which can be any identifier in the background knowledge source and a label.

There are currently two relevant capabilities (interfaces): [`SynonymCapability`]() for external resources that contain synonyms (or heuristics to obtain those) and [`HypernymCapability`]() for external resources that contain hypernyms (broader concepts).


## Matching with Wiktionary
[Wiktionary](https://www.wiktionary.org/) is a collaboratively built dictionary. As there is no official API for this dataset, the [DBnary](http://kaiko.getalp.org/about-dbnary/) graph is used.