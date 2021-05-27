package de.uni_mannheim.informatik.dws.melt.matching_base.receiver;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import java.net.URL;

/**
 * This class implements the SEALS interface (via MatcherURL) and calls the provided matcher class
 * (the matcher class is provided via a file in the SEALS package in folder /conf/extenal/main_class.txt ).
 * If this class is renamed or moved, then the name needs to be adjusted in matching assembly project
 * in file SealsDescriptorHandler.java (method finalizeArchiveCreation - line 45).
 */
public class SealsWrapper extends MatcherURL{
    
    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        TypeTransformerRegistry.addMeltDefaultTransformers(); // because SEALS does not allow the service registry.
        String mainClass = MainMatcherClassExtractor.extractMainClass();
        AlignmentAndParameters result = GenericMatcherCaller.runMatcher(mainClass, source, target, inputAlignment);
        return TypeTransformerRegistry.getTransformedObject(result.getAlignment(), URL.class);
    }
}
