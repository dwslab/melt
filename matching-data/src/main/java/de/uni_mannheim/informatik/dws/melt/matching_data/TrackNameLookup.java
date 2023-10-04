package de.uni_mannheim.informatik.dws.melt.matching_data;

import java.util.*;
import java.util.Map.Entry;

public class TrackNameLookup {


    /**
     * A map linking from each track to the allowed string representations to identify the track.
     * The List order matters: The first string will be the preferred string which will show up in the command line
     * client. The preferred strings must be lower-cased.
     * <p>
     * The map is not complete. It contains only the latest version of the most-used tracks.
     */
    private static final Map<Track, List<String>> trackToNameMap;
    private static final Map<String, Track> nameToTrackMap;
    
    static {
        trackToNameMap = new TreeMap<>();

        // anatomy
        add(TrackRepository.Anatomy.Default, "anatomy");
        
        //ProcessMatching
        add(TrackRepository.ProcessMatching.ALL, "pm-all", "process-matching-all", "processmatching-all");
        add(TrackRepository.ProcessMatching.BR, "pm-br", "process-matching-br", "processmatching-br");
        add(TrackRepository.ProcessMatching.UA, "pm-ua", "process-matching-ua", "processmatching-ua");

        // conference
        add(TrackRepository.Conference.V1, "conference");
        add(TrackRepository.Conference.V1_ALL_TESTCASES, "conferenceall", "conference-all");
        add(TrackRepository.Conference.ConferenceDBpedia, "conferencedbpedia", "conference-dbpedia");
        
        //Multifarm
        add(TrackRepository.Multifarm.ALL_IN_ONE_TRACK, "multifarm", "multifarmall", "multifarm-all");
        for (String languages : TrackRepository.Multifarm.LANGUAGE_PAIRS) {
            //adding all language pairs
            String language1 = languages.substring(0, 2);
            String language2 = languages.substring(3, 5);
            add(TrackRepository.Multifarm.getSpecificMultifarmTrack(languages),
                    "multifarm-" + languages, 
                    "multifarm-" + language2 + "-" + language1);
        }
                
        // complex
        add(TrackRepository.Complex.Hydrography, "complex-hydrography");
        add(TrackRepository.Complex.GeoLink, "complex-geolink");
        add(TrackRepository.Complex.PopgeoLink, "complex-popgeolink");
        add(TrackRepository.Complex.Popenslaved, "complex-popenslaved");
        add(TrackRepository.Complex.Popconference0, "complex-popconference0");
        add(TrackRepository.Complex.Popconference20, "complex-popconference20");
        add(TrackRepository.Complex.Popconference40, "complex-popconference40");
        add(TrackRepository.Complex.Popconference60, "complex-popconference60");
        add(TrackRepository.Complex.Popconference80, "complex-popconference80");
        add(TrackRepository.Complex.Popconference100, "complex-popconference100");
        
        //Food
        add(TrackRepository.Food.V1,"foodv1","food-v1");
        add(TrackRepository.Food.V2, "foodv2","food-v2");
        add(TrackRepository.Food.V2SUB, "foodv2sub","food-v2-sub","food-v2sub");
        
        // InstanceMatching
        add(TrackRepository.InstanceMatching.GeoLinkCruise, "instancematching", "instancematching-geolinkcruise", "instancematching-geolink-cruise");
        
        // largebio
        add(TrackRepository.Largebio.V2016.ALL, "largebio", "largebio-all");
        add(TrackRepository.Largebio.V2016.FMA_NCI_SMALL, "largebio-fma-nci-small");
        add(TrackRepository.Largebio.V2016.FMA_NCI_WHOLE, "largebio-fma-nci-whole");
        add(TrackRepository.Largebio.V2016.FMA_SNOMED_SMALL, "largebio-fma-snomed-small");
        add(TrackRepository.Largebio.V2016.FMA_SNOMED_WHOLE, "largebio-fma-snomed-whole");
        add(TrackRepository.Largebio.V2016.SNOMED_NCI_SMALL, "largebio-snomed-nci-small");
        add(TrackRepository.Largebio.V2016.SNOMED_NCI_WHOLE, "largebio-snomed-nci-whole");
        add(TrackRepository.Largebio.V2016.ONLY_SMALL, "largebio-small");
        add(TrackRepository.Largebio.V2016.ONLY_WHOLE, "largebio-whole");
        
        
        //Phenotype
        add(TrackRepository.Phenotype.V2017.DOID_ORDO, "phenotype-doid-ordo");
        add(TrackRepository.Phenotype.V2017.HP_MP, "phenotype-hp-mp");
        add(TrackRepository.Phenotype.V2017.HP_MESH, "phenotype-hp-mesh");
        add(TrackRepository.Phenotype.V2017.HP_OMIM, "phenotype-hp-omim");
        
        //BioML
        add(TrackRepository.BioML.V2022.EQUIV_SUPERVISED, "bioml-equiv-supervised");        
        add(TrackRepository.BioML.V2022.EQUIV_UNSUPERVISED, "bioml-equiv-unsupervised");
        
        // biodiv
        add(TrackRepository.Biodiv.Default, "biodiv-default", "biodiversity-default");
        add(TrackRepository.Biodiv.V2021, "biodiv-2021", "biodiversity-2021");
        add(TrackRepository.Biodiv.V2021OWL, "biodiv-2021owl", "biodiversity-2021owl");
        add(TrackRepository.Biodiv.V2022, "biodiv-2022", "biodiversity-2022");
        add(TrackRepository.Biodiv.V2023, "biodiv-2023", "biodiversity-2023");
        
        //MSE
        add(TrackRepository.MSE.V2021, "mse-2021");
        
        //Link
        add(TrackRepository.Link.Default, "link");
        
        // IIMB
        add(TrackRepository.IIMB.V1, "iimb");
        
        //commonkg
        add(TrackRepository.CommonKG.NELL_DBPEDIA_V1, "commonkg-nd");
        add(TrackRepository.CommonKG.YAGO_WIKIDATA_V1, "commonkg-yw");
        add(TrackRepository.CommonKG.YAGO_WIKIDATA_V1_SMALL, "commonkg-yw-small");

        // knowledge graph
        add(TrackRepository.Knowledgegraph.V4, "knowledgegraph", "kg");
        add(TrackRepository.Knowledgegraph.V3, "knowledgegraph-v3");
        add(TrackRepository.Knowledgegraph.V2, "knowledgegraph-v2");
        add(TrackRepository.Knowledgegraph.V1, "knowledgegraph-v1");
        
        //Laboratory
        add(TrackRepository.Laboratory.V1, "laboratory");

        //Pharmacogenomics
        add(TrackRepository.Pharmacogenomics.V1, "pharmacogenomics");

        // benchmark
        add(TrackRepository.SystematicBenchmark.Biblio.V2016.R1, "benchmark-biblio-r1");
        add(TrackRepository.SystematicBenchmark.Biblio.V2016.R2, "benchmark-biblio-r2");
        add(TrackRepository.SystematicBenchmark.Biblio.V2016.R3, "benchmark-biblio-r3");
        add(TrackRepository.SystematicBenchmark.Biblio.V2016.R4, "benchmark-biblio-r4");
        add(TrackRepository.SystematicBenchmark.Biblio.V2016.R5, "benchmark-biblio-r5");
        add(TrackRepository.SystematicBenchmark.Film.V2016.R1, "benchmark-film-r1");
        add(TrackRepository.SystematicBenchmark.Film.V2016.R2, "benchmark-film-r2");
        add(TrackRepository.SystematicBenchmark.Film.V2016.R3, "benchmark-film-r3");
        add(TrackRepository.SystematicBenchmark.Film.V2016.R4, "benchmark-film-r4");
        add(TrackRepository.SystematicBenchmark.Film.V2016.R5, "benchmark-film-r5");
        
        
        nameToTrackMap = new HashMap<>();
        for(Entry<Track, List<String>> entry : trackToNameMap.entrySet()){
            for(String name : entry.getValue()){
                if(nameToTrackMap.containsKey(name)){
                    throw new IllegalArgumentException("Built in tracks has multiple simple names for a track: " + name);
                }else{
                    nameToTrackMap.put(name, entry.getKey());
                }
            }
        }
    }
    
    private static void add(Track track, String... names) {
        if(trackToNameMap.containsKey(track)){
            throw new IllegalArgumentException("Built in tracks has multiple definitions for a track: " + track.toString());
        }else{
            trackToNameMap.put(track, Arrays.asList(names));
        }
    }

    /**
     * Get all <i>default</i> string representations for each track.
     * For example, the result will contain {@code "instancematching"} but not {@code "instancematching-geolink-cruise"}
     * because the latter is not the default identifying name.
     * @return A list of default names.
     */
    public static List<String> getTrackOptions() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<Track, List<String>> entry : trackToNameMap.entrySet()) {
            List<String> writings = entry.getValue();
            if (!writings.isEmpty()) {
                result.add(writings.get(0));
            }
        }
        Collections.sort(result);
        return result;
    }

    public static Track getTrackByString(String trackString) {
        if (trackString == null) {
            return null;
        }
        trackString = trackString.toLowerCase(Locale.ROOT).trim();        
        return nameToTrackMap.get(trackString);
    }

    
    public static void main(String[] args){
        System.out.println("test");
        System.out.println(getTrackByString("kg"));
        System.out.println(getTrackOptions());
    }
}
