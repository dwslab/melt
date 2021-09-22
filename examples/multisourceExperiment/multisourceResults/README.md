# Explanation of results files

File knowledge_graph.csv and knowledge_graph_extended.csv corresponding to table 1 in the paper.
For the conference track, only the extended version (conference_extended.csv) is shown in table 2 whereas
conference.csv is only executed but not shown in the paper due to space reasons.

All files starting with clustering shows the results corresponding to table 3.

Each file is structured in the same way. 
When using the AutoFilter, you can slide the data according to your desire, e.g. showing the micro precision, micro recall, and micro F1 of instance matches of all matchers on a specific track.
The columns are explained in the following:

```
Track   - the track (in this case only conference or knowledge graph)
Matcher - the name of the matching system (this also includes the base matcher and the multi-source approach together with the ordering)
Type    - All results are divided into ALL, CLASSES, PROPERTIES, and INSTANCES (not that the runtimes are only given for the ALL case)
Macro/Micro Precision, Recall and F1 - usual measures
Residual Macro/Micro Recall          - this measure shows the recall in comparison to a simple matcher (string comparisons)
# of TP/ Residual TP/ FP / FN        - the absolute numbers of true positives, true positives (without the ones from the simple matcher), false positives, and false negatives
# of Correspondences                 - number of correspondences returned by the system under test
Total Runtime                        - the total runtime in nano seconds (for reference)
Total Runtime(HH:MM:SS)              - the total runtime formatted as HH:MM:SS
BaseMatcher                          - the base matcher used by the multi source strategy
Method                               - the multi source strategy
```

In the clustering files, the names of the matcher have the following meaning:
First the base matcher, then AllPairs (because only there it makes sense to apply these approaches) and then the clustering approach.
For the clustering approaches Center, MergeCenter, StarOne, and StarTwo the next part in the name can be either Min or Max (which represents the priority order).
For all FAMER approaches (the ones mentioned before plus ConnectedComponents and CLIP) we also tried to add correspondences for entities in the same cluster.
This represents the last part of the name which can be true or false (true when adding correspondences and false if only correspondences are removed).
If the approach starts with "Err", then it corresponds to "Raad et al.: Detecting erroneous identity links on the web using network metrics".
The number after it, shows the threshold which is used to remove correspondences.