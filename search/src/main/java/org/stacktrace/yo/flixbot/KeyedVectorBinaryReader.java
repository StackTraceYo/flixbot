package org.stacktrace.yo.flixbot;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class KeyedVectorBinaryReader {

    public static KeyedVectorsBuffers readBinary(File dir) throws Exception {
        return new KeyedVectorBinaryReader(dir).read();
    }

    public static KeyedVectorsBuffers readBinary(String dir) throws Exception {
        return KeyedVectorBinaryReader.readBinary(new File(dir));
    }


    private final File dir;

    private KeyedVectorBinaryReader(File dir) {
        this.dir = dir;
    }

    public KeyedVectorsBuffers read() throws Exception {
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
}
