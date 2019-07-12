package com.drcnet.highway.dto.response;

import lombok.Data;

/**
 * @Author jack
 * @Date: 2019/6/19 9:46
 * @Desc: 统计指定车辆的进出车牌不一致数据
 *        入口车牌数据
 **/
@Data
public class DiffCarNoEnvlpDto {

    /**
     * 进站出牌
     */
    private String carNo;

    /**
     * 进站车牌id
     */
    private Integer carId;

    /**
     * 二绕进站次数
     */
    private Integer inNum;

    /**
     * 总的出站次数
     */
    private Integer outNum;

    /**
     * 出站车牌
     */
    private String outCarNo;
}
