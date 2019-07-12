package com.drcnet.highway.interceptor;

import com.drcnet.highway.util.EntityUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/13 10:16
 * @Description:
 */
@Component
public class MonthInterceptor implements HandlerInterceptor {

    private static final String BEGIN_MONTH = "beginMonth";

    /**
     * 检查月份格式是否正确
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method targetMethod = handlerMethod.getMethod();
            for (Parameter parameter : targetMethod.getParameters()) {
                if (parameter.getName().equalsIgnoreCase(BEGIN_MONTH)) {
                    RequestParam annotation = parameter.getAnnotation(RequestParam.class);
                    if (annotation!=null && annotation.required()) {
                        String param = request.getParameter(BEGIN_MONTH);
                        if (param!=null){
                            EntityUtil.dateMonthChecked(param);
                        }
                    }
                }
            }
        }


        return true;
    }
}
