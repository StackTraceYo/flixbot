package org.stacktrace.yo.flixbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

public class KeyedVectorIO {

    public static KeyedVectorsBuffers readBinary(File dir) throws Exception {
        return KeyedVectorIO.readBuffer(dir);
    }

    public static KeyedVectorsBuffers readBinary(String dir) throws Exception {
        return KeyedVectorIO.readBinary(new File(dir));
    }

    public static KeyedVectorsBuffers readBuffer(File dir) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(dir);
        FileChannel channel = fileInputStream.getChannel();
        // todo files greater than 2GB
        ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).order(ByteOrder.LITTLE_ENDIAN);

        // KV Header
        StringBuilder sb = new StringBuilder();
        char c = (char) buffer.get();
        while (c != '\n') {
            sb.append(c);
            c = (char) buffer.get();
        }
        String firstLine = sb.toString();
        int index = firstLine.indexOf(' ');
        int vocabSize = Integer.parseInt(firstLine.substring(0, index));
        int layerSize = Integer.parseInt(firstLine.substring(index + 1));

        ByteBuffer[] keys = new ByteBuffer[vocabSize];
        ByteBuffer[] vectors = new ByteBuffer[vocabSize];

        for (int ln = 0; ln < vocabSize; ln++) {
            int keyLength = 0;
            c = (char) buffer.get();
            while (c != ' ') {
                if (c != '\n') {
                    keyLength += 1;
                }
                c = (char) buffer.get();
            }
            // save original positions
            int ogLimit = buffer.limit();
            int ogPosition = buffer.position();

            //set pointers and slice
            buffer.position(ogPosition - keyLength - 1);
            buffer.limit(ogPosition - 1);
            keys[ln] = buffer.slice();
            // reset
            buffer.limit(ogLimit);
            buffer.position(ogPosition);

            int start = buffer.position();
            int end = start + (layerSize * Float.BYTES);
            buffer.limit(end);
            vectors[ln] = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
            buffer.limit(ogLimit);
            buffer.position(end);
        }
        return new KeyedVectorsBuffers(buffer, keys, vectors, layerSize);
    }

    public static byte[] toByteArray(double[] doubleArray) {
        int times = Double.SIZE / Byte.SIZE;
        byte[] bytes = new byte[doubleArray.length * times];
        for (int i = 0; i < doubleArray.length; i++) {
            ByteBuffer.wrap(bytes, i * times, times).putDouble(doubleArray[i]);
        }
        return bytes;
    }

    public static KeyedVectors readVector(String path) throws IOException {
        return readVector(new File(path));
    }

    public static KeyedVectors readVector(File dir) throws IOException {
        FileInputStream fs = new FileInputStream(dir);
        FileChannel channel = fs.getChannel();
        ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        int layerSize = buffer.getInt();
        int vocabSize = buffer.getInt();
        String[] keys = new String[vocabSize];
        double[][] vectors = new double[vocabSize][layerSize];
        for (int ln = 0; ln < vocabSize; ln++) {
            StringBuilder key = new StringBuilder();
            char c = (char) buffer.get();
            while (c != ' ') {
                if (c != '\n') {
                    key.append(c);
                }
                c = (char) buffer.get();
            }
            double[] vector = new double[layerSize];
            DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
            doubleBuffer.get(vector);
            keys[ln] = key.toString();
            vectors[ln] = vector;
            buffer.position(buffer.position() + Double.BYTES * layerSize);
        }
        return new KeyedVectors(keys, vectors, layerSize);
    }

    public static void writeVectors(KeyedVectors kv, String path, Predicate<String> filter) throws IOException {
        String[] keys = kv.keys();
        double[][] vectors = kv.vectors();
        FileOutputStream fs = new FileOutputStream(new File(path));
        FileChannel channel = fs.getChannel();

        ByteBuffer header = ByteBuffer.allocate(Integer.BYTES);
        header.putInt(0, kv.layerSize());
        channel.write(header);
        header.putInt(0, keys.length);
        channel.write(header);

        int wrote = 0;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (filter.test(key)) {
                fs.write(key.getBytes(StandardCharsets.UTF_8));
                fs.write(' ');
                fs.write(toByteArray(vectors[i]));
                fs.write('\n');
                wrote++;
            }
        }
        header.putInt(0, wrote).rewind();
        channel.write(header, 4);

    }
}
