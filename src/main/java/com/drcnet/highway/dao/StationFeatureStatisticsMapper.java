package com.drcnet.highway.dao;

import com.drcnet.highway.dto.request.StationSpeedQueryDto;
import com.drcnet.highway.entity.StationFeatureStatistics;
import com.drcnet.highway.util.templates.MyMapper;

import java.util.List;

public interface StationFeatureStatisticsMapper extends MyMapper<StationFeatureStatistics> {
    List<StationFeatureStatistics> listStationSpeedInfo(StationSpeedQueryDto queryDto);
}