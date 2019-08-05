package com.drcnet.highway.dto.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

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
     * 轴数
     */
    private Integer axleNum;


    private String flags;

    /**
     * 排序字段，按照传入的风险项的总次数排序
     */
    private List<String> fields;
}
