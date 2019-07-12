package com.drcnet.highway.service;

import com.drcnet.highway.dao.StationFeatureStatisticsMapper;
import com.drcnet.highway.dto.request.StationSpeedQueryDto;
import com.drcnet.highway.entity.StationFeatureStatistics;
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
 * @CreateTime: 2019/6/19 13:13
 * @Description:
 */
@Service
@Slf4j
public class StationFeatureStatisticService implements BaseService<StationFeatureStatistics,Integer> {

    @Resource
    private StationFeatureStatisticsMapper thisMapper;

    @Override
    public MyMapper<StationFeatureStatistics> getMapper() {
        return thisMapper;
    }

    /**
     * 查询车站路段速度异常数据
     * @param queryDto 查询条件
     */
    public PageVo<StationFeatureStatistics> listStationSpeedInfo(StationSpeedQueryDto queryDto) {
        PageHelper.startPage(queryDto.getPageNum(),queryDto.getPageSize());
        List<StationFeatureStatistics> stationFeatureStatistics = thisMapper.listStationSpeedInfo(queryDto);
        stationFeatureStatistics.stream().forEach(fs -> {
            fs.setAvgSpeed(fs.getAvgSpeedByVc(queryDto.getVc()));
            fs.setVc(queryDto.getVc());
        });

        return PageVo.of(stationFeatureStatistics);
    }
}
