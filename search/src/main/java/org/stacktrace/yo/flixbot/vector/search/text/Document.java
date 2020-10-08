package org.stacktrace.yo.flixbot.vector.search.text;

import java.util.List;
import java.util.Map;

public class Document {

    int id;
    String key;
    String raw;


    public static class ParsedDocument {

        List<Term> terms;
        int id;
        String key;
        Map<String, Integer> freq;

    }

    public static class Term {

        String word;
        int pos;
    }

}
