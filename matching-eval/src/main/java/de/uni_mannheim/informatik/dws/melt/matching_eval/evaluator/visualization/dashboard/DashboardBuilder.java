package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.dashboard;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a dashboard with dc.js components based on the generated csv file.
 * Example on how to use:
 * <pre>
 * {@code
 * ExecutionResultSet s = Executor.loadFromAnatomyResultsFolder("...");
 * s.addAll(Executor.loadFromConferenceResultsFolder("..."));
 * PageBuilder pb = new PageBuilder(s);
 * pb.addDefaultDashboard();
 * pb.writeToFile(new File("dashboard.html"));
 * }
 * </pre>
 */
public class DashboardBuilder extends Evaluator {


    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardBuilder.class);
    
    protected Supplier<String> csvSupplier;
    
    protected Template template;
    
    protected List<DcjsElement> currentRow;
    protected List<List<DcjsElement>> rows;
    
    protected Set<String> dimensionDefinitions;
    protected Set<String> groupDefinitions;
    
    protected String title;
    protected String additionalText;
    
    protected boolean dataLoadingIndicator;

    public DashboardBuilder(Supplier<String> csvSupplier, ExecutionResultSet executionResultSet, String titleOfPage, String additionalText){
        super(executionResultSet);
        Velocity.setProperty("resource.loaders", "classpath");
        Velocity.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());        
        Velocity.init();
        this.template = Velocity.getTemplate("templates/dashboard/dashboard.vm");
        this.csvSupplier = csvSupplier;
        this.rows = new ArrayList<>();
        this.currentRow = new ArrayList<>();
        this.title = titleOfPage;
        this.additionalText = additionalText;
        this.dataLoadingIndicator = false;
        
        addDefaultDashboard();
    }

    /**
     * Constructor
     * @param evaluatorCSV The CSV evaluator to be used. The evaluator can be configured, e.g. in terms of the columns
     *                     that should be printed.
     * @param titleOfPage The title of the generated HTML page.
     * @param additionalText additionalText
     */
    public DashboardBuilder(EvaluatorCSV evaluatorCSV, String titleOfPage, String additionalText){
        this(() -> {
            evaluatorCSV.setPrintCorrespondenceExtensions(false); 
            return evaluatorCSV.getAlignmentsCubeAsString().trim();
        }, evaluatorCSV.getResults(), titleOfPage, additionalText);
    }

    /**
     * Constructor
     * @param executionResultSet The execution result set to be evaluated and printed.
     * @param titleOfPage The title of the generated HTML page.
     * @param additionalText additionalText
     */
    public DashboardBuilder(ExecutionResultSet executionResultSet, String titleOfPage, String additionalText){
        this(new EvaluatorCSV(executionResultSet), titleOfPage, additionalText);
    }

    /**
     * Constructor
     * @param executionResultSet The execution result set to be evaluated and printed.
     */
    public DashboardBuilder(ExecutionResultSet executionResultSet){
        this(executionResultSet, "MELT Dashboard", "");
    }

    /**
     * Constructor
     * @param evaluatorCSV The CSV evaluator object to be used.
     */
    public DashboardBuilder(EvaluatorCSV evaluatorCSV){
        this(evaluatorCSV, "MELT Dashboard", "");
    }
    
    /**
     * Constructor which just uses an csv file and not the whole EvaluatorCSV.
     * @param csvFile the csv file
     * @param titleOfPage title of the page
     * @param additionalText additional text for the page
     */
    public DashboardBuilder(File csvFile, String titleOfPage, String additionalText){
        this(()-> {
            try{
                return new String(Files.readAllBytes(csvFile.toPath()), StandardCharsets.UTF_8);
            }catch(IOException ex){
                LOGGER.error("Could not read csv file", ex);
                return "";
            }
        },null, titleOfPage, additionalText);
    } 
    
    public DashboardBuilder addDefaultDashboard(){
        this.addSelectMenu("trackSelection", "Track", "width:190px");
        this.addTrackTestcaseSunburst();
        this.addConfidenceBar();
        this.addPieChart("relationChart", "Relation");
        this.addPieChart("matcherChart", "Matcher");
        this.addPieChartEvaluation();
        this.newRow();
        this.addBoxPlotMatcherConfidence();
        this.newRow();
        this.addPieChartMultiValue("typeLeftChart", "Type Left");
        this.addPieChartMultiValue("typeRightChart", "Type Right");
        this.addPieChart("residualChart", "Residual True Positive");
        this.addResultPerTestCase();
        this.addResultPerMatcher();
        //this.addMetricTable();
        //this.addConfusionHeatMap();
        //this.addMetricTableOnlySelected();
        this.newRow();
        //this.addMetricTableSelectedAndMatcher();
        //this.addMatcherMetricTableOnlyMatcher();
        this.addMetricTableSelectedAndMatcher();
        //this.addPrecisionRecallScatterPlot();
        this.newRow();
        this.addDataCount();
        //this.addTextFilter();
        this.addDataChart();
        return this;
    }
    
    
    
    public DashboardBuilder addBoxPlotMatcherConfidence(){
        DcjsElement e = new DcjsElement("dc.boxPlot", "boxPlotMatcherConfidence");
        
        e.createDimensionDefinitionCsvFieldString("ConfidenceBoxPlotMatcherDimension", "Matcher");
        String groupConfidenceBoxPlot = e.createGroupDefinitionReduceSortedAttribute("ConfidenceBoxPlotMatcherDimension", "Confidence (Matcher)", "Evaluation Result");
        e.setAnchorStyle("width:1000px");
        e.setDimension("ConfidenceBoxPlotMatcherDimension");
        e.setGroup(groupConfidenceBoxPlot);       
        e.addJsMethod(
                "elasticX(true)",
                "y(d3.scaleLinear().domain([0.0,1.05]))",
                "on(\"renderlet\", function(chart) { chart.select('.axis.x').attr(\"text-anchor\", \"end\").selectAll(\"text\").attr(\"transform\", \"rotate(-60)\").attr(\"dy\", \"0.1em\").attr(\"dx\", \"-1em\");})",
                "margins({top: 10, right: 50, bottom: 80, left: 40})"
        );
        return addElement(e);
    }

    public DashboardBuilder addMetricTableSelectedAndMatcher(){
        DcjsElement e = new DcjsElement("dc.dataTable", "metricTableSelectedAndMatcher");
        
        e.createDimensionDefinitionCsvFieldString("ResultPerTestCaseDimension", "TestCase");
        String group_testcase_result = e.createGroupDefinitionReduceField("ResultPerTestCaseDimension", "Evaluation Result");
        
        e.createDimensionDefinitionCsvFieldString("MetricTableMatcherDimension", "Matcher");
        String group_matcher_testcase_result = e.createGroupDefinitionReduceTwoFields("MetricTableMatcherDimension", "TestCase", "Evaluation Result");
        
        e.addJsHelperFileName("computeMetrics.js"); //because compute_metrics_for_group is used in next line
        String groupMetric = e.createGroupDefinition("metricGroup", String.format("compute_matcher_selected_metrics(%s, %s);", group_testcase_result,group_matcher_testcase_result));
        e.setDimension(groupMetric);            
       
        e.addJsMethod(
                "columns(['Name', 'Prec(micro)', 'Rec(micro)', 'F-m.(micro)', 'Prec(macro)', 'Rec(macro)', 'F-m.(macro)'])",
                "showSections(false)"
        );
        e.addAnchorClass("table");
        e.setAnchorStyle("width:700px");
        return addElement(e);
    }
    
    public DashboardBuilder addPrecisionRecallScatterPlot(){
        DcjsElement e = new DcjsElement("dc.scatterPlot", "scatterplot");
        
        e.createDimensionDefinitionCsvFieldString("ResultPerTestCaseDimension", "TestCase");
        String group_testcase_result = e.createGroupDefinitionReduceField("ResultPerTestCaseDimension", "Evaluation Result");
        
        e.createDimensionDefinitionCsvFieldString("MetricTableMatcherDimension", "Matcher");
        String group_matcher_testcase_result = e.createGroupDefinitionReduceTwoFields("MetricTableMatcherDimension", "TestCase", "Evaluation Result");
        
        e.addJsHelperFileName("computeMetrics.js"); //because compute_metrics_for_group is used in next line
        String groupMetric = e.createGroupDefinition("metricGroup", String.format("compute_matcher_selected_metrics(%s, %s);", group_testcase_result,group_matcher_testcase_result));
        
        
        e.createDimensionDefinitionCsvFieldString("PrecisionRecallDimension", "Matcher");
        e.setDimension("PrecisionRecallDimension");
        e.setGroup(groupMetric);
        
        e.addJsMethod(
                "xAxisLabel(\"Recall\")",
                "x(d3.scaleLinear().domain([0,1]))",
                "yAxisLabel(\"Precision\")",
                "y(d3.scaleLinear().domain([0,1]))"
        );
        
        return addElement(e);
    }
    
    
    
    public DashboardBuilder addMetricTableOnlySelected(){
        DcjsElement e = new DcjsElement("dc.dataTable", "metricTableSelected");        
        e.createDimensionDefinitionCsvFieldString("ResultPerTestCaseDimension", "TestCase");
        String group = e.createGroupDefinitionReduceField("ResultPerTestCaseDimension", "Evaluation Result");
        e.addJsHelperFileName("computeMetrics.js"); //because compute_metrics_for_group is used in next line
        String groupMetric = e.createGroupDefinition("metricGroup", String.format("compute_metrics_for_group(%s);", group));
        e.setDimension(groupMetric);                
        e.addJsMethod(
                "columns(['name', 'precision', 'recall', 'fmeasure'])",
                "showSections(false)"
        );
        e.addAnchorClass("table");
        e.setAnchorStyle("width:310px");
        return addElement(e);
    }
    
    public DashboardBuilder addMatcherMetricTableOnlyMatcher(){
        DcjsElement e = new DcjsElement("dc.dataTable", "metricTableMatcher");
        e.createDimensionDefinitionCsvFieldString("MetricTableMatcherDimension", "Matcher");
        String group = e.createGroupDefinitionReduceTwoFields("MetricTableMatcherDimension", "TestCase", "Evaluation Result");
        e.addJsHelperFileName("computeMetrics.js"); //because compute_matcher_metrics is used in next line
        String groupMetric = e.createGroupDefinition("matcherMetricGroup", String.format("compute_matcher_metrics(%s);", group));
        e.setDimension(groupMetric);                
        e.addJsMethod(
                "columns(['name', 'precision', 'recall', 'fmeasure'])",
                "showSections(false)"
        );
        e.addAnchorClass("table");
        e.setAnchorStyle("width:310px");
        return addElement(e);
    }
    
    public DashboardBuilder addDataCount(){
        DcjsElement e = new DcjsElement("dc.dataCount", "dataCount");
        e.createGroupDefinition("allGroup", "ndx.groupAll();");
        e.addJsMethod(
                "crossfilter(ndx)",
                "groupAll(allGroup)",
                "html({" + 
                  "some: '<strong>%filter-count</strong> selected out of <strong>%total-count</strong> records | <a href=\\'javascript:dc.filterAll(); dc.redrawAll();\\'>Reset All</a>', " +
                  "all: 'All %total-count records selected'" +
                "})"
        );
        return addElement(e);
    }
    
    public DashboardBuilder addConfusionHeatMap(){
        DcjsElement e = new DcjsElement("dc.heatMap", "confusionHeatmap");
        e.addJsHelperFileName("heatMap.js");//because of js functions getTrueCondition and getPredictedCondition
        e.createDimensionDefinition("heatDimension", 
                "function(d) { return [ getTrueCondition(d[\"Evaluation Result\"]), getPredictedCondition(d[\"Evaluation Result\"])];}");
        e.setDimension("heatDimension");
        e.setGroup(e.createGroupDefinitionBasedOnDimension("heatDimension"));
        e.addJsMethod(
                "keyAccessor(function(d) { return d.key[0]; })",
                "valueAccessor(function(d) { return d.key[1]; })",
                "colorAccessor(function(d) { return d.value; })",
                "colors([\"#ffffd9\",\"#edf8b1\",\"#c7e9b4\",\"#7fcdbb\",\"#41b6c4\",\"#1d91c0\",\"#225ea8\",\"#253494\",\"#081d58\"])",
                "calculateColorDomain()"
        );
        return addElement(e);
    }
    
    public DashboardBuilder addResultPerMatcher(){
        DcjsElement e = new DcjsElement("dc.barChart", "resultperMatcher");
        e.setTitle("Result per Matcher");
        e.setResetText("reset");
        e.addJsMethod(
                "x(d3.scaleBand())",
                "xUnits(dc.units.ordinal)",
                "elasticX(true)",
                "elasticY(true)",
                "yAxisLabel(\"\")",
                "colors(d3.scaleOrdinal(dc.config.defaultColors()))", //quick hack can be removed when fixed in dc.js https://github.com/dc-js/dc.js/issues/1564
                //rotate x axis labels
                "on(\"renderlet\", function(chart) { chart.select('.axis.x').attr(\"text-anchor\", \"end\").selectAll(\"text\").attr(\"transform\", \"rotate(-60)\").attr(\"dy\", \"0.1em\").attr(\"dx\", \"-1em\");})",
                "margins({top: 10, right: 50, bottom: 90, left: 40})"
        );
        e.createDimensionDefinitionCsvFieldString("ResultPerMatcherDimension", "Matcher");
        e.setDimension("ResultPerMatcherDimension");
        String group = e.createGroupDefinitionReduceField("ResultPerMatcherDimension", "Evaluation Result");
        String groupEmptyBins = e.createGroupDefinitionRemoveEmptyBins(group);
        
        e.setGroupStacked(groupEmptyBins, "true positive");//check order -> important for same color
        e.addStack(groupEmptyBins, "false positive");
        e.addStack(groupEmptyBins, "false negative");        
        return addElement(e);
    }
    
    public DashboardBuilder addResultPerTestCase(){
        DcjsElement e = new DcjsElement("dc.barChart", "resutPerTestcase");
        e.setTitle("Result per TestCase");
        e.setResetText("reset");
        e.addJsMethod(
                "x(d3.scaleBand())",
                "xUnits(dc.units.ordinal)",
                "elasticX(true)",
                "elasticY(true)",
                "yAxisLabel(\"\")",
                "colors(d3.scaleOrdinal(dc.config.defaultColors()))", //quick hack can be removed when fixed in dc.js https://github.com/dc-js/dc.js/issues/1564
                //rotate x axis labels
                "on(\"renderlet\", function(chart) { chart.select('.axis.x').attr(\"text-anchor\", \"end\").selectAll(\"text\").attr(\"transform\", \"rotate(-60)\").attr(\"dy\", \"0.1em\").attr(\"dx\", \"-1em\");})",
                "margins({top: 10, right: 50, bottom: 90, left: 40})"
        );
        e.createDimensionDefinitionCsvFieldString("ResultPerTestCaseDimension", "TestCase");
        e.setDimension("ResultPerTestCaseDimension");
        String group = e.createGroupDefinitionReduceField("ResultPerTestCaseDimension", "Evaluation Result");
        String groupEmptyBins = e.createGroupDefinitionRemoveEmptyBins(group);
        
        e.setGroupStacked(groupEmptyBins, "true positive");//reverse order
        e.addStack(groupEmptyBins, "false positive");
        e.addStack(groupEmptyBins, "false negative");        
        return addElement(e);
    }
    
       
    public DashboardBuilder addSelectMenu(String name, String csvField, String style){
        DcjsElement e = new DcjsElement("dc.selectMenu", name);
        e.setTitle(csvField);
        e.setResetText("reset");
        e.setAnchorStyle(style);
        
        e.createDimensionDefinitionCsvFieldString(name + "Dimension", csvField);
        e.setDimension(name + "Dimension");
        e.setGroup(e.createGroupDefinitionBasedOnDimension(name + "Dimension"));        
        e.addJsMethod("multiple(true)");
        return addElement(e);
    }
    
    public DashboardBuilder addPieChartEvaluation(){
        String name = "evalResultChart";
        String csvField = "Evaluation Result";
        DcjsElement e = new DcjsElement("dc.pieChart", name);
        e.setTitle(csvField);
        e.setResetText("reset");
        //e.setFilterText("<span class='filter'></span>");
        e.addJsMethod("width(280)");
        e.addJsMethod("height(280)");
        
        e.addJsMethod("ordering(function(d){return getSortedEvaluationResult(d.key);})");
        e.addJsHelperFileName("sortedEvalResult.js");
        e.createDimensionDefinitionCsvFieldString(name + "Dimension", csvField);
        e.setDimension(name + "Dimension");
        e.setGroup(e.createGroupDefinitionBasedOnDimension(name + "Dimension"));
        return addElement(e);
    }
    
    public DashboardBuilder addPieChart(String name, String csvField){
        DcjsElement e = new DcjsElement("dc.pieChart", name);
        e.setTitle(csvField);
        e.setResetText("reset");
        e.addJsMethod("width(280)");
        e.addJsMethod("height(280)");
        //e.setFilterText("<span class='filter'></span>");
        e.createDimensionDefinitionCsvFieldString(name + "Dimension", csvField);
        e.setDimension(name + "Dimension");
        e.setGroup(e.createGroupDefinitionBasedOnDimension(name + "Dimension"));
        return addElement(e);
    }
    
    public DashboardBuilder addPieChartMultiValue(String name, String csvField){
        DcjsElement e = new DcjsElement("dc.pieChart", name);
        e.setTitle(csvField);
        e.setResetText("reset");
        e.addJsMethod("width(280)","height(280)");
        //e.setFilterText("<span class='filter'></span>");
        e.createDimensionDefinitionCsvFieldJsonArray(name + "Dimension", csvField);
        e.setDimension(name + "Dimension");
        e.setGroup(e.createGroupDefinitionBasedOnDimension(name + "Dimension"));
        return addElement(e);
    }
    
    public DashboardBuilder addTrackTestcaseSunburst(){
        DcjsElement e = new DcjsElement("dc.sunburstChart", "selectTrackTestCase");
        
        e.createDimensionDefinitionMultipleCsvFields("TrackTestCaseDimension", "Track", "TestCase");
        e.setDimension("TrackTestCaseDimension");
        e.setGroup(e.createGroupDefinitionBasedOnDimension("TrackTestCaseDimension"));        
        e.addJsMethod("width(280)","height(280)");
        e.addJsMethod("innerRadius(50)");
        e.setResetText("reset");
        e.setTitle("Track/Testcase");
        //e.addJsMethod("legend(dc.legend())");
        return addElement(e);
    }
    
    
    public DashboardBuilder addConfidenceBar(){
        String stackFunction = "function(d) { if(d.key.split(':')[1] === \"%s\"){return d.value;} return 0; }";
        
        DcjsElement e = new DcjsElement("dc.barChart", "selectConfidence");
        
        String dimName ="ConfidenceEvalResultDimension";
        e.createDimensionDefinition(dimName, "function(d) {return (+d[\"Confidence (Matcher)\"]).toFixed(2) + ':' + d[\"Evaluation Result\"] }");
        e.setDimension(dimName);
        String group = e.createGroupDefinitionBasedOnDimension(dimName);
        
        e.setGroupStacked(group, "true positive", String.format(stackFunction, "true positive"));
        e.addStack(group, "false positive", String.format(stackFunction, "false positive"));
        e.addStack(group, "false negative", "function(d) {return 0;}");  
        
        
        e.setTitle("Confidence (Matcher)");
        e.setResetText("reset");
        e.setFilterText("range: <span class='filter'></span>");
        e.addJsMethod(
                "x(d3.scaleLinear().domain([0.0,1.05]))",
                "xUnits(function(){return 20;})",
                "elasticY(true)",
                "xAxisLabel(\"confidence\")",
                "yAxisLabel(\"\")",
                "keyAccessor(function(p) {return p.key.split(':')[0];})",
                //"legend(dc.legend())",
                "colors(d3.scaleOrdinal(dc.config.defaultColors()))", //quick hack can be removed when fixed in dc.js https://github.com/dc-js/dc.js/issues/1564
                "filterHandler(confidenceFilterHandler)");
        e.addJsHelperFileName("filterHandler.js");//becuase above confidenceFilterHandler is used.
        return addElement(e);
    }
    
    
    public DashboardBuilder addTextFilter(){
        DcjsElement e = new DcjsElement("dc.TextFilterWidget", "textFilter");
        e.createDimensionDefinitionCsvFieldString("URILeftDimension", "URI Left");
        e.setDimension("URILeftDimension");
        return addElement(e);
        
    }
    
    public DashboardBuilder addDataChart(){
        DcjsElement e = new DcjsElement("dc.dataTable", "dataTable");
        String dimName = "MatcherDimension";
        e.createDimensionDefinitionCsvFieldString(dimName, "Matcher");
        e.setDimension(dimName);
        e.addJsMethod(
                "columns(d3.keys(experiments[0]))",
                "showSections(false)",
                "size(Infinity)",
                "order(d3.ascending)",
                //"sortBy(function (d) { return [fmt(+d.Expt),fmt(+d.Run)]; })"//TODO: make id?
                "on('preRender', update_offset(ndx))",
                "on('preRedraw', update_offset)",
                "on('pretransition', display)"
        );
        e.setPreText("<div id=\"paging\">\n" +
"                    &nbsp; | Showing <span id=\"begin\"></span>-<span id=\"end\"></span>\n" +
"                    <button id=\"last\" class=\"btn btn-sm btn-outline-secondary\" type=\"button\" style=\"font-size: 0.7em;\" onclick=\"javascript:last()\">Last</button>\n" +
"                    <button id=\"next\" class=\"btn btn-sm btn-outline-secondary\" type=\"button\" style=\"font-size: 0.7em;\" onclick=\"javascript:next()\">Next</button>\n" +
"                </div>");
        e.addJsHelperFileName("table_pagination.js");//becuase above confidenceFilterHandler is used.
        e.addAnchorClass("table");
        e.setAnchorStyle("table-layout:fixed; word-wrap:break-word;");
        return addElement(e);
    }
    
    
    public DashboardBuilder addElement(DcjsElement element){
        this.currentRow.add(element);
        return this;
    }
    
    public DashboardBuilder clearElements(){
        this.rows.clear();
        this.currentRow.clear();
        return this;
    }
    
    public DashboardBuilder newRow(){
        if(this.currentRow.size() > 0){
            if(this.currentRow.size() > 3){
                boolean hasTableInRow = false;
                for(DcjsElement e : this.currentRow){
                    if(e.hasAnchorClass("table")){
                        hasTableInRow = true;
                        break;
                    }
                }
                if(!hasTableInRow){
                    for(DcjsElement e : this.currentRow){
                        e.addAnchorClass("col");
                    }
                }
            }
            this.rows.add(this.currentRow);
            this.currentRow = new ArrayList<>();
        }
        return this;
    }
    
    public DashboardBuilder setTitle(String newTitle){
        this.title = newTitle;
        return this;
    }
    
    public DashboardBuilder setAdditionalText(String newAdditionalText){
        this.additionalText = newAdditionalText;
        return this;
    }
    
    public DashboardBuilder setDataLoadingIndicator(boolean newState){
        this.dataLoadingIndicator = newState;
        return this;
    }

    @Override
    public void writeResultsToDirectory(File baseDirectory) {
        writeToFile(new File(baseDirectory, "meltDashboard.html"));
    }

    public void writeToFile(String htmlFilePath){
        this.writeToFile(new File(htmlFilePath));
    }

    /**
     * Writes the HTML content to one file. This includes also the data (csv) which is included in the HTML file.
     * This HTML file can be opened directly by a browser.
     * @param htmlFile the file where all html data should be written to
     */
    public void writeToFile(File htmlFile){
        if(htmlFile == null) {
            LOGGER.error("The specified file is NULL. ABORT.");
            return;
        }

        //in case the last row is not closed:
        newRow();

        VelocityContext context = prepareVelocityContext();
        context.put("csvData", this.csvSupplier.get());
        /*context.put("csvData", "Track,TestCase,left_label,left_comment,left_explicit_type,left_uri,relation,confidence,right_uri,right_label,right_comment,right explicit type,result\n" +
"conference,conference-sigkdd,[],[],[http://www.w3.org/2002/07/owl-Class],http://conference-Review,=,1.0,http://sigkdd-Review,[],[],[http://www.w3.org/2002/07/owl-Class],true positive\n" +
"conference,conference-sigkdd,[],[],[http://www.w3.org/2002/07/owl-Class],http://conference-Committee,=,1.0,http://sigkdd-Committee,[],[],[http://www.w3.org/2002/07/owl-Class],true positive\n" +
"conference,conference-sigkdd,[],[],[http://www.w3.org/2002/07/owl-Class],http://conference-Person,=,1.0,http://sigkdd-Person,[],[],[http://www.w3.org/2002/07/owl-Class],true positive");
        */
        
        try(Writer writer = new FileWriter(htmlFile)){
            template.merge( context, writer );
        } catch (IOException ex) {
            LOGGER.error("Could not write to file.", ex);
        }
    }
    
    /**
     * Writes the HTML content to htmlFile and the data (csv) to another file.
     * This is for publishing the dashboard to a server.
     * @param htmlFile the file where all html code should be written to
     * @param csvFile the file where all data should be written to
     */
    public void writeToFile(File htmlFile, File csvFile){
        newRow();
        
        try(Writer writer = new BufferedWriter(new FileWriter(csvFile))){
            writer.write(this.csvSupplier.get());
        } catch (IOException ex) {
            LOGGER.error("Could not write to file.", ex);
        }
        VelocityContext context = prepareVelocityContext();
        context.put("remoteLocation", csvFile.getName());
        try(Writer writer = new FileWriter(htmlFile)){
            template.merge( context, writer );
        } catch (IOException ex) {
            LOGGER.error("Could not write to file.", ex);
        }
    }
    
    /**
     * Writes the HTML content to htmlFile and the data (csv) to another file.
     * The data file is compressed by gzip and base64 encoded.
     * This is for publishing the dashboard to a server.
     * @param htmlFile the file where all html code should be written to
     * @param csvFile the file where all data should be written to (compressed by gzip and base64 encoded)
     */
    public void writeToCompressedFile(File htmlFile, File csvFile){
        newRow();
        
        try(Writer writer = new BufferedWriter(new FileWriter(csvFile))){
            String csv = this.csvSupplier.get();
            writer.write(Base64.getEncoder().encodeToString(getGzippedByteArray(csv)));
        } catch (IOException ex) {
            LOGGER.error("Could not write to file.", ex);
        }
        VelocityContext context = prepareVelocityContext();
        context.put("compressedRemoteLocation", csvFile.getName());
        try(Writer writer = new FileWriter(htmlFile)){
            template.merge( context, writer );
        } catch (IOException ex) {
            LOGGER.error("Could not write to file.", ex);
        }
    }
    
    //Private helper methods
    
    private byte[] getGzippedByteArray(String text){
        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream()){            
            try(GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)){
                gzipStream.write(text.getBytes(StandardCharsets.UTF_8));
            }            
            return byteStream.toByteArray();
        } catch (IOException ex) {
            LOGGER.error("Could not create gzipped byte array", ex);
            return new byte[0];
        }
    }
    
    private VelocityContext prepareVelocityContext(){
        VelocityContext context = new VelocityContext();
        context.put("title", title);
        context.put("additionalText", additionalText);
        context.put("dcjsElements", rows);
        context.put("loadingSpinner", this.dataLoadingIndicator);
        context.put("dimensionDefinition", this.getAllDimensionDefinitions());
        context.put("groupDefinition", this.getAllGroupDefinitions());
        context.put("jsHelperFileNames", this.getAllJsHelperFileNames());
        return context;
    }
    
    private List<String> getAllDimensionDefinitions(){
        Set<String> definitions = new HashSet<>();
        for(List<DcjsElement> list : this.rows){
            for(DcjsElement e : list){
                String def = e.getDimensionDefinition();
                if(StringUtils.isNotBlank(def)){
                    definitions.add(def.trim());
                }
            }
        }
        List<String> l = new ArrayList<>(definitions);
        Collections.sort(l);
        return l;
    }
    
    private List<String> getAllGroupDefinitions(){
        Set<String> definitions = new HashSet<>();
        for(List<DcjsElement> list : this.rows){
            for(DcjsElement e : list){
                for(String def : e.getGroupDefinitions()){
                    if(StringUtils.isNotBlank(def)){
                        definitions.add(def.trim());
                    }
                }
            }
        }
        List<String> l = new ArrayList<>(definitions);
        Collections.sort(l);
        return l;
    }
    
    private List<String> getAllJsHelperFileNames(){
        Set<String> helperFilesNames = new HashSet<>();
        for(List<DcjsElement> list : this.rows){
            for(DcjsElement e : list){
                for(String fileName : e.getJsHelperFileNames()){
                    if(StringUtils.isNotBlank(fileName)){
                        helperFilesNames.add(fileName.trim());
                    }
                }
            }
        }
        List<String> l = new ArrayList<>(helperFilesNames);
        Collections.sort(l);
        return l;
    }
    
    //http://dc-js.github.io/dc.js/examples/filter-stacks.html
    //https://stackoverflow.com/questions/36494956/elasticxtrue-doesnt-work-dc-js
    //https://github.com/dc-js/dc.js/wiki/FAQ#fake-groups
    //https://dc-js.github.io/dc.js/examples/table-on-aggregated-data.html
    //https://stackoverflow.com/questions/28362475/crossfilter-calculating-percent-of-all-records-with-a-property
    //https://stackoverflow.com/questions/21519856/dc-js-how-to-get-the-average-of-a-column-in-data-set
    //https://stackoverflow.com/questions/45487174/can-i-filter-data-based-on-an-intersection-and-in-crossfilter-dc-js

}
