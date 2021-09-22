# Multisource Experiment

This example project provides a CLI to run 1:1 matchers in a multisource setup.
All documentation about those approaches can be found in the [MELT documentation about multisource matching](https://dwslab.github.io/melt/matcher-development/with-multiple-sources).

The matcher and additional KGs which are used for the CLI can be found at [Google Drive](https://drive.google.com/file/d/13xJuB34I1BgqMSuct_liRqxq6a-Np2ZA/view?usp=sharing)
Unzip the content and add them to the root folder of the multisourceExperiment project.
Furthermore build the jar file by executing `mvn package` (again in this folder).
Then copy the jar file at `multisourceExperiment/target/multisourceexperiment-1.0-jar-with-dependencies.jar` into the root folder.

The final structure should look like:
```
multisourceExperiment/
├─ conference_add/
├─ kgAdd/
├─ multisourceResults/
├─ oaeimatcher/
├─ src/
├─ multisourceexperiment-1.0-jar-with-dependencies.jar
├─ pom.xml
├─ README

```

To run the conference track with the LogMap 1:1 matcher, execute:
```
java -jar multisourceexperiment-1.0-jar-with-dependencies.jar -t conference -m oaeimatcher/LogMap.zip
```
If you want to add the [PARIS matcher](https://github.com/dig-team/PARIS) you can directly use the parameter `p` like:
```
java -jar multisourceexperiment-1.0-jar-with-dependencies.jar -t conference -p
```

To run the conference extended version, provide additional graphs with the `-g`option:
```
java -jar multisourceexperiment-1.0-jar-with-dependencies.jar -t conference -g conference_add/*.rdf -m oaeimatcher/LogMap.zip
```

similarly for the KG track and ATBox as the 1:1 matcher:
```
java -jar multisourceexperiment-1.0-jar-with-dependencies.jar -t kg -g kgAdd/*.nt -m oaeimatcher/ATBox.zip
```

Finally you can create the final result overview with:
```
java -jar multisourceexperiment-1.0-jar-with-dependencies.jar -w {your results folder}
```
e.g.
```
java -jar multisourceexperiment-1.0-jar-with-dependencies.jar -w results/results_2021-08-01_14-39-50
```
this generates a new results folder which contains a Latex file. 