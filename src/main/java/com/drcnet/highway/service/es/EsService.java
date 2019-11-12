package com.drcnet.highway.service.es;

import com.drcnet.highway.constants.EsIndexConsts;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author jack
 * @Date: 2019/10/12 13:28
 * @Desc:
 **/
@Service
@Slf4j
public class EsService {

    @Autowired
    private RestHighLevelClient client;

    public boolean addData(IndexRequest request) throws IOException {
        try {
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            int status = response.status().getStatus();
            if (status == HttpStatus.OK.value() || status == HttpStatus.CREATED.value()) {
                if (log.isInfoEnabled()) {
                    log.info("保存或更新成功");
                }
                return true;
            } else {
                if (log.isInfoEnabled()) {
                    log.info("保存或更新失败，原因：{}", response.toString());
                }
                return false;
            }
        } catch (Exception e) {
            log.error("保存或更新失败", e);
            throw e;
        }
    }

    public boolean batchAddData(BulkRequest bulkRequest) throws IOException {
        boolean res = false;
        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            int status = bulkResponse.status().getStatus();
            if (status == HttpStatus.OK.value() || status == HttpStatus.CREATED.value()) {
                if (log.isInfoEnabled()) {
                    log.info("批量保存或更新成功, 该批次数量：{}", bulkRequest.requests().size());
                }
                res = true;
            }
            //todo 失败的处理
            return true;
        } catch (Exception e) {
            log.error("批量保存或更新失败，该批次数量：{}, 本批次数据量大小：{}", bulkRequest.requests().size(), bulkRequest.estimatedSizeInBytes(), e);
            throw e;
        }
    }

    public void search() {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        SearchRequest searchRequest = new SearchRequest(EsIndexConsts.GANTRY_DATA);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("plateNum", "川ALK860"));
        sourceBuilder.from(0);
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.sort("snapshotTime", SortOrder.DESC);
        searchRequest.source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(response.getHits().getHits().length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
