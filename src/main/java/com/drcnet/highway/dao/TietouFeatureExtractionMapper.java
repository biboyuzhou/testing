package com.drcnet.highway.dao;

import com.drcnet.highway.entity.TietouFeatureExtraction;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface TietouFeatureExtractionMapper extends MyMapper<TietouFeatureExtraction> {
    int updateSameTimeRangeByIds(@Param("ids") List<Integer> ids);

    int updateNull2Zero(@Param("extractionName")String extractionName);

    int updateSameCarOutInFlag(@Param("idSet") Set<Integer> idSet,@Param("extractionName") String extractionName);

    int updateSameCarOutInFlagOne(@Param("id") Integer id);

    List<TietouFeatureExtraction> listAllRiskOiginalData(@Param("featureTableName") String featureTableName);

    List<Integer> listSameRouteCarId(@Param("extractionName") String extractionName);

    List<TietouOrigin> listAllSameRouteByCar(@Param("vlpId") Integer vlpId, @Param("extractionName") String extractionName, @Param("originName")String originName);

    int updateSameRouteMark(@Param("id") Integer id,@Param("sameRouteMark") String sameRouteMark,@Param("extractionName")String originName);

    int updateSameRouteMarkAndLabel(@Param("id") Integer id,@Param("sameRouteMark") String sameRouteMark);

    List<Integer> listZhouShuDiffId(@Param("begin") int begin,@Param("end") int end);

    /**
     * 修改标记表 ,将错标为轴数异常的数据，改为轴数正常
     * @param idList
     * @return
     */
    int updateExtractionZhouShuDiff(@Param("idList") List<Integer> idList);

    int replaceSameCarNumByVlpId(@Param("monthTime") Integer monthTime);

    int replaceSameCarNumByEnVlpId(@Param("monthTime") Integer monthTime);


    int updateSameCarNumById(@Param("id") Integer id,@Param("flag") Integer flag);

    /**
     * 根据carId查询出same_route_mask 不为空的数据
     * @param carId
     * @return
     */
    List<TietouFeatureExtraction> listExtractionByCarId(@Param("carId") Integer carId);
}