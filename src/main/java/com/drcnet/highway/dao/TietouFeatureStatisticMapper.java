package com.drcnet.highway.dao;

import com.drcnet.highway.domain.StatisticCount;
import com.drcnet.highway.entity.TietouFeatureStatistic;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TietouFeatureStatisticMapper extends MyMapper<TietouFeatureStatistic> {
    List<TietouFeatureStatisticGyh> listAllRiskData(@Param("statisticName") String statisticName);

    List<TietouFeatureStatistic> listByMonthTime(@Param("monthTime") Integer monthTime);

    Integer selectMaxId();

    List<TietouFeatureStatistic> listVlpIdByIdPeriod(@Param("begin") int begin,@Param("end") int end);

    int updateCarTypeById(@Param("id") Integer id,@Param("carType") Integer carType);

    /**
     * 查询进出车牌不一致超过两次的车牌ID
     * @return
     */
    List<Integer> listOver2SameCarNumVlpIds();

    /**
     * 批量插入tietou
     * @param list
     */
    void insertBatch(@Param("list")List<TietouFeatureStatistic> list);

    /**
     * 批量更改通行总次数
     * @param statisticCountList
     */
    void updatetransitTimesByBatch(@Param("list") List<StatisticCount> statisticCountList);

    /**
     * 查询免费车辆的车牌和车牌id
     * @return
     */
    List<TietouFeatureStatistic> listFreeCar();

    /**
     * 在将每项异常的数量统计完写入static表后，在算法跑分之前需要将is_free_car进行赋值
     * 方便排除免费的内部车辆
     * 1：免费车辆
     * @param maxId
     * @return
     */
    int updateIsFreeCar(@Param("maxId") Integer maxId);

    int copyScore2Static();
}