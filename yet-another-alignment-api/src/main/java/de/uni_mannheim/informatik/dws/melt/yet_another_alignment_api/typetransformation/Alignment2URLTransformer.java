package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.basetransformers.TypeTransformerHelper;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentSerializer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Alignment2URLTransformer extends AbstractTypeTransformer<Alignment, URL>{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Alignment2URLTransformer.class);
    
    private static final String FILE_PREFIX = "alignment";
    private static final String FILE_SUFFIX = ".rdf";

    public Alignment2URLTransformer() {
        super(Alignment.class, URL.class);
    }
    
    @Override
    public URL transform(Alignment value, Properties parameters) throws TypeTransformationException {
        try{
            File serializationFile = TypeTransformerHelper.getRandomSerializationFile(parameters, FILE_PREFIX, FILE_SUFFIX);
            AlignmentSerializer.serialize(value, serializationFile);
            return serializationFile.toURI().toURL();
        }catch(IOException e){
            throw new TypeTransformationException("Could not transform Alignment to URL", e);
        }
    }
    
    
    public static URL serializeAlignmentToTmpDir(Alignment alignment) throws IOException{
        File serializationFile = File.createTempFile(FILE_PREFIX, FILE_SUFFIX);
        AlignmentSerializer.serialize(alignment, serializationFile);
        return serializationFile.toURI().toURL();
    }
}
