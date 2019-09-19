package com.drcnet.highway.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: penghao
 * @CreateTime: 2019/9/2 16:59
 * @Description: 需要切换多数据的地方加上该注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicDataSource {
    //路段标记的参数name
    String value() default "";

}
