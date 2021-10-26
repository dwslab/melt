---
layout: default
title: MELT Track Repository
nav_order: 5
permalink: /track-repository
---


# MELT Track Repository
The [`TrackRepository`](https://github.com/dwslab/melt/blob/master/matching-data/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_data/TrackRepository.java) 
checks whether the required ontologies and alignments are available in the cache folder (`~/oaei_track_cache`); if data is missing, it is automatically downloading and 
caching it for the next access. The MELT Track Repository is used by the OAEI since 2020.

Exemplary call using the `TrackRepository`:
```java
// access the Anatomy track
TrackRepository.Anatomy.Default;

// access all Conference test cases
TrackRepository.Conference.V1.getTestCases();
```

The resulting instances can be directly used by the Executor or any other MELT functionality that requires tracks or
test cases.

## Available Tracks
MELT also provides a server with default datasets used by the OAEI. 
They are identified by three components: 
1. `Repository` (location-URI): http://oaei.webdatacommons.org/tdrs/	 
2. `Track/Suite - ID` (collection-name): \<see table\>
3. `Version ID`: \<see table below\>

The [MELT evaluation client](https://dwslab.github.io/melt/matcher-evaluation/client) uses these track identifiers.
The MELT repository is built so that it is fully compatible with the former SEALS client. For example, you can still use the SEALS client JAR to fetch the data from the MELT repository.

You can also download a ZIP file of the data using the links provided in the table below.

Name | Suite-ID | Version-ID | Download |
---- | -------- | ---------- | -------- |
[anatomy](http://oaei.ontologymatching.org/2019/anatomy/index.html) | `anatomy_track` | `anatomy_track-default` | [📥]()
[conference](http://oaei.ontologymatching.org/2019/conference/index.html) | `conference` | - `conference-v1` <br> - `conference-v1-all` (also testcases without reference) <br> - `conference-dbpedia` | [📥]()
[knowledgegraph](http://oaei.ontologymatching.org/2019/knowledgegraph/index.html) | `knowledgegraph` | - `v3` <br/> - `commonkg` ([paper](https://github.com/OmaimaFallatah/KG_GoldeStandard)) | [📥]()
[iimb](http://islab.di.unimi.it/content/im_oaei/2018/) | `iimb` | `v1` | [📥]()
[biodiv](http://oaei.ontologymatching.org/2018/biodiv/index.html) | `biodiv` | - `2018` <br/> - `2021` <br/> - `2021owl` | [📥]()
[link](https://project-hobbit.eu/challenges/om2019/) | `link` | `2017` | [📥]()
[phenotype](https://sws.ifi.uio.no/oaei/phenotype/) | `phenotype` | - `phenotype-hp-mp-2017-bioportal`<br/>- `phenotype-doid-ordo-2017-bioportal` | [📥]()
[multifarm](http://oaei.ontologymatching.org/2018/multifarm/index.html) | `<language_pair>`<br> `multifarm`  |  `<language_pair>-v2` <br> `all-v2` | [📥]()
[largebio](http://www.cs.ox.ac.uk/isg/projects/SEALS/oaei/) | `largebio` |  -`largebio-all_tasks_2016`<br>- `largebio-fma_nci_small_2016`<br>- `largebio-fma_nci_whole_2016`<br>- `largebio-fma_snomed_small_2016`<br>- `largebio-fma_snomed_whole_2016`<br>- `largebio-snomed_nci_small_2016`<br>- `largebio-snomed_nci_whole_2016` | [📥]()
[complex](http://oaei.ontologymatching.org/2019/complex/index.html) | - `geolink`<br>- `hydrography`<br>- `popgeolink`<br>- `popenslaved`<br>- `popconference`|  - `geolink-v1`<br>- `hydrography-v1`<br>- `popgeolink-v1`<br>- `popenslaved-v1`<br>- `popconference-[0-20-40-60-80-100]-v1` | [📥]()
GeoLinkCruise | `geolinkcruise`| `geolinkcruise-v1` | [📥]()
Laboratory | `laboratory`| `laboratory-v1` | [📥]()

Available multifarm language pairs:<br/>
`ar-cn`, `ar-cz`, `ar-de`, `ar-en`, `ar-es`, `ar-fr`, `ar-nl`, `ar-pt`, `ar-ru`, `cn-cz`, `cn-de`, `cn-en`, `cn-es`, 
`cn-fr`, `cn-nl`, `cn-pt`, `cn-ru`, `cz-de`, `cz-en`, `cz-es`, `cz-fr`, `cz-nl`, `cz-pt`, `cz-ru`, `de-en`, `de-es`, 
`de-fr`, `de-nl`, `de-pt`, `de-ru`, `en-es`, `en-fr`, `en-nl`, `en-pt`, `en-ru`, `es-fr`, `es-nl`, `es-pt`, `es-ru`, 
`fr-nl`, `fr-pt`, `fr-ru`, `nl-pt`, `nl-ru`, `pt-ru`


## Further Services

### TestCase/Track Validation Service
Creating new tracks and test case can be very cumbersome. The MELT validation service allows you to check whether your 
test cases:
1. Contain parseable ontologies.
2. Contain a parseable reference alignment.
3. Mention only URIs in the reference alignment that also appear in the corresponding source and target ontologies.

Exemplary call using the [`TestCaseValidationService`](https://github.com/dwslab/melt/blob/master/matching-validation/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_validation/TestCaseValidationService.java):
```java
URI sourceUri = new File("<path to source ontology file>").toURI();
URI targetUri = new File("<path to target ontology file>").toURI();
URI referenceUri = new File("<path to reference alignment file>").toURI();
TestCase testCase = new TestCase("FSDM", sourceUri, targetUri, referenceUri, null);
TestCaseValidationService validator = new TestCaseValidationService(testCase)
System.out.println(validator);
```
You can also test your track on different versions of Jena and the OWL API automatically
by adapting the [`TestLocalFile`](https://github.com/dwslab/melt/blob/master/matching-validation/src/test/java/de/uni_mannheim/informatik/dws/melt/matching_validation/local/TestLocalFile.java) 
and running `runAll.cmd` in the Windows shell. The release versions to be tested can be edited in the corresponding
[`pom.xml`](https://github.com/dwslab/melt/blob/master/matching-validation/pom.xml).
