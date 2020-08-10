package org.stacktrace.yo.flixbot;


import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public class KeyedVectorsBuffers {

    private ByteBuffer underlyingBuffer;
    private final ByteBuffer[] keys;
    private final ByteBuffer[] vectors;
    private final int layerSize;

    public KeyedVectorsBuffers(ByteBuffer underlyingBuffer, ByteBuffer[] keys, ByteBuffer[] vectors, int layerSize) {
        this.underlyingBuffer = underlyingBuffer;
        this.layerSize = layerSize;
        this.keys = keys;
        this.vectors = vectors;
    }

    public ByteBuffer[] keys() {
        return keys;
    }

    public ByteBuffer[] vectors() {
        return vectors;
    }

    public int layerSize() {
        return layerSize;
    }

    public void release() {
        if (underlyingBuffer instanceof MappedByteBuffer) {
            sun.misc.Cleaner cleaner = ((DirectBuffer) underlyingBuffer).cleaner();
            cleaner.clean();
        }
        underlyingBuffer = null;
    }
}
