package com.drcnet.highway.dao;

import com.drcnet.highway.domain.RiskByRankQuery;
import com.drcnet.highway.dto.request.RiskInOutDto;
import com.drcnet.highway.entity.TietouSameStationFrequently;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TietouSameStationFrequentlyMapper extends MyMapper<TietouSameStationFrequently> {
    Integer hasMonthTime(@Param("monthTime") Integer monthTime);

    /**
     * 根据时间、里程通行时间等搜索同站先出后进记录
     * @param riskInOutDto
     * @return
     */
    List<TietouSameStationFrequently> selectByTimeAndDistance(RiskInOutDto riskInOutDto);

    /**
     * 根据时间、里程通行时间等搜索铁投所有数据的同站先出后进记录,
     * @param riskInOutDto
     * @return
     */
    List<TietouSameStationFrequently> selectByTimeAndDistanceFromAll(RiskInOutDto riskInOutDto);

    int pullSameStationFrequentlyFromAll(@Param("list") List<Integer> stationIdList);

    void truncate();

    /**
     * 根据时间、里程通行时间等搜索铁投所有数据的同站先出后进记录的数量
     * @param query
     */
    Integer getSameStationCountByTimeAndDistance(RiskByRankQuery query);
}