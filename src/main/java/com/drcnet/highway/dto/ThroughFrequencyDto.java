package com.drcnet.highway.dto;

import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/14 13:40
 * @Description:
 */
@Data
public class ThroughFrequencyDto {

    private String entranceStation;

    private String exitStation;

    private String stationName;

    private Integer amount;

}
