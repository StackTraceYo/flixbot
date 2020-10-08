package org.stacktrace.yo.flixbot.vector.search;

import org.stacktrace.yo.flixbot.search.Search;
import org.stacktrace.yo.flixbot.search.Searcher;
import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors;

import java.util.Collections;
import java.util.List;

public class KeyedSimilaritySearch implements KeyedSimilarity {

    private final KeyedVectors kv;
    private final Searcher<double []> searcher;

    public KeyedSimilaritySearch(KeyedVectors kv, Searcher<double[]> searcher) {
        this.kv = kv;
        this.searcher =  searcher;
    }

    public Search.Result mostSimilar(String key) {
        double[] vector = kv. keyVector(key);
        if (null != vector) {
            return mostSimilar(vector);
        }
        return new Search.Result(Collections.emptyList());
    }


    public Search.Result mostSimilar(double[] vec) {
        return new Search.Result(searcher.search(vec));
    }

    public Search.Result mostSimilar(List<String> keys) {
        double[] mean = kv.mean(keys, kv.layerSize);
        return mostSimilar(mean);
    }

    public Search.Result mostSimilar(List<String> pos, List<String> neg) {
        double[] mean = kv.mean(pos, neg);
        return mostSimilar(mean);
    }

    public Search.Result mostSimilar(double[] pos, double[] neg) {
        double[] mean = kv.mean(pos, neg);
        return mostSimilar(mean);
    }



}
