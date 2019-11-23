package com.seko.testcontainers.es;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

public class EsTestContainerTest {
    @ClassRule
    public static EsTestContainer esTestContainer = new EsTestContainer();

    @Test
    public void getClient() {
        Assert.assertNotNull(esTestContainer.getClient());
    }

    @Test
    public void uploadIndexTemplates() throws IOException {
        String path = "src/test/resources/templates";
        System.setProperty(EsTestContainer.INDEX_TEMPLATE_PATH_CONFIG, path);
        esTestContainer.uploadIndexTemplates();
        GetIndexTemplatesRequest getIndexTemplatesRequest = new GetIndexTemplatesRequest("test_template");
        GetIndexTemplatesResponse template = esTestContainer.getClient().indices().getTemplate(getIndexTemplatesRequest, RequestOptions.DEFAULT);
        Assert.assertTrue(template.getIndexTemplates().stream().anyMatch(it -> it.getName().equals("test_template")));
    }

    @Test
    public void uploadData() throws IOException {
        String path = "src/test/resources/data";
        System.setProperty(EsTestContainer.DATA_PATH_CONFIG, path);
        esTestContainer.uploadData();

        refresh();

        SearchRequest searchRequest = new SearchRequest("order-*");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.fetchSource(false);
        searchRequest.source(searchSourceBuilder);
        long search = esTestContainer.getClient().search(searchRequest, RequestOptions.DEFAULT).getHits().totalHits;
        Assert.assertEquals(2, search);
    }

    private void refresh(String... indexes) throws IOException {
        RefreshRequest request = new RefreshRequest(indexes);
        esTestContainer.getClient().indices().refresh(request, RequestOptions.DEFAULT);
    }
}