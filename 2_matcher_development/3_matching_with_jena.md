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
You can chain multiple Jena matchers using classes `MatcherPipelineYAAA` (Jena-independent) or `MatcherPipelineYAAAJena` (only Jena matchers).

### Packaging Your First Jena Pipeline Matcher
The following example can be found in the examples directory ([`simpleMatcherPipelineYAAAJena`](https://github.com/dwslab/melt/tree/master/examples/simpleMatcherPipelineYAAAJena)).
In the example, a new Matcher class `MyPipelineMatcher` is developed which extends [`MatcherPipelineYAAAJena`](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJena.java). 
The `initializeMatchers()` method is implemented. In this method, two matchers are chained: `ExactStringMatcher` and a WordNet matcher.
You can try out the new matcher by running the `main` method. Here, we run the matcher on the OAEI Anatomy test case. 

If you remove a matcher component (method `initializeMatchers()`) and run the evaluation again, you will see that the performance changes.
You can find a [list of all matching systems](https://dwslab.github.io/melt/matcher-components/full-matcher-list) and a [list of all filters](https://dwslab.github.io/melt/matcher-components/full-filter-list) in the corresponding sections in the user guide.

```java
// imports...

public class MyPipelineMatcher extends MatcherPipelineYAAAJena {


    @Override
    protected List<MatcherYAAAJena> initializeMatchers() {
        List<MatcherYAAAJena> result = new ArrayList<>();

        // let's add a simple exact string matcher
        result.add(new ExactStringMatcher());

        // let's add a background matcher
        result.add(new BackgroundMatcher(new WordNetKnowledgeSource(),
                ImplementedBackgroundMatchingStrategies.SYNONYMY, 0.5));

        return result;
    }

    public static void main(String[] args) {
        // let's initialize our matcher
        MyPipelineMatcher myMatcher = new MyPipelineMatcher();

        // let's execute our matcher on the OAEI Anatomy test case
        ExecutionResultSet ers = Executor.run(TrackRepository.Anatomy.Default.getFirstTestCase(), myMatcher);

        // let's evaluate our matcher (you can find the results in the `results` folder (will be created if it
        // does not exist).
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(ers);
        evaluatorCSV.writeToDirectory();
    }
}
```

Alternatively to extending class `MatcherPipelineYAAAJena`, you can use class `MatcherPipelineYAAAJenaConstructor` to pass the matching systems in the constructor.


# Jena Helpers

## LiteralExtractors
A recurring problem is obtaining natural language representations for Jena resources. `LiteralExtractor`s address this problem by retrieving literals for any given Jena resource. Implementations include [LiteralExtractorAllLiterals](), [LiteralExtractorByProperty](), or [LiteralExtractorUrlFragment](). Refer to the [JavaDoc documentation]() to find all implementing classes.

## ValueExtractors
A recurring problem is obtaining natural language representations for Jena resources. `ValueExtractor`s address this problem by retrieving Strings for any given Jena resource. Note that in most cases (for example when the language annotation is relevant), a `LiteralExtractor` may be the better design option.



