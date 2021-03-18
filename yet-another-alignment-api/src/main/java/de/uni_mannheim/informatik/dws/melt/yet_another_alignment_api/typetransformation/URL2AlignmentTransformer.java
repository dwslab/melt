package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.TypeTransformerHelper;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentXmlRepair;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;


public class URL2AlignmentTransformer extends AbstractTypeTransformer<URL, Alignment> {
    public URL2AlignmentTransformer() {
        super(URL.class, Alignment.class);
    }

    @Override
    public Alignment transform(URL value, Properties parameters) throws TypeTransformationException {
        try {
            return AlignmentParser.parse(value);
        } catch (SAXException | IOException  e) {            
            if(TypeTransformerHelper.getOrDefault(parameters, ParameterConfigKeys.ALLOW_ALIGNMENT_REPAIR, Boolean.class, true)){
                //works only for file URL
                URI uri;
                try {
                    uri = value.toURI();
                } catch (URISyntaxException ex) {
                    throw new TypeTransformationException("Could not convert URL to URI", e);
                }
                if(uri.getScheme().equalsIgnoreCase("file")){
                    try {
                        return AlignmentXmlRepair.loadRepairedAlignment(Paths.get(uri).toFile());
                    } catch (SAXException | IOException ex) { 
                        throw new TypeTransformationException("Tried to repair alignment file, but even that did not help.", ex);
                    }
                }
            }
            throw new TypeTransformationException("Could not transform URL to Alignment", e);
        }
    }
}
