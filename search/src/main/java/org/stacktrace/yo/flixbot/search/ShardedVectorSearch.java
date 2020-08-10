package org.stacktrace.yo.flixbot.search;

import com.google.common.collect.Lists;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;
import org.stacktrace.yo.flixbot.KeyedVectors;
import org.stacktrace.yo.flixbot.commons.Futures;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ShardedVectorSearch implements KeyedVectorSearch {


    private final int layerSize;
    private final VectorSearchShard[] searchShards;
    private final HashFunction hashFunction = Hashing.murmur3_32(5390);
    private final Executor searchExecuter;

    public ShardedVectorSearch(final KeyedVectors keyedVectors, final int shards) {
        this.layerSize = keyedVectors.layerSize();
        this.searchShards = new VectorSearchShard[shards];
        this.searchExecuter = Executors.newFixedThreadPool(shards);

        double[][] vectors = keyedVectors.vectors();
        ByteBuffer[] keys = keyedVectors.keys();

        io.vavr.collection.List<VectorSearchShard.VectorSearchShardData> searchShardData = io.vavr.collection.List.range(0, shards)
                .map(s -> new VectorSearchShard.VectorSearchShardData(layerSize))
                .toList();

        Charset charset = StandardCharsets.UTF_8;
        for (int i = 0; i < keys.length; i++) {
            String key = charset.decode(keys[i]).toString();
            int shard = getShard(key);
            searchShardData.get(shard).addVector(key, vectors[i]);
        }
        searchShardData.forEachWithIndex((vectorSearchShardData, value) -> searchShards[value] = vectorSearchShardData.asShard());
    }

    public Boolean contains(String word) {
        return Arrays.stream(searchShards).anyMatch(s -> s.contains(word));
    }

    public int getShard(String key) {
        return Math.max(0, Hashing.consistentHash(hashFunction.hashString(key, StandardCharsets.UTF_8), searchShards.length) - 1);
    }

    public VectorSearchShard shardForKey(String key) {
        return searchShards[getShard(key)];
    }

    @Override
    public double[] keyVector(String key) {
        return shardForKey(key).keyVector(key);
    }

    @Override
    public double[] keyVector(java.util.List<String> keys) {
        VectorialMean mean = new VectorialMean(this.layerSize);

        int count = 0;

        for (String key : keys) {
            double[] shardResult = shardForKey(key).keyVector(key);
            if (shardResult.length > 0) {
                mean.increment(shardResult);
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
    public java.util.List<Answer> search(final double[] vec, int maxNumMatches) {

        return ANSWER_ORDERING.greatestOf(
                Futures.ofAll(
                        Arrays.stream(searchShards)
                                .map(shard -> CompletableFuture.supplyAsync(() -> shard.search(vec, maxNumMatches), searchExecuter))
                                .collect(Collectors.toList())
                ).join()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()),
                maxNumMatches);

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