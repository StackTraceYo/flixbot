package org.stacktrace.yo.flixbot.vector.search;

import com.google.common.collect.Lists;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.concurrent.Future;
import org.stacktrace.yo.flixbot.commons.AnswerQueue;
import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors;
import org.stacktrace.yo.flixbot.vector.scoring.Scorer;

import java.util.List;
import java.util.concurrent.Executor;

public class TopKShardedSearcher extends VectorSearcher {


    private final AnswerQueue queue;
    private final KeyedVectors[] kv;
    private final Executor ex;

    public TopKShardedSearcher(KeyedVectors[] kv, Scorer scorer, int k, Executor ex) {
        super(scorer, k);
        this.kv = kv;
        this.queue = new AnswerQueue(k);
        this.ex = ex;
    }

    public void reset() {
        vec = null;
        queue.clear();
    }

    public List<KeyedVectors.Answer> search() {
        return Future.traverse(ex, Iterator.of(kv), this::asyncSearch).map(this::topN).get();
    }

    private Future<List<KeyedVectors.Answer>> asyncSearch(KeyedVectors keyedVectors){
        return Future.of(ex, () -> shardSearch(keyedVectors));
    }

    private List<KeyedVectors.Answer> shardSearch(KeyedVectors keyedVectors){
        VectorSearcher searcher = keyedVectors.searcher(k);
        searcher.forVector(vec);
        return searcher.search();
    }

    private List<KeyedVectors.Answer> topN(Seq<List<KeyedVectors.Answer>> shardAnswers){
        for(List<KeyedVectors.Answer> shardAnswer : shardAnswers){
            for(KeyedVectors.Answer answer : shardAnswer){
                queue.insertWithOverflow(answer);
            }
        }

        KeyedVectors.Answer[] top = new KeyedVectors.Answer[k];
        for (int i = k - 1; i >= 0; i--) {
            top[i] = queue.pop();
        }
        return Lists.newArrayList(top);
    }


}
