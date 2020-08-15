package org.stacktrace.yo.flixbot.vector.scoring;

public interface Scorer {

    public double score(double[] vec, double[] ov);

}
