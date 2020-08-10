package org.stacktrace.yo.flixbot.search;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;

import java.util.List;
import java.util.stream.Stream;

public interface KeyedVectorSearch {

    class SearchResult {
        final List<Answer> answers;

        public SearchResult(List<Answer> answers) {
            this.answers = answers;
        }
    }

    class Answer {
        final String name;
        final double score;

        public Answer(String name, double score) {
            this.name = name;
            this.score = score;
        }
    }

    /**
     * Returns true if KeyedVectorModel contains the provided key
     *
     * @return Boolean
     */
    Boolean contains(String key);

    /**
     * Returns the Vector Representing the the provided key
     *
     * @return Keyed Vector Optional
     */
    double[] keyVector(String key);

    /**
     * Returns the Vector Representing the the provided keys
     * <p>
     * takes the mean of the vectors returned with no weight
     *
     * @return Keyed Vector Optional
     */

    double[] keyVector(List<String> keys);

    SearchResult mostSimilar(String key, int top);

    SearchResult mostSimilar(List<String> keys, int top);

    SearchResult mostSimilar(List<String> pos, List<String> neg, int top);

    SearchResult mostSimilar(double[] pos, double[] neg, int top);

    SearchResult mostSimilar(double[] vec, int top);

    List<Answer> search(final double[] vec, int maxNumMatches);


    Ordering<WordVectorSearch.Answer> ANSWER_ORDERING = Ordering.natural().onResultOf((Function<Answer, Double>) answer -> answer != null ? answer.score : 0);

    default double cosineDistance(double[] otherVec, double[] vec) {
        RealVector oV = new ArrayRealVector(otherVec);
        RealVector v = new ArrayRealVector(vec);
        return oV.cosine(v);
    }

    default double[] applyWeight(double[] vec, double scalar) {
        RealVector v = new ArrayRealVector(vec);
        return v.mapMultiply(scalar).toArray();
    }

    default double[] mean(List<String> keys, int layerSize) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (String key : keys) {
            double[] vec = keyVector(key);
            if (null != vec) {
                vectorialMean.increment(vec);
            }
        }
        return vectorialMean.getResult();
    }

    default double[] mean(List<String> keys, double weight, int layerSize) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (String key : keys) {
            double[] vec = keyVector(key);
            if (null != vec) {
                vectorialMean.increment(applyWeight(vec, weight));
            }
        }
        return vectorialMean.getResult();
    }

    default double[] mean(List<String> pos, List<String> neg, int layerSize) {
        return mean(layerSize, mean(pos, layerSize), mean(neg, -1.0, layerSize));
    }

    default double[] mean(int layerSize, double[]... vecs) {
        VectorialMean vectorialMean = new VectorialMean(layerSize);
        for (double[] vec : vecs) {
            vectorialMean.increment(vec);
        }
        return vectorialMean.getResult();
    }
}
