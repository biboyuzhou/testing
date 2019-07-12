package com.drcnet.highway.annotation;

import java.lang.annotation.*;

/**
 * @Author: penghao
 * @CreateTime: 2018/12/17 18:05
 * @Description: 用于标记列名
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ColumnName {

    String value();

}
