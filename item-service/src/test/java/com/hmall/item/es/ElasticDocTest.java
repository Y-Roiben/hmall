package com.hmall.item.es;



import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticDocTest {
    @Autowired
    private IItemService itemService;
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
    void TestIndexDoc() throws IOException {

        Item item = itemService.getById(317578L);
        ItemDoc itemDoc = BeanUtils.copyProperties(item, ItemDoc.class);
        String jsonStr = JSONUtil.toJsonStr(itemDoc);

        // 准备request
        IndexRequest request = new IndexRequest("item").id(itemDoc.getId());
        // 准备文档
        // 全量更新
        request.source(jsonStr, XContentType.JSON);

        restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void GetTest() {
        GetRequest request = new GetRequest("item", "317578");
        try {
            GetResponse doc = restHighLevelClient.get(request, RequestOptions.DEFAULT);
            String source = doc.getSourceAsString();
            ItemDoc bean = JSONUtil.toBean(source, ItemDoc.class);
            System.out.println(bean);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void DeleteTest() {
        DeleteRequest request = new DeleteRequest("item", "317578");
        try {
            restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void updateTest() {
        UpdateRequest request = new UpdateRequest("item", "317578");
        request.doc("price", 19900);
        try {
            restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void BatchTest() throws IOException {
        int pageNo = 1;
        int pageSize = 500;
        while (true){
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));
            List<Item> records = page.getRecords();
            if (records==null || records.isEmpty()) {
                return;
            }
            // 批量操作
            BulkRequest request = new BulkRequest();
            // 准备请求参数
            for (Item record : records) {
                ItemDoc itemDoc = BeanUtils.copyProperties(record, ItemDoc.class);
                String jsonStr = JSONUtil.toJsonStr(itemDoc);
                IndexRequest indexRequest = new IndexRequest("item").id(itemDoc.getId());
                indexRequest.source(jsonStr, XContentType.JSON);
                request.add(indexRequest);
            }
            // 发送请求
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
        }
    }
}
