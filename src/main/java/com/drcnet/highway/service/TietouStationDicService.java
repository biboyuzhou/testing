package com.drcnet.highway.service;

import com.drcnet.highway.dao.StationDicMapper;
import com.drcnet.highway.dto.response.StationDicDto;
import com.drcnet.highway.entity.dic.StationDic;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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

    public Integer getOrInertByName(String stationName) {
        if (StringUtils.isEmpty(stationName)){
            return null;
        }
        StationDic stationDic = selectByStationName(stationName);
        if (stationDic == null) {
            StationDic dicInsert = new StationDic();
            dicInsert.setStationName(stationName);
            thisMapper.insertSelective(dicInsert);
            log.info("未找到站点:{},已新增一条记录", stationName);
            return dicInsert.getId();
        } else {
            return stationDic.getId();
        }
    }

    /**
     * 查询二绕西的收费站点
     * @return
     */
    public List<StationDicDto> get2ndRoundStation() {
        List<StationDic> dicList = thisMapper.select2ndRound();
        List<StationDicDto> stationDicDtoList = new ArrayList<>(dicList.size());
        for (StationDic dic : dicList) {
            StationDicDto dicDto = new StationDicDto();
            dicDto.setId(dic.getId());
            dicDto.setStationName(dic.getStationName());
            stationDicDtoList.add(dicDto);
        }
        return stationDicDtoList;
    }
}
