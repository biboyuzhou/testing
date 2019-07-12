package com.drcnet.highway.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/16 13:29
 * @Description:
 */
@Data
public class SameStationInOutVo implements Serializable {
    private static final long serialVersionUID = 4534238761300501438L;

    private Integer amount;

    private Integer inCarNoId;

    private Integer outCarNoId;

    private String inCarNo;

    private String outCarNo;

    private Boolean hasRecord;

    private String stationName;

    private Integer stationId;

    private Integer monthTime;

}
