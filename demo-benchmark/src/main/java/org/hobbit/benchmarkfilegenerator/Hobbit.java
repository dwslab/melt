/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hobbit.benchmarkfilegenerator;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 * @author Sven Hertling
 */
public class Hobbit {
    public static final String uri="http://w3id.org/hobbit/vocab#";
    
    protected static final Resource resource( String local )
        { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
        { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource API = resource( "API");
    public static final Resource KPI = resource( "KPI");
    public static final Resource Experiment = resource( "Experiment");
    public static final Resource Challenge = resource( "Challenge");
    public static final Resource Benchmark = resource( "Benchmark");
    public static final Resource Parameter = resource( "Parameter");
    public static final Resource FeatureParameter = resource( "FeatureParameter");
    public static final Resource ConfigurableParameter = resource( "ConfigurableParameter");
    
    public static final Property imageName = property( "imageName");
    public static final Property usesImage = property( "usesImage");
    public static final Property hasAPI = property( "hasAPI");
    public static final Property version = property( "version");
    public static final Property measuresKPI = property( "measuresKPI");
    public static final Property hasParameter = property( "hasParameter");
    public static final Property defaultValue = property( "defaultValue");
    
}
