package com.seko.simple;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("NDIxOTUyNTc3YmI0", "YTZhMTBmYjllN2M0"));
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("es-bss-oma-elasticsearch.paas-apps8.openshift.sdntest.netcracker.com", 80))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider));
        RestHighLevelClient client = new RestHighLevelClient(builder);

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest("*-demo*");
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(500);
        searchSourceBuilder.fetchSource(null, new String[]{"error.stackTrace", "error.source.content"});
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        try (BufferedWriter b = new BufferedWriter(new FileWriter("./result.json"))) {

            while (searchHits != null && searchHits.length > 0) {
                StringBuilder stringBuilder = new StringBuilder("_bulk\n");
                for (SearchHit searchHit : searchHits) {
                    String id = searchHit.getId();
                    String index = searchHit.getIndex();
                    String type = "doc";
                    String header = String.format("{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\", \"_id\" : \"%s\" } }", index, type, id);
                    stringBuilder.append(header);
                    stringBuilder.append("\n");
                    stringBuilder.append(searchHit.getSourceAsString());
                    stringBuilder.append("\n");
                }
                String result = stringBuilder.toString();
                b.write(result);
                b.write("\n\n\n");

                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
            }
        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();
        System.out.println("ВСЕ!");
    }
}
