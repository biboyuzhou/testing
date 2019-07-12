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
    private Integer non;

    private Integer low;

    private Integer middle;

    private Integer high;

}
