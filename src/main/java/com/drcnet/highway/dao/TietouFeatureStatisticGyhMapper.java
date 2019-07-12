package com.drcnet.highway.dao;

import com.drcnet.highway.dto.RiskPeriodAmount;
import com.drcnet.highway.dto.request.CheatingListDto;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface TietouFeatureStatisticGyhMapper extends MyMapper<TietouFeatureStatisticGyh> {
    List<TietouFeatureStatisticGyh> listCheatingCar(CheatingListDto dto);

    RiskPeriodAmount getRiskProportion(@Param("carType") Integer carType);

    TietouFeatureStatisticGyh selectByMonthAndCarId(TietouFeatureStatisticGyh tietouFeatureStatisticGyh);

    int updateSecondMarkByVlpIds(@Param("ids") Set<Object> keys);

    List<TietouFeatureStatisticGyh> listByPeriod();
}