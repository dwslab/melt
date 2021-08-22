package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_base.external.seals.MatcherSeals;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import java.io.File;
import java.nio.file.Paths;

/**
 * This class tests the generated seals package.
 * The exit code is important because it used in the continuouns integration 
 * pipeline (CI) in github to test the package.
 */
public class TestPackage {
    public static void main(String[] args) {        
        // cache seals client in CI (thus the location .m2 because it is cache already)
        File sealsClient = Paths.get(System.getProperty("user.home"), ".m2", "seals_client.jar").toFile(); 
        MatcherSeals sealsMatcher = new MatcherSeals(new File("target/simpleSealsMatcher-1.0-seals_external.zip"), sealsClient);
        ExecutionResult result = Executor.runSingle(TrackRepository.Anatomy.Default.getFirstTestCase(), sealsMatcher);
        
        if(result.getSystemAlignment().isEmpty()){
            System.exit(1);
        }
    }
}
