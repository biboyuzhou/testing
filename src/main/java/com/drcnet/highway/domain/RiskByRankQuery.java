package com.drcnet.highway.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/9/2 15:13
 * @Desc:
 **/
@Data
public class RiskByRankQuery implements Serializable {


    private static final long serialVersionUID = 1282337415993336115L;

    private Integer carId;

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
     * 最小距离 单位千米
     */
    private Integer minDistance;

    /**
     * 最大距离 单位千米
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

    /**
     * 是否查询本路段数据
     */
    private Integer isCurrent;

    /**
     * 查询总数据还是当前路段数据的table
     */
    private String tableName;
}
