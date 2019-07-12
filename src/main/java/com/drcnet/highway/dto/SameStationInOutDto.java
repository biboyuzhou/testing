package com.drcnet.highway.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/16 15:56
 * @Description:
 */
@Data
public class SameStationInOutDto implements Serializable {

    private static final long serialVersionUID = -5810971615659332971L;
    private Integer carNoId;

    private Integer stationAmount;

    private List<PeriodAmountDto> inOutAmountMonth;

    private List<PeriodAmountDto> inOutAmountPeriod;

}
