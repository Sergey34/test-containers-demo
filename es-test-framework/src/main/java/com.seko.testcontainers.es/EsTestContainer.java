package com.seko.testcontainers.es;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EsTestContainer extends ElasticsearchContainer {
    private static final Gson gson = new Gson();
    private static final Type esDocMapType = new TypeToken<Map<String, Object>>() {
    }.getType();
    public static final String DOCKER_ELASTIC_DEFAULT = "docker.elastic.co/elasticsearch/elasticsearch-oss:6.4.3";
    public static final String DEFAULT_INDEX_TEMPLATE_PATH = "./templates";
    public static final String CONTAINER_NAME = "CONTAINER_NAME";
    public static final String INDEX_TEMPLATE_PATH_CONFIG = "INDEX_TEMPLATE_PATH";
    private static final String DEFAULT_DATA_PATH = "./data";
    public static final String DATA_PATH_CONFIG = "DATA_PATH";
    private RestHighLevelClient client;
    private BulkProcessor bp;

    public EsTestContainer() {
        super(getImageName());
    }

    public EsTestContainer(String dockerImageName) {
        super(dockerImageName);
    }

    public static String getImageName() {
        return System.getProperty(CONTAINER_NAME, DOCKER_ELASTIC_DEFAULT);
    }

    public RestHighLevelClient getClient() {
        if (this.client == null) {
            String httpHostAddress = getHttpHostAddress();
            String[] hostAndPort = httpHostAddress.split(":");
            this.client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(hostAndPort[0], Integer.parseInt(hostAndPort[1]), "http")));
        }
        return client;
    }

    public BulkProcessor getBulkProcessor() {
        if (this.bp == null) {
            BulkProcessor.Listener listener = new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId, BulkRequest request) {

                }

                @Override
                public void afterBulk(long executionId, BulkRequest request,
                                      BulkResponse response) {

                }

                @Override
                public void afterBulk(long executionId, BulkRequest request,
                                      Throwable failure) {

                }
            };

            this.bp = BulkProcessor.builder(
                    (request, bulkListener) ->
                            getClient().bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                    listener)
                    .setConcurrentRequests(0)
                    .build();

        }
        return bp;
    }

    public String getHost() {
        return this.getContainerIpAddress();
    }

    public Integer getPort() {
        return this.getMappedPort(9200);
    }

    protected String getIndexTemplatePath() {
        return System.getProperty(INDEX_TEMPLATE_PATH_CONFIG, DEFAULT_INDEX_TEMPLATE_PATH);
    }

    protected String getDataPath() {
        return System.getProperty(DATA_PATH_CONFIG, DEFAULT_DATA_PATH);
    }

    public void uploadIndexTemplates() {
        String indexTemplatePath = getIndexTemplatePath();
        List<File> files = loadFiles(indexTemplatePath);
        files.stream()
                .map(it -> Pair.of(it.getName().split("\\.json")[0], getContent(it)))
                .forEach(this::addTemplate);
    }

    private void addTemplate(Pair<String, String> template) {
        PutIndexTemplateRequest request = new PutIndexTemplateRequest(template.getKey())
                .create(true)
                .source(template.getValue(), XContentType.JSON);
        try {
            getClient().indices().putTemplate(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getContent(File file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file.toURI())));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<File> loadFiles(String directory) {
        File dir = new File(directory);
        return loadFiles(dir);
    }

    private List<File> loadFiles(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalStateException(dir.getAbsolutePath() + " is not directory");
        }
        List<File> result = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(loadFiles(file));
            } else {
                result.add(file);
            }
        }
        return result;
    }

    public BulkResponse uploadData() {
        String dataPath = getDataPath();
        List<File> files = loadFiles(dataPath);
        BulkRequest br = new BulkRequest();
        br.timeout(TimeValue.timeValueMinutes(10));

        files.stream()
                .map(this::getContent)
                .map(this::getRequest)
                .forEach(br::add);
        return applyBulk(br);
    }

    private BulkResponse applyBulk(BulkRequest br) {
        try {
            return getClient().bulk(br, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private IndexRequest getRequest(String jsonDoc) {
        Map<String, Object> map = gson.fromJson(jsonDoc, esDocMapType);
        IndexRequest request = new IndexRequest(map.get("_index").toString(), map.get("_type").toString(), map.get("_id").toString());
        request.source((Map) map.get("_source"));
        return request;
    }

    public void uploadAll() {
        uploadIndexTemplates();
        uploadData();
    }
}
