package com.drcnet.highway.dto.response;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author jack
 * @Date: 2019/8/28 15:43
 * @Desc:
 **/
@Data
public class CarDetailResponse implements Serializable {
    private static final long serialVersionUID = -2386149882588137606L;

    private Integer id;
    /**
     * 车牌
     */
    private String carNo;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDateTime createTime;
    /**
     * 是否有效车牌
     */
    private Boolean useFlag;
    /**
     * 是否白名单车辆
     */
    private Boolean whiteFlag;
    /**
     * 车辆类型
     */
    private Integer carType;

    /**
     * 轴数
     */
    private Integer axlenum;

    /**
     * 最小载重
     */
    private Integer weightMin;

    /**
     * 最大载重
     */
    private Integer weightMax;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * tietou总的通行次数
     */
    private Integer totalTravelCount;

    /**
     * 当前路段通行次数
     */
    private Integer travelCount;
}
