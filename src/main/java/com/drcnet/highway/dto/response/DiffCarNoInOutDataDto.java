package com.drcnet.highway.dto.response;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author jack
 * @Date: 2019/6/19 10:52
 * @Desc:
 **/
@Data
public class DiffCarNoInOutDataDto  {
    /**
     * 入口时间
     */
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDateTime inTime;

    /**
     * 入口站
     */
    private String inStationName;

    /**
     * 入口车型
     */
    private Integer inVc;

    /**
     * 入口里程
     */
    private Integer inDistance;

    /**
     * 入口速度
     */
    private Integer inSpeed;

    /**
     * 入口车道
     */
    private String inExlane;

    /**
     * 疑似车辆出口时间
     */
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDateTime outTime;

    /**
     * 出口站
     */
    private String outStationName;

    /**
     * 出口车型
     */
    private Integer outVc;

    /**
     * 出口里程
     */
    private Integer outDistance;

    /**
     * 出口速度
     */
    private Integer outSpeed;

    /**
     * 出口车道
     */
    private String outExlane;
}
