package com.drcnet.highway.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/7/23 15:17
 * @Desc:
 **/
@Setter
@Getter
public class StationRiskCountDto implements Serializable {
    private static final long serialVersionUID = -5019605520776594573L;

    private Integer ckId;
    private String ckName;
    /**
     * 经度
     */
    private String longitude;
    /**
     * 纬度
     */
    private String latitude;
    private Integer total;
    private Integer high;
    private Integer middle;
    private Integer low;
}
