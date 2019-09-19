package com.drcnet.highway.domain;

import lombok.Data;

import java.util.List;

/**
 * @Author jack
 * @Date: 2019/8/23 13:46
 * @Desc:
 **/
@Data
public class ListCheatingCarByTimeQuery {

    /**
     * 车辆类型
     */
    private Integer carType;

    /**
     * 车辆详细类型
     */
    private Integer carDetailType;

    /**
     * 风险等级
     */
    private Integer riskFlag;

    /**
     * 开始日期
     */
    private String beginDate;
    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 进站id
     */
    private Integer rkId;

    /**
     * 出站id
     */
    private Integer ckId;

    /**
     * 轴数
     */
    private Integer axleNum;

    /**
     * 排序字段，按照传入的风险项的总次数排序
     */
    private List<String> fields;

    /**
     * 最小距离 单位米
     */
    private Integer minDistance;

    /**
     * 最大距离 单位米
     */
    private Integer maxDistance;

    /**
     * 最小行程时间 单位分钟
     */
    private Integer minTravelTime;

    /**
     * 最大行程时间 单位分钟
     */
    private Integer maxTravelTime;
}
