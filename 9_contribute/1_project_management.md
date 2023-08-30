---
layout: default
title: MELT Project Management
parent: Contribute
nav_order: 1
permalink: /contribute/project-management
---

# MELT Project Management

## Creating a new MELT Release

### Prerequisits
- make sure you have the latest commit
- make sure that the unit tests run on your local machine and that there are no javadoc errors (`mvn clean install`)

### Process

Run:
`mvn release:prepare -P melt-release,default`

Run:
`mvn release:perform -P melt-release,default`


## Adding Data to the MELT Repository

- Bring your data in the format required by class `LocalTrack` (one folder per test case containing: `source.rdf`, `target.rdf`, `reference.rdf`).
- Write a SEALS repository using class `SaveAsSealsRepo`.
- [requires admin rights] Place Track Data in `/data5/tdrs/testdata/persistent/` on `wifo5-14` (download server). 
- Create entries in `TrackRepository`.
- If desired, add the track to BuilInTracks to create a shortcut in the CLI.


## Run test coverage locally

```
mvn clean test jacoco:report
```

To do it only for one class/method:

```
mvn test jacoco:report -Djacoco.append=false -Dtest=de.uni_mannheim.informatik.dws.melt.{the selected class}
```

then you find in folder `target\site\jacoco` the corresponding files.