package com.drcnet.highway.config;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * @Author jack
 * @Date: 2019/10/12 14:39
 * @Desc:
 **/
@Configuration
public class EsConfig {
    // 集群地址，多个用,隔开
    @Value("${es.hosts}")
    private String es_hosts;
    @Value("${es.port}")
    private Integer es_port;

    private String schema = "http"; // 使用的协议
    private ArrayList<HttpHost> hostList = null;

    private int connectTimeOut = 1000; // 连接超时时间
    private int socketTimeOut = 30000; // 连接超时时间
    private int connectionRequestTimeOut = 500; // 获取连接的超时时间

    private int maxConnectNum = 100; // 最大连接数
    private int maxConnectPerRoute = 100; // 最大路由连接数

    @Bean
    public RestHighLevelClient client() {
        hostList = new ArrayList<>();
        String[] hostStrs = es_hosts.split(",");
        for (String host : hostStrs) {
            hostList.add(new HttpHost(host, es_port, schema));
        }
        RestClientBuilder builder = RestClient.builder(hostList.toArray(new HttpHost[0]));
        // 异步httpclient连接延时配置
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(connectTimeOut);
                requestConfigBuilder.setSocketTimeout(socketTimeOut);
                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
                return requestConfigBuilder;
            }
        });
        // 异步httpclient连接数配置
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.setMaxConnTotal(maxConnectNum);
                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                return httpClientBuilder;
            }
        });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
