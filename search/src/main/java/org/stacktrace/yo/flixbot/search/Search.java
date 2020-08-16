package org.stacktrace.yo.flixbot.search;

import java.util.List;

public class Search {

    public static class Result {
        public final List<Answer> answers;

        public Result(List<Answer> answers) {
            this.answers = answers;
        }
    }

    public static class Answer {
        public final String name;
        public final double score;

        public Answer(String name, double score) {
            this.name = name;
            this.score = score;
        }
    }
}
