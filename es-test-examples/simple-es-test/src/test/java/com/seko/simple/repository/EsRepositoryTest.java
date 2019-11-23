package com.seko.simple.repository;

import com.seko.testcontainers.es.EsTestContainer;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EsRepositoryTest {
    @ClassRule
    public static EsTestContainer esTestContainer = new EsTestContainer();

    @Test
    public void createDoc() throws IOException {
        RestHighLevelClient client = esTestContainer.getClient();
        EsRepository esRepository = new EsRepository(client);

        Map<String, Object> doc = new HashMap<>();
        doc.put("field_1", "value_1");
        doc.put("field_2", "value_2");
        doc.put("field_3", "value_3");
        esRepository.createDoc(doc, "1");

        GetResponse esDoc = client.get(new GetRequest("my_index", "doc", "1"), RequestOptions.DEFAULT);
        Assert.assertEquals(doc, esDoc.getSourceAsMap());
    }
}