package com.drcnet.highway.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @Author: penghao
 * @CreateTime: 2019/9/2 16:51
 * @Description: 动态数据源切换配置
 */
public class DynamicDataSourceConfig extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceType.get();
    }
}
