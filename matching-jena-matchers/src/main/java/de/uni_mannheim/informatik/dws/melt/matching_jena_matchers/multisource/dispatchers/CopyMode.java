package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_jena.JenaHelper;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TdbUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.JenaTransformerHelper;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;

/**
 * Defines different modes how a model should be copied during incremental merge.
 */
public interface CopyMode {
    
    public Set<Object> getCopiedModel(Set<Object> modelRepresentations, Properties parameters) throws TypeTransformationException;
        
    /**
     * Do not make any copy but use the models which are provided.
     * Do not use it if the initial models are stored in TDB, because those would be modifed.
     */
    public static CopyMode NONE = new CopyMode() {
        @Override
        public Set<Object> getCopiedModel(Set<Object> modelRepresentations, Properties parameters) throws TypeTransformationException {
            if(modelRepresentations.size() <= 1){
                return modelRepresentations; // only one representation available - do not remove anything
            }
            //remove URL representations of KG/model because the copied model will be modified
            //and thus the URL is not a representation anymore.
            Set<Object> modelRep = new HashSet<>();
            for(Object o : modelRepresentations){
                if(o instanceof URL == false){
                    modelRep.add(o);
                }
            }
            return modelRep;
        }
    };
    
    /**
     * Creates a new TDB storage for every KG which is the target of a merge (all triples are added to this KG).
     * This might require a lot of disk space because each TDB requires at least 200 MB.
     * This can be only used for sequential merges.
     */
    public static CopyMode CREATE_TDB = new CopyMode() {
        @Override
        public Set<Object> getCopiedModel(Set<Object> modelRepresentations, Properties parameters) throws TypeTransformationException {
            URL modelURL = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(modelRepresentations, URL.class, parameters);
            File tdbLocation = FileUtil.createFolderWithRandomNumberInUserTmp("incrementalMergeIntermediateKG");
            tdbLocation.mkdirs();
            OntModel copiedModel = TdbUtil.bulkLoadToTdbOntModel(tdbLocation.getAbsolutePath(), modelURL.toString(), JenaTransformerHelper.getSpec(parameters));
            Set<Object> models = new HashSet<>();
            models.add(copiedModel);
            try {
                models.add(tdbLocation.toURI().toURL());
            } catch (MalformedURLException ex) {} //Do nothing
            return models;
        }
    };
    
    /**
     * Creates a new TDB storage for the target KG but only if the model is large (by default greater than 100_000 triples - also see {@link #createTdbForLargeKg(int)}).
     * This works well for a large number of KGs with different sizes because only the large KGs are actually stored in a TDB.
     * This will reuse TDBs. This means if the input are TDB backed jena model, the TDB stoarge might be modified.
     * Thus better only used it if the input are in-memory models / rdf serialized files.
     */
     public static CopyMode CREATE_TDB_FOR_LARGE_KG = createTdbForLargeKg(100_000);
     
     /**
      * Creates a new TDB storage for the target KG but only if the model is large (which can be further defined by the number of triples as the parameter).
      * This works well for a large number of KGs with different sizes because only the large KGs are actually stored in a TDB.
      * This will reuse TDBs. This means if the input are TDB backed jena model, the TDB stoarge might be modified.
      * Thus better only used it if the input are in-memory models / rdf serialized files.
      * @param numberOfTriples creates a TDB if the model has more triples than this given number
      * @return the created copy mode.
      */
     public static CopyMode createTdbForLargeKg(int numberOfTriples){
         return new CopyMode() {
             @Override
             public Set<Object> getCopiedModel(Set<Object> modelRepresentations, Properties parameters) throws TypeTransformationException {
                 Model model = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(modelRepresentations, Model.class, parameters);
                //if model is already a TDB return it
                if(TdbUtil.isModelBackedByTDB(model)){
                    //LOGGER.info("already TDB");
                    // only return the model because it will be modified and then the URL is not valid anymore
                    return new HashSet<>(Arrays.asList(model));
                }
                //only build TDB for larger KGs
                if(model.size() < numberOfTriples){
                    //LOGGER.info("Too small for TDB");
                    // only return the model because it will be modified and then the URL is not valid anymore
                    return new HashSet<>(Arrays.asList(model));
                }
                //LOGGER.info("Create TDB storage");
                File tdbLocation = FileUtil.createFolderWithRandomNumberInUserTmp("incrementalMergeIntermediateKG");
                tdbLocation.mkdirs();
                OntModel copiedModel = TdbUtil.createTDBbackedModel(tdbLocation.getAbsolutePath(), model, JenaTransformerHelper.getSpec(parameters));
                Set<Object> models = new HashSet<>();
                models.add(copiedModel);
                try {
                    models.add(tdbLocation.toURI().toURL());
                } catch (MalformedURLException ex) {} //Do nothing
                return models;
             }
         };
    }
    
    /**
     * Copy the model in memory. This will not modify the provided models, 
     * but will require a lot of memory.
     */
    public static CopyMode COPY_IN_MEMORY = new CopyMode() {
        @Override
        public Set<Object> getCopiedModel(Set<Object> modelRepresentations, Properties parameters) throws TypeTransformationException {
            Model model = TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(modelRepresentations, Model.class, parameters);
            if(model == null){
                throw new IllegalArgumentException("Could not transform model during copying.");
            }
            OntModel copiedModel = JenaHelper.createNewOntModel(parameters);

            copiedModel.add(model);
            return new HashSet<>(Arrays.asList(copiedModel));
        }
    };
}
