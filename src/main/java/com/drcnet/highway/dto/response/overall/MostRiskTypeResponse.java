package com.drcnet.highway.dto.response.overall;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/10/8 14:21
 * @Desc:
 **/
@Data
public class MostRiskTypeResponse implements Serializable {
    private static final long serialVersionUID = -8046869436875708814L;

    private String firstRiskName;
    private Integer firstRiskValue;
    private String secondRiskName;
    private Integer secondRiskValue;
    private String thirdRiskName;
    private Integer thirdRiskValue;
}
