package com.drcnet.highway.service;

import com.drcnet.highway.dao.TietouSameStationFrequentlyMapper;
import com.drcnet.highway.dto.request.RiskInOutDto;
import com.drcnet.highway.entity.TietouSameStationFrequently;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.PageVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/5/20 14:52
 * @Description:
 */
@Service
@Slf4j
public class TietouSameStationFrequentlyService implements BaseService<TietouSameStationFrequently,Integer> {

    @Resource
    private TietouSameStationFrequentlyMapper thisMapper;

    @Override
    public MyMapper<TietouSameStationFrequently> getMapper() {
        return thisMapper;
    }


    public PageVo<TietouSameStationFrequently> listByQuery(RiskInOutDto riskInOutDto) {
        PageHelper.startPage(riskInOutDto.getPageNum(),riskInOutDto.getPageSize(),"out_time");
        TietouSameStationFrequently query = new TietouSameStationFrequently();
//        query.setMonthTime(Integer.valueOf(riskInOutDto.getBeginMonth()));
        query.setVlpId(riskInOutDto.getCarId());
        List<TietouSameStationFrequently> select = thisMapper.select(query);
        return PageVo.of(select);
    }

    /**
     * 判断数据库里是否有对应时间的数据
     * @param monthTime
     * @return
     */
    public boolean hasMonthTime(int monthTime) {
        thisMapper.hasMonthTime(monthTime);
        return false;
    }
}
