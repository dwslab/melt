package de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.TypeTransformerHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OntModel2URLTransformer extends AbstractTypeTransformer<OntModel, URL>{
    private static final Logger LOGGER = LoggerFactory.getLogger(OntModel2URLTransformer.class);

    public OntModel2URLTransformer() {
        super(OntModel.class, URL.class);
    }
    
    @Override
    public URL transform(OntModel value, Properties parameters) throws Exception {
        
        Lang lang = RDFLanguages.nameToLang(parameters.getProperty(ParameterConfigKeys.DEFAULT_ONTOLOGY_SERIALIZATION_FORMAT, "RDF/XML"));
        if(lang == null){
            LOGGER.warn("The DEFAULT_ONTOLOGY_SERIALIZATION_FORMAT could not be converted to a jena lang. Defaulting to RDF/XML.");
            lang = Lang.RDFXML;
        }

        String fileExtension = ".xml";
        List<String> fileExtensions = lang.getFileExtensions();
        if(fileExtensions.size() > 0)
            fileExtension = "." + fileExtensions.get(0);
        
        File f = TypeTransformerHelper.getRandomSerializationFile(parameters, "model", fileExtension);
        try(OutputStream out = new FileOutputStream(f)){
            RDFDataMgr.write(out, value, lang);     
            //value.write(out, parameters.getProperty(ParameterConfigKeys.DEFAULT_ONTOLOGY_SERIALIZATION_FORMAT));
        } catch (IOException ex) {
            LOGGER.warn("Could not write the ontModel to File.");
            return null;
        }
        return f.toURI().toURL();
    }
}
