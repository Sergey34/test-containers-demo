package com.seko.spring.repository;

import com.seko.testcontainers.es.EsTestContainer;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class EsRepositoryTest {
    @ClassRule
    public static EsTestContainer esTestContainer = new EsTestContainer();

    @Test
    public void createDocTestWithoutSpring() throws IOException {
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

    @Test
    public void createDocTestWithSpring() throws IOException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.seko.spring");

        EsRepository esRepository = context.getBean("esRepository", EsRepository.class);

        Map<String, Object> doc = new HashMap<>();
        doc.put("field_1", "value_1");
        doc.put("field_2", "value_2");
        doc.put("field_3", "value_3");
        esRepository.createDoc(doc, "1");

        GetResponse esDoc = esTestContainer.getClient().get(new GetRequest("my_index", "doc", "1"), RequestOptions.DEFAULT);
        Assert.assertEquals(doc, esDoc.getSourceAsMap());
    }

    @Bean
    @Primary
    public RestHighLevelClient testEsRepository() {
        String host = esTestContainer.getHost();
        Integer port = esTestContainer.getPort();
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
    }
}