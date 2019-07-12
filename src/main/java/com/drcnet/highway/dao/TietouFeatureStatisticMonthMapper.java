package com.drcnet.highway.dao;

import com.drcnet.highway.entity.TietouFeatureStatisticMonth;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TietouFeatureStatisticMonthMapper extends MyMapper<TietouFeatureStatisticMonth> {
    List<TietouFeatureStatisticMonth> statisticFeatureAmountByMonth(Integer monthTime);


    /**
     * 按周期查询统计月表数据
     * @param start
     * @param end
     * @return
     */
    List<TietouFeatureStatisticMonth> listAllByPeriod(@Param("start") int start, @Param("end") int end);
}