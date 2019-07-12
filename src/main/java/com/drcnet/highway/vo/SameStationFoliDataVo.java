package com.drcnet.highway.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

import static com.drcnet.highway.constants.TimeConsts.GMT8;
import static com.drcnet.highway.constants.TimeConsts.TIME_FORMAT;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/14 14:58
 * @Description:
 */
@Data
public class SameStationFoliDataVo implements Serializable {
    private static final long serialVersionUID = -2167416763207896323L;

    private Integer id;

    private Integer carNoId;

    private String carNo;

    private Integer stationId;

    private String stationName;

    private Integer amount;

    private Boolean hasRecord;

    private Integer entranceStationId;

    private Integer exitStationId;

    private String entranceName;

    private String exitName;

    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    private LocalDateTime inTime;

    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    private LocalDateTime outTime;
}
