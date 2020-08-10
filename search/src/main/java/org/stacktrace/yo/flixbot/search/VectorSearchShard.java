package org.stacktrace.yo.flixbot.search;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VectorSearchShard implements KeyedVectorSearch {


    public static class VectorSearchShardData {

        private int idx = 0;
        private final int layerSize;
        private final ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        private final List<double[]> shardVectors;

        public VectorSearchShardData(int laySize) {
            shardVectors = Lists.newArrayList();
            layerSize = laySize;
        }

        public VectorSearchShardData addVector(String key, double[] vector) {
            builder.put(key, idx);
            shardVectors.add(vector);
            idx++;
            return this;
        }

        public VectorSearchShard asShard() {
            return new VectorSearchShard(
                    shardVectors.toArray(new double[0][0]),
                    builder.build(),
                    layerSize
            );
        }


    }

    private final double[][] vectors;
    private final String[] keys;
    private final int layerSize;
    private final ImmutableMap<String, Integer> w2vectorOffset;

    public VectorSearchShard(double[][] vectors, ImmutableMap<String, Integer> offsets, int layerSize) {
        this.vectors = vectors;
        this.layerSize = layerSize;
        this.w2vectorOffset = offsets;
        this.keys = offsets.keySet().toArray(new String[0]);
    }

    public Boolean contains(String word) {
        return w2vectorOffset.containsKey(word);
    }

    @Override
    public double[] keyVector(String key) {
        final Integer index = w2vectorOffset.get(key);
        if (index == null) {
            return null;
        }
        return vectors[index];
    }

    @Override
    public double[] keyVector(List<String> keys) {
        VectorialMean mean = new VectorialMean(this.layerSize);

        int count = 0;

        for (String key : keys) {
            final Integer index = w2vectorOffset.get(key);
            if (index != null) {
                double[] result = vectors[index];
                mean.increment(result);
                count += 1;
            }
        }
        if (count == 0) {
            return null;
        }
        return mean.getResult();
    }

    @Override
    public SearchResult mostSimilar(String key, int top) {
        double[] vector = keyVector(key);
        if (null != vector) {
            return mostSimilar(vector, top + 1);
        }
        return new SearchResult(Lists.newArrayList());
    }

    @Override
    public SearchResult mostSimilar(double[] vec, int top) {
        return new SearchResult(search(vec, top));
    }

    public List<Answer> search(final double[] vec, int maxNumMatches) {
        return ANSWER_ORDERING.greatestOf(
                Arrays.stream(keys).map(other -> {
                    double[] ov = keyVector(other);
                    double d = cosineDistance(ov, vec);
                    return new Answer(other, d);
                }).collect(Collectors.toList()),
                maxNumMatches
        );
    }

    @Override
    public SearchResult mostSimilar(List<String> keys, int top) {
        double[] mean = mean(keys, layerSize);
        return mostSimilar(mean, top + keys.size());
    }

    @Override
    public SearchResult mostSimilar(List<String> pos, List<String> neg, int top) {
        double[] mean = mean(pos, neg, layerSize);
        return mostSimilar(mean, top);
    }

    @Override
    public SearchResult mostSimilar(double[] pos, double[] neg, int top) {
        double[] mean = mean(layerSize, pos, neg);
        return mostSimilar(mean, top);
    }


}