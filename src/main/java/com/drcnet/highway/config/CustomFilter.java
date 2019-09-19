package com.drcnet.highway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * powered by IntelliJ IDEA
 *
 * @Author: penghao
 * @Date: 2018/4/20 18:28
 * @Description:
 **/
@Slf4j
//@WebFilter(urlPatterns = "/*")
//@Component
public class CustomFilter implements Filter {

    @Value("${drcnet.swagger}")
    private boolean enableFlag;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse rep = (HttpServletResponse) servletResponse;

        /*if (req.getCookies()!=null){
            for (Cookie cookie:req.getCookies()){
                if (cookie.getName().equals("JSESSIONID")){
                    log.info("JSESSIONID--------------->"+cookie.getValue());
                    break;
                }
            }
        }*/
        if (enableFlag){
            log.info("请求的方式-->{}请求的地址-->{}",req.getMethod()+','+req.getRemoteAddr(),req.getServletPath());
        }
        filterChain.doFilter(req,rep);
    }

}
