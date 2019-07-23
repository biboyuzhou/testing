package com.drcnet.highway.dao;

import com.drcnet.highway.dto.response.StationRiskCountDto;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.util.templates.MyMapper;

import java.util.List;

public interface StationDicMapper extends MyMapper<StationDic> {
    List<StationDic> select2ndRound();

    /**
     * 查询二绕所有站点信息
     * @return
     */
    List<StationRiskCountDto> list2ndStation();
}