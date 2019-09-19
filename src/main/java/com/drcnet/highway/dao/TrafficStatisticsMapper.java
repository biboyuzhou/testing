package com.drcnet.highway.dao;

import com.drcnet.highway.dto.request.DateSearchDto;
import com.drcnet.highway.dto.response.TrafficStatisticsVo;
import com.drcnet.highway.entity.TrafficStatistics;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrafficStatisticsMapper extends MyMapper<TrafficStatistics> {
    void truncate();

    int insertCkStatisticData();

    List<TrafficStatisticsVo> listStationTrafficStatistics(@Param("searchDto") DateSearchDto searchDto,
                                                           @Param("type") Integer type,@Param("stationIdList") List<Integer> stationIdList);

    LocalDate getNewestDay(@Param("type") Integer type,@Param("stationIdList") List<Integer> stationIdList);

    List<TrafficStatisticsVo> listDateTrafficStatistics(@Param("searchDto") DateSearchDto searchDto,
                                                        @Param("type") Integer type,@Param("stationIdList") List<Integer> stationIdList);

    int insertRkStatisticData();

    /**
     * 通过stationId,currentDay,boundType
     * @param trafficStatistics
     * @return
     */
    TrafficStatistics selectByUniqueKey(TrafficStatistics trafficStatistics);

    List<TrafficStatistics> selectRkStatisticData();

}