package com.drcnet.highway.dao;

import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface TietouCarDicMapper extends MyMapper<TietouCarDic> {
    List<TietouCarDic> queryCarNo(@Param("carNo") String carNo, @Param("carType") Integer carType);

    Integer getMaxId(@Param("originFlag") Integer originFlag);

    List<TietouCarDic> selectByPeriod(@Param("begin") int begin, @Param("end") int end,@Param("originFlag") Integer originFlag);

    /**
     * 批量插入TietouCarDic
     * @param list
     */
    void insertByBatch(@Param("list")List<TietouCarDic> list);

    TietouCarDic selectByCarNo(@Param("carNo") String carNo);

    List<TietouCarDic> selectAbnormalCarDic();

    int updateCarDic(@Param("id") Integer id);

    TietouCarDic getIdByCarNo(@Param("carNo") String carNo);

    List<TietouCarDic> selectUsefulAndOverLengthCar();

    TietouCarDic selectById(@Param("id") Integer carNo);

    List<TietouCarDic> selectNewRepeatCar(@Param("maxId") int maxId);

    List<TietouCarDic> selectByIdIn(@Param("keys") Set<Object> keys);

    int updateMaxAndMinWeight(@Param("keys") Set<Object> keys,@Param("maxId") Integer maxId);
}