package com.drcnet.highway.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 16:06
 * @Description: 风险数量Dto
 */
@Data
public class RiskAmountDto implements Serializable {

    private static final long serialVersionUID = 5980614998257853704L;
    private Integer diffFlagstationInfo;
    private Integer shortDisOverweight;
    private Integer longDisLightweight;
    private Integer speed;
    private Integer sameStation;
    private Integer sameCarType;
    private Integer sameCarSituation;
    private Integer differentZhou;
    private Integer sameCarNumber;
    private Integer minOutIn;
    private Integer flagstationLost;
    private Integer sameTimeRangeAgain;
    private Integer highSpeed;
    private Integer lowSpeed;

}
