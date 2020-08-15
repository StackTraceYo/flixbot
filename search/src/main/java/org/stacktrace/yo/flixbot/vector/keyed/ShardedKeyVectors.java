package org.stacktrace.yo.flixbot.vector.keyed;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;
import org.stacktrace.yo.flixbot.vector.io.KeyedVectorData;
import org.stacktrace.yo.flixbot.commons.ArrayUtil;
import org.stacktrace.yo.flixbot.vector.search.TopKShardedSearcher;
import org.stacktrace.yo.flixbot.vector.search.VectorSearcher;
import org.stacktrace.yo.flixbot.vector.scoring.CosineScorer;
import org.stacktrace.yo.flixbot.vector.scoring.Scorer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("UnstableApiUsage")
public class ShardedKeyVectors extends KeyedVectors {


    protected final KeyedVectorShard[] searchShards;
    private final HashFunction hashFunction = Hashing.murmur3_32(5390);
    private final Executor searchExecutor;
    protected Scorer cosineScoring = new CosineScorer();

    public ShardedKeyVectors(final KeyedVectorData keyedVectors, final int shards, Executor searchExecutor) {
        super(keyedVectors.layerSize());
        this.searchShards = new KeyedVectorShard[shards];
        this.searchExecutor = searchExecutor;

        double[][] vectors = keyedVectors.vectors();
        String[] keys = keyedVectors.keys();

        io.vavr.collection.List<KeyedVectorShard.VectorSearchShardData> searchShardData = io.vavr.collection.List.range(0, shards)
                .map(s -> new KeyedVectorShard.VectorSearchShardData(layerSize))
                .toList();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            int shard = getShard(key);
            searchShardData.get(shard).addVector(key, vectors[i]);
        }
        searchShardData.forEachWithIndex((vectorSearchShardData, value) -> searchShards[value] = vectorSearchShardData.asShard());
    }

    public ShardedKeyVectors(final KeyedVectorData keyedVectors, final int shards) {
        this(keyedVectors, shards, Executors.newFixedThreadPool(shards));
    }

    public Boolean contains(String word) {
        return Arrays.stream(searchShards).anyMatch(s -> s.contains(word));
    }

    public int getShard(String key) {
        return Math.max(0, Hashing.consistentHash(hashFunction.hashString(key, StandardCharsets.UTF_8), searchShards.length) - 1);
    }

    public KeyedVectorShard shardForKey(String key) {
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
    public VectorSearcher searcher(int top) {
        return new TopKShardedSearcher(searchShards, cosineScoring, top, searchExecutor);
    }

    @Override
    public int layerSize() {
        return this.layerSize;
    }

    @Override
    public String[] keys() {
        String[][] s = new String[searchShards.length][];
        for (int i = 0, searchShardsLength = searchShards.length; i < searchShardsLength; i++) {
            s[i] = searchShards[i].keys;
        }
        return ArrayUtil.concat(s);
    }
}