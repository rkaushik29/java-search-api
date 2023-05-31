package com.example;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.Query;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SearchAPI {
    private static final String INDEX_PATH = "../../../resources/cranfield";

    public static void main(String[] args) throws Exception {
        Javalin app = Javalin.create().start(5000);

        app.get("/search", SearchAPI::handleSearchRequest);
    }

    private static Query createQuery(String queryString) throws ParseException {
        String fieldName = "content"; // Replace with your field name

        QueryParser queryParser = new QueryParser(fieldName, new StandardAnalyzer());
        Query query = queryParser.parse(queryString);

        return query;
    }

    private static void handleSearchRequest(Context context) throws Exception {
        Query query = createQuery(context.queryParam("q"));

        // Create an IndexSearcher
        FSDirectory indexDir = FSDirectory.open(Paths.get(INDEX_PATH));
        DirectoryReader reader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity());

        // Perform the search
        TopDocs topDocs = searcher.search(query, 10);
        ScoreDoc[] hits = topDocs.scoreDocs;

        // Collect the search results
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document document = searcher.doc(docId);
            documents.add(document);
        }

        // Prepare the response
        SearchResult result = new SearchResult(documents);
        context.json(result);

        // Close the IndexReader
        reader.close();
    }

    static class SearchResult {
        private List<Document> results;

        public SearchResult(List<Document> results) {
            this.results = results;
        }

        public List<Document> getResults() {
            return results;
        }
    }
}