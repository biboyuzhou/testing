package com.drcnet.highway.dao;

import com.drcnet.highway.dto.response.StationRiskCountDto;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.util.templates.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StationDicMapper extends MyMapper<StationDic> {
    /**
     * 根据公司code查询对应的站口信息
     * @param code
     * @return
     */
    List<StationDic> select2ndRound(@Param("code") Integer code);

    /**
     * 查询二绕所有站点信息
     * @return
     * @param code
     */
    List<StationRiskCountDto> list2ndStation(@Param("code") Integer code);

    /**
     * 查询所有站点数据
     * @return
     */
    List<StationDic> selectAllStation();

    /**
     * 根据id查询站点
     * @param rkId
     * @return
     */
    StationDic selectById(Integer rkId);

    /**
     *
     * @param stationName
     * @return
     */
    StationDic selectByStationName(@Param("stationName") String stationName);

    int insertStationName(StationDic dicInsert);

    /**
     * 根据高速公路code查询当前路公司的站点id
     * @param enterpriseCode
     * @return
     */
    List<Integer> getCurrentStationId(@Param("code") Integer enterpriseCode);
}