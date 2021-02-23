---
layout: default
title: Matcher Development
has_children: true
nav_order: 2
---

# Matcher Development

## Release Version
MELT is available in [maven central](https://repo1.maven.org/maven2/de/uni-mannheim/informatik/dws/melt/) and can be added as a dependency with e.g.:
```xml
<dependency>
    <groupId>de.uni-mannheim.informatik.dws.melt</groupId>
    <artifactId>matching-eval</artifactId>
    <version>2.6</version>
</dependency>
```

## Development Version
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

