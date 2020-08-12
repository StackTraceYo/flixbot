package org.stacktrace.yo.flixbot.search;

import com.google.common.collect.ImmutableMap;
import org.stacktrace.yo.flixbot.KeyedVectors;

public class AllInOneVectorSearch extends VectorSearchShard {


    public AllInOneVectorSearch(final KeyedVectors vectors) {
        super(vectors.vectors(), vectors.keyOffsets(), vectors.layerSize());
    }

}