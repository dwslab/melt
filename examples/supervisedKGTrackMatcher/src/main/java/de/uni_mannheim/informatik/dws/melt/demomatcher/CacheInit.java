package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;
import org.apache.jena.tdb.store.GraphTDB;

public class CacheInit{
        
    public static void populateKGTrack(String path){
        populateCache(TrackRepository.Knowledgegraph.V3, path);
        populateCache(TrackRepository.Knowledgegraph.V3_NonMatch_Small, path);
    }
    
    public static void populateCache(Track t, String baseDir){
        for(TestCase tc : t.getTestCases()){
            populateCache(tc, baseDir);
        }
    }
    
    public static void populateCache(TestCase tc, String baseDir){
        OntModelSpec spec = OntModelSpec.OWL_DL_MEM;        
        String[] names = tc.getName().split("-");
        OntologyCacheJena.put(tc.getSource().toString() + "_" + spec.hashCode(), loadTDBcache(baseDir + names[0], spec));
        OntologyCacheJena.put(tc.getTarget().toString() + "_" + spec.hashCode(), loadTDBcache(baseDir + names[1], spec));
    }
    
    private static OntModel loadTDBcache(String tdblocation, OntModelSpec spec){
        Dataset d = TDBFactory.createDataset(tdblocation);
        return ModelFactory.createOntologyModel(spec, d.getDefaultModel());
    }
    
    public static void createTDBcache(String url, String tdblocation){
        Dataset d = TDBFactory.createDataset(tdblocation);
        GraphTDB graphTDB = (GraphTDB)d.asDatasetGraph().getDefaultGraph();
        TDBLoader.load(graphTDB,url, true);
    }
}
