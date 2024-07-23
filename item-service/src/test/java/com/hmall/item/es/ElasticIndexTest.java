package com.hmall.item.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class ElasticIndexTest {
    private RestHighLevelClient restHighLevelClient;

    @BeforeEach
    void setUp() {
         restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                 HttpHost.create("http://192.168.21.132:9200")
         ));
    }

    @AfterEach
    void tearDown() {
        if (restHighLevelClient != null) {
            try {
                restHighLevelClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testConnection() {
        // 测试连接
        System.out.println(restHighLevelClient);
    }
    String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\":\n" +
            "      {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"price\":\n" +
            "      {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"images\":\n" +
            "      {\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\":\n" +
            "      {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\":\n" +
            "      {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\":\n" +
            "      {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\":\n" +
            "      {\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"isAD\":\n" +
            "      {\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\":\n" +
            "      {\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    @Test
    void testCreateIndex() throws IOException {
        // 创建request对象
        CreateIndexRequest request = new CreateIndexRequest("items");
        // 发送请求
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteIndex() throws IOException {
        // 删除索引
        DeleteIndexRequest request = new DeleteIndexRequest("items");
        restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testExistIndex() {
        GetIndexRequest request = new GetIndexRequest("items");
        try {
            boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
            System.out.println(exists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testGetIndex() {
        GetIndexRequest request = new GetIndexRequest("items");
        try {
            GetIndexResponse response = restHighLevelClient.indices().get(request, RequestOptions.DEFAULT);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
