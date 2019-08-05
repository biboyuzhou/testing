package com.drcnet.highway.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/7/31 17:11
 * @Desc:
 **/
@Data
public class StationDicDto implements Serializable {

    private static final long serialVersionUID = -4592134435192628144L;

    private Integer id;

    private String stationName;

}
