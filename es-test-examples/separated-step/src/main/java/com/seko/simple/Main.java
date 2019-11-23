package com.seko.simple;

import com.seko.simple.repository.EsRepository;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class Main {
    public static void main(String[] args) {
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));

        EsRepository esRepository = new EsRepository(restHighLevelClient);
        loadData(esRepository);
    }

    private static void loadData(EsRepository esRepository) {
        // some logic
    }
}
