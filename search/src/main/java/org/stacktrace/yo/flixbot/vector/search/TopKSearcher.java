package org.stacktrace.yo.flixbot.vector.search;

import com.google.common.collect.Lists;
import org.stacktrace.yo.flixbot.commons.AnswerQueue;
import org.stacktrace.yo.flixbot.search.Search;
import org.stacktrace.yo.flixbot.search.Searcher;
import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors;
import org.stacktrace.yo.flixbot.vector.scoring.Scorer;

import java.util.List;

public class TopKSearcher implements Searcher<double[]> {


    protected final KeyedVectors kv;
    private final AnswerQueue queue;
    private final int k;
    private final Scorer scorer;

    public TopKSearcher(KeyedVectors kv, Scorer scorer, int k) {
        this.k = k;
        this.scorer = scorer;
        this.kv = kv;
        this.queue = new AnswerQueue(this.k);
    }

    @Override
    public List<Search.Answer> search(double[] vec) {

        for (String other : kv.keys()) {
            double[] ov = kv.keyVector(other);
            double d = scorer.score(ov, vec);
            queue.insertWithOverflow(new Search.Answer(other, d));
        }

        Search.Answer[] top = new Search.Answer[k];
        for (int i = k - 1; i >= 0; i--) {
            top[i] = queue.pop();
        }
        return Lists.newArrayList(top);
    }

}
