package org.stacktrace.yo.flixbot.commons;

import org.stacktrace.yo.flixbot.search.Search;

import java.util.function.Supplier;

public class AnswerQueue extends PriorityQueue<Search.Answer> {

    private static final Supplier<Search.Answer> sentinelSupplier = () -> new Search.Answer("", -1.0);

    public AnswerQueue(int maxSize) {
        super(maxSize, sentinelSupplier);
    }

    @Override
    protected boolean lessThan(Search.Answer a, Search.Answer b) {
        return a.score < b.score;
    }
}
