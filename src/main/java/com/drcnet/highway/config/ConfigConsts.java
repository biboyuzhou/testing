package com.drcnet.highway.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/1 15:31
 * @Description:
 */
public class ConfigConsts {

    /**
     * 编辑距离算法后距离大于0.7，可判定为相同车牌
     */
    public static final double SAME_CAR_SCORE = 0.7;
    //里程小于30km，总重大于核对总重的80%，为短途重载，里程大于100km，总重小于核重的30%，为长途轻载
    public static final double SHORT_DISOVER_WEIGHT_RATE = 0.8;
    //短途重载里程指标 30km
    public static final double SHORT_DISOVER_WEIGHT_MILEAGE = 30000;
    public static final double LONG_DISLIGHT_WEIGHT_RATE = 0.3;
    //长度轻载里程指标 100km
    public static final double LONG_DISLIGHT_WEIGHT_MILEAGE = 100000;
    //车速在20km/h和140km/h以上为异常车速
    public static final int MIN_SPEED = 20;
    public static final int MAX_SPEED = 180;
    //进站时间最早在2017年，否则可能数据异常
    public static final LocalDateTime LAST_TIME = LocalDateTime.of(2017,1,1,0,0,0);
    //轴数对应的载重,载重单位:kg
    public static final Map<Integer,Integer> AXLENUM_WEIGHT;
    //进站时间相同，出站时间差距在1800秒内不算进出时间重叠,也不算同站进出
    public static final long MAX_TIME_DIS = 1800;
    public static final long MAX_IN_AND_EXIT_DIS = 600;
    //当前轴数的通行次数超过总通行次数的百分之10,则记为异常
    public static final double AXLENUM_RATE = 0.1;


    static {
        AXLENUM_WEIGHT = new HashMap<>();
        AXLENUM_WEIGHT.put(2,18000);
        AXLENUM_WEIGHT.put(3,27000);
        AXLENUM_WEIGHT.put(4,36000);
        AXLENUM_WEIGHT.put(5,43000);
        AXLENUM_WEIGHT.put(6,49000);
    }
}
