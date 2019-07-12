package com.drcnet.highway.vo;

import com.drcnet.highway.annotation.ColumnName;
import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/14 14:14
 * @Description:
 */
@Data
public class CarInOutAmountVo implements Serializable {
    private Integer id;

    /**
     * 车牌id
     */
    private Integer carNoId;

    /**
     * 车牌号
     */
    @ColumnName("车牌号")
    private String carNo;

    /**
     * 进站数
     */
    @ColumnName("进站数")
    private Integer inAmount;

    /**
     * 出站数
     */
    @ColumnName("出站数")
    private Integer outAmount;

    /**
     * 总计数量
     */
    @ColumnName("总数")
    private Integer totalAmount;

    /**
     * 月度时间，格式yyyyMM
     */
    @ColumnName("时间段")
    private Integer monthTime;
    @ColumnName("进站车型")
    private String inCarTypes;
    @ColumnName("出站车型")
    private String outCarTypes;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT, timezone = TimeConsts.GMT8)
    private LocalDateTime updateTime;
    
    private Boolean hasRecord;
}
