package org.stacktrace.yo.flixbot.vector.keyed;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;

import java.util.List;

public class KeyedVectorShard extends KeyedVectors {


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

        public KeyedVectorShard asShard() {
            return new KeyedVectorShard(
                    shardVectors.toArray(new double[0][0]),
                    builder.build(),
                    layerSize
            );
        }


    }

    protected final double[][] vectors;
    protected final String[] keys;
    protected final ImmutableMap<String, Integer> vectorOffset;

    public KeyedVectorShard(double[][] vectors, ImmutableMap<String, Integer> offsets, int layerSize) {
        super(layerSize);
        this.vectors = vectors;
        this.vectorOffset = offsets;
        this.keys = offsets.keySet().toArray(new String[0]);
    }

    public Boolean contains(String word) {
        return vectorOffset.containsKey(word);
    }

    @Override
    public double[] keyVector(String key) {
        final Integer index = vectorOffset.get(key);
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
            final Integer index = vectorOffset.get(key);
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
    public String[] keys() {
        return keys;
    }


}