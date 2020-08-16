package org.stacktrace.yo.flixbot.vector.search;

import org.stacktrace.yo.flixbot.search.Search;

import java.util.List;

public interface KeyedSimilarity {

    public Search.Result mostSimilar(String key);


    public Search.Result mostSimilar(double[] vec);

    public Search.Result mostSimilar(List<String> keys);

    public Search.Result mostSimilar(List<String> pos, List<String> neg);

    public Search.Result mostSimilar(double[] pos, double[] neg);

}
