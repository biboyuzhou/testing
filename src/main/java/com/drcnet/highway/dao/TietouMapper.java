package com.drcnet.highway.dao;

import com.drcnet.highway.domain.RiskByRankQuery;
import com.drcnet.highway.domain.SameCarNum;
import com.drcnet.highway.domain.StartEndTimeDomain;
import com.drcnet.highway.dto.*;
import com.drcnet.highway.dto.request.BlackDetailQueryDto;
import com.drcnet.highway.dto.request.CarMonthQueryDto;
import com.drcnet.highway.dto.request.RiskInOutDto;
import com.drcnet.highway.dto.response.CommonTypeCountDto;
import com.drcnet.highway.dto.response.DiffCarNoEnvlpDto;
import com.drcnet.highway.dto.response.StationRiskCountDto;
import com.drcnet.highway.dto.response.StationTripCountDto;
import com.drcnet.highway.entity.Tietou2019;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface TietouMapper extends MyMapper<TietouOrigin> {
    Set<Integer> listAxleNumDifferIds();

    List<TurnoverStationDto> listInStation(@Param("carId") Integer carId);

    List<TurnoverStationDto> listOutStation(@Param("carId") Integer carId);

    List<TurnoverStationDto> listInStationDetail(@Param("carId") Integer carId, @Param("stationId") Integer stationId,@Param("tableName") String tableName);

    String selectCarType(@Param("carId") Integer carId,@Param("tableName") String tableName);

    Integer selectCarTypeCode(@Param("carId") Integer carId,@Param("tableName") String tableName);

    ViolationScore getMaxViolationScore(@Param("carId") Integer carId, @Param("tableName") String tableName,@Param("featureTableName") String featureTableName);

    List<PeriodAmountDto> listPeriodViolationAmount(@Param("carId") Integer carId,@Param("type") Integer type);

    List<TietouOrigin> listRiskInOutDetail(RiskInOutDto riskInOutDto);

    List<TietouOrigin> listRiskInOutDetailFromAll(RiskInOutDto riskInOutDto);

    RiskAmountDto getRiskAmount(RiskByRankQuery riskByRankQuery);

    List<CheatingViolationDto> listCheatingCar(@Param("tableName") String tableName, @Param("scoreName") String scoreName);

    RiskAmountDto getCheatingCount(@Param("carType") Integer carType);

    List<PeriodAmountDto> listCheatingPeriod(@Param("carType") Integer carType, @Param("queryMonth") Integer queryMonth);

    List<TietouOrigin> listDetailFromAllTimes(@Param("queryDto") BlackDetailQueryDto queryDto, @Param("carNoId") Integer carNoId, @Param("tables") List<String> tables);

    TietouOrigin selectByTableNameAndId(@Param("tableName") String tableName,@Param("id") Integer id);

    int updateMark(@Param("first") TietouOrigin tietou,@Param("tableName") String tableName);

    List<ThroughFrequencyDto> listThroughFrequency(@Param("dto") CarMonthQueryDto dto,@Param("tableName") String tableName);

    List<ThroughFrequencyDto> listInOutStationRelation(@Param("dto") CarMonthQueryDto dto,@Param("tableName") String tableName);

    List<TietouOrigin> selectAllTimeByMonth(@Param("monthTime") Integer monthTime,@Param("firstTime") LocalDateTime firstTime);

    List<Integer> listSamePeriodId(@Param("vlpId") Integer vlpId, @Param("entime") LocalDateTime entime,
                                   @Param("extime") LocalDateTime extime,@Param("monthTime") Integer monthTime,@Param("firstTime") LocalDateTime firstTime);

    Integer countThrough(@Param("carId") Integer carId);

    List<PeriodAmountDto> getAxlenum(@Param("carId") Integer carId);

    Integer selectMaxId();

    List<TietouOrigin> listByIdPeriod(@Param("begin") int begin,@Param("end") int end);

    List<TietouOrigin> listAllByIdPeriod(@Param("begin") int begin,@Param("end") int end);

    /**
     * 查询出现次数最多的车轴
     * @param vlpId 车牌ID
     * @param inOutFlag 1为以出站车牌查询，0为以进站车牌查询
     */
    Integer getMostUseAxleNum(@Param("vlpId") Integer vlpId,@Param("inOutFlag") int inOutFlag, @Param("isTruck") Integer isTruck);

    /**
     * 查询出现次数最多的车型
     * @param vlpId 车牌ID
     * @param inOutFlag 1为以出站车牌查询，0为以进站车牌查询
     */
    Integer getMostUseCarType(@Param("vlpId") Integer vlpId,@Param("inOutFlag") int inOutFlag, @Param("isTruck") Integer isTruck);

    Integer updateFlagLostExtractionTrue(@Param("flagLostMembers") Set<Object> flagLostMembers);

    /**
     * 分批查询tietou表的理论标志站和实际标志站数据
     * @param begin
     * @param end
     * @return
     */
    List<TietouOrigin> listStationFlagByPeroid(@Param("begin") int begin,@Param("end") int end);

    /**
     * 分批查询tietou表的理论标志站和实际标志站数据
     * @param begin
     * @param end
     * @return
     */
    List<TietouOrigin> listStationByPeroid(@Param("begin") int begin,@Param("end") int end);

    /**
     * 批量插入tietou
     * @param list
     */
    void insertBatch(@Param("list")List<TietouOrigin> list);

    /**
     * 查询envlp为空的数据
     * @return
     */
    List<TietouOrigin> listEnvlpIsNullData();

    int updateTietou(@Param("tietou") TietouOrigin tietou);

    List<HashMap<String, Integer>> countAxlenumByVlpId(@Param("vlpId") Integer vlpId);

    /**
     * 根据id查询所有不同的vlpId
     * @param idList
     * @return
     */
    List<Integer> listAllZhouShuDiffVlpId(@Param("idList") List<Integer> idList);

    /**
     * 根据vlpid查询所有de tietou
     * @param vlpIdList
     * @return
     */
    List<TietouOrigin> listAllZhouShuDiffTietous(@Param("vlpIdList") List<Integer> vlpIdList);

    List<TietouOrigin> listByVlpId(@Param("vlpId")Integer vlpId);

    List<TietouOrigin> listNonSameCarRecordByMonth(@Param("monthTime") Integer monthTime);

    List<SameCarEnvlpDto> listSameEnvlpOver2(@Param("vlpId") Integer vlpId);

    List<SameCarEnvlpDto> listEnVlpByVlpId(@Param("vlpId")Integer vlpId);

    /**
     * 根据起始位置查询轴数不一致的tietou表id、vkpId、alexnum
     * @param start
     * @param end
     * @return
     */
    List<TietouOrigin> listZhouShuDIffTietouByextraction(@Param("start") int start, @Param("end") int end);


    /**
     * 根据vlpid获取行程总数
     * @param vlpId
     * @return
     */
    Integer getCountByVlpId(@Param("vlpId") Integer vlpId,@Param("axlenum") Integer axlenum);

    Integer is2ndRoundCar(@Param("vlpId") Integer vlpId, @Param("secondRoundIds") List<Integer> secondRoundIds);

    /**
     * 根据进出口车牌、进出口日期、操作员查询行程记录
     * @param envlpId 进口车牌
     * @param vlpId 出口车牌
     * @param inStartTime 进口日期开始时间
     * @param inEndTime 出口日期结束时间
     * @param outStartTime 出口日期开始时间
     * @param outEndTime 出口日期结束时间
     * @param oper 操作员
     * @param carType
     * @return
     */
    List<TietouOrigin> queryTravelRecords(@Param("envlpId") Integer envlpId, @Param("vlpId") Integer vlpId,
                                          @Param("inStartTime") String inStartTime, @Param("inEndTime") String inEndTime, @Param("outStartTime") String outStartTime,
                                          @Param("outEndTime") String outEndTime, @Param("oper") String oper, @Param("carType") Integer carType,
                                          @Param("card") String card);

    /**
     * 根据指定车牌统计进出车牌不一致的各个入口车牌的次数
     * @param carId
     * @return
     */
    List<DiffCarNoEnvlpDto> stasticDiffCarNoByCarId(@Param("carId") Integer carId, @Param("tableName") String tableName, @Param("extractionName") String extractionName);

    /**
     * 查询指定出站车牌id的通行总次数
     * @param carId
     * @return
     */
    Integer staticOutNumByEnvlp(@Param("vlpId") Integer carId, @Param("tableName") String tableName);

    /**
     * 根据入站和出站车牌查询通行记录 按照出站时间排倒叙
     * @param envlpId
     * @param vlpId
     * @return
     */
    List<TietouOrigin> listAllByEnvlpAndVlp(@Param("envlpId") Integer envlpId, @Param("vlpId") Integer vlpId);

    /**
     * 根据入口车牌查与本次入口行程时间前后3天时间范围内间隔最近的出口行程
     * @param vlpId
     * @param startTimeStr
     * @param endTimeStr
     * @return
     */
    List<TietouOrigin> queryOutTravelByEnvlp(@Param("vlpId") Integer vlpId, @Param("outStartTime") String startTimeStr, @Param("outEndTime") String endTimeStr);

    List<PeriodAmountDto> listCarTypeDetail(@Param("vlpId") Integer vlpId, @Param("flag") Integer flag,@Param("isCurrent") Integer isCurrent);

    /**
     * 查询指定车牌的速度异常、时间重叠风险
     * @param carId
     * @return
     */
    List<TietouOrigin> listSpeedAndSameTimeByVlpId(@Param("vlpId") Integer carId);

    /**
     * 查询指定车牌的车牌不一致、速度异常风险
     * @param carId
     * @return
     */
    List<TietouOrigin> listDiffCarNoAndSpeedByVlpId(@Param("vlpId") Integer carId);

    /**
     * 查询指定车牌的车牌不一致、时间重叠风险
     * @param carId
     * @return
     */
    List<TietouOrigin> listDiffCarNoAndSameTimeByVlpId(@Param("vlpId") Integer carId);

    /**
     * 查询指定车牌的时间重叠、速度异常、车牌不一致综合风险
     * @param carId
     * @return
     */
    List<TietouOrigin> listAllRiskByVlpId(@Param("vlpId") Integer carId);

    List<TietouOrigin> listSpeedIllegalRecord();

    List<TietouOrigin> listAxlenumIllegalRecord();


    List<TietouOrigin> listByPeriod(@Param("start") int begin, @Param("end") int end);

    List<SameCarNum> getSameNumCar(@Param("maxId") Integer maxId);

    List<SameCarNum> getSameNumCarByVlpId(@Param("vlpId") Integer vlpId);

    int updateVlpIdById(@Param("vlpId") Integer vlpId, @Param("id") Integer id);

    int updateVlpIdAndEnvlpIdById(@Param("envlpId") Integer envlpId, @Param("vlpId") Integer vlpId, @Param("id") Integer id);

    /**
     * 二绕：统计站点之间互相通行的数据
     * @return
     */
    List<StationTripCountDto> secondStatisticTripCount(@Param("stationIdList") List<Integer> stationIdList);

    List<CommonTypeCountDto> statisticCarTypeCount();

    /**
     * 统计二绕每个站点的车辆总数、高中低风险数
     * @return
     * @param stationIdList
     */
    List<StationRiskCountDto> statistic2ndStationRiskCount(@Param("stationIdList") List<Integer> stationIdList);

    Integer testMycat(@Param("routingId") Integer routingId);

    List<Long> testListMycat(@Param("routingId") Integer routingId);

    List<TietouOrigin> listLowSpeedAndWeight(@Param("carNoId") Integer carNoId, @Param("limit") int limit
            ,@Param("overWeightFlag") boolean overWeightFlag,@Param("lightWeightFlag") boolean lightWeightFlag);

    List<TietouOrigin> listSameCarNumRecord(@Param("carNoId") Integer carNoId,@Param("limit") int limit);

    /**
     * 二绕：统计每个出口站的行程记录数量
     * @return
     */
    List<CommonTypeCountDto> secondStatisticCkCount(@Param("stationIdList") List<Integer> stationIdList);

    List<TietouOrigin> listByTimePeriod(@Param("beginDate") LocalDate beginDate,@Param("endDate") LocalDate endDate);

    LocalDate selectMaxTime(@Param("localDate") LocalDate localDate);

    int insertIgnore(TietouOrigin tietouOrigin);


    /**
     * 根据idList查询tietou记录
     * @param idList
     * @return
     */
    List<TietouOrigin> listAllByIdList(@Param("idList") List<Integer> idList);

    /**
     * 查询铁投表有重复记录的ID列表
     */
    List<String> selectRepeatIds(@Param("tietouFlag") boolean tietouFlag);

    int deleteRepeatByIds(@Param("ids") List<Integer> ids,@Param("tietouFlag") boolean tietouFlag);

    int deleteRepeatByTableIds(@Param("table") String table,@Param("ids") List<Integer> ids);


    Integer selectByInAndOut(TietouOrigin origin);

    Integer selectTietou2018ByInAndOut(TietouOrigin origin);

    int updateTotalDistance(@Param("id") Integer tietouId, @Param("distance") Double distance);

    int update2019TotalDistance(@Param("id") Integer tietou2019Id, @Param("distance") Double distance);

    int updateTietouVlpId(@Param("vlpId") Integer vlpId, @Param("carNo") String carNo, @Param("newVlpId") Integer newVlpId,
                          @Param("beginVc") Integer beginVc, @Param("endVc") Integer endVc);

    int updateTietouEnVlpId(@Param("vlpId") Integer vlpId, @Param("carNo") String carNo, @Param("newVlpId") Integer newVlpId,
                            @Param("beginVc") Integer beginVc, @Param("endVc") Integer endVc);

    void updateVlpByVlpId(@Param("vlpId") Integer id, @Param("carNo") String carNo);

    void updateEnVlpByVlpId(@Param("vlpId") Integer id, @Param("carNo") String carNo);

    /**
     * 查询配置表
     * @param type
     * @return
     */
    Integer selectExecuteConfig(@Param("type") Integer type);

    /**
     *
     * @param vlpId
     * @param carNo
     * @param oldTruckId
     */
    void updateVlpIdAndCarNo(@Param("vlpId") Integer vlpId, @Param("carNo") String carNo, @Param("oldTruckId") Integer oldTruckId);

    /**
     *
     * @param vlpId
     * @param carNo
     * @param oldTruckId
     */
    void updateEnVlpIdAndCarNo(@Param("vlpId") Integer vlpId, @Param("carNo") String carNo, @Param("oldTruckId") Integer oldTruckId);

    /**
     * 统计2019中指定vlpId的最大最小载重
     * @param vlpId
     * @param isTrunck
     * @return
     */
    SameCarNum getWeightFrom2019ByVlpId(@Param("vlpId") Integer vlpId, @Param("isTruck") Integer isTrunck);

    /**
     * 从铁投总数据中查询各异常的数量统计
     * @param query
     * @return
     */
    RiskAmountDto getRiskAmountFromAll(RiskByRankQuery query);

    /**
     * 获取当前路段tietou表的最大id
     * @return
     */
    Integer selectCurrentMaxId();

    /**
     * 批量插入tietou
     * @param list
     */
    void insertBatchWithIdAndIgnore(@Param("list") List<Tietou2019> list);

    int updateByPrimaryKeyAction(TietouOrigin tietou);

    /**
     * 查询指定vlp_id数据的最大出口时间和最小出口时间
     * @param vlpIdList
     * @return
     */
    StartEndTimeDomain getMaxMinExtimeOfTop20(@Param("vlpIdList") List<Integer> vlpIdList);
}