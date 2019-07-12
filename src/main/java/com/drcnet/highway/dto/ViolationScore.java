package com.drcnet.highway.dto;

import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 11:08
 * @Description:
 */
@Data
public class ViolationScore {

    private Integer sameStation;

    /**
     * 平均速度异常风险
     */
    private Integer speed;

    /**
     * 进出车牌不一致风险
     */
    private Integer sameCarNumber;

    /**
     * 进出车型不一致风险
     */
    private Integer sameCarType;

    /**
     * 进出车情不一致风险
     */
    private Integer sameCarSituation;

    /**
     * 短途重载风险
     */
    private Integer shortDisOverweight;

    /**
     * 长途轻载风险
     */
    private Integer longDisLightweight;

    /**
     * 轴数不同风险
     */
    private Integer differentZhou;

    /**
     * 路段标志异常风险
     */
    private Integer diffFlagstationInfo;

    private Integer score;

}
