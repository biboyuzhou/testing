package com.drcnet.highway.dto.request;

import lombok.Data;

/**
 * @Author jack
 * @Date: 2019/6/18 10:59
 * @Desc: 通行记录查询列表入参
 **/
@Data
public class TravelRecordQueryDto extends PagingDto {
    /**
     * 进口车牌号
     */
    private String inCarNo;
    /**
     * 出口车牌号
     */
    private String outCarNo;
    /**
     * 入口日期
     */
    private String inDate;
    /**
     * 出口日期
     */
    private String outDate;
    /**
     * 操作员
     */
    private String oper;

    /**
     * 车辆类型
     */
    private Integer carType;

    /**
     * 高速通行卡号
     */
    private String card;

    //-----------------------------------------以下参数在MainController-queryTravelRecordsByTime中用
    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 结束时间
     */
    private Integer carId;
}
