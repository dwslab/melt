package de.uni_mannheim.informatik.dws.melt.matching_eval_client;

import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;

import java.util.*;

public class BuiltInTracks {


    /**
     * A map linking from each track to the allowed string representations to identify the track.
     * The List order matters: The first string will be the preferred string which will show up in the command line
     * client.
     *
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
        simpleNameToTrackMap.put(TrackRepository.Knowledgegraph.V4, new ArrayList<String>(){
            {
                add("knowledgegraph");
                add("kg");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Knowledgegraph.V3, new ArrayList<String>(){
            {
                add("knowledgegraph-v3");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Knowledgegraph.V2, new ArrayList<String>(){
            {
                add("knowledgegraph-v2");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Knowledgegraph.V1, new ArrayList<String>(){
            {
                add("knowledgegraph-v1");
            }
        });

        // largebio
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.ALL, new ArrayList<String>(){
            {
                add("largebio");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.ONLY_SMALL, new ArrayList<String>(){
            {
                add("largebio-small");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.FMA_NCI_SMALL, new ArrayList<String>(){
            {
                add("largebio-fma-nci-small");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.FMA_NCI_WHOLE, new ArrayList<String>(){
            {
                add("largebio-fma-nci-whole");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.FMA_SNOMED_SMALL, new ArrayList<String>(){
            {
                add("largebio-fma-snomed-small");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.FMA_SNOMED_WHOLE, new ArrayList<String>(){
            {
                add("largebio-fma-snomed-whole");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.SNOMED_NCI_SMALL, new ArrayList<String>(){
            {
                add("largebio-snomed-nci-small");
            }
        });
        simpleNameToTrackMap.put(TrackRepository.Largebio.V2016.SNOMED_NCI_WHOLE, new ArrayList<String>(){
            {
                add("largebio-snomed-nci-whole");
            }
        });

        simpleNameToTrackMap.put(TrackRepository.Multifarm.ALL_IN_ONE_TRACK, new ArrayList<String>(){
            {
                add("multifarm");
            }
        });

        for(String languages : TrackRepository.Multifarm.LANGUAGE_PAIRS){


            simpleNameToTrackMap.put(TrackRepository.Multifarm.ALL_IN_ONE_TRACK, new ArrayList<String>(){
                {
                    add("multifarm-" + languages);
                }
            });
        }
    }

    public static List<String> getTrackOptions(){
        List<String> result = new ArrayList<>();
        for(Map.Entry<Track, List<String>> entry: simpleNameToTrackMap.entrySet()){
            List<String> writings = entry.getValue();
            if(!writings.isEmpty()) {
                result.add(writings.get(0));
            }
        }
        return result;
    }

    public static Track getTrackByString(String trackString){
        if(trackString == null){
            return null;
        }
        trackString = trackString.toLowerCase(Locale.ROOT).trim();

        for(Map.Entry<Track, List<String>> entry : simpleNameToTrackMap.entrySet()){
            for(String entryTrackString : entry.getValue()){
                if(entryTrackString.equals(trackString)){
                    return entry.getKey();
                }
            }
        }
        return null;
    }

}
