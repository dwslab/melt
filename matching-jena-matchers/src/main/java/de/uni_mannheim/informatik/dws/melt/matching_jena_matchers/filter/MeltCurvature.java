package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.ArrayUtils;

/**
 * How the curvature should be determined.
 */
public interface MeltCurvature {
    
    
    /**
     * Computes the y value where the curvature changes.
     * It returns the y value and not the x value.
     * IMPORTANT: in case the values are sorted, it has to be ascending.
     * @param xVals the x values
     * @param yVals the y values
     * @return the y value where the curvature changes
     */
    public double computeCurvature(double[] xVals, double[] yVals);
    
    default double computeCurvature(double[] yVals) {
        double[] xVals = new double[yVals.length];
        for(int i = 0; i < yVals.length; i++){
            xVals[i] = (double) i;
        }
        return computeCurvature(xVals, yVals);
    }
    
    /**
     * Computes the y values where the maximum slope is detected (the highest difference of y values).
     * Does not work well for large data.
     */
    public static final MeltCurvature MAX_SLOPE = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            double highestDiff = 0.0d;
            int pos = 0;
            for (int i = 0; i < yVals.length - 1; i++) {
                double diff = Math.abs(yVals[i] - yVals[i + 1]);
                if (diff >= highestDiff) {
                    highestDiff = diff;
                    pos = i + 1;
                }
            }
            return yVals[pos];
        }
    };
    
    /**
     * Computes the y values where the minimum slope is detected (the lowest difference of y values).
     * Does not work well for large data.
     */
    public static final MeltCurvature MIN_SLOPE = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            double lowestDiff = Double.MAX_VALUE;
            int pos = 0;
            for (int i = 0; i < yVals.length - 1; i++) {
                double diff = Math.abs(yVals[i] - yVals[i + 1]);
                if (diff < lowestDiff) {
                    lowestDiff = diff;
                    pos = i + 1;
                }
            }
            return yVals[pos];
        }
    };
    
    /**
     * Compute the y value of the elbow point (the point with the maximum
     * absolute second derivative). https://stackoverflow.com/a/4473065/11951900
     * Does not work well for large data.
     */
    public static final MeltCurvature MAX_SECOND_DERIVATIVE = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            double highestDiff = 0.0d;
            int pos = 0;
            for (int i = 1; i < yVals.length - 1; i++) {
                double diff = Math.abs(yVals[i + 1] + yVals[i - 1] - (2 * yVals[i]));
                if (diff >= highestDiff) {
                    highestDiff = diff;
                    pos = i;
                }
            }
            return yVals[pos];
        }
    };
    
    
    /**
     * Compute the y value where the second derivative has the smallest value.
     */
    public static final MeltCurvature MIN_SECOND_DERIVATIVE = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            double lowestDiff = Double.MAX_VALUE;
            int pos = 0;
            for (int i = 1; i < yVals.length - 1; i++) {
                double diff = Math.abs(yVals[i + 1] + yVals[i - 1] - (2 * yVals[i]));
                if (diff < lowestDiff) {
                    lowestDiff = diff;
                    pos = i;
                }
            }
            return yVals[pos];
        }
    };
    
    /**
     * Compute the median value (e.g. taking the value which lies in the middle).
     */
    public static final MeltCurvature MEDIAN = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            int pos = yVals.length / 2;
            return yVals[pos];
        }
    };
    
    
    /**
     * Menger curvature to detect elbow points.
     * Implementation details also available <a href="https://github.com/mariolpantunes/ml/blob/master/src/main/java/pt/it/av/tnav/ml/clustering/curvature/MengerCurvature.java">at MengerCurvature.java</a>.
     * Paper from X. Tolsa, “Principal Values for the Cauchy Integral and Rectifiability,” in American Mathematical Society, 2000.
     */
    public static final MeltCurvature MENGER_ELBOW = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            int pos = 0;
            double maxCurvature = Double.MIN_VALUE;
            for (int i = 1; i < xVals.length - 1; i++) {
                double curvature = mengerDC(xVals, yVals, i);
                if (curvature > maxCurvature) {
                    maxCurvature = curvature;
                    pos = i;
                }
            }
            return yVals[pos];
        }
    };
    
    /**
     * Menger curvature to detect knee points.
     * Implementation details also available <a href="https://github.com/mariolpantunes/ml/blob/master/src/main/java/pt/it/av/tnav/ml/clustering/curvature/MengerCurvature.java">at MengerCurvature.java</a>.
     * Paper from X. Tolsa, “Principal Values for the Cauchy Integral and Rectifiability,” in American Mathematical Society, 2000.
     */
    public static final MeltCurvature MENGER_KNEE = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            int pos = 0;
            double minCurvature = Double.MAX_VALUE;
            for (int i = 1; i < xVals.length - 1; i++) {
                double curvature = mengerDC(xVals, yVals, i);
                if (curvature < minCurvature) {
                    minCurvature = curvature;
                    pos = i;
                }
            }
            return yVals[pos];
        }
    };
    
    static double mengerDC(final double[] x, final double[] y, final int i) {
        double pq = Math.sqrt(Math.pow(x[i - 1] - x[i], 2.0) + Math.pow(y[i - 1] - y[i], 2.0)),
                qr = Math.sqrt(Math.pow(x[i] - x[i + 1], 2.0) + Math.pow(y[i] - y[i + 1], 2.0)),
                rp = Math.sqrt(Math.pow(x[i - 1] - x[i + 1], 2.0) + Math.pow(y[i - 1] - y[i + 1], 2.0));

        double A = 4.0 * Math.pow(pq, 2.0) * Math.pow(qr, 2.0),
                B = Math.pow(pq, 2.0) + Math.pow(qr, 2.0) - Math.pow(rp, 2.0);

        return Math.sqrt(A - Math.pow(B, 2.0)) / (pq * qr * rp);
    }
    
    
    public static MeltCurvature configureLongestDistanceToStraightLine(boolean knee, double percentageDiff){
        return new MeltCurvature() {
            @Override
            public double computeCurvature(double[] xVals, double[] yVals) {
                int minValPos = 0;
                double minVal = yVals[minValPos];

                int maxValPos = yVals.length - 1;
                double maxVal = yVals[maxValPos];

                
                if(percentageDiff > 0.0){
                    //System.out.println("before: minvalpos: " + minValPos + " minval: " + minVal + "    maxvalpos: " + maxValPos + " maxval: " + maxVal);
                    double interval = (maxVal - minVal);
                    double possibleDiff = interval / (percentageDiff * 100.0);
                    //System.out.println(possibleDiff);
                    while(Math.abs(yVals[maxValPos] - maxVal) < possibleDiff){
                        maxValPos--;
                    }
                    maxVal = yVals[maxValPos];
                    
                    while(Math.abs(yVals[minValPos] - minVal) < possibleDiff){
                        minValPos++;
                    }
                    minVal = yVals[minValPos];
                    //System.out.println("after: minvalpos: " + minValPos + " minval: " + minVal + "    maxvalpos: " + maxValPos + " maxval: " + maxVal);
                }
                
                //compute the distance between the line and each point.
                //see https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line#Line_defined_by_two_points
                double xdiff = xVals[maxValPos] - xVals[minValPos];
                double ydiff = maxVal - minVal;
                double normalizing = Math.sqrt(Math.pow(xdiff, 2.0) + Math.pow(ydiff, 2.0));
                int pos = 0;
                if(knee){
                    double maxPositiveDistance = 0.0;
                    for (int i = 0; i < yVals.length; i++) {
                        double distance = ((xdiff * (minVal - yVals[i])) - ((xVals[minValPos] - xVals[i]) * ydiff)) / normalizing;
                        if(distance > maxPositiveDistance){
                            maxPositiveDistance = distance;
                            pos = i;
                        }
                    }
                }else{
                    double maxNegativeDistance = 0.0;
                    for (int i = 0; i < yVals.length; i++) {
                        double distance = ((xdiff * (minVal - yVals[i])) - ((xVals[minValPos] - xVals[i]) * ydiff)) / normalizing;
                        if(distance < maxNegativeDistance){
                            maxNegativeDistance = distance;
                            pos = i;
                        }
                    }
                }
                return yVals[pos];
            }
        };
    }
    
    public static final MeltCurvature LONGEST_DISTANCE_TO_STRAIT_LINE_KNEE = configureLongestDistanceToStraightLine(true, 0.0);
    
    public static final MeltCurvature LONGEST_DISTANCE_TO_STRAIT_LINE_ELBOW = configureLongestDistanceToStraightLine(false, 0.0);
    
    public static final MeltCurvature LONGEST_DISTANCE_TO_ADJUSTED_STRAIT_LINE_KNEE = configureLongestDistanceToStraightLine(true, 0.99);
    
    public static final MeltCurvature LONGEST_DISTANCE_TO_ADJUSTED_STRAIT_LINE_ELBOW = configureLongestDistanceToStraightLine(false, 0.99);
    
    
    /**
     * Implementation of the L method from: S. Salvador and P. Chan, “Determining the Number of Clusters/Segments in Hierarchical Clustering/Segmentation Algorithms,” in ICTAI, 2004.
     * Two regression lines which should fit best to the data. The intersection point is the knee.
     * Implementation details from <a href="https://github.com/mariolpantunes/ml/blob/master/src/main/java/pt/it/av/tnav/ml/clustering/curvature/Lmethod.java">Lmethod.java</a>.
     */
    public static final MeltCurvature L_METHOD_KNEE = new MeltCurvature() {
        
        private static final int MINCUTOFF = 20;
        
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            int cutoff = xVals.length;
            int point = xVals.length;
            int lastPoint;
            
            do {
              lastPoint = point;
              point = lMethod(xVals, yVals, cutoff);
              cutoff = Math.min(point * 2, xVals.length);
            } while (point < lastPoint && cutoff >= MINCUTOFF);

            return yVals[point];
        }
        
        private int lMethod(double[] xVals, double[] yVals, int length){
            int pos = 0;
            double minCurvature = Double.MAX_VALUE;
            for (int i = 1; i < xVals.length - 1; i++) {
                LeastSquares left = new LeastSquares(xVals, yVals,0,0,i+1);
                LeastSquares right = new LeastSquares(xVals, yVals, i, i, length - i);
                
                double leftRmse = left.rmse(xVals, yVals, 0, i + 1);
                double rightRmse = right.rmse(xVals, yVals, i, length);
                
                double curvature = ((i-1) * leftRmse + (length-i) * rightRmse)/ (length-1);
                if (curvature < minCurvature) {
                    minCurvature = curvature;
                    pos = i;
                }
            }
            return pos;
        }
        
        /**
         * Represents least squares with formula ax +b
         */
        class LeastSquares {
            private double a;
            private double b;

            public LeastSquares(double[] x, double[] y, int bX, int bY, int l) {
                double sx = 0.0;
                double sy = 0.0;
                double xy = 0.0;
                double x2 = 0.0;

                for (int i = 0; i < l; i++) {
                  sx += x[i + bX];
                  x2 += Math.pow(x[i + bX], 2);
                  xy += x[i + bX] * y[i + bY];
                  sy += y[i + bY];
                }
                double d = l * x2 - Math.pow(sx, 2.0);
                
                this.a = (l * xy - sx * sy) / d;
                this.b = (sy * x2 - sx * xy) / d;
            }
            
            public double solve(double x){
                return a*x+b;
            }
            
            public double rmse(double x[], double[] y, int idx, int length){
                double mse = 0.0;
                for(int i = idx; i < length; i++) {
                  mse += Math.pow(y[i] - solve(x[i]), 2.0);
                }
                return Math.sqrt(mse);
            }            
        }
    };
    
    /**
     * Implementation of the L method from: S. Salvador and P. Chan, “Determining the Number of Clusters/Segments in Hierarchical Clustering/Segmentation Algorithms,” in ICTAI, 2004.
     * Two regression lines which should fit best to the data. The intersection point is the elbow.
     * The data is just reversed.
     * Implementation details from <a href="https://github.com/mariolpantunes/ml/blob/master/src/main/java/pt/it/av/tnav/ml/clustering/curvature/Lmethod.java">Lmethod.java</a>.
     */
    public static final MeltCurvature L_METHOD_ELBOW = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            ArrayUtils.reverse(xVals);
            ArrayUtils.reverse(yVals);
            return L_METHOD_KNEE.computeCurvature(xVals, yVals);
        }
    };
    
    
    /**
     * Implementation of "Finding a Kneedle in a Haystack: Detecting Knee Points in System Behavior" by Ville Satopaa; Jeannie Albrecht; David Irwin; Barath Raghavan.
     * Partially used from library <a href="https://github.com/lukehb/137-stopmove/blob/8961b4a9bbcbc20d72930f2653cb8064ad8dcffc/src/main/java/onethreeseven/stopmove/algorithm/Kneedle.java">137-stopmove</a>.
     */
    public static final MeltCurvature KNEEDLE_ELBOW = new MeltCurvature() {
        @Override
        public double computeCurvature(double[] xVals, double[] yVals) {
            if(yVals.length <= 1){
                return 0;
            }
            double[] normalisedData = prepare(gaussianSmooth(yVals, 3));
            int elbowIdx = findElbowIndex(normalisedData);
            return yVals[elbowIdx];
        }
        
        private double[] gaussianSmooth(double[] data, int n){
            double[] smoothed = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                int startIdx = Math.max(0, i - n);
                int endIdx = Math.min(data.length - 1, i + n);

                double sumWeights = 0;
                double sumIndexWeight = 0;

                for (int j = startIdx; j < endIdx + 1; j++) {
                    double indexScore = Math.abs(j - i)/(double)n;
                    double indexWeight = gaussian(indexScore, 1, 0, 1);
                    sumWeights += (indexWeight * data[j]);
                    sumIndexWeight += indexWeight;
                }
                smoothed[i] = sumWeights/sumIndexWeight;
            }
            return smoothed;
        }
        private double gaussian(double x, double height, double center, double width){
            return height * Math.exp(-(x-center)*(x-center)/(2.0*width*width) );
        }
        
        private double[] prepare(double[] data){
            double curMin = Double.POSITIVE_INFINITY;
            double curMax = Double.NEGATIVE_INFINITY;
            for (double v : data) {
                if(v < curMin){
                    curMin = v;
                }
                if(v > curMax){
                    curMax = v;
                }
            }            
            final double range = curMax - curMin;
            double[] normalisedData = new double[data.length];
            for (int i = 0; i < normalisedData.length; i++) {
                double normalisedIndex = (double)i / data.length;
                normalisedData[i] = ((data[i] - curMin) / range) - normalisedIndex;
            }
            return normalisedData;
        }
        
        private int findElbowIndex(double[] data){
            int bestIdx = 0;
            double bestScore = 0;
            for (int i = 0; i < data.length; i++) {
                double score = Math.abs(data[i]);
                if(score > bestScore){
                    bestScore = score;
                    bestIdx = i;
                }
            }
            return bestIdx;
        }
    };
    
    public static Map<String, MeltCurvature> getAllPossibleCurvatureMethods() {
        Map<String, MeltCurvature> methods = new TreeMap<>();
        for (Field field : MeltCurvature.class.getDeclaredFields()) {
            int fieldModifier = field.getModifiers();
            if (Modifier.isStatic(fieldModifier) && Modifier.isPublic(fieldModifier)) {
                if (MeltCurvature.class.isAssignableFrom(field.getType())) {
                    try {
                        methods.put(field.getName(), (MeltCurvature)field.get(null));
                    } catch (IllegalArgumentException | IllegalAccessException ex) { }
                }
            }
        }
        return methods;
    }
}
