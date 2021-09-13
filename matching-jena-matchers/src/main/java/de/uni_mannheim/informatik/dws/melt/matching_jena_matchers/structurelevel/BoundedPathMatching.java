package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Structure based matcher which allows to find matches in hierarchies which are between two already matched entities.
 * Example:
 * <pre>
 *     SourceSuperClass  ---already matched with c=0.8---  TargetSuperClass
 *      ^                                                         ^
 *      |                                                         |
 *    rdfs:subClassOf                                         rdfs:subClassOf
 *      |                                                         |
 *  SourceIntermediateClass  --new with c=(0.8+0.9)/2=0.85-- TargetIntermediateClass 
 *      ^                                                         ^
 *      |                                                         |
 *    rdfs:subClassOf                                         rdfs:subClassOf
 *      |                                                         |
 *     SourceSubclass  ----already matched with c=0.9---    TargetSubclass
 * </pre>
 *
 * Per default it only matches the class hierarchy but it can be customized for further
 * hierarchies like properties via the configurations object within this class.
 */
public class BoundedPathMatching extends MatcherYAAAJena {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoundedPathMatching.class);
    
    private List<BoundedPathMatchingConfiguration> configurations;
    
    public BoundedPathMatching(){
        this.configurations = Arrays.asList(BoundedPathMatchingConfiguration.createClassHierarchyConfiguration());
    }

    public BoundedPathMatching(List<BoundedPathMatchingConfiguration> configurations) {
        this.configurations = configurations;
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {

        Set<String> matchedSources = inputAlignment.getDistinctSourcesAsSet();
        Set<String> matchedTargets = inputAlignment.getDistinctTargetsAsSet();
        for(Correspondence startCorrespondence : inputAlignment){                    
            OntResource sourceResource = source.getOntResource(startCorrespondence.getEntityOne());
            OntResource targetResource = target.getOntResource(startCorrespondence.getEntityTwo());
            if(sourceResource == null || targetResource == null){
                continue;
            }
            
            for(BoundedPathMatchingConfiguration config : this.configurations){
                if(config.isOfInterest(sourceResource, targetResource) == false)
                    continue;
                //compute possible path in each graph
                List<List<Resource>> sourcePaths = runDfs(sourceResource, matchedSources, config);
                List<List<Resource>> targetPaths = runDfs(targetResource, matchedTargets, config);

                // fill lastResourceToPaths
                Map<String, Set<List<Resource>>> lastResourceToTargetPaths = new HashMap<>();
                for(List<Resource> targetPath : targetPaths){
                    String lastElementURI = targetPath.get(targetPath.size()-1).getURI();
                    if(lastElementURI == null)
                        continue;
                    lastResourceToTargetPaths.computeIfAbsent(lastElementURI, __-> new HashSet<>()).add(targetPath);
                }

                //search for paths which match
                for(List<Resource> sourcePath : sourcePaths){
                    String lastElementURI = sourcePath.get(sourcePath.size()-1).getURI();
                    if(lastElementURI == null)
                        continue;

                    for(Correspondence endCorrespondence : inputAlignment.getCorrespondencesSourceRelation(lastElementURI, CorrespondenceRelation.EQUIVALENCE)){
                        for(List<Resource> targetPath : lastResourceToTargetPaths.getOrDefault(endCorrespondence.getEntityTwo(), new HashSet<>())){
                            //decide which paths should be matched -> only those with same length...
                            if(sourcePath.size() == targetPath.size()){
                                //match
                                LOGGER.info("Found matching paths where start and end are already matched. Thus matching all in between:");
                                LOGGER.info("Found path in source: {}", sourcePath);
                                LOGGER.info("Found path in target: {}", targetPath);
                                //start and end does not need to be matched.
                                double averagedConfidence = (startCorrespondence.getConfidence() + endCorrespondence.getConfidence()) / 2.0d;
                                for(int i = 1; i < sourcePath.size() - 1; i++){
                                    String sourcePathElementURI = sourcePath.get(i).getURI();
                                    String targetPathElementURI = targetPath.get(i).getURI();
                                    if(sourcePathElementURI == null ||targetPathElementURI == null)
                                        continue;
                                    inputAlignment.add(sourcePathElementURI, targetPathElementURI, averagedConfidence);
                                }
                            }
                        }
                    }
                }
            }
        }
        return inputAlignment;
    }
    
    private List<List<Resource>> runDfs(OntResource start, Set<String> targetNodes, BoundedPathMatchingConfiguration config){
        List<Resource> currentPath = new ArrayList<>();
        currentPath.add(start);
        
        Set<Resource> visited = new HashSet<>();
        visited.add(start);
        
        Set<String> updatedTargetNodes = new HashSet<>(targetNodes);
        String startURI = start.getURI();
        if(startURI != null){
            updatedTargetNodes.remove(startURI);
        }
        
        List<List<Resource>> results = new ArrayList<>();
        if(updatedTargetNodes.isEmpty())
            return results;
        dfs(currentPath, updatedTargetNodes, visited, results, config);
        return results;
    }
    
    private void dfs(List<Resource> currentPath, Set<String> targetNodes, Set<Resource> visited, List<List<Resource>> results, BoundedPathMatchingConfiguration config){
        Resource lastResource = currentPath.get(currentPath.size() - 1);
        String lastResourceURI = lastResource.getURI();
        if(lastResourceURI != null && targetNodes.contains(lastResourceURI)){
            if(currentPath.size() > 2)
                results.add(new ArrayList<>(currentPath));
        }
        
        if(currentPath.size() > (config.getMaxIntermediateNodes() + 1))
            return;
        for (Resource node : config.getSuccesors(lastResource)) {
            if (visited.contains(node) == false) {
                currentPath.add(node);
                visited.add(node);
                dfs(currentPath, targetNodes, visited, results, config);
                currentPath.remove(currentPath.size() - 1);
                visited.remove(node);
            }
        }
    }
    
    public List<BoundedPathMatchingConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<BoundedPathMatchingConfiguration> configurations) {
        if(configurations == null)
            throw new IllegalArgumentException("List of configurations should not be null.");
        this.configurations = configurations;
    }
    
    public void addConfiguration(BoundedPathMatchingConfiguration configuration) {
        this.configurations.add(configuration);
    }
    
    public void addConfigurationClassHierarchy() {
        this.configurations.add(BoundedPathMatchingConfiguration.createClassHierarchyConfiguration());
    }
    
    public void addConfigurationPropertyHierarchy() {
        this.configurations.add(BoundedPathMatchingConfiguration.createPropertyHierarchyConfiguration());
    }
}
