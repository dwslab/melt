package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SealsTrack extends Track {
    private static final Logger logger = LoggerFactory.getLogger(SealsTrack.class);
    
    protected String tdrsLocation;
    protected String testDataCollectionName;
    protected String testDataVersionNumber;
    
    public SealsTrack(String tdrsLocation, String testDataCollectionName, String testDataVersionNumber){
        this(tdrsLocation, testDataCollectionName, testDataVersionNumber, getNiceRemoteLocation(tdrsLocation));
    }

    public SealsTrack(String tdrsLocation, String testDataCollectionName, String testDataVersionNumber, String nicerLocation){
        this(tdrsLocation, testDataCollectionName, testDataVersionNumber, nicerLocation, false);
    }
    
    public SealsTrack(String tdrsLocation, String testDataCollectionName, String testDataVersionNumber, boolean useDuplicateFreeStorageLayout){
        this(tdrsLocation, testDataCollectionName, testDataVersionNumber, getNiceRemoteLocation(tdrsLocation), useDuplicateFreeStorageLayout);
    }
    
    public SealsTrack(String tdrsLocation, String testDataCollectionName, String testDataVersionNumber, String nicerLocation, boolean useDuplicateFreeStorageLayout){
        super(nicerLocation, testDataCollectionName, testDataVersionNumber, useDuplicateFreeStorageLayout);
        this.tdrsLocation = tdrsLocation;
        this.testDataCollectionName = testDataCollectionName;
        this.testDataVersionNumber = testDataVersionNumber;
    }    

    @Override
    protected void downloadToCache() throws IOException {
        logger.info("Downloading track {}", testDataCollectionName);
        SealsDownloadHelper bmd = new SealsDownloadHelper(tdrsLocation, testDataCollectionName, testDataVersionNumber);
        for(String testCaseId : bmd.getTestCases()){
            logger.info("  currently downloading {}", testCaseId);
            URL source = bmd.getDataItem(testCaseId, "source");
            URL target = bmd.getDataItem(testCaseId, "target");
            URL reference = bmd.getDataItem(testCaseId, "reference");
            
            if(exists(source) == false || exists(target) == false){
                logger.error("Source or Target is not defined - continue");
                continue;
            }
            
            this.saveInDefaultLayout(source, testCaseId, TestCaseType.SOURCE);
            this.saveInDefaultLayout(target, testCaseId, TestCaseType.TARGET);
            if(exists(reference)){
                this.saveInDefaultLayout(reference, testCaseId, TestCaseType.REFERENCE);
            }
        }
        logger.info("Finished downloading track {}", testDataCollectionName);
    }
    
    private static boolean exists(URL url)
    {
        try
        {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con = (HttpURLConnection) new URL(String.valueOf(url)).openConnection();
                con.setRequestMethod("HEAD");
                return con.getResponseCode() == HttpURLConnection.HTTP_OK;
        }
        catch(IOException e){ return false; }
    }
}
