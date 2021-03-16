---
layout: default
title: Matching with Jena
parent: Matcher Development
nav_order: 3
permalink: /matcher-development/matching-with-jena
---

# Matching with Jena
MELT contains built-in functionality to work smoothly with [Apache Jena](https://jena.apache.org/). You need to import the corresponding dependency for matcher development:

```xml
<dependency>
    <groupId>de.uni-mannheim.informatik.dws.melt</groupId>
    <artifactId>matching-jena</artifactId>
    <version>2.6</version>
</dependency>
```

If you want to use matcher tooling (such as integrated usage of background knowledge, convenience functions, pre-built matchers), you need to import:
```xml
<dependency>
    <groupId>de.uni-mannheim.informatik.dws.melt</groupId>
    <artifactId>matching-jena-matchers</artifactId>
    <version>2.6</version>
</dependency>
```

Check [maven central](https://mvnrepository.com/artifact/de.uni-mannheim.informatik.dws.melt/matching-jena) for the latest version.


## Developing Your Own Jena Matcher
If you develop a matching system based on Jena, you can extend the abstract class [`MatcherYAAAJena`](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAAJena.java) (rather than `MatcherYAAA` or `IMatcher`) and implement: 
```java
Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties)
```

## Chaining Jena Matchers
You can chain multiple Jena matchers using classes `MatcherPipelineYAAA` (Jena-inedependent) or `MatcherPipelineYAAAJena` (only Jena matchers).

# Jena Helpers

## LiteralExtractors
A recurring problem is obtaining natural language representations for Jena resources. `LiteralExtractor`s address this problem by retrieving literals for any given Jena resource. Implementations include [LiteralExtractorAllLiterals](), [LiteralExtractorByProperty](), or [LiteralExtractorUrlFragment](). Refer to the [JavaDoc documentation]() to find all implementing classes.

## ValueExtractors
A recurring problem is obtaining natural language representations for Jena resources. `ValueExtractor`s address this problem by retrieving Strings for any given Jena resource. Note that in most cases (for example when the language annotation is relevant), a `LiteralExtractor` may be the better design option.



