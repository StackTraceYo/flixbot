package org.stacktrace.yo.flixbot.vector.search;

import com.google.common.collect.Lists;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.concurrent.Future;
import org.stacktrace.yo.flixbot.commons.AnswerQueue;
import org.stacktrace.yo.flixbot.search.Search;
import org.stacktrace.yo.flixbot.search.Searcher;
import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors;
import org.stacktrace.yo.flixbot.vector.scoring.Scorer;

import java.util.List;
import java.util.concurrent.Executor;

public class TopKShardedSearcher implements Searcher<double []> {


    private final AnswerQueue queue;
    private final KeyedVectors[] shards;
    private final Executor ex;
    private final int k;
    private final Scorer scorer;

    public TopKShardedSearcher(KeyedVectors[] shards, Scorer scorer, int k, Executor ex) {
        this.k = k;
        this.shards = shards;
        this.queue = new AnswerQueue(k);
        this.ex = ex;
        this.scorer = scorer;
    }

    @Override
    public List<Search.Answer> search(double[] vec) {
        return Future.traverse(ex, Iterator.of(shards), kv -> asyncSearch(kv, vec)).map(this::topN).get();
    }

    private Future<List<Search.Answer>> asyncSearch(KeyedVectors keyedVectors, double[] vec) {
        return Future.of(ex, () -> shardSearch(keyedVectors, vec));
    }

    private List<Search.Answer> shardSearch(KeyedVectors kv, double[] vec) {
        Searcher searcher = new TopKSearcher(kv, scorer, k);
        return searcher.search(vec);
    }

    private List<Search.Answer> topN(Seq<List<Search.Answer>> shardAnswers) {
        return collectAnswers(shardAnswers, queue, k);
    }

    static List<Search.Answer> collectAnswers(Seq<List<Search.Answer>> shardAnswers, AnswerQueue queue, int k) {
        for (List<Search.Answer> shardAnswer : shardAnswers) {
            for (Search.Answer answer : shardAnswer) {
                queue.insertWithOverflow(answer);
            }
        }

        Search.Answer[] top = new Search.Answer[k];
        for (int i = k - 1; i >= 0; i--) {
            top[i] = queue.pop();
        }
        return Lists.newArrayList(top);
    }


}
