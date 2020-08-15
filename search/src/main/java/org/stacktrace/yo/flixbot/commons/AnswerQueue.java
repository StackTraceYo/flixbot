package org.stacktrace.yo.flixbot.commons;

import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors;

import java.util.function.Supplier;

public class AnswerQueue extends PriorityQueue<KeyedVectors.Answer> {

    private static final Supplier<KeyedVectors.Answer> sentinelSupplier = () -> new KeyedVectors.Answer("", -1.0);

    public AnswerQueue(int maxSize) {
        super(maxSize, sentinelSupplier);
    }

    @Override
    protected boolean lessThan(KeyedVectors.Answer a, KeyedVectors.Answer b) {
        return a.score < b.score;
    }
}
