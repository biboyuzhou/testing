package com.drcnet.highway.dto.request.enternal;

import lombok.Data;

/**
 * @Author jack
 * @Date: 2019/10/21 16:14
 * @Desc:
 **/
@Data
public class GantryDataDto {

    /**
     * 任务编号，龙门架编号
     */
    private String taskNum;
    /**
     * 任务名称，龙门架名称
     */
    private String taskName;
    /**
     * 车牌图片相对路径
     */
    private String snapshotPath;
    /**
     * 车型图片相对路径
     */
    private String vehicleTypePath;
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
     * 转向id
     */
    private Integer trunDirNo;
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
     * 车头间距
     */
    private Integer vehicleSpacing;
    /**
     * 车头时距
     */
    private Integer timeDistance;
    /**
     * 车头方向
     */
    private Integer vehicleDirection;
    /**
     * 上传时间
     */
    private String date;
    /**
     * 数据来源
     */
    private String dataSource;
}
