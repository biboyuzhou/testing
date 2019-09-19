package com.drcnet.highway.config.datasource;

import com.drcnet.highway.annotation.DynamicDataSource;
import com.drcnet.highway.util.ServletUtil;
import com.google.common.base.Enums;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: penghao
 * @CreateTime: 2019/9/2 17:00
 * @Description: 多数据源注解切面，切注解 @DynamicDataSource
 */
@Aspect
//@Component
public class DataSourceAop {

    private static final String ROUTE = "route";

    @Around("@annotation(com.drcnet.highway.annotation.DynamicDataSource)")
    @Order(1)
    public Object setDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            DynamicDataSource annotation = signature.getMethod().getAnnotation(DynamicDataSource.class);
            String param = annotation.value();
            if (!param.equals("")) {
                Object[] args = joinPoint.getArgs();
                String[] paramsName = signature.getParameterNames();
                for (int i = 0; i < paramsName.length; i++) {
                    if (paramsName[i].equals(param)) {
                        if (i < args.length && args[i] != null) {
                            String arg = String.valueOf(args[i]).toUpperCase();
                            if (Enums.getIfPresent(DataSourceType.DataSourceEnum.class, arg).isPresent()) {
                                //设置数据源
                                DataSourceType.set(DataSourceType.DataSourceEnum.valueOf(arg));
                            }
                        }
                        break;
                    }
                }
            }else {
                String header;
                HttpServletRequest request = ServletUtil.getHttpServletRequest();
                if (request!=null && (header = request.getHeader(ROUTE)) != null
                        && Enums.getIfPresent(DataSourceType.DataSourceEnum.class, header.toUpperCase()).isPresent()){
                    DataSourceType.set(DataSourceType.DataSourceEnum.valueOf(header.toUpperCase()));
                }
            }
            return joinPoint.proceed();
        } finally {
            DataSourceType.clear();
        }
    }
}
