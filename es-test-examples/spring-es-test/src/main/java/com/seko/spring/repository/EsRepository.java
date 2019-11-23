package com.seko.spring.repository;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class EsRepository {
    private final RestHighLevelClient client;

    @Autowired
    public EsRepository(RestHighLevelClient client) {
        this.client = client;
    }

    public IndexResponse createDoc(Map<String, Object> doc, String id) throws IOException {
        IndexRequest indexRequest = new IndexRequest();
        indexRequest.source(doc);
        indexRequest.index("my_index");
        indexRequest.type("doc");
        indexRequest.id(id);
        return client.index(indexRequest, RequestOptions.DEFAULT);
    }
}
