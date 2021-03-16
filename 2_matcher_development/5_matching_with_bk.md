---
layout: default
title: Matching with Background Knowledge
parent: Matcher Development
nav_order: 5
permalink: /matcher-development/with-background-knowledge
---

# Matching with Background Knowledge
MELT supports multiple external sources of background knowledge for matching:
1. [WordNet](#matching-with-wordnet)
2. [Wiktionary](#matching-with-wiktionary)
3. [Wikidata](#matching-with-wikidata)
4. [DBpedia](#matching-with-dbpedia)
5. [BabelNet](#matching-with-babelnet)
6. [WebIsALOD](#matching-with-webisalod)


## Core Concepts
The related classes/implementations can be found in [`de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external`](https://github.com/dwslab/melt/tree/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external).

Any external background knowledge source implements `ExternalResource` and, therefore, has a name (`getName()`) and an associated linker (`getLinker()`). A `LabelToConceptLinker` is responsible for linking natural language Strings, such as "European Union" to concepts in the background knowledge source, such as [Q458](https://www.wikidata.org/wiki/Q458). Throughout the implementation, there is a distinction between a link which can be any identifier in the background knowledge source and a label.

There are currently two relevant capabilities (interfaces): [`SynonymCapability`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/SynonymCapability.java) for external resources that contain synonyms (or heuristics to obtain those) and [`HypernymCapability`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/HypernymCapability.java) for external resources that contain hypernyms (broader concepts).


## Matching with WordNet
[WordNet](https://wordnet.princeton.edu/) is a well known lexical resource. It is a database of English words grouped in sets which represent a particular meaning, called synsets; further semantic relations such as hypernymy also exist in the database. The resource is publicly available. The knowledge source can be used to obtain synonyms (`SynonymCapability`) and hypernyms (`HypernymCapability`). The core class is [`WordNetKnowledgeSource`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/wordNet/WordNetKnowledgeSource.java).

## Matching with Wiktionary
[Wiktionary](https://www.wiktionary.org/) is a collaboratively built dictionary. As there is no official API for this dataset, the [DBnary](http://kaiko.getalp.org/about-dbnary/) graph is used. The knowledge source can be used to obtain synonyms (`SynonymCapability`) and hypernyms (`HypernymCapability`).

The core class is [`WiktionaryKnowledgeSource`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/wiktionary/WiktionaryKnowledgeSource.java). If a TDB path is passed to the constuctor, TDB is used, else a SPARQL connection to the endpoint is established.

### Use Wiktionary with TDB
- Download the core files in your desired language from the [DBnary download page](http://kaiko.getalp.org/about-dbnary/download/). 
- Unzip the bz2 file. 
- Install [Apache TDB Command Line Utilities](https://jena.apache.org/documentation/tdb/commands.html).
- Create your TDB dataset e.g. by running `tdbloader2 --loc ./wiktionary_tdb en_dbnary_ontolex_20210301.ttl`
- Initialize `WiktionaryKnowledgeSource` with the path to your tdb directory (in this case `<...>/wiktionary_tdb`)

## Matching with Wikidata
[Wikidata](https://www.wikidata.org/) is a publicly built knowledge graph. The knowledge source can be used to obtain synonyms (`SynonymCapability`) and hypernyms (`HypernymCapability`). The core class is [`WikidataKnowledgeSource`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/wikidata/WikidataKnowledgeSource.java).

## Matching with DBpedia
The knowledge source can be used to obtain synonyms (`SynonymCapability`) and hypernyms (`HypernymCapability`). The core class is [`DBpediaKnowledgeSourceTest`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/dbpedia/DBpediaKnowledgeSource.java). If a TDB path is passed to the constuctor, TDB is used, else a SPARQL connection to the endpoint is established.

### Use DBpedia with TDB
Create a TDB dataset (see instructions above) which is comprised of at least the following files:
- [`disambiguations_lang=en.ttl`](https://downloads.dbpedia.org/repo/dbpedia/generic/disambiguations/2020.12.01/disambiguations_lang=en.ttl.bz2)
- [`labels_lang=en.ttl`](https://downloads.dbpedia.org/repo/dbpedia/generic/labels/2020.12.01/labels_lang=en.ttl.bz2)
- [`instance-types_lang=en_specific.ttl`](https://downloads.dbpedia.org/repo/dbpedia/mappings/instance-types/2020.12.01/instance-types_lang=en_specific.ttl.bz2)
- [`mappingbased-literals_lang=en.ttl`](https://downloads.dbpedia.org/repo/dbpedia/mappings/mappingbased-literals/2020.12.01/mappingbased-literals_lang=en.ttl.bz2)

A full overview of DBpedia download links can be found on the [databus Web page](https://databus.dbpedia.org/dbpedia/collections/latest-core). 

## Matching with BabelNet
[BabelNet](https://babelnet.org/) is a very large multilingual knowledge graph that combines multiple other sources such as Wikipedia, WordNet, and Wiktionary. Unlike the other sources of background knowledge, BabelNet cannot be easily mass-queried. Researchers need to ask for the Lucene indices. Those are required to run the MELT BabelNet module. The core class is [`BabelNetKnowledgeSource`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/babelnet/BabelNetKnowledgeSource.java).

In order to use BabelNet, perform the following steps:
1) Obtain the BabelNet indices (you can request them via the BabelNet Web site).
2) Copy the [`config`](https://github.com/dwslab/melt/tree/master/matching-jena-matchers/config) folder to your project root.
3) Set the `babelnet.dir` in the [`template_babelnet.var.properties`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/config/template_babelnet.var.properties) file and rename it to `babelnet.var.properties`. The `babelnet.dir` property needs contain the directory where the BabelNet indices are stored.
4) Download the [WordNet database files](https://wordnet.princeton.edu/download/current-version).
5) Set the `jlt.wordnetVersion` and the `jlt.wordnetPrefix` in  [`template_jlt.var.properties`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/config/template_jlt.var.properties). Rename the file to `jlt.var.properties`.

If you build a JAR file, make sure that the config directory exists in the same directory in which the JAR is executed.

## Matching with WebIsALOD
[WebIsALOD](http://webisa.webdatacommons.org/) is a large RDF graph consisting of Web crawled hypernymy relations. The graph is available in two flavors: A filtered version containing less noise (referred to in the implementation as `WebIsAlod`**`Classic`**) and the full version containing a decent amount of noise (referred to in the implementation as `WebIsAlod`**`XL`**). The core classes are [`WebIsAlodClassicKnowledgeSource`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/webIsAlod/classic/WebIsAlodClassicKnowledgeSource.java) and [`WebIsAlodXLKnowledgeSource`](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/webIsAlod/xl/WebIsAlodXLKnowledgeSource.java).