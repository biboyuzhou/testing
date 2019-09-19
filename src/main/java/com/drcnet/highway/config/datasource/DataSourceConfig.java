package com.drcnet.highway.config.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tk.mybatis.mapper.autoconfigure.MybatisProperties;

import javax.sql.DataSource;


/**
 * @Author: penghao
 * @CreateTime: 2019/9/2 14:19
 * @Description: 副数据源配置
 */
//@Configuration
public class DataSourceConfig {


    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix="spring.datasource.hikari")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "secondDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.second")
    public DataSource secondDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "ylDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.yl")
    public DataSource ylDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "ndlDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.ndl")
    public DataSource ndlDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "bgyDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.bgy")
    public DataSource bgyDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "czlDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.czl")
    public DataSource czlDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "cmfxDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.cmfx")
    public DataSource cmfxDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "mnDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.mn")
    public DataSource mnDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "nwrDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.nwr")
    public DataSource nwrDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "xgDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.xg")
    public DataSource xgDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "yxDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.yx")
    public DataSource yxDataSource() {
        return DataSourceBuilder.create().build();
    }
    @Bean(name = "zlDataSource")
    @ConfigurationProperties(prefix="muiltidatasource.zl")
    public DataSource zlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "primarySqlSessionFactory")
    public SqlSessionFactory primarySqlSessionFactory(@Qualifier("primaryDataSource") DataSource dataSource,
                                                      MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }
    @Bean(name = "secondSqlSessionFactory")
    public SqlSessionFactory secondSqlSessionFactory(@Qualifier("secondDataSource") DataSource dataSource,
                                                     MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }
    @Bean(name = "ylSqlSessionFactory")
    public SqlSessionFactory ylSqlSessionFactory(@Qualifier("ylDataSource") DataSource dataSource,
                                                 MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }
    @Bean(name = "ndlSqlSessionFactory")
    public SqlSessionFactory ndlSqlSessionFactory(@Qualifier("ndlDataSource") DataSource dataSource,
                                                  MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }
    @Bean(name = "bgySqlSessionFactory")
    public SqlSessionFactory bgySqlSessionFactory(@Qualifier("bgyDataSource") DataSource dataSource,
                                                  MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }
    @Bean(name = "czlSqlSessionFactory")
    public SqlSessionFactory czlSqlSessionFactory(@Qualifier("czlDataSource") DataSource dataSource,
                                                  MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }
    @Bean(name = "cmfxSqlSessionFactory")
    public SqlSessionFactory cmfxSqlSessionFactory(@Qualifier("cmfxDataSource") DataSource dataSource,
                                                   MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }

    @Bean(name = "mnSqlSessionFactory")
    public SqlSessionFactory mnSqlSessionFactory(@Qualifier("mnDataSource") DataSource dataSource,
                                                   MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }

    @Bean(name = "nwrSqlSessionFactory")
    public SqlSessionFactory nwrSqlSessionFactory(@Qualifier("nwrDataSource") DataSource dataSource,
                                                   MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }

    @Bean(name = "xgSqlSessionFactory")
    public SqlSessionFactory xgSqlSessionFactory(@Qualifier("xgDataSource") DataSource dataSource,
                                                   MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }

    @Bean(name = "yxSqlSessionFactory")
    public SqlSessionFactory yxSqlSessionFactory(@Qualifier("yxDataSource") DataSource dataSource,
                                                   MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }

    @Bean(name = "zlSqlSessionFactory")
    public SqlSessionFactory zlSqlSessionFactory(@Qualifier("zlDataSource") DataSource dataSource,
                                                   MybatisProperties mybatisProperties) throws Exception {
        return buildSqlSessionFactory(dataSource,mybatisProperties).getObject();
    }

    static SqlSessionFactoryBean buildSqlSessionFactory(DataSource dataSource,MybatisProperties mybatisProperties) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setConfiguration(mybatisProperties.getConfiguration());
        bean.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mybatisProperties.getMapperLocations()[0]));
        bean.setDataSource(dataSource);
        return bean;
    }


    @Primary
    @Bean("primarySqlSessionTemplate")
    public SqlSessionTemplate primarySqlSessionTemplate(SqlSessionFactory primarySqlSessionFactory) {
        return new SqlSessionTemplate(primarySqlSessionFactory);
    }
    @Bean("secondSqlSessionTemplate")
    public SqlSessionTemplate secondSqlSessionTemplate(@Qualifier("secondSqlSessionFactory") SqlSessionFactory secondSqlSessionFactory) {
        return new SqlSessionTemplate(secondSqlSessionFactory);
    }
    @Bean("ylSqlSessionTemplate")
    public SqlSessionTemplate ylSqlSessionTemplate(@Qualifier("ylSqlSessionFactory") SqlSessionFactory ylSqlSessionFactory) {
        return new SqlSessionTemplate(ylSqlSessionFactory);
    }
    @Bean("ndlSqlSessionTemplate")
    public SqlSessionTemplate ndlSqlSessionTemplate(@Qualifier("ndlSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    @Bean("bgySqlSessionTemplate")
    public SqlSessionTemplate bgySqlSessionTemplate(@Qualifier("bgySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    @Bean("czlSqlSessionTemplate")
    public SqlSessionTemplate czlSqlSessionTemplate(@Qualifier("czlSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
    @Bean("cmfxSqlSessionTemplate")
    public SqlSessionTemplate cmfxSqlSessionTemplate(@Qualifier("cmfxSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("mnSqlSessionTemplate")
    public SqlSessionTemplate mnSqlSessionTemplate(@Qualifier("mnSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("nwrSqlSessionTemplate")
    public SqlSessionTemplate nwrSqlSessionTemplate(@Qualifier("nwrSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("xgSqlSessionTemplate")
    public SqlSessionTemplate xgSqlSessionTemplate(@Qualifier("xgSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("yxSqlSessionTemplate")
    public SqlSessionTemplate yxSqlSessionTemplate(@Qualifier("yxSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean("zlSqlSessionTemplate")
    public SqlSessionTemplate zlSqlSessionTemplate(@Qualifier("zlSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
