package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/8/28 16:12
 * @Desc:
 **/
@Data
public class RiskByRankRequest implements Serializable {
    private static final long serialVersionUID = -4848544941533461746L;

    /**
     * 车辆id
     */
    @NotNull(groups = AddValid.class)
    private Integer carId;

    /**
     * 开始日期
     */
    private String beginDate;
    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 进站id
     */
    private Integer rkId;

    /**
     * 出站id
     */
    private Integer ckId;

    /**
     * 最小距离 单位千米
     */
    private Integer minDistance;

    /**
     * 最大距离 单位千米
     */
    private Integer maxDistance;

    /**
     * 最小行程时间 单位分钟
     */
    private Integer minTravelTime;

    /**
     * 最大行程时间 单位分钟
     */
    private Integer maxTravelTime;

    /**
     * 是否查询本路段数据
     */
    @NotNull(groups = AddValid.class)
    private Integer isCurrent;
}
