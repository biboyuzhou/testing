package com.drcnet.highway.dao;

import com.drcnet.highway.domain.StatisticCount;
import com.drcnet.highway.entity.TietouFeatureStatistic;
import com.drcnet.highway.entity.TietouFeatureStatisticGyh;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
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
    int updateIsFreeCar(@Param("maxId") Integer maxId, @Param("start") Integer start);

    int copyScore2Static();

    /**
     * 打标完成后执行sql，生成tietou_feature_statistic表的数据
     */
    void insertStatisticDataBySql();

    /**
     * 打标完成后执行sql，重新生成tietou_feature_statistic表的数据之前，先删除tietou_feature_statistic的数据
     */
    void truncateStatistic();

    /**
     * 将白名单数据的分数置为0
     * @param vlpId
     */
    void updateScoreByVlpId(@Param("vlpId") Integer vlpId);

    /**
     * 删除白名单数据时将异常分数恢复
     * @param vlpId
     */
    void updateScoreFromGyhByVlpId(@Param("vlpId") Integer vlpId);

    /**
     * 将car_dic表中useFlag为false的车牌在statistics表内设置is_free_car为1
     */
    int updateStatisticsFreeCar();

    /**
     * 根据vlpId查询分数
     * @param id
     * @return
     */
    BigDecimal getScoreByVlpId(@Param("vlpId") Integer id);

    int insertStatisticsMonthData(@Param("monthTime") int monthTime);

    void truncateStatisticMonth();

    int insertStatisticsByMonth();

    /**
     * 从铁投总表中拷贝statistic表的分数
     */
    int pullStatisticScoreFromAll();

    List<Integer> getTop20VlpId();

    TietouFeatureStatistic selectFromAllByVlpId(@Param("vlpId") Integer vlpId);
}