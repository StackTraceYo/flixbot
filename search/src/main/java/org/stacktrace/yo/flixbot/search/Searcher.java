package org.stacktrace.yo.flixbot.search;

import java.util.List;

public interface Searcher<T> {

    List<Search.Answer> search(T in);


}
