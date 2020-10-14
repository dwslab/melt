package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.resultspage;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Results page generator in Latex.
 */
public class ResultsPageLatex extends Evaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsPageLatex.class);

    protected VelocityEngine velocityEngine;
    protected ResultsPageUtil resultsPageUtil;
    
    public ResultsPageLatex(ExecutionResultSet results, boolean isMicro) {
        super(results);
        this.resultsPageUtil = new ResultsPageUtil(results, isMicro);
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("resource.loaders", "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());  
        velocityEngine.init();        
    }
    
    
    public ResultsPageLatex(ExecutionResultSet results) {
        this(results, false);
    }

    @Override
    protected void writeResultsToDirectory(File baseDirectory) {
        writeToFile(new File(baseDirectory, "resultspage.tex"));        
    }
    
    
    /**
     * Writes the HTML content to one file. This includes also the data (csv) which is included in the HTML file.
     * This HTML file can be opened directly by a browser.
     * @param htmlFile the file where all html data should be written to
     */
    public void writeToFile(File htmlFile){        
        VelocityContext context = new VelocityContext();
        context.put("util", resultsPageUtil);
        
        Template template = velocityEngine.getTemplate("templates/resultspage/LatexMatcherVertical.vm");
        try(Writer writer = new FileWriter(htmlFile)){
            template.merge( context, writer );
        } catch (IOException ex) {
            LOGGER.error("Could not write to file.", ex);
        }
    }
}
