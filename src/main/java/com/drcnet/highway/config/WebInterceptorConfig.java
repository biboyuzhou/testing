package com.drcnet.highway.config;

import com.drcnet.highway.interceptor.MonthInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author: penghao
 * @CreateTime: 2019/4/2 14:49
 * @Description:
 */
//@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {

    @Resource
    private MonthInterceptor monthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //设置
        // 自定义拦截器，添加拦截路径和排除拦截路径
        registry.addInterceptor(monthInterceptor).addPathPatterns("/**");
    }


}
