package com.drcnet.highway.dto.response.overall;

import com.drcnet.highway.dto.response.StationRiskCountDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author jack
 * @Date: 2019/10/8 10:20
 * @Desc:
 **/
@Data
public class EveryRoadStationRiskResponse implements Serializable {
    private static final long serialVersionUID = 1969895548820870556L;

    /**
     * 二绕风险站点数据
     */
    private List<StationRiskCountDto> second;

    /**
     * 宜泸风险站点数据
     */
    private List<StationRiskCountDto> yl;

    /**
     * 巴广渝风险站点数据
     */
    private List<StationRiskCountDto> bgy;

    /**
     * 南大梁风险站点数据
     */
    private List<StationRiskCountDto> ndl;

    /**
     * 成自泸风险站点数据
     */
    private List<StationRiskCountDto> czl;

    /**
     * 成绵复线风险站点数据
     */
    private List<StationRiskCountDto> cmfx;

    /**
     * 绵南风险站点数据
     */
    private List<StationRiskCountDto> mn;

    /**
     * 内威荣风险站点数据
     */
    private List<StationRiskCountDto> nwr;

    /**
     * 叙古风险站点数据
     */
    private List<StationRiskCountDto> xg;

    /**
     * 宜叙风险站点数据
     */
    private List<StationRiskCountDto> yx;

    /**
     * 自隆风险站点数据
     */
    private List<StationRiskCountDto> zl;
}
