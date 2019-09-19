package com.drcnet.highway.domain;

import lombok.Data;

/**
 * @Author jack
 * @Date: 2019/6/26 11:32
 * @Desc:
 **/
@Data
public class SameCarNum {
    private Integer num;
    private Integer vlpId;
    private Integer vc;
    private Integer carType;
    /**
     * 客车车型出现的次数
     */
    private Integer carNum;
    /**
     * 货车车型出现的次数
     */
    private Integer trackNum;

    /**
     * 最小载重
     */
    private Integer minWeight;

    /**
     * 最大载重
     */
    private Integer maxWeight;

    private Integer id;
}
