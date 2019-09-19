package com.drcnet.highway.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/30 16:04
 * @Description:
 */
public class ServletUtil {

    /**
     * @param
     * @return HttpServletRequest
     * @Description: 获取当前线程的request
     */
    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        return sra == null ? null : sra.getRequest();
    }

}
