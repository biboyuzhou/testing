package com.drcnet.highway.constants;

/**
 * @Author jack
 * @Date: 2019/7/24 17:22
 * @Desc: 缓存key配置
 **/
public class CacheKeyConsts {

    /**
     * 二绕
     */
    public static final String SECOND_FIRST_PAGE_RISK_MAP_CACHE_KEY = "2nd_station_risk_count";
    public static final String SECOND_FIRST_PAGE_RELATION_CACHE_KEY = "2nd_station_trip_count";

    /**
     * 宜泸
     */
    public static final String YILU_FIRST_PAGE_RISK_MAP_CACHE_KEY = "yilu_station_risk_count";
    public static final String YILU_FIRST_PAGE_RELATION_CACHE_KEY = "yilu_station_trip_count";

    /**
     * 叙古
     */
    public static final String XUGU_FIRST_PAGE_RISK_MAP_CACHE_KEY = "xg_station_risk_count";
    public static final String XUGU_FIRST_PAGE_RELATION_CACHE_KEY = "xg_station_trip_count";

    /**
     * 南大梁
     */
    public static final String NADALIANG_FIRST_PAGE_RISK_MAP_CACHE_KEY = "ndl_station_risk_count";
    public static final String NADALIANG_FIRST_PAGE_RELATION_CACHE_KEY = "ndl_station_trip_count";

    /**
     * 宜叙
     */
    public static final String YIXU_FIRST_PAGE_RISK_MAP_CACHE_KEY = "yx_station_risk_count";
    public static final String YIXU_FIRST_PAGE_RELATION_CACHE_KEY = "yx_station_trip_count";

    /**
     * 巴广渝
     */
    public static final String BAGUANGYU_FIRST_PAGE_RISK_MAP_CACHE_KEY = "bgy_station_risk_count";
    public static final String BAGUANGYU_FIRST_PAGE_RELATION_CACHE_KEY = "bgy_station_trip_count";

    /**
     * 绵南
     */
    public static final String MIANNAN_FIRST_PAGE_RISK_MAP_CACHE_KEY = "mn_station_risk_count";
    public static final String MIANNAN_FIRST_PAGE_RELATION_CACHE_KEY = "mn_station_trip_count";

    /**
     * 成绵复线
     */
    public static final String CMFX_FIRST_PAGE_RISK_MAP_CACHE_KEY = "cmfx_station_risk_count";
    public static final String CMFX_FIRST_PAGE_RELATION_CACHE_KEY = "cmfx_station_trip_count";

    /**
     * 自隆
     */
    public static final String ZILONG_FIRST_PAGE_RISK_MAP_CACHE_KEY = "zl_station_risk_count";
    public static final String ZILONG_FIRST_PAGE_RELATION_CACHE_KEY = "zl_station_trip_count";

    /**
     * 成自泸
     */
    public static final String CHENGZILU_FIRST_PAGE_RISK_MAP_CACHE_KEY = "czl_station_risk_count";
    public static final String CHENGZILU_FIRST_PAGE_RELATION_CACHE_KEY = "czl_station_trip_count";

    /**
     * 内威荣
     */
    public static final String NEIWEIRONG_FIRST_PAGE_RISK_MAP_CACHE_KEY = "nwr_station_risk_count";
    public static final String NEIWEIRONG_FIRST_PAGE_RELATION_CACHE_KEY = "nwr_station_trip_count";

    /**
     * 江习古
     */
    public static final String JIANGXIGU_FIRST_PAGE_RISK_MAP_CACHE_KEY = "jxg_station_risk_count";
    public static final String JIANGXIGU_FIRST_PAGE_RELATION_CACHE_KEY = "jxg_station_trip_count";

    /**
     * 前一次数据计算缓存key
     */
    public static final String PREVIOUS_Id_CACHE = "previous_id";
    /**
     * 前一次数据计算tietou最大的id
     */
    public static final String PREVIOUS_TIETOU_ID = "previous_tietou_id";
    /**
     * 前一次数据计算carDic最大id
     */
    public static final String PREVIOUS_CAR_ID = "previous_car_id";

    /**
     * 前一次数据计算carDic最大id
     */
    public static final String PREVIOUS_END_MONTH = "previous_end_month";

    /**
     * use_flag为false且是异常的车牌
     */
    public static final String USELESS_CAR_USE_FLAG_TRUE = "useless_car_use_flag_true";
    public static final String CAR_CACHE_USELESS = "car_cache_useless";

    /**
     * car_dic的缓存
     */
    public static final String CAR_DIC_CACHE = "car_cache";




    //-------------overall 总体数据相关-------------------
    /**
     * 各个路段高风险车辆数据
     */
    public static final String EVERY_ROAD_HIGH_RISK_DATA = "every_road_high_risk_data";

    /**
     * 风险类别分布top3
     */
    public static final String TOP_3_RISK_TYPE_MAX_MONTH = "top_3_risk_type_max_month";
    public static final String TOP_3_RISK_TYPE_CURRENT_MONTH = "top_3_risk_type_current_month_";
    public static final String TOP_3_RISK_TYPE_LAST_MONTH = "top_3_risk_type_last_month";
    public static final String TOP_3_RISK_TYPE_BEFORE_LAST_MONTH = "top_3_risk_type_before_last_month";
    public static final String TOP_3_RISK_TYPE_OLDEST_MONTH = "top_3_risk_type_oldest_month";
}
