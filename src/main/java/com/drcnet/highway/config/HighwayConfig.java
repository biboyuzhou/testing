package com.drcnet.highway.config;

import com.drcnet.highway.util.FilePathUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by ml on 2018/11/9.
 */
@Configuration
public class HighwayConfig {

    public static final String LINUX_TEMP = "/home/project/highway/temp";
    public static final String WINDOWS_TEMP = "C:/tmp/highway";


    @Resource
    private RedisTemplate<Serializable,Object> redisTemplate;

    @PostConstruct
    public void serializeRedisTemplate() {

        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());
        //设置序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);//key序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);//value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);//Hash key序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);//Hash value序列化
        redisTemplate.setEnableTransactionSupport(false);
        redisTemplate.afterPropertiesSet();
    }

    //异步线程池
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(16);
        // 设置最大线程数
        executor.setMaxPoolSize(32);
        // 设置队列容量
        executor.setQueueCapacity(50);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("hello-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    //全局跨域配置
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");// 允许跨域访问的域名
        corsConfiguration.addAllowedHeader("*");// 请求头
        corsConfiguration.addAllowedMethod(HttpMethod.DELETE);// 请求方法
        corsConfiguration.addAllowedMethod(HttpMethod.POST);
        corsConfiguration.addAllowedMethod(HttpMethod.GET);
        corsConfiguration.addAllowedMethod(HttpMethod.PUT);
        corsConfiguration.addAllowedMethod(HttpMethod.DELETE);
        corsConfiguration.addAllowedMethod(HttpMethod.OPTIONS);
        corsConfiguration.setMaxAge(3600L);// 预检请求的有效期，单位为秒。
        corsConfiguration.setAllowCredentials(true);// 是否支持安全证书
        return corsConfiguration;
    }

    /**
     * 文件上传临时路径
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() throws IOException {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        String filePath;
        if (FilePathUtil.isWindows()){
            factory.setLocation(WINDOWS_TEMP);
            filePath = WINDOWS_TEMP;
        } else{
            factory.setLocation(LINUX_TEMP);
            filePath = LINUX_TEMP;
        }
        File file = new File(filePath);
        if (!file.exists())
            file.mkdirs();
        if (!FilePathUtil.isWindows())
            Runtime.getRuntime().exec("chmod 777 -R " + filePath);
        return factory.createMultipartConfig();
    }
}
