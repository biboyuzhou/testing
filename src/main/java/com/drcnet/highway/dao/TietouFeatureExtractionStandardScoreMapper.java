package com.drcnet.highway.dao;

import com.drcnet.highway.entity.TietouFeatureExtractionStandardScore;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TietouFeatureExtractionStandardScoreMapper extends MyMapper<TietouFeatureExtractionStandardScore> {
    List<TietouFeatureStatisticGyh> listCheatingAndViolationData(@Param("beginTime") Integer beginTime);

    TietouFeatureExtractionStandardScore getMaxViolationScore(TietouFeatureExtractionStandardScore query);
}