package com.drcnet.highway.dto;

import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/8 17:05
 * @Description:
 */
@Data
public class TurnoverStationDto {

    private Integer inAmount;

    private Integer outAmount;

    private String stationName;

    private String stationId;

}
