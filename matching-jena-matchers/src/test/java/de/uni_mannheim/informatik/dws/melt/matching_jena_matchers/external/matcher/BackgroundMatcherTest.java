package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet.WordNetKnowledgeSource;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundMatcherTest {



    @Test
    void match(){
        BackgroundMatcher backgroundMatcher = new BackgroundMatcher(new WordNetKnowledgeSource());
        TestCase tc1 = TrackRepository.Conference.V1.getFirstTestCase();
        try {
            Alignment result = backgroundMatcher.match(tc1.getSourceOntology(OntModel.class),
                    tc1.getTargetOntology(OntModel.class), null,
                    null);
            assertNotNull(result);
            assertTrue(result.size() > 1);
        } catch (Exception e){
            fail(e);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void setGetAllowForCumulativeMatches(boolean bool){
        BackgroundMatcher matcher = new BackgroundMatcher(new WordNetKnowledgeSource());
        matcher.setAllowForCumulativeMatches(bool);
        assertEquals(bool, matcher.isAllowForCumulativeMatches());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void setGetVerboseLogging(boolean bool){
        BackgroundMatcher matcher = new BackgroundMatcher(new WordNetKnowledgeSource());
        matcher.setVerboseLoggingOutput(bool);
        assertEquals(bool, matcher.isVerboseLoggingOutput());
    }

    @Test
    void tokenizeAndFilter() {
        String s1 = "hello_World";
        String s2 = "HelloWorld";
        String s3 = "hello____world";
        String s4 = "___hello____world_";
        String s5 = "hello   World_";
        String s6 = "hello   _World_";

        HashSet<String> s1result = BackgroundMatcher.tokenizeAndFilter(s1);
        HashSet<String> s2result = BackgroundMatcher.tokenizeAndFilter(s2);
        HashSet<String> s3result = BackgroundMatcher.tokenizeAndFilter(s3);
        HashSet<String> s4result = BackgroundMatcher.tokenizeAndFilter(s4);
        HashSet<String> s5result = BackgroundMatcher.tokenizeAndFilter(s5);
        HashSet<String> s6result = BackgroundMatcher.tokenizeAndFilter(s6);

        String[] s123456solution = {"hello", "world"};

        assertTrue(s1result.size() == 2);
        assertTrue(s2result.size() == 2);
        assertTrue(s3result.size() == 2);
        assertTrue(s4result.size() == 2);
        assertTrue(s5result.size() == 2);
        assertTrue(s6result.size() == 2);

        for(String solution : s123456solution){
            assertTrue(s1result.contains(solution));
            assertTrue(s2result.contains(solution));
            assertTrue(s3result.contains(solution));
            assertTrue(s4result.contains(solution));
            assertTrue(s5result.contains(solution));
            assertTrue(s6result.contains(solution));
        }
    }


    @Test
    void isTokenSynonymous(){
        BackgroundMatcher matcher = new BackgroundMatcher(new WordNetKnowledgeSource(), ImplementedBackgroundMatchingStrategies.SYNONYMY, 0.0);

        // list 1
        List<Set<String>> list1 = new LinkedList<>();
        HashSet<String> set1 = new HashSet<>();
        set1.add("humankind");
        set1.add("peace");
        list1.add(set1);

        // list 2
        List<Set<String>> list2 = new LinkedList<>();
        HashSet<String> set2 = new HashSet<>();
        set2.add("mankind");
        set2.add("peace");
        HashSet<String> set2a = new HashSet<>();
        set2a.add("random");
        set2a.add("blubb");
        list2.add(set2);
        list2.add(set2a);

        // case: synonymous sets, testing both ways
        assertTrue(matcher.isTokenSetSynonymous(list1, list2));
        assertTrue(matcher.isTokenSetSynonymous(list2, list1));

        // list 3
        List<Set<String>> list3 = new LinkedList<>();
        HashSet<String> set3 = new HashSet<>();
        set3.add("random");
        set3.add("blubbb");
        list3.add(set3);

        // case: non-synonymous sets, testing both ways
        assertFalse(matcher.isTokenSetSynonymous(list3, list2));
        assertFalse(matcher.isTokenSetSynonymous(list2, list3));

        // list 4
        List<Set<String>> list4 = new LinkedList<>();
        HashSet<String> set4 = new HashSet<>();
        set4.add("random");
        set4.add("blubbb");
        set4.add("addition");
        list4.add(set4);

        // case: non-synonymous set but one contains the other
        assertFalse(matcher.isTokenSetSynonymous(list3, list4));
        assertFalse(matcher.isTokenSetSynonymous(list4, list3));


        // list 5
        List<Set<String>> list5 = new LinkedList<>();
        HashSet<String> set5 = new HashSet<>();
        set5.add("xckfg");
        list5.add(set5);

        // list 6
        List<Set<String>> list6 = new LinkedList<>();
        HashSet<String> set6 = new HashSet<>();
        set6.add("xckfgabc");
        list6.add(set6);

        // case: non-synonymous, non-linkable
        assertFalse(matcher.isTokenSetSynonymous(list5, list6));
        assertFalse(matcher.isTokenSetSynonymous(list6, list5));

        // test for long lists
        List<Set<String>> list7 = new LinkedList<>();
        HashSet<String> set7 = new HashSet<>();
        set7.add("12345");
        set7.add("dog");
        set7.add("warlock");
        set7.add("car");
        set7.add("transmittance");
        set7.add("glasses");
        list7.add(set7);

        List<Set<String>> list8 = new LinkedList<>();
        HashSet<String> set8 = new HashSet<>();
        set8.add("automobile");
        set8.add("transmission");
        set8.add("hound");
        set8.add("12345");
        set8.add("specs");
        set8.add("warlock");
        list8.add(set8);

        assertTrue(matcher.isTokenSetSynonymous(list8, list7));
        assertTrue(matcher.isTokenSetSynonymous(list7, list8));
    }

    @Test
    void strategyTest(){
        BackgroundMatcher matcher = new BackgroundMatcher(new WordNetKnowledgeSource(), ImplementedBackgroundMatchingStrategies.SYNONYMY, 0.0);
        matcher.setStrategy(ImplementedBackgroundMatchingStrategies.SYNONYMY);
        assertEquals("SYNONYMY", matcher.getStrategy().toString());
    }


}