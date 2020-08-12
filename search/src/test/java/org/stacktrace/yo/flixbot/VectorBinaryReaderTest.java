package org.stacktrace.yo.flixbot;

import io.vavr.collection.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class VectorBinaryReaderTest {

    @Test
    public void testBinaryReader() throws Exception {
        KeyedVectorsBuffers kv = KeyedVectorIO.readBinary("/Users/ahmad/Desktop/f_m2v_50_300_10_w2v_format");
//        KeyedVectorsBuffers kv = KeyedVectorBinaryReader.readBinary(getClass().getResource("/test_word_embeddings").getPath());

        Assertions.assertEquals(15662, kv.keys().length);
        Assertions.assertEquals(100, kv.layerSize());

        Charset charset = StandardCharsets.UTF_8;
        Stream.of("wine", "flavors", "fruit", "finish")
                .zip(Stream.range(0, 4).map(i -> kv.keys()[i]).map(buffer -> charset.decode(buffer).toString()))
                .forEach(pair -> {
                    Assertions.assertEquals(pair._1, pair._2);
                });

        Stream.of(0.063834615f, 0.09068402f, -0.44066244f, 2.263171f)
                .zip(Stream.range(0, 4).map(i -> kv.vectors()[i]).map(ByteBuffer::getFloat))
                .forEach(pair -> {
                    Assertions.assertEquals(pair._1, pair._2);
                });


    }

}