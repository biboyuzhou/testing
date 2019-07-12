package com.drcnet.highway.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

import static com.drcnet.highway.constants.TimeConsts.GMT8;
import static com.drcnet.highway.constants.TimeConsts.TIME_FORMAT;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/21 10:47
 * @Description:
 */
@Data
public class OriginalDataVo implements Serializable {

    private static final long serialVersionUID = 6929108310484001872L;
    private Integer id;

    private Integer entranceStationId;

    private Integer exitStationId;

    private String entranceName;

    private String exitName;

    private Integer inCarNoId;

    private Integer outCarNoId;

    private String inCarNo;

    private String outCarNo;

    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    private LocalDateTime inTime;

    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    private LocalDateTime outTime;

    private Integer inCarType;

    private Integer outCarType;
}
