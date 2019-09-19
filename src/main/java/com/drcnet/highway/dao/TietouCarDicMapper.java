package com.drcnet.highway.dao;

import com.drcnet.highway.entity.dic.TietouCarDic;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface TietouCarDicMapper extends MyMapper<TietouCarDic> {
    List<TietouCarDic> queryCarNo(@Param("carNo") String carNo, @Param("carType") Integer carType);

    Integer getMaxId();

    Integer getCurrentMaxId();

    List<TietouCarDic> selectByPeriod(@Param("begin") int begin, @Param("end") int end);

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

    int updateMaxAndMinWeight(@Param("keys") Set<Integer> keys, @Param("startId") Integer startId, @Param("endId") Integer endId,@Param("vlpFlag") boolean vlpFlag);

    TietouCarDic selectByCarNoFromAll(@Param("carNo") String carNo);

    List<TietouCarDic> selectByCarNoIn(@Param("carNoList") List<String> carNoList);

    List<TietouCarDic> listHighRiskCar(@Param("score") double score);

    List<TietouCarDic> selectUselessCarByPeriod(@Param("begin") int begin, @Param("end") int end);

    /**
     * 根据id更改二绕中car_dic的id
     * @param carNo
     * @param id
     * @return
     */
    int updateCarNoById(@Param("carNO") String carNo, @Param("id") Integer id);

    /**
     * 新增新的car到二绕car_dic中
     * @param newCar
     * @return
     */
    int insertNewCar(TietouCarDic newCar);

    /**
     * 通过id更新car_dic 的轴数、车辆类型、最小最大载重
     * @param carDic
     */
    int updateAxlenumById(TietouCarDic carDic);

    /**
     * 根据id将车辆移除白名单
     * @param id
     */
    void moveOutWhiteFlagById(@Param("id") Integer id);

    /**
     * 批量将use_flag置为0
     * @param idList
     */
    void updateCar2Unuse(@Param("idList") List<Integer> idList);

    /**
     * 更新white_flag,use_flag
     * @param tietouCarDic
     */
    void updateWhiteAndUseFlag(TietouCarDic tietouCarDic);

    /**
     * 查询白名单车辆
     * @return
     */
    List<TietouCarDic> selectWhiteFlag();

    /**
     * 查询车辆字典表中，车辆类型为货车但是车牌却不带货字的记录
     * @return
     */
    List<TietouCarDic> getTruckNoHuoList();

    void updateRegionById(@Param("value") int value, @Param("id") Integer id);
}