# Release Notes


## Changes in 3.1

**New**
- MELT now has an evaluation command line interface (MELT Evaluation CLI, module `matching-eval-cli`)

**Fixed**
- Test Cases of tracks are now ordered in a deterministic way. `getFirstTestCase()`, for instance, will now return
the same test case on every machine.

**Improvements**
- Docker Web Packages follow the convention that the file name of the package must carry the name of the image
  (with an optional `-latest` postfix). The constructor of `MatcherDockerFile` has been adapted so that it is 
  sufficient to provide a file.
- Docker Web packaging plugin: Image name now included in name of package.
