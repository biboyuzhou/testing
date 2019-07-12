package com.drcnet.highway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
@EnableAsync(order = Ordered.HIGHEST_PRECEDENCE,proxyTargetClass = true)
@EnableAspectJAutoProxy(exposeProxy = true,proxyTargetClass = true)
@MapperScan(basePackages = "com.drcnet.highway.dao")
public class HighwayApplication {

    public static void main(String[] args) {
        SpringApplication.run(HighwayApplication.class, args);
    }

}

