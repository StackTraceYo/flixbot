package org.stacktrace.yo.flixbot.vector.search;

import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors;
import org.stacktrace.yo.flixbot.vector.scoring.Scorer;

import java.util.List;

public abstract class VectorSearcher {

    protected double[] vec;

    protected final int k;
    protected final Scorer scorer;

    public VectorSearcher(Scorer scorer, int k) {
        this.k = k;
        this.scorer = scorer;
    }

    public void forVector(double[] vec) {
        this.vec = vec;
    }

    public void reset() {
        vec = null;
    }

    public List<KeyedVectors.Answer> search(final double[] vec){
        forVector(vec);
        return search();
    }

    public abstract List<KeyedVectors.Answer> search();
}
