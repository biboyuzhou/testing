package com.drcnet.highway.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/7/23 10:06
 * @Desc:
 **/
@Getter
@Setter
public class StationTripCountDto implements Serializable {
    private static final long serialVersionUID = 5608402799643271345L;

    private Integer rkId;
    private String rkName;
    private Integer ckId;
    private String ckName;
    /**
     * 以出口和入口为维度，通行记录数
     */
    private Integer num;
    /**
     * 以出口站为维度，总的通行记录数
     */
    private Integer totalCount;
}
