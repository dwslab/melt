---
layout: default
title: HOBBIT
parent: Matcher Packaging
nav_order: 2
permalink: /matcher-packaging/hobbit
---

# Packaging Matchers for HOBBIT

## TL;DR
1. Have a look at [examples/simpleHobbitMatcher](https://github.com/dwslab/melt/tree/master/examples/simpleHobbitMatcher).
2. Create hobbit account and gitlab access token.
3. Adjust settings in pom.xml to your needs.
4. Implement your matcher (see Matcher development).
5. Make sure docker is running on your machine.
6. Execute ```mvn deploy``` to deploy the docker image to hobbit server.
   - If you only execute ```mvn install``` it will create the hobbit docker image locally.
7.  The hobbit docker image can be found in the local docker repository.

## In More Detail
- for Hobbit submission
  - Prerequisites for Hobbit is a working docker installation ([download docker](https://www.docker.com/get-docker))
  - create a user account
      - open [http://master.project-hobbit.eu/](http://master.project-hobbit.eu/)  and click on ```Register```
 - user name should be the first part (local part - everything before the @) of your mail address
      - mail: `max.power@example.org` then user name should be `max.power`
 - more information at [the  hobbit wiki page](https://hobbit-project.github.io/master.html#user-registration)
  - update settings in gitlab (in Hobbit every matcher corresponds to a gitlab project)
      - go to page [http://git.project-hobbit.eu](http://git.project-hobbit.eu) and log in (same account as for the platform itself)
      - click on the upper right user icon and choose `settings`
  - create a Personal Access Token (click on `Access Tokens` give it a name and choose only the `api` scope)
      - use this access token and your username and password to create the settings file (see the pom.xml)
- adjust pom.xml to your needs
  - definitely change the following:
      - `groupId` and `artifactId` (only artifactId is used to identify the matcher -> make it unique)
      - `oaei.mainClass`: set it to the fully qualified path to the matcher (the class implementing ```IOntologyMatchingToolBridge``` or any subclass like ```MatcherURL``` or ```MatcherYAAAJena```)
      - benchmarks: change the benchmarks to the ones your system can deal with
      - create a settings file with username, password and access_token (see an example at the bottom of the [externalPythonMatcherHobbit pom file](https://github.com/dwslab/melt/blob/b1a34274f9368b5f07224b159b619710a6af3043/examples/externalPythonMatcherHobbit/pom.xml#L151))
- implement your matcher (see Matcher development)
- build your matcher
  - execute maven goals from command line or from any IDE
  - ```mvn install``` will create the hobbit docker image locally
      - On MacOS, you have to run ```export DOCKER_HOST=unix:///var/run/docker.sock``` (see [issue of docker-maven-plugin](https://github.com/spotify/docker-maven-plugin/issues/218)) in order to allow maven to communicate with docker.
  - ```mvn deploy``` will deploy docker image to hobbit server.


## Evaluate Your Matcher in HOBBIT

- you can start an experiment in hobbit online platform
- go to page [http://master.project-hobbit.eu/](http://master.project-hobbit.eu/), log in and choose `Benchmarks`
- select the benchmark you want to use
- select the system you want to use
- (optionally) specify configuration parameters and click on `submit`
- click on the Hobbit ID in the pop up to see the results (reload the page if it is not finished)
- More information can be found on the hobbit wiki page ['Benchmarking'](https://hobbit-project.github.io/benchmarking) and ['Browsing Results'](https://hobbit-project.github.io/browsing_results.html).
