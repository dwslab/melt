package de.uni_mannheim.informatik.dws.melt.matching_data.processdatasets;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CreateFoodTestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateFoodTestCase.class);
    
    public static void main(String[] args){
        //downloaded from https://doi.org/10.15454/BVXD7I
        String filename = "Gold standard 73 Ciqual food product FoodOn food product.tab";
        
        try(CSVParser csvParser = CSVFormat.TDF.withFirstRecordAsHeader().parse(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))){
            Alignment a = new Alignment();
            for (CSVRecord record : csvParser) {
                String ciqualid = record.get("ciqualid");
                ciqualid = ciqualid.substring(0, ciqualid.lastIndexOf('.'));
                String source = "https://ico.iate.inra.fr/meatylab/origin_databases/2/foods/" + ciqualid;
                String target = record.get("closestfoodonProductId");
                Correspondence correspondence = new Correspondence(source.trim(), target.trim());
                a.add(correspondence);
            }
            a.serialize(new File("reference.rdf"));
        } catch (IOException ex) {
            LOGGER.error("Could not write alignment file", ex);
        }
        
    }
}
