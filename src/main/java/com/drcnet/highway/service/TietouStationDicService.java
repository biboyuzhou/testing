package com.drcnet.highway.service;

import com.drcnet.highway.dao.StationDicMapper;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/7/15 14:46
 * @Desc:
 **/
@Service
public class TietouStationDicService implements BaseService<StationDic, Integer> {
    @Resource
    private StationDicMapper thisMapper;

    @Override
    public MyMapper<StationDic> getMapper() {
        return thisMapper;
    }
}
