package com.drcnet.highway.service;

import com.drcnet.highway.dao.StationDicMapper;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/7/15 14:46
 * @Desc:
 **/
@Service
@Slf4j
public class TietouStationDicService implements BaseService<StationDic, Integer> {
    @Resource
    private StationDicMapper thisMapper;

    @Override
    public MyMapper<StationDic> getMapper() {
        return thisMapper;
    }


    public StationDic selectByStationName(String stationName) {
        Example example = Example.builder(StationDic.class).build();
        example.createCriteria().andEqualTo("stationName", stationName);
        return thisMapper.selectOneByExample(example);
    }

    public StationDic getOrInertByName(String stationName) {
        StationDic stationDic = selectByStationName(stationName);
        if (stationDic == null) {
            StationDic dicInsert = new StationDic();
            dicInsert.setStationName(stationName);
            thisMapper.insertSelective(dicInsert);
            log.info("未找到站点:{},已新增一条记录", stationName);
            return dicInsert;
        } else {
            return stationDic;
        }
    }
}
