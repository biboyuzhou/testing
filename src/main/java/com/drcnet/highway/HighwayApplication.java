package com.drcnet.highway;

import com.drcnet.highway.config.YamlProfilesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync(order = Ordered.HIGHEST_PRECEDENCE,proxyTargetClass = true)
@EnableAspectJAutoProxy(exposeProxy = true,proxyTargetClass = true)
@MapperScan(basePackages = "com.drcnet.highway.dao")
@ComponentScan(value = {"com.drcnet.response", "com.drcnet.usermodule", "com.drcnet.highway"})
public class HighwayApplication {

    public static void main(String[] args) {
        YamlProfilesConfig.initArgs(args);
        SpringApplication.run(HighwayApplication.class, args);
    }

}

