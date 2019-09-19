package com.drcnet.highway.dao;

import com.drcnet.highway.dto.request.StationSpeedQueryDto;
import com.drcnet.highway.entity.StationFeatureStatistics;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StationFeatureStatisticsMapper extends MyMapper<StationFeatureStatistics> {
    List<StationFeatureStatistics> listStationSpeedInfo(StationSpeedQueryDto queryDto);

    /**
     * 删除全部数据，后面重新计算
     */
    void truncateData();

    /**
     * 重新生成station_feature_statistic表的数据
     */
    void rebuildTableData();

    /**
     * 根据ckId和rkId查询总表的路段平均速度
     * @param ckId
     * @param rkId
     * @return
     */
    StationFeatureStatistics selectByCkIdAndRkId(@Param("ckId") Integer ckId, @Param("rkId") Integer rkId);
}