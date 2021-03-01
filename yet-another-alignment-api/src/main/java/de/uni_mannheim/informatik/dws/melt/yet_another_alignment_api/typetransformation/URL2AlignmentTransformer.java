package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AbstractTypeTransformer;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
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
            throw new TypeTransformationException("Could not transform URL to Alignment", e);
        }
    }
}
