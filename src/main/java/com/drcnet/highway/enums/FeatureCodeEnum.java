package com.drcnet.highway.enums;

/**
 * @Author: penghao
 * @CreateTime: 2019/6/19 11:06
 * @Description:
 */
public enum FeatureCodeEnum {

    DIFF_FLAGSTATION_INFO(1,"路径异常(标记不一致)"),
    SHORT_DIS_OVERWEIGHT(2,"短途重载"),
    LONG_DIS_LIGHTWEIGHT(3,"长途轻载"),
    SPEED(4,"速度异常"),
    SAME_STATION(5,"同站进出"),
    SAME_CAR_TYPE(6,"车型不一致"),
    SAME_CAR_SITUATION(7,"车情不一致"),
    DIFFERENT_ZHOU(8,"轴数不一致"),
    SAME_CAR_NUMBER(9,"车牌不一致"),
    MIN_OUT_IN(10,"5分钟内先出后进"),
    SAME_TIME_RANGE_AGAIN(11,"时间重叠"),
    FLAGSTATION_LOST(12,"路径缺失"),
    HIGH_SPEED(13,"高速异常"),
    LOW_SPEED(14,"低速异常"),;
    public final int code;

    public final String msg;

    FeatureCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
