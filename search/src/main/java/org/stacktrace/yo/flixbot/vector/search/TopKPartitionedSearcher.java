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

import static org.stacktrace.yo.flixbot.vector.search.TopKShardedSearcher.collectAnswers;

public class TopKPartitionedSearcher implements Searcher<double[]> {


    private final AnswerQueue queue;
    private final KeyedVectors kv;
    private final int partitions;
    private final Executor ex;
    private final int k;
    private final Scorer scorer;

    public TopKPartitionedSearcher(KeyedVectors kv, Scorer scorer, int k, int partitions, Executor ex) {
        this.k = k;
        this.kv = kv;
        this.queue = new AnswerQueue(k );
        this.partitions = partitions;
        this.ex = ex;
        this.scorer = scorer;
    }

    @Override
    public List<Search.Answer> search(double[] vec) {
        return Future.traverse(ex, partitions(), p -> partitionSearch(p, vec)).map(this::topN).get();
    }
    
    private Iterator<TopKPartition> partitions() {
        return io.vavr.collection.List.of(kv.keys())
                .grouped(partitions)
                .map(TopKPartition::new);
    }

    private Future<List<Search.Answer>> partitionSearch(TopKPartition partition, double[] vec) {
        return Future.of(ex, () -> partition.search(vec));
    }

    private List<Search.Answer> topN(Seq<List<Search.Answer>> shardAnswers) {
        return collectAnswers(shardAnswers, queue, k);
    }


     class TopKPartition implements Searcher<double[]> {


        private final AnswerQueue pq;
        private final io.vavr.collection.List<String> keys;

        private TopKPartition(io.vavr.collection.List<String> keys) {
            this.keys = keys;
            this.pq = new AnswerQueue(k);
        }

        @Override
        public List<Search.Answer> search(double[] vec) {

            for (String other : keys) {
                double[] ov = kv.keyVector(other);
                double d = scorer.score(ov, vec);
                pq.insertWithOverflow(new Search.Answer(other, d));
            }

            Search.Answer[] top = new Search.Answer[k];
            for (int i = k - 1; i >= 0; i--) {
                top[i] = pq.pop();
            }
            return Lists.newArrayList(top);
        }

    }


}
