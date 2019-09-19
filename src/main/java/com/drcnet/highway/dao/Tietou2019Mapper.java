package com.drcnet.highway.dao;

import com.drcnet.highway.domain.SameCarNum;
import com.drcnet.highway.dto.request.TravelRecordQueryDto;
import com.drcnet.highway.entity.Tietou2019;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface Tietou2019Mapper extends MyMapper<Tietou2019> {
    /**
     * 查询最大id
     * @return
     */
    Integer selectMaxId();

    List<Tietou2019> listAllByperoid(@Param("begin") int begin, @Param("end") int end);

    /**
     * 根据车口车辆id查询所有行程记录id
     * @param carId
     * @return
     */
    List<Integer> getIdListByCarId(@Param("vlpId") Integer carId);

    /**
     * 查询tietou表某条记录在tietou2019表中的id
     * @param recordId
     * @return
     */
    Integer getTietou2019IdByTietouId(@Param("recordId") Integer recordId);

    /**
     * 根据车辆id查询在铁投路段的所有数据
     * @param id
     * @return
     */
    Integer getCountByVlpId(@Param("vlpId") Integer id);

    /**
     * 查询tietou_2019中车辆类型与car_DIC中不一致的数据，主要指客车、货车不一致
     * @param startId
     * @param endId
     * @return
     */
    List<SameCarNum> getDifferentCarType(@Param("startId") Integer startId, @Param("endId") Integer endId);

    /**
     * 根据id更新tietou_2019的vlpId
     * @param tietou2019Id
     * @param carId
     */
    void updateVlpIdById(@Param("id") Integer tietou2019Id, @Param("carId") Integer carId);

    /**
     * 将旧的vlp_id更新为新的
     * @param newVlpId
     * @param carNo
     * @param oldVlpId
     */
    void updateVlpIdAndVlpByVlpId(@Param("newVlpId") Integer newVlpId, @Param("carNo")String carNo, @Param("oldVlpId")Integer oldVlpId);

    /**
     * 在铁投2019中获取当前路段数据的最大id
     * @param stationIdList
     * @return
     */
    Integer selectMaxCurrentTietouId(@Param("stationIds") List<Integer> stationIdList);

    /**
     * 根据id区间查询数据
     * @param start
     * @param end
     * @return
     */
    List<Tietou2019> listByIdPeriod(@Param("start") int start, @Param("end") int end);

    int updateByPrimaryKeyAction(TietouOrigin tietou);

    /**
     * 根据idList查询tietou记录
     * @param idList
     * @return
     */
    List<TietouOrigin> listAllByIdList(@Param("idList") List<Integer> idList);

    /**
     * 根据时间区间查询tietou2019
     * @param travelRecordQueryDto
     * @return
     */
    List<TietouOrigin> listByTime(TravelRecordQueryDto travelRecordQueryDto);
}