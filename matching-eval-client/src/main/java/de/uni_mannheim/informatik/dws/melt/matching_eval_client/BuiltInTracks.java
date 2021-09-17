package de.uni_mannheim.informatik.dws.melt.matching_eval_client;

import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import org.apache.jena.ext.com.google.common.collect.Comparators;

import java.util.*;

public class BuiltInTracks {


    /**
     * A map linking from each track to the allowed string representations to identify the track.
     * The List order matters: The first string will be the preferred string which will show up in the command line
     * client.
     * <p>
     * The map is not complete. It contains only the latest version of the most-used tracks.
     */
    private static TreeMap<Track, List<String>> simpleNameToTrackMap;

    static {
        simpleNameToTrackMap = new TreeMap<>();

        // anatomy
        simpleNameToTrackMap.put(TrackRepository.Anatomy.Default, new ArrayList<String>() {
            {
                add("anatomy");
            }
        });

        // conference
        simpleNameToTrackMap.put(TrackRepository.Conference.V1, new ArrayList<String>() {
            {
                add("conference");
            }
        });

        // knowledge graph
        simpleNameToTrackMap.put(TrackRepository.Knowledgegraph.V4, new ArrayList<String>() {
            {
                add("knowledgegraph");
                add("kg");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Knowledgegraph.V3, new ArrayList<String>() {
            {
                add("knowledgegraph-v3");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Knowledgegraph.V2, new ArrayList<String>() {
            {
                add("knowledgegraph-v2");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Knowledgegraph.V1, new ArrayList<String>() {
            {
                add("knowledgegraph-v1");
            }
        });

        // largebio
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.ALL, new ArrayList<String>() {
            {
                add("largebio");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.ONLY_SMALL, new ArrayList<String>() {
            {
                add("largebio-small");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.FMA_NCI_SMALL, new ArrayList<String>() {
            {
                add("largebio-fma-nci-small");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.FMA_NCI_WHOLE, new ArrayList<String>() {
            {
                add("largebio-fma-nci-whole");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.FMA_SNOMED_SMALL, new ArrayList<String>() {
            {
                add("largebio-fma-snomed-small");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.FMA_SNOMED_WHOLE, new ArrayList<String>() {
            {
                add("largebio-fma-snomed-whole");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.SNOMED_NCI_SMALL, new ArrayList<String>() {
            {
                add("largebio-snomed-nci-small");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.SNOMED_NCI_WHOLE, new ArrayList<String>() {
            {
                add("largebio-snomed-nci-whole");
            }
        });

        simpleNameToTrackMap.put(TrackRepository.Multifarm.ALL_IN_ONE_TRACK, new ArrayList<String>() {
            {
                add("multifarm");
            }
        });

        // Adding all language pairs
        for (String languages : TrackRepository.Multifarm.LANGUAGE_PAIRS) {
            String language1 = languages.substring(0, 2);
            String language2 = languages.substring(3, 5);
            simpleNameToTrackMap.put(TrackRepository.Multifarm.getSpecificMultifarmTrack(languages),
                    new ArrayList<String>() {
                        {
                            add("multifarm-" + languages);
                            add("multifarm-" + language2 + "-" + language1);
                        }
                    });
        }

        // biodiv
        simpleNameToTrackMap.put(TrackRepository.Biodiv.Default, new ArrayList<String>() {
            {
                add("biodiv");
                add("biodiversity");
            }
        });

        // complex
        simpleNameToTrackMap.put(TrackRepository.Complex.GeoLink, new ArrayList<String>() {
            {
                add("complex-geolink");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.Hydrography, new ArrayList<String>() {
            {
                add("complex-hydrography");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.PopgeoLink, new ArrayList<String>() {
            {
                add("complex-popgeolink");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.Popconference0, new ArrayList<String>() {
            {
                add("complex-popconference0");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.Popconference20, new ArrayList<String>() {
            {
                add("complex-popconference20");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.Popconference40, new ArrayList<String>() {
            {
                add("complex-popconference40");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.Popconference60, new ArrayList<String>() {
            {
                add("complex-popconference60");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.Popconference80, new ArrayList<String>() {
            {
                add("complex-popconference80");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.Popconference100, new ArrayList<String>() {
            {
                add("complex-popconference100");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Complex.Popenslaved, new ArrayList<String>() {
            {
                add("complex-popenslaved");
            }
        });

        // IIMB
        simpleNameToTrackMap.put(TrackRepository.IIMB.V1, new ArrayList<String>() {
            {
                add("iimb");
            }
        });

        // IIMB
        simpleNameToTrackMap.put(TrackRepository.InstanceMatching.GeoLinkCruise, new ArrayList<String>() {
            {
                add("instancematching");
                add("instancematching-geolinkcruise");
                add("instancematching-geolink-cruise");
            }
        });

        simpleNameToTrackMap.put(TrackRepository.Laboratory.V1, new ArrayList<String>() {
            {
                add("laboratory");
            }
        });

        simpleNameToTrackMap.put(TrackRepository.Link.Default, new ArrayList<String>() {
            {
                add("link");
            }
        });

        simpleNameToTrackMap.put(TrackRepository.Phenotype.V2017.DOID_ORDO, new ArrayList<String>() {
            {
                add("phenotype-doid-ordo");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Phenotype.V2017.HP_MP, new ArrayList<String>() {
            {
                add("phenotype-hp-mp");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Phenotype.V2017.HP_MESH, new ArrayList<String>() {
            {
                add("phenotype-hp-mesh");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Phenotype.V2017.HP_OMIM, new ArrayList<String>() {
            {
                add("phenotype-hp-omim");
            }
        });

        // benchmark
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Biblio.V2016.R1, new ArrayList<String>() {
            {
                add("benchmark-biblio-r1");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Biblio.V2016.R2, new ArrayList<String>() {
            {
                add("benchmark-biblio-r2");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Biblio.V2016.R3, new ArrayList<String>() {
            {
                add("benchmark-biblio-r3");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Biblio.V2016.R4, new ArrayList<String>() {
            {
                add("benchmark-biblio-r4");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Biblio.V2016.R5, new ArrayList<String>() {
            {
                add("benchmark-biblio-r5");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Film.V2016.R1, new ArrayList<String>() {
            {
                add("benchmark-film-r1");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Film.V2016.R2, new ArrayList<String>() {
            {
                add("benchmark-film-r2");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Film.V2016.R3, new ArrayList<String>() {
            {
                add("benchmark-film-r3");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Film.V2016.R4, new ArrayList<String>() {
            {
                add("benchmark-film-r4");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.SystematicBenchmark.Film.V2016.R5, new ArrayList<String>() {
            {
                add("benchmark-film-r5");
            }
        });
    }

    public static List<String> getTrackOptions() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<Track, List<String>> entry : simpleNameToTrackMap.entrySet()) {
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

        for (Map.Entry<Track, List<String>> entry : simpleNameToTrackMap.entrySet()) {
            for (String entryTrackString : entry.getValue()) {
                if (entryTrackString.equals(trackString)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

}
