package com.drcnet.highway.constants.enumtype;

/**
 * @Author jack
 * @Date: 2019/8/13 20:50
 * @Desc:
 **/
public enum CalculateStateEnum {
    BEGIN("任务开始", 0),
    STATION_STATISTIC("计算站点之间的各个车型的平均速度完成", 1),
    ALEXNUM_AND_CARTYPE_STATISTIC("更新车辆的轴数和车辆类型完成", 2),
    MAX_AND_MIN_WEIGHT_STATISTIC("更新车辆的最大载重和最小载重完成", 3),
    CACHE_USELESS_CAR("更新异常车牌的缓存完成", 4),
    TEN_RISK_STATISTIC("对10项异常进行打标完成", 5),
    SAME_TIME_RANGE_STATISTIC("对时间重叠风险进行打标完成", 6),
    OUT_AND_IN_STATISTIC("对先出后进数据进行打标完成", 7),
    INSERT_STATISTIC_TABLE("插入statistic表数据完成", 8),
    UPDATE_ISFREE_CAR("将免费车辆进行打标完成", 9),
    UPDATE_IN_OUT_FREQUENCY_EXT("更新先出后进表的冗余字段值完成", 10),
    CALCULATE_SCORE("算法进行分数计算完成", 11);

    private String name;
    private Integer code;

    CalculateStateEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public static CalculateStateEnum getEnumByCode(Integer code) {
        for(CalculateStateEnum calculateStateEnum : CalculateStateEnum.values()) {
            if (calculateStateEnum.getCode().equals(code)) {
                return calculateStateEnum;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
