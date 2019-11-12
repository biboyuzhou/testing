package com.drcnet.highway.dto.response.overall;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/9/29 15:41
 * @Desc: 总体数据每条路风险数据接口返回实体
 **/
@Data
public class EveryRoadRiskDataResponse implements Serializable {

    private static final long serialVersionUID = -7596158087496516175L;

    /**
     * 路段标识
     */
    private String roadCode;
    /**
     * 高风险车辆数
     */
    private Integer highRiskNum;
}
