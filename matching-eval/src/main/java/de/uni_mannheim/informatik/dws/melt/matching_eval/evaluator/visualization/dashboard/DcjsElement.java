package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

/**
 * This class is only a helper class.
 * To generate an HTML page use PageBuilder class.
 */
public class DcjsElement {
    
    private String name;

    /*
    Reset text: put null or empty for no reset or any text or reset. If you want to show the current filter use the follwoing placeholder:
    "<span class='filter'></span>"
    */
    private String resetText;    
    
    /*
    Text which is displayed when a filter is applied. You can show the filter with 
    "<span class='filter'></span>"
    */
    private String filterText;
    
    private String title;
    
    private String chartType;
    
    private String anchorStyle;
    
    private Set<String> anchorClass;
    
    private String tagName;
    
    private String preText;
    
    private String dimensionDefinition;
    
    private Set<String> groupDefinitions;
    
    private List<String> jsMethods;

    private Set<String> jsHelperFileNames;

    public DcjsElement(String chartType, String name) {
        this.chartType = chartType;
        this.tagName = this.chartType.equals("dc.dataTable") ? "table" : "div";
        this.name = makeJsIdentifier(name);
        this.groupDefinitions = new HashSet<>();
        this.jsMethods = new ArrayList<>();
        this.jsHelperFileNames = new HashSet<>();
        this.anchorClass = new HashSet<>();
    }
    
    //Dimension Definition
    public void setDimension(String dim){
        this.addJsMethod("dimension(" + dim + ")");
    }
    
    public void createDimensionDefinition(String dimensionName, String dimensionDefinition){
        this.dimensionDefinition = String.format("var %s = ndx.dimension(%s);", dimensionName, dimensionDefinition);
    }
    
    public void createDimensionDefinitionCsvFieldString(String dimensionName, String csvFieldName){
        createDimensionDefinitionCsvField(dimensionName, csvFieldName, "\"\"");
    }
    public void createDimensionDefinitionCsvFieldNumber(String dimensionName, String csvFieldName){
        createDimensionDefinitionCsvField(dimensionName, csvFieldName, "0");
    }
    public void createDimensionDefinitionCsvFieldJsonArray(String dimensionName, String csvFieldName){
        createDimensionDefinition(dimensionName, String.format("function(d) {return JSON.parse(d[\"%s\"]) || [];}, true", csvFieldName));
    }    
    
    public void createDimensionDefinitionCsvField(String dimensionName, String csvFieldName, String defaultValue){
        createDimensionDefinition(dimensionName, String.format("function(d) {return d[\"%s\"] || %s;}", csvFieldName, defaultValue));
    }
    
    public void createDimensionDefinitionMultipleCsvFields(String dimensionName, String... csvFieldName){
        List<String> fieldNameList = Arrays.asList(csvFieldName);
        String accessors = fieldNameList.stream()
                .map(field -> String.format("d[\"%s\"] || \"\"", field))
                .collect(Collectors.joining(","));
        createDimensionDefinition(dimensionName, String.format("function(d) {return [%s];}", accessors));
    }
    
    //Group Definition
    public void setGroup(String group){
        this.addJsMethod("group(" + group + ")");
    }
    public void setGroupStacked(String group, String name, String accessor){
        this.addJsMethod(String.format("group(%s, \"%s\", %s)", group, name, accessor));
    }
    public void setGroupStacked(String group, String csvName){
        setGroupStacked(group, csvName, String.format("function(d){return d.value[\"%s\"];}", csvName));
    }
    
    
    
    public String createGroupDefinition(String groupName, String groupDefinition){
        this.groupDefinitions.add("var " + groupName + " = " + groupDefinition);
        return groupName;
    }
    
    public String createGroupDefinitionBasedOnDimension(String dimensionName){
        return createGroupDefinition(removeDimensionText(dimensionName) + "Group" ,dimensionName + ".group();");
    }
    
    public String createGroupDefinitionReduceField(String dimensionName, String reduceField){
        this.jsHelperFileNames.add("reduceField.js");
        return createGroupDefinition(removeDimensionText(dimensionName) + "Reduce" + makeJsIdentifier(reduceField) + "Group", 
                String.format("%s.group().reduce(reduceFieldAdd(\"%s\"), reduceFieldRemove(\"%s\"), reduceFieldInit());", 
                dimensionName, reduceField, reduceField)
        );
    }
    
    public String createGroupDefinitionReduceSortedAttribute(String dimensionName, String field, String fieldEvaluationResult){
        this.jsHelperFileNames.add("reduceSortedAttribute.js");
        return createGroupDefinition(removeDimensionText(dimensionName) + "ReduceSortedAttribute" + makeJsIdentifier(field) + "Group", 
                String.format("%s.group().reduce(reduceSortedAttributeAdd(\"%s\", \"%s\"), reduceSortedAttributeRemove(\"%s\"), reduceSortedAttributeInit());", 
                dimensionName, field, fieldEvaluationResult, field)
        );
    }
    
    public String createGroupDefinitionReduceTwoFields(String dimensionName, String reduceFieldOne, String reduceFieldTwo){
        this.jsHelperFileNames.add("reduceTwoFields.js");
        return createGroupDefinition(removeDimensionText(dimensionName) + "Reduce" + makeJsIdentifier(reduceFieldOne) + "And" + makeJsIdentifier(reduceFieldTwo) + "Group", 
                String.format("%s.group().reduce(reduceTwoFielsdAdd(\"%s\", \"%s\"), reduceTwoFieldsRemove(\"%s\", \"%s\"), reduceTwoFieldsInit());", 
                dimensionName, reduceFieldOne, reduceFieldTwo, reduceFieldOne, reduceFieldTwo)
        );
    }
    
    public String createGroupDefinitionRemoveEmptyBins(String groupName){
        this.jsHelperFileNames.add("removeEmptyBins.js");
        return createGroupDefinition(groupName + "RemovedEmptyBins", String.format("remove_empty_bins(%s);", groupName));
    }
    
    public void addStack(String groupName, String stackName, String stackAccessor){
        this.jsMethods.add("stack(" + groupName + ", \"" + stackName + "\", " + stackAccessor + ")");
    }
    public void addStack(String groupName, String csvContentName){
        this.jsMethods.add(String.format("stack(%s, \"%s\", function(d){return d.value[\"%s\"];})", groupName, csvContentName, csvContentName));
    }
    
    public void addJsHelperFileName(String fileName){
        this.jsHelperFileNames.add(fileName);
    }
    
    public void addJsMethod(String method){
        this.jsMethods.add(method);
    }
    
    public void addJsMethod(String... methods){
        this.jsMethods.addAll(Arrays.asList(methods));
    }
    
    public void addJsMethod(List<String> methods){
        this.jsMethods.addAll(methods);
    }

    public void setResetText(String resetText) {
        this.resetText = resetText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAnchorStyle(String anchorStyle) {
        this.anchorStyle = anchorStyle;
    }

    
    
    
    public String getTagName() {
        return tagName;
    }
    
    public String getChartType() {
        return chartType;
    }

    public String getName() {
        return name;
    }
    
    public String getResetText() {
        return resetText;
    }

    public String getTitle() {
        return title;
    }

    public String getDimensionDefinition() {
        return dimensionDefinition;
    }

    public Set<String> getGroupDefinitions() {
        return groupDefinitions;
    }

    public String getFilterText() {
        return filterText;
    }

    public String getAnchorStyle() {
        return anchorStyle;
    }

    public Set<String> getJsHelperFileNames() {
        return jsHelperFileNames;
    }

    public List<String> getJsMethods() {
        if(StringUtils.isNotBlank(this.resetText) || StringUtils.isNotBlank(this.filterText)){
            this.addJsMethod("controlsUseVisibility(true)");
            //this.addJsMethod("turnOnControls(true)");            
        }
        return jsMethods;
    }

    public void addAnchorClass(String newAnchorClass) {
        this.anchorClass.add(newAnchorClass.trim());
    }
    
    public boolean hasAnchorClass(String anchorClassToCheck) {
        return this.anchorClass.contains(anchorClassToCheck.trim());
    }
    
    public void clearAnchorClass() {
        this.anchorClass.clear();
    }
    
    public String getAnchorClass() {
        return String.join(" ", this.anchorClass);
    }

    public String getPreText() {
        return preText;
    }

    public void setPreText(String preText) {
        this.preText = preText;
    }
  
    
    private static String makeJsIdentifier(String s){
        return s.replaceAll("[^0-9a-zA-Z_$]+", "").replaceFirst("^[0-9]+", "");
    }
    
    private static String removeDimensionText(String str) {
      if (str.endsWith("Dimension")) {
          return str.substring(0, str.length() - 9);
      }
      return str;
  }
}
