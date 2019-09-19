package com.drcnet.highway.dto.request;

import com.drcnet.highway.util.validate.AddValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/10 15:14
 * @Description:
 */
@Data
public class RiskInOutDto extends PagingDto {

    @NotNull(groups = AddValid.class)
    private Integer carId;
    private String beginMonth;
    @NotNull(groups = AddValid.class)
    private Integer code;

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
