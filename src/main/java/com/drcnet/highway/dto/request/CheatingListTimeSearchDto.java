package com.drcnet.highway.dto.request;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/7/30 11:25
 * @Desc:
 **/
@Data
public class CheatingListTimeSearchDto extends PagingDto implements Serializable {

    private static final long serialVersionUID = 3335115157705131113L;

    /**
     * 车辆类型
     */
    private Integer carType;

    /**
     * 车辆详细类型
     */
    private Integer carDetailType;

    /**
     * 风险等级
     */
    private Integer riskFlag;

    /**
     * 开始日期 只支持日期格式
     */
    private String beginDate;
    /**
     * 结束日期 只支持日期格式
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
     * 轴数
     */
    private Integer axleNum;


    private String flags;

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
     * 车辆报告加入限定条数
     */
    private Integer limit;

    public boolean isTietouQuery(CheatingListTimeSearchDto dto) {
        if (!StringUtils.isEmpty(beginDate) || !StringUtils.isEmpty(endDate) || rkId != null || ckId != null || minDistance != null
                || maxDistance != null || minTravelTime != null || maxTravelTime != null ) {
            return true;
        }
        return false;
    }

    public boolean isExtractionQuery(CheatingListTimeSearchDto dto) {
        if (!StringUtils.isEmpty(flags)) {
            return true;
        }
        return false;
    }
}
