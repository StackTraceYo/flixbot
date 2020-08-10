package org.stacktrace.yo.flixbot;


import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.stacktrace.yo.flixbot.KeyedVectorBinaryReader.readBinary;

public class KeyedVectors {


    public static KeyedVectors getKeyedVectors(String dir) throws Exception {
        return l2Norm(readBinary(dir));
    }

    public static KeyedVectors getKeyedVectors(File dir) throws Exception {
        return l2Norm(readBinary(dir));
    }

    private final ByteBuffer[] keys;
    private final double[][] vectors;
    private final int layerSize;

    private KeyedVectors(ByteBuffer[] keys, double[][] vectors, int layerSize) {
        this.layerSize = layerSize;
        this.keys = keys;
        this.vectors = vectors;
    }

    public ByteBuffer[] keys() {
        return keys;
    }

    public double[][] vectors() {
        return vectors;
    }

    public int layerSize() {
        return layerSize;
    }


    private static KeyedVectors l2Norm(KeyedVectorsBuffers buffers) {
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
        return new KeyedVectors(keys, vectors, buffers.layerSize());
    }

}
