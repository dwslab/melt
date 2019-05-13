package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

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
