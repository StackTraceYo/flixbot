package org.stacktrace.yo.flixbot.vector.search.text;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class InvertedIndex {

    public static class DocumentCorpus {
        private final List<Document.ParsedDocument> documents = Lists.newArrayList();
    }

    public static class DocumentGroup {
        private final String groupTerm;
        private final List<Document.ParsedDocument> documents = Lists.newArrayList();

        public DocumentGroup(String term) {
            this.groupTerm = term;
        }
    }

    private final DocumentCorpus corpus;
    private final Map<String, DocumentGroup> termGroup;

    public InvertedIndex(DocumentCorpus corpus) {
        Map<String, DocumentGroup> builder = Maps.newHashMap();
        for (Document.ParsedDocument doc : corpus.documents) {
            for(Document.Term term : doc.terms){
                String word = term.word;
                if(!builder.containsKey(word)){
                    builder.put(word, new DocumentGroup(word));
                }
                builder.get(word).documents.add(doc);
            }
        }
        this.termGroup = ImmutableMap.copyOf(builder);
        this.corpus = corpus;
    }
}
