package com.drcnet.highway.config.datasource;

import com.drcnet.highway.config.YamlProfilesConfig;

/**
 * @Author: penghao
 * @CreateTime: 2019/9/2 16:38
 * @Description: 数据源类型切换载体类
 */
public class DataSourceType {

    private static final ThreadLocal<DataSourceEnum> TYPE = new ThreadLocal<>();


    public enum DataSourceEnum {
        PRIMARY, SECOND, YL, NDL, BGY, CZL, CMFX, MN, NWR, XG, YX, ZL;
    }

    public static void set(DataSourceEnum dataSourceEnum) {
        if (dataSourceEnum == null) {
            throw new NullPointerException();
        }
        TYPE.set(dataSourceEnum);
    }

    public static DataSourceEnum get() {
        DataSourceEnum dataSourceEnum = TYPE.get();
        return dataSourceEnum != null ? dataSourceEnum : DataSourceEnum.PRIMARY;
    }

    public static void clear() {
        TYPE.remove();
    }

}
