package de.uni_mannheim.informatik.dws.melt.matching_jena;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBException;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;
import org.apache.jena.tdb.solver.stats.Stats;
import org.apache.jena.tdb.solver.stats.StatsCollector;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb.store.GraphTDB;
import org.apache.jena.tdb.store.bulkloader.BulkLoader;
import static org.apache.jena.tdb.store.bulkloader.BulkLoader.createLoadMonitor;
import org.apache.jena.tdb.store.bulkloader.BulkStreamRDF;
import org.apache.jena.tdb.store.bulkloader.LoadMonitor;
import org.apache.jena.tdb.store.bulkloader.LoaderNodeTupleTable;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb.store.nodetupletable.NodeTupleTableView;
import org.apache.jena.tdb.sys.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TDB util for generating and inspecting TDB datasets.
 */
public class TdbUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(TdbUtil.class);
    
    public static OntModel bulkLoadToTdbOntModel(String tdblocation, String url, OntModelSpec spec){
        Dataset d = TDBFactory.createDataset(tdblocation);
        GraphTDB graphTDB = (GraphTDB)d.asDatasetGraph().getDefaultGraph();
        //TDBLoader.load(graphTDB,url, true);
        
        DatasetGraphTDB dsg = graphTDB.getDatasetGraphTDB();
        
        BulkStreamRDF dest = new DestinationGraph(dsg, null, true, false);
        dest.startBulk() ;
        //for ( String url : urls ) {
        TDB.logLoader.info("Load: " + url + " -- " + DateTimeUtils.nowAsString()) ;
        Lang lang = RDFLanguages.filenameToLang(url, Lang.NTRIPLES) ;
        //RDFDataMgr.parse(dest, url, lang) ;
        
        
        RDFParser.create()
                .source(url)
                .base(url)
                .errorHandler(ErrorHandlerFactory.errorHandlerWarn)
                .lang(lang)
                .parse(dest);
        
        //}
        dest.finishBulk() ;
        
        return ModelFactory.createOntologyModel(spec, d.getDefaultModel());
    }
    
    /*
    public static OntModel bulkLoadToTdbOntModel(String tdblocation, String url, OntModelSpec spec){
        Dataset d = TDBFactory.createDataset(tdblocation);
        GraphTDB graphTDB = (GraphTDB)d.asDatasetGraph().getDefaultGraph();
        TDBLoader.load(graphTDB,url, true);
        return ModelFactory.createOntologyModel(spec, d.getDefaultModel());
    }
    
    public static Model bulkLoadToTdbModel(String tdblocation, String url){
        Dataset d = TDBFactory.createDataset(tdblocation);
        GraphTDB graphTDB = (GraphTDB)d.asDatasetGraph().getDefaultGraph();
        TDBLoader.load(graphTDB,url, true);
        return d.getDefaultModel();
    }
    */
    
    public static void createTDB(String url, String tdblocation){
        Dataset d = TDBFactory.createDataset(tdblocation);
        GraphTDB graphTDB = (GraphTDB)d.asDatasetGraph().getDefaultGraph();
        TDBLoader.load(graphTDB,url, true);
    }
    
    public static OntModel getOntModelFromTDB(String tdblocation, OntModelSpec spec){
        Dataset d = TDBFactory.createDataset(tdblocation);
        return ModelFactory.createOntologyModel(spec, d.getDefaultModel());
    }
    
    public static Model getModelFromTDB(String tdblocation){
        Dataset d = TDBFactory.createDataset(tdblocation);
        return d.getDefaultModel();
    }
    
    
    /**
     * Checks if there is at least one file in the directory denoted by this url which ends with .dat or .idn.
     * This indicates that there is a TDB dataset.
     * There is also the function TDBFactory#inUseLocation() but it also returns true if the directory contains
     * one rdf file and is definitely not a TDB dataset.
     * The definition when a directory is a TDB directory might change in the future.
     * @param directory the directory as a File
     * @return true if it is a TDB dataset e.g. contains at least of file which ends with .dat or .idn
     */
    public static boolean isTDB1Dataset(File directory){
        if(directory == null)
            return false;
        if(directory.isDirectory() == false)// if not exists or is not a directory
            return false;
        
        File[] entries = directory.listFiles((dir, name) -> {
            if(name.endsWith(".dat") || name.endsWith(".idn"))
                return true;
            return false;
        });
        return entries.length > 0 ;
    }
    /**
     * Returns a file object given a url or path as string.
     * @param url a url or path as string
     * @return the file object
     */
    public static File getFileFromURL(String url){
        try {
            return Paths.get(new URI(url)).toFile();
        }catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException | SecurityException ex) {
            return null;
        }
    }
    
    
    
    //copied from jena because DestinationDSG is private
    
    private static final class DestinationGraph implements BulkStreamRDF {
        final private DatasetGraphTDB      dsg ;
        final private Node                 graphName ;
        final private LoadMonitor          monitor ;
        final private LoaderNodeTupleTable loaderTriples ;
        final private boolean              startedEmpty ;
        private long                       count = 0 ;
        private StatsCollector             stats = null ;
        private final boolean              collectStats ;

        // Graph node is null for default graph.
        DestinationGraph(final DatasetGraphTDB dsg, Node graphNode, boolean showProgress, boolean collectStats) {
            this.dsg = dsg ;
            this.graphName = graphNode ;
            this.collectStats = collectStats ;
            // Choose NodeTupleTable.
            NodeTupleTable nodeTupleTable ;
            if ( graphNode == null || Quad.isDefaultGraph(graphNode) )
                nodeTupleTable = dsg.getTripleTable().getNodeTupleTable() ;
            else {
                NodeTupleTable ntt = dsg.getQuadTable().getNodeTupleTable() ;
                nodeTupleTable = new NodeTupleTableView(ntt, graphName) ;
            }
            startedEmpty = dsg.isEmpty() ;
            monitor = createLoadMonitor(dsg, "triples", showProgress) ;
            loaderTriples = new LoaderNodeTupleTable(nodeTupleTable, "triples", monitor) ;
        }

        @Override
        final public void startBulk() {
            loaderTriples.loadStart() ;
            loaderTriples.loadDataStart() ;
            if ( collectStats )
                this.stats = new StatsCollector() ;
        }

        @Override
        final public void triple(Triple triple) {
            Node s = triple.getSubject() ;
            Node p = triple.getPredicate() ;
            Node o = triple.getObject() ;

            loaderTriples.load(s, p, o) ;
            if ( stats != null )
                stats.record(null, s, p, o) ;
            count++ ;
        }

        @Override
        final public void finishBulk() {
            loaderTriples.loadDataFinish() ;
            loaderTriples.loadIndexStart() ;
            loaderTriples.loadIndexFinish() ;
            loaderTriples.loadFinish() ;

            if ( !dsg.getLocation().isMem() && startedEmpty && stats != null ) {
                String filename = dsg.getLocation().getPath(Names.optStats) ;
                Stats.write(filename, stats.results()) ;
            }
            forceSync(dsg) ;
        }

        @Override
        public void start() {}

        @Override
        public void quad(Quad quad) {
            throw new TDBException("Quad encountered while loading a single graph") ;
        }

        @Override
        public void base(String base) {}

        @Override
        public void prefix(String prefix, String iri) {
            if ( graphName != null && graphName.isBlank() ) {
                TDB.logLoader.warn("Prefixes for blank node graphs not stored") ;
                return ;
            }

            PrefixMapping pmap = (graphName == null)
                                                    ? dsg.getPrefixes().getPrefixMapping()
                                                    : dsg.getPrefixes().getPrefixMapping(graphName.getURI()) ;
            pmap.setNsPrefix(prefix, iri) ;
        }

        @Override
        public void finish() {}
    }

    static void forceSync(DatasetGraphTDB dsg) {
        // Force sync - we have been bypassing DSG tables.
        // THIS DOES NOT WORK IF modules check for SYNC necessity.
        dsg.getTripleTable().getNodeTupleTable().getNodeTable().sync() ;
        dsg.getQuadTable().getNodeTupleTable().getNodeTable().sync() ;
        dsg.getQuadTable().getNodeTupleTable().getNodeTable().sync() ;
        dsg.getPrefixes().getNodeTupleTable().getNodeTable().sync() ;
        // This is not enough -- modules check whether sync needed.
        dsg.sync() ;

    }
    
}
