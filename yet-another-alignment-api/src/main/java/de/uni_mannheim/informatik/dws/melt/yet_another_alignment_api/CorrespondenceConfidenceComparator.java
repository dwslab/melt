package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.Comparator;

/**
 * Comparator for {@link Correspondence}.
 * @author Sven Hertling
 */
public class CorrespondenceConfidenceComparator implements Comparator<Correspondence>  {
    @Override
    public int compare(Correspondence one, Correspondence two) {
        return Double.compare(one.confidence, two.confidence);
    }
}
