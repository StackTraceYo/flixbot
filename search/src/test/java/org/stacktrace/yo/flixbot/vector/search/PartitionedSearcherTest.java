package org.stacktrace.yo.flixbot.vector.search;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.stacktrace.yo.flixbot.search.Search;
import org.stacktrace.yo.flixbot.vector.io.KeyedVectorData;
import org.stacktrace.yo.flixbot.vector.keyed.ShardedKeyVectors;
import org.stacktrace.yo.flixbot.vector.scoring.CosineScorer;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class PartitionedSearcherTest {

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final KeyedVectorData kvd = KeyedVectorData.normalizedVectors(getClass().getResource("/test_word_embeddings").getPath());
    private final ShardedKeyVectors shardedKV = new ShardedKeyVectors(kvd,4);

    PartitionedSearcherTest() throws Exception {
    }

    @AfterAll
    public static void clean(){
        executor.shutdown();
    }

//    @Test
//    public void can_get_top_answers2() throws Exception {
//        ShardedKeyVectors search = new ShardedKeyVectors(KeyedVectorData.vectors("/Users/ahmad/projects/flixbot/search/docs"), 8);
//        ShardedSimilaritySearch searcher = new ShardedSimilaritySearch(search, new CosineScorer(), 10, executor);
//        for (Search.Answer s : searcher.mostSimilar("espionage")
//                .answers) {
//            System.out.println(s.name + " - " + s.score);
//        }
//    }

    @Test
    public void can_get_top_answers() throws Exception {
        KeyedSimilaritySearch searcher = new KeyedSimilaritySearch(
                shardedKV,
                new TopKPartitionedSearcher(
                        shardedKV,
                        new CosineScorer(),
                        11,
                        4,
                        executor
                )
        );
        Search.Result cheese = searcher.mostSimilar("cheese");

        // drop first cause itll be they key
        List<Tuple2<String, Double>> actual = List.ofAll(cheese.answers).map(answer -> Tuple.of(answer.name, answer.score)).drop(1);
        List<Tuple2<String, Double>> expected = List.of(
                Tuple.of("cheeses", 0.5757971077802179),
                Tuple.of("desserts", 0.4875313334880542),
                Tuple.of("cheesy", 0.47023039310483444),
                Tuple.of("salami", 0.45819410307913744),
                Tuple.of("mozzarella", 0.4508893578546861),
                Tuple.of("sardines", 0.43518204170706426),
                Tuple.of("foie", 0.4317561047539808),
                Tuple.of("gouda", 0.42627069254590266),
                Tuple.of("gras", 0.42483496092305334),
                Tuple.of("tortellini", 0.4210070673822508)
        );

        Assertions.assertEquals(expected.length(), actual.length());
        actual.zip(expected).forEach(pair -> {
            Assertions.assertEquals(pair._2._1, pair._1._1);
            Assertions.assertEquals(pair._2._2, pair._1._2);
        });
    }

    @Test
    public void can_get_top_answers_with_multiple_keys() throws Exception {
        KeyedSimilaritySearch searcher = new KeyedSimilaritySearch(
                shardedKV,
                new TopKPartitionedSearcher(
                        shardedKV,
                        new CosineScorer(),
                        12,
                        4,
                        executor
                )
        );
        Search.Result sweetDry = searcher.mostSimilar(Arrays.asList("sweet", "dry"));

        List<Tuple2<String, Double>> actual = List.ofAll(sweetDry.answers).map(answer -> Tuple.of(answer.name, answer.score));

        List<Tuple2<String, Double>> expected = List.of(
                Tuple.of("dry", 0.8759210690736203),
                Tuple.of("sweet", 0.8759210686913993),
                Tuple.of("ripe", 0.6726014817302532),
                Tuple.of("rich", 0.6421434815213293),
                Tuple.of("flavors", 0.6367455592702406),
                Tuple.of("dryness", 0.5947025388971793),
                Tuple.of("infused", 0.5650071633354815),
                Tuple.of("jam", 0.564500017669762),
                Tuple.of("delicious", 0.5526460630655531),
                Tuple.of("fine", 0.5505645427245808),
                Tuple.of("sugared", 0.5377821445638518),
                Tuple.of("complex", 0.5361230977345216)
        );

        Assertions.assertEquals(expected.length(), actual.length());
        actual.zip(expected).forEach(pair -> {
            Assertions.assertEquals(pair._2._1, pair._1._1);
            Assertions.assertEquals(pair._2._2, pair._1._2);
        });
    }

}