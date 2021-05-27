package de.uni_mannheim.informatik.dws.melt.matching_assembly;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;

/**
 * A small helper class which contains all dependencies as a list and the full file name of the base version.
 */
public class LibsAndBaseVersion {
    private List<String> libList;
    private String matchingBaseFullFileName;
    
    public LibsAndBaseVersion(Archiver archiver) throws ArchiverException{
        this.libList = new ArrayList<>(); 
        ResourceIterator ri = archiver.getResources();
        while (ri.hasNext()) {
            String normalisedName = normalise(ri.next().getName());
            if(normalisedName.startsWith("bin/lib/")){                
                this.libList.add(normalisedName.substring(4));
            }
            int index = normalisedName.indexOf("matching-base-");
            if(index >= 0){
                matchingBaseFullFileName = normalisedName.substring(index);
            }
        }
        Collections.sort(this.libList);
        if(matchingBaseFullFileName == null){
            throw new ArchiverException("Could not find matching-base in the dependencies. Please add the dependency matching-base to your project.");
        }
    }
    
    private static String normalise(String path){
        return path.replace( '\\', '/' ).replace( File.separatorChar, '/' );
    }

    public List<String> getLibList() {
        return libList;
    }

    public String getMatchingBaseFullFileName() {
        return matchingBaseFullFileName;
    }
    
    public static String getMatchingBaseFullFileName(Archiver archiver) throws ArchiverException{
        ResourceIterator ri = archiver.getResources();
        while (ri.hasNext()) {
            String normalisedName = normalise(ri.next().getName());
            int index = normalisedName.indexOf("matching-base-");
            if(index >= 0){
                return normalisedName.substring(index);
            }
        }
        throw new ArchiverException("Could not find matching-base in the dependencies. Please add the dependency matching-base to your project.");
    }
}
