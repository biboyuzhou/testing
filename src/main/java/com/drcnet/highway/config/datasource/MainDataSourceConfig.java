package com.drcnet.highway.config.datasource;

import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tk.mybatis.mapper.autoconfigure.MybatisProperties;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: penghao
 * @CreateTime: 2019/9/2 15:07
 * @Description: 主数据源配置，主数据源为动态数据源，副数据源将注入主数据源
 */
//@Configuration
public class MainDataSourceConfig {


    @Bean(name = "dynamicDataSource")
    public DynamicDataSourceConfig dynamicDataSourceConfig(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                                           @Qualifier("secondDataSource") DataSource secondDataSource,
                                                           @Qualifier("ylDataSource") DataSource ylDataSource,
                                                           @Qualifier("ndlDataSource") DataSource ndlDataSource,
                                                           @Qualifier("bgyDataSource") DataSource bgyDataSource,
                                                           @Qualifier("czlDataSource") DataSource czlDataSource,
                                                           @Qualifier("cmfxDataSource") DataSource cmfxDataSource,
                                                           @Qualifier("mnDataSource") DataSource mnDataSource,
                                                           @Qualifier("nwrDataSource") DataSource nwrDataSource,
                                                           @Qualifier("xgDataSource") DataSource xgDataSource,
                                                           @Qualifier("yxDataSource") DataSource yxDataSource,
                                                           @Qualifier("zlDataSource") DataSource zlDataSource) {
        Map<Object, Object> targetDataSource = new HashMap<>();
        targetDataSource.put(DataSourceType.DataSourceEnum.PRIMARY, primaryDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.SECOND, secondDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.YL, ylDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.NDL, ndlDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.BGY, bgyDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.CZL, czlDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.CMFX, cmfxDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.MN, mnDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.NWR, nwrDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.XG, xgDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.YX, yxDataSource);
        targetDataSource.put(DataSourceType.DataSourceEnum.ZL, zlDataSource);
        DynamicDataSourceConfig dataSource = new DynamicDataSourceConfig();
        dataSource.setTargetDataSources(targetDataSource);
        dataSource.setDefaultTargetDataSource(primaryDataSource);
        return dataSource;
    }

    @Bean(name = "SqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dynamicDataSource") DataSource dynamicDataSource,
                                               MybatisProperties mybatisProperties,
                                               @Qualifier("pageHelperProperties") Properties pageHelperProperties)
            throws Exception {
        SqlSessionFactoryBean bean = DataSourceConfig.buildSqlSessionFactory(dynamicDataSource,mybatisProperties);
        Interceptor pageInterceptor = new PageInterceptor();
        pageInterceptor.setProperties(pageHelperProperties);
        bean.setPlugins(new Interceptor[]{pageInterceptor});
        return bean.getObject();
    }

    @Bean("pageHelperProperties")
    @ConfigurationProperties(prefix = "pagehelper")
    public Properties pageHelperProperties() {
        return new Properties();
    }

}
