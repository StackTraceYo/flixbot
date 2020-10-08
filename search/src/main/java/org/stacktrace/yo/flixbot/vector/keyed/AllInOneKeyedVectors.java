package org.stacktrace.yo.flixbot.vector.keyed;

import org.stacktrace.yo.flixbot.vector.io.KeyedVectorData;

public class AllInOneKeyedVectors extends KeyedVectorShard {


    public AllInOneKeyedVectors(final KeyedVectorData vectors) {
        super(vectors.vectors(), vectors.keyOffsets(), vectors.layerSize());
    }

}