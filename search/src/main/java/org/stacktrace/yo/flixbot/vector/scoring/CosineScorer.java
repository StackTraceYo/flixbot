package org.stacktrace.yo.flixbot.vector.scoring;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class CosineScorer implements Scorer {

    @Override
    public double score(double[] vec, double[] ov) {
        RealVector oV = new ArrayRealVector(ov, false);
        RealVector v = new ArrayRealVector(vec, false);
        return oV.cosine(v);
    }
}
