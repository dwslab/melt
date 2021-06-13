<h1 align="left">MELT - Matching EvaLuation Toolkit</h1>
<p>
<a href="https://github.com/dwslab/melt/actions/workflows/java_build.yml"><img src="https://github.com/dwslab/melt/actions/workflows/java_build.yml/badge.svg"></a>
<a href="https://github.com/dwslab/melt/actions/workflows/java_documentation.yml"><img src="https://github.com/dwslab/melt/actions/workflows/java_documentation.yml/badge.svg"></a>
<a href="https://github.com/dwslab/melt/actions/workflows/documentation_check.yml"><img src="https://github.com/dwslab/melt/actions/workflows/documentation_check.yml/badge.svg"></a>
<a href="https://github.com/dwslab/melt/actions/workflows/java_doclet.yml"><img src="https://github.com/dwslab/melt/actions/workflows/java_doclet.yml/badge.svg"></a>
<a href="https://github.com/dwslab/melt/actions/workflows/java_coverage.yml"><img src="https://github.com/dwslab/melt/actions/workflows/java_coverage.yml/badge.svg"></a>
<a href="https://coveralls.io/github/dwslab/melt?branch=master"><img src="https://coveralls.io/repos/github/dwslab/melt/badge.svg?branch=master"></a>
<a href="https://mvnrepository.com/artifact/de.uni-mannheim.informatik.dws.melt"><img src="https://img.shields.io/maven-central/v/de.uni-mannheim.informatik.dws.melt/matching-eval"></a>
<a href="https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white"><img src="https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white"></a>
<a href="https://img.shields.io/github/license/dwslab/melt"><img src="https://img.shields.io/github/license/dwslab/melt"></a>
</p>
<p align="left" style="font-style:italic">A powerful framework for ontology, instance, and knowledge graph matching.</p>

MELT is a powerful maven framework for developing, tuning, evaluating, and packaging ontology matching systems.
It is optimized to be used in [OAEI](http://oaei.ontologymatching.org/) campaigns and allows to submit matchers to the SEALS and HOBBIT evaluation platform easily. MELT can also be used for non OAEI-related matching tasks and evaluation.

Found a bug? Don't hesitate to <a href="https://github.com/dwslab/melt/issues">open an issue</a>.

## User Guide
All functionality is documented in our <a href="https://dwslab.github.io/melt/">User Guide</a>.<br/>
Note that you can also easily search the entire user guide:
<a href="https://dwslab.github.io/melt/">
![image](https://raw.githubusercontent.com/dwslab/melt/gh-pages/media/search_screenshot.png)
</a>

## JavaDoc
- [Latest Commit / Developer Version](https://dwslab.github.io/melt/javadoc_latest/index.html)
- [Releases](https://javadoc.io/doc/de.uni-mannheim.informatik.dws.melt)

## Matcher Development in Java

### Release Version
MELT is now available in [maven central](https://repo1.maven.org/maven2/de/uni-mannheim/informatik/dws/melt/) and can be added as a dependency with e.g.:
```xml
<dependency>
    <groupId>de.uni-mannheim.informatik.dws.melt</groupId>
    <artifactId>matching-eval</artifactId>
    <version>3.0</version>
</dependency>
```

### Developer Version
If you want to use the bleeding edge, but don't want to build it yourself 
(by cloning the repository and execute `mvn install` in the main directory),
you can use [jitpack.io](https://jitpack.io/#dwslab/melt/) :

```xml
<dependency>
    <groupId>com.github.dwslab.melt</groupId>
    <artifactId>matching-eval</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```
for the version, you can use any [release tag](https://github.com/dwslab/melt/releases), [short commit hash](https://github.com/dwslab/melt/commits/master) or `master-SNAPSHOT` to get the current development version.
They are also listed directly at [jitpack.io/#dwslab/melt/](https://jitpack.io/#dwslab/melt/).
Furthermore, you need to add the jitpack repository in your pom file:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
