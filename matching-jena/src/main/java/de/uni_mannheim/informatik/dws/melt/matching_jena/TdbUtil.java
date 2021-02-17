package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;
import org.apache.jena.tdb.store.GraphTDB;

/**
 * TDB util for generating and inspecting TDB datasets.
 */
public class TdbUtil {
    
    
    public static void createTDB(String url, String tdblocation){
        Dataset d = TDBFactory.createDataset(tdblocation);
        GraphTDB graphTDB = (GraphTDB)d.asDatasetGraph().getDefaultGraph();
        TDBLoader.load(graphTDB,url, true);
    }
    
    public static OntModel getOntModelFromTDB(String tdblocation, OntModelSpec spec){
        Dataset d = TDBFactory.createDataset(tdblocation);
        return ModelFactory.createOntologyModel(spec, d.getDefaultModel());
    }
    
    public static Model getModelFromTDB(String tdblocation){
        Dataset d = TDBFactory.createDataset(tdblocation);
        return d.getDefaultModel();
    }
    
    
    /**
     * Checks if there is at least one file in the directory denoted by this url which ends with .dat or .idn.
     * This indicates that there is a TDB dataset.
     * There is also the function {@link TDBFactory#inUseLocation()} but it also returns true if the directory contains
     * one rdf file and is definitely not a TDB dataset.
     * The definition when a directory is a TDB directory might change in the future.
     * @param directory the directory as a File
     * @return true if it is a TDB dataset e.g. contains at least of file which ends with .dat or .idn
     */
    public static boolean isTDB1Dataset(File directory){
        if(directory == null)
            return false;
        if(directory.isDirectory() == false)// if not exists or is not a directory
            return false;
        
        File[] entries = directory.listFiles((dir, name) -> {
            if(name.endsWith(".dat") || name.endsWith(".idn"))
                return true;
            return false;
        });
        return entries.length > 0 ;
    }
    /**
     * Returns a file object given a url or path as string.
     * @param url a url or path as string
     * @return the file object
     */
    public static File getFileFromURL(String url){
        try {
            return Paths.get(new URL(url).toURI()).toFile();
        } catch (MalformedURLException | URISyntaxException ex) {
            return new File(url); // if it is just a path and not a URL.
        }
    }
    
}
