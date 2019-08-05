package com.drcnet.highway.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/7/23 11:06
 * @Desc:
 **/
@Setter
@Getter
public class CarTypeCountDto implements Serializable {
    private static final long serialVersionUID = -5227148721843216926L;

    private Integer carType;
    private Integer count;

}
