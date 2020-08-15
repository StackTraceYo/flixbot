package org.stacktrace.yo.flixbot.vector.search;

import com.google.common.collect.Lists;
import org.stacktrace.yo.flixbot.commons.AnswerQueue;
import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors;
import org.stacktrace.yo.flixbot.vector.scoring.Scorer;

import java.util.List;

public class TopKSearcher extends VectorSearcher {


    private final AnswerQueue queue;
    private final KeyedVectors kv;

    public TopKSearcher(KeyedVectors kv, Scorer scorer, int k) {
        super(scorer, k);
        this.kv = kv;
        this.queue = new AnswerQueue(k);
    }

    public void reset() {
        vec = null;
        queue.clear();
    }

    public List<KeyedVectors.Answer> search() {

        for (String other : kv.keys()) {
            double[] ov = kv.keyVector(other);
            double d = scorer.score(ov, vec);
            queue.insertWithOverflow(new KeyedVectors.Answer(other, d));
        }

        KeyedVectors.Answer[] top = new KeyedVectors.Answer[k];
        for (int i = k - 1; i >= 0; i--) {
            top[i] = queue.pop();
        }
        return Lists.newArrayList(top);
    }

}
