package com.drcnet.highway.constants.enumtype;

/**
 * @Author jack
 * @Date: 2019/8/5 14:52
 * @Desc:
 **/
public enum RiskFlagEnum {
    DIFF_FLAG_STATION_INFO("diff_flagstation_info", 1, "行驶路径不一致"),
    SHORT_DIS_OVERWEIGHT("short_dis_overweight", 2, "短途重载风险"),
    LONG_DIS_LIGHTWEIGHT("long_dis_lightweight", 3, "长途轻载风险"),
    SPEED("speed", 4, "行驶速度异常风险"),
    SAME_STATION("same_station", 5, "同站进出风险"),
    SAME_CAR_TYPE("same_car_type", 6, "进出车型不一致风险"),
    SAME_CAR_SITUATION("same_car_situation", 7, "进出车情不一致风险"),
    DIFFERENT_ZHOU("different_zhou", 8, "轴数异常风险"),
    SAME_CAR_NUMBER("same_car_number", 9, "进出车牌不一致"),
    MIN_OUT_IN("min_out_in", 10, "高频进出风险"),
    SAME_TIME_RANGE_AGAIN("same_time_range_again", 11, "重复驶入风险"),
    FLAG_STATION_LOST("flagstation_lost", 12, "行驶路径值缺失"),
    HIGH_SPEED("high_speed", 13, "高速异常"),
    LOW_SPEED("low_speed", 14, "低速异常");


    private String riskName;
    private Integer riskCode;
    private String desc;

    RiskFlagEnum(String riskName, Integer riskCode, String desc) {
        this.riskName = riskName;
        this.riskCode = riskCode;
        this.desc = desc;
    }

    public static RiskFlagEnum getEnumByRiskName(String riskName) {
        for(RiskFlagEnum riskFlagEnum : RiskFlagEnum.values()) {
            if (riskFlagEnum.getRiskName().equals(riskName)) {
                return riskFlagEnum;
            }
        }
        return null;
    }

    public static RiskFlagEnum getEnumByCode(Integer riskCode) {
        for(RiskFlagEnum riskFlagEnum : RiskFlagEnum.values()) {
            if (riskFlagEnum.getRiskCode().equals(riskCode)) {
                return riskFlagEnum;
            }
        }
        return null;
    }

    public String getRiskName() {
        return riskName;
    }

    public void setRiskName(String riskName) {
        this.riskName = riskName;
    }

    public Integer getRiskCode() {
        return riskCode;
    }

    public void setRiskCode(Integer riskCode) {
        this.riskCode = riskCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }}
