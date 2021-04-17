package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.net.URL;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import eu.sealsproject.platform.res.tool.api.ToolException;
import eu.sealsproject.platform.res.tool.api.ToolType;
import eu.sealsproject.platform.res.tool.impl.AbstractPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RawMatcher which implements the minimal interface for being executed under
 * the SEALS platform. The only method which should be implemented is the
 * align(URL, URL, URL) method.
 *
 * @author Sven Hertling
 */
public abstract class MatcherURL extends AbstractPlugin implements IOntologyMatchingToolBridge {


    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherURL.class);

    /**
     * Aligns two ontologies specified via their URL and returns the URL of the
     * resulting alignment, which should be stored locally.
     *
     * @param source this url represents the source ontology
     * @param target this url represents the target ontology
     * @return a url which points to the resulting alignment, which should be
     * stored locally
     */
    @Override
    public URL align(URL source, URL target) throws ToolBridgeException {
        return align(source, target, null);
    }

    /**
     * Aligns two ontologies specified via their URL, with an input alignment
     * specified by its URL, and returns the URL of the resulting alignment,
     * which should be stored locally.
     *
     * @param source this url represents the source ontology
     * @param target this url represents the target ontology
     * @param inputAlignment this url represents the input alignment
     * @return a url which points to the resulting alignment, which should be
     * stored locally in the alignment format http://alignapi.gforge.inria.fr/format.html
     */
    @Override
    public URL align(URL source, URL target, URL inputAlignment) throws ToolBridgeException {
        try {
            return match(source, target, inputAlignment);
        } catch (Exception exception) {
            LOGGER.error("Tool Exception!", exception);
            throw new ToolException("Tool exception", exception);
        }
    }

    /**
     * Match two ontologies / knowledge graphs together and returns an alignment.
     * @param source The source ontology / knowledge graph
     * @param target The target ontology / knowledge graph
     * @param inputAlignment The input alignment as URL
     *                       (<a href="https://moex.gitlabpages.inria.fr/alignapi/format.html">alignment API format</a>)
     * @return An alignment as URL (most often as file URL) the format is again the
     * <a href="https://moex.gitlabpages.inria.fr/alignapi/format.html">alignment API format</a>.
     * @throws Exception in case something went wrong.
     */
    public abstract URL match(URL source, URL target, URL inputAlignment) throws Exception;

    /**
     * In our case the DemoMatcher can be executed on the fly. In case
     * prerequisites are required it can be checked here.
     *
     * @return Value which represents the boolean value if the matcher can be
     * executed.
     */
    @Override
    public boolean canExecute() {
        return true;
    }

    /**
     * This tool is an ontology matching tool. SEALS supports the evaluation of
     * different tool types like e.g., reasoner and storage systems.
     *
     * @return The type of system - in this case OntologyMatchingTool.
     */
    @Override
    public ToolType getType() {
        return ToolType.OntologyMatchingTool;
    }
}