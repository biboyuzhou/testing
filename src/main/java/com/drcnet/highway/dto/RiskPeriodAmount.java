package com.drcnet.highway.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/14 14:17
 * @Description:
 */
@Data
public class RiskPeriodAmount implements Serializable {

    private static final long serialVersionUID = -5268113669006423591L;
    /**
     * 无风险
     */
    private Integer non;

    /**
     * 低风险
     */
    private Integer low;

    /**
     * 中风险
     */
    private Integer middle;

    /**
     * 高风险
     */
    private Integer high;

}
