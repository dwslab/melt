package de.uni_mannheim.informatik.dws.ontmatching.matchingexternal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the external matcher (especially the search url functions)
 */
public class MatcherExternalTest {
    
    @Test
    void getLastUrlTest() throws MalformedURLException {
        MatcherExternal e = new MatcherExternal() {
            @Override
            protected List<String> getCommand(URL source, URL target, URL inputAlignment) throws Exception {
                return new ArrayList<>();
            }
        };
        assertEquals(URI.create("file://foo.bar").toURL(), e.getLastUrlInString("Test file://test.de\nand so on\nfile://foo.bar\nnext line with some content."));
    }
}
