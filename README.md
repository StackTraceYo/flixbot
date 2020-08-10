# flixbot
movie/tv recommender


## search index
documents -> corpus -> inverted_index

## doucment parsing
document text -> parsed_document

## document term
word and position

## collection
```
private String word;
private List<DocumentPosting> postings;
private HashSet<ParsedDocument> uniqueDocuments;

```

```
    private ImmutableList<DocumentTerm> documentTerms;
    private ImmutableMap<String, Integer> wordFrequencyMap;
    private ImmutableSet<String> uniqueWords;
    private Object uniqueId;
```

## create index
```
  Map<String, DocumentPostingCollection> termToPostingsMap = new HashMap<>();
        for (ParsedDocument document : corpus.getParsedDocuments()) {
            for (DocumentTerm documentTerm : document.getDocumentTerms()) {
                final String word = documentTerm.getWord();
                if (!termToPostingsMap.containsKey(word)) {
                    termToPostingsMap.put(word, new DocumentPostingCollection(word));
                }
                termToPostingsMap.get(word).addPosting(documentTerm, document);
            }
        }

        termToPostings = ImmutableMap.copyOf(termToPostingsMap);

        //init metrics cache
        Map<ParsedDocument, ParsedDocumentMetrics> metricsMap = new HashMap<>();
        for (ParsedDocument document : corpus.getParsedDocuments()) {
            metricsMap.put(document, new ParsedDocumentMetrics(corpus, document, termToPostings));
        }
        docToMetrics = ImmutableMap.copyOf(metricsMap);
```



```
 List<Document> documents = new ArrayList<>();
    documents.add(new Document("mad", new Integer(1)));
    documents.add(new Document("in pursuit", new Integer(2)));
    documents.add(new Document("abcd", new Integer(3)));
    documents.add(new Document("possession so and", new Integer(4)));

    TextSearchIndex index = SearchIndexFactory.buildIndex(documents);

    String searchTerm = "Mad in pursuit and in possession so";

    SearchResultBatch batch = index.search(searchTerm, 10);
```