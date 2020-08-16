package org.stacktrace.yo.flixbot.vector.keyed;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;

import java.util.List;

public abstract class KeyedVectors {


    public final int layerSize;

    public KeyedVectors(int layerSize) {
        this.layerSize = layerSize;
    }

    public abstract Boolean contains(String key);

    public abstract double[] keyVector(String key);

    public abstract double[] keyVector(List<String> keys);

    public abstract String[] keys();


    double[] applyWeight(double[] vec, double scalar) {
        RealVector v = new ArrayRealVector(vec, false);
        return v.mapMultiply(scalar).toArray();
    }

    public double[] mean(List<String> keys) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (String key : keys) {
            double[] vec = keyVector(key);
            if (null != vec) {
                vectorialMean.increment(vec);
            }
        }
        return unitVector(vectorialMean.getResult());
    }

    public double[] mean(List<String> keys, double weight) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (String key : keys) {
            double[] vec = keyVector(key);
            if (null != vec) {
                vectorialMean.increment(applyWeight(vec, weight));
            }
        }
        return unitVector(vectorialMean.getResult());
    }

    public double[] mean(List<String> pos, List<String> neg) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        if (!pos.isEmpty()) {
            for (String key : pos) {
                double[] vec = keyVector(key);
                if (null != vec) {
                    vectorialMean.increment(vec);
                }
            }
        }
        if (!neg.isEmpty()) {
            for (String key : neg) {
                double[] vec = keyVector(key);
                if (null != vec) {
                    vectorialMean.increment(applyWeight(vec, -1.0));
                }
            }
        }
        return unitVector(vectorialMean.getResult());
    }

    public double[] mean(double[]... vecs) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (double[] vec : vecs) {
            vectorialMean.increment(vec);
        }
        return unitVector(vectorialMean.getResult());
    }

    public static double[] unitVector(double[] vec){
        RealVector v = new ArrayRealVector(vec, false);
        return v.unitVector().toArray();
    }
}
