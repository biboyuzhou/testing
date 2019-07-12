package com.drcnet.highway.dao;

import com.drcnet.highway.entity.TietouSameStationFrequently;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

public interface TietouSameStationFrequentlyMapper extends MyMapper<TietouSameStationFrequently> {
    Integer hasMonthTime(@Param("monthTime") Integer monthTime);
}