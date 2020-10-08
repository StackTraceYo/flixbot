package org.stacktrace.yo.flixbot.vector.io;


import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.stacktrace.yo.flixbot.vector.io.KeyedVectorIO.readBinary;

public class KeyedVectorData {


    public static KeyedVectorData normalizedVectors(String dir) throws Exception {
        return l2Norm(readBinary(dir));
    }

    public static KeyedVectorData normalizedVectors(File dir) throws Exception {
        return l2Norm(readBinary(dir));
    }

    public static KeyedVectorData vectors(String dir) throws Exception {
        return KeyedVectorIO.readVector(dir);
    }

    public static KeyedVectorData vectors(File dir) throws Exception {
        return KeyedVectorIO.readVector(dir);
    }


    private final String[] keys;
    private final double[][] vectors;
    private final int layerSize;

    KeyedVectorData(String[] keys, double[][] vectors, int layerSize) {
        this.layerSize = layerSize;
        this.keys = keys;
        this.vectors = vectors;
    }

    KeyedVectorData(ByteBuffer[] keys, double[][] vectors, int layerSize) {
        this.layerSize = layerSize;
        this.keys = new String[keys.length];
        Charset charset = StandardCharsets.UTF_8;
        for (int i = 0; i < keys.length; i++) {
            this.keys[i] = charset.decode(keys[i]).toString();
        }
        this.vectors = vectors;
    }

    public String[] keys() {
        return keys;
    }

    public ImmutableMap<String, Integer> keyOffsets() {
        final ImmutableMap.Builder<String, Integer> result = ImmutableMap.builder();
        for (int i = 0; i < keys.length; i++) {
            result.put(keys[i], i);
        }
        return result.build();
    }

    public double[][] vectors() {
        return vectors;
    }

    public int layerSize() {
        return layerSize;
    }


    private static KeyedVectorData l2Norm(KeyedVectorsBuffers buffers) {
        ByteBuffer[] original = buffers.vectors();
        ByteBuffer[] originalKeys = buffers.keys();

        double[][] vectors = new double[original.length][];
        ByteBuffer[] keys = new ByteBuffer[originalKeys.length];
        for (int i = 0; i < original.length; i++) {
            double length = 0;
            ByteBuffer duplicate = original[i].duplicate().order(ByteOrder.LITTLE_ENDIAN);
            while (duplicate.hasRemaining()) {
                float aFloat = duplicate.getFloat();
                length += aFloat * aFloat;
            }
            length = Math.sqrt(length);

            double[] normalized = new double[buffers.layerSize()];
            duplicate.rewind();
            int j = 0;
            while (duplicate.hasRemaining()) {
                float aFloat = duplicate.getFloat();
                normalized[j] = (aFloat / length);
                j += 1;
            }
            vectors[i] = normalized;
            ByteBuffer keyDuplicate = originalKeys[i].duplicate();
            ByteBuffer newKey = ByteBuffer.allocateDirect(keyDuplicate.capacity());
            while (keyDuplicate.hasRemaining()) {
                newKey.put(keyDuplicate.get());
            }
            newKey.rewind();
            keys[i] = newKey;
        }
        buffers.release();
        return new KeyedVectorData(keys, vectors, buffers.layerSize());
    }


}
