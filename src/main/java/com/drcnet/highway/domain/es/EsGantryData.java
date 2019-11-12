package com.drcnet.highway.domain.es;

import lombok.Data;

/**
 * @Author jack
 * @Date: 2019/10/31 9:50
 * @Desc:
 **/
@Data
public class EsGantryData {

    /**
     * id
     */
    private String id;

    /**
     * 任务编号，龙门架编号
     */
    private String taskNum;
    /**
     * 任务名称，龙门架名称
     */
    private String taskName;
    /**
     * 车牌图片
     */
    private String imgBase64;
    /**
     * 车型图片
     */
    private String imgBase64Type;
    /**
     * 车道编号
     */
    private Integer roadNum;
    /**
     * 车道描述
     */
    private String roadName;
    /**
     * 车牌
     */
    private String plateNum;
    /**
     * 车型
     */
    private Integer vehicleType;
    /**
     * 颜色
     */
    private Integer vehicleColor;
    /**
     * 车速
     */
    private Integer vehicleSpeed;
    /**
     * 抓拍时间
     */
    private String snapshotTime;
    /**
     * 行车时间
     */
    private Integer runningTime;
    /**
     * 车头方向
     */
    private Integer vehicleDirection;
    /**
     * 上传时间
     */
    private String sendDate;
}
