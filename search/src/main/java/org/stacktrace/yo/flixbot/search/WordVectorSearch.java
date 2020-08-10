package org.stacktrace.yo.flixbot.search;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;
import org.stacktrace.yo.flixbot.KeyedVectors;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WordVectorSearch implements KeyedVectorSearch {

    private final double[][] vectors;
    private final String[] keys;
    private final int layerSize;
    private final ImmutableMap<String, Integer> w2vectorOffset;


    public WordVectorSearch(final KeyedVectors vectors) {
        this.vectors = vectors.vectors();
        this.layerSize = vectors.layerSize();

        Charset charset = StandardCharsets.UTF_8;
        ByteBuffer[] keys = vectors.keys();
        final ImmutableMap.Builder<String, Integer> result = ImmutableMap.builder();
        for (int i = 0; i < keys.length; i++) {
            String key = charset.decode(keys[i]).toString();
            result.put(key, i);
        }
        w2vectorOffset = result.build();
        this.keys = w2vectorOffset.keySet().toArray(new String[0]);
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

    @Override
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