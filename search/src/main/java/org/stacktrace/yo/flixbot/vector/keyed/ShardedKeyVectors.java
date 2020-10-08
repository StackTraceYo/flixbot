package org.stacktrace.yo.flixbot.vector.keyed;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.math3.stat.descriptive.moment.VectorialMean;
import org.stacktrace.yo.flixbot.commons.ArrayUtil;
import org.stacktrace.yo.flixbot.vector.io.KeyedVectorData;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SuppressWarnings("UnstableApiUsage")
public class ShardedKeyVectors extends KeyedVectors {


    protected final KeyedVectorShard[] shards;
    private final HashFunction hashFunction = Hashing.murmur3_32(5390);

    public ShardedKeyVectors(final KeyedVectorData keyedVectors, final int shards) {
        super(keyedVectors.layerSize());
        this.shards = new KeyedVectorShard[shards];

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
        searchShardData.forEachWithIndex((vectorSearchShardData, value) -> this.shards[value] = vectorSearchShardData.asShard());
    }

    public Boolean contains(String word) {
        return Arrays.stream(shards).anyMatch(s -> s.contains(word));
    }

    public int getShard(String key) {
        return Math.max(0, Hashing.consistentHash(hashFunction.hashString(key, StandardCharsets.UTF_8), shards.length) - 1);
    }

    public KeyedVectorShard shardForKey(String key) {
        return shards[getShard(key)];
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
    public String[] keys() {
        String[][] s = new String[shards.length][];
        for (int i = 0, searchShardsLength = shards.length; i < searchShardsLength; i++) {
            s[i] = shards[i].keys;
        }
        return ArrayUtil.concat(s);
    }

    public KeyedVectorShard[] shards() {
        return this.shards;
    }
}