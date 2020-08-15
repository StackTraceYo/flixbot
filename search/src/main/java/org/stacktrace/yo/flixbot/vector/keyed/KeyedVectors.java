package org.stacktrace.yo.flixbot.vector.keyed;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;
import org.stacktrace.yo.flixbot.vector.search.VectorSearcher;

import java.util.Collections;
import java.util.List;

public abstract class KeyedVectors {

    public static class SearchResult {
        public final List<Answer> answers;

        public SearchResult(List<Answer> answers) {
            this.answers = answers;
        }
    }

    public static class Answer {
        public final String name;
        public final double score;

        public Answer(String name, double score) {
            this.name = name;
            this.score = score;
        }
    }

    protected final int layerSize;
    public KeyedVectors(int layerSize){

        this.layerSize = layerSize;
    }

    /**
     * Returns true if KeyedVectorModel contains the provided key
     *
     * @return Boolean
     */
    public abstract Boolean contains(String key);

    /**
     * Returns the Vector Representing the the provided key
     *
     * @return Keyed Vector Optional
     */
    public abstract double[] keyVector(String key);

    /**
     * Returns the Vector Representing the the provided keys
     * <p>
     * takes the mean of the vectors returned with no weight
     *
     * @return Keyed Vector Optional
     */

    public abstract double[] keyVector(List<String> keys);

    public SearchResult mostSimilar(String key, int top) {
        double[] vector = keyVector(key);
        if (null != vector) {
            return mostSimilar(vector, top + 1);
        }
        return new SearchResult(Collections.emptyList());
    }


    public SearchResult mostSimilar(double[] vec, int top) {
        VectorSearcher searcher = searcher(top);
        searcher.forVector(vec);
        return new SearchResult(searcher.search());
    }

    public SearchResult mostSimilar(List<String> keys, int top) {
        double[] mean = mean(keys, layerSize);
        return mostSimilar(mean, top + keys.size());
    }

    public SearchResult mostSimilar(List<String> pos, List<String> neg, int top) {
        double[] mean = mean(pos, neg, layerSize);
        return mostSimilar(mean, top + pos.size());
    }

    public SearchResult mostSimilar(double[] pos, double[] neg, int top) {
        double[] mean = mean(layerSize, pos, neg);
        return mostSimilar(mean, top);
    }

    public abstract VectorSearcher searcher(int top);

    public int layerSize(){
        return this.layerSize;
    }

    public abstract String[] keys();


     double[] applyWeight(double[] vec, double scalar) {
        RealVector v = new ArrayRealVector(vec, false);
        return v.mapMultiplyToSelf(scalar).toArray();
    }

     double[] mean(List<String> keys, int layerSize) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (String key : keys) {
            double[] vec = keyVector(key);
            if (null != vec) {
                vectorialMean.increment(vec);
            }
        }
        return vectorialMean.getResult();
    }

     double[] mean(List<String> keys, double weight, int layerSize) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (String key : keys) {
            double[] vec = keyVector(key);
            if (null != vec) {
                vectorialMean.increment(applyWeight(vec, weight));
            }
        }
        return vectorialMean.getResult();
    }

     double[] mean(List<String> pos, List<String> neg, int layerSize) {
        return mean(layerSize, mean(pos, layerSize), mean(neg, -1.0, layerSize));
    }

     double[] mean(int layerSize, double[]... vecs) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (double[] vec : vecs) {
            vectorialMean.increment(vec);
        }
        return vectorialMean.getResult();
    }
}
