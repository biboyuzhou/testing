package com.drcnet.highway.service;

import com.drcnet.highway.config.LocalVariableConfig;
import com.drcnet.highway.constants.TipsConsts;
import com.drcnet.highway.dao.DataImportTaskMapper;
import com.drcnet.highway.dao.StationDicMapper;
import com.drcnet.highway.dao.TrafficStatisticsMapper;
import com.drcnet.highway.dto.request.DateSearchDto;
import com.drcnet.highway.dto.response.TrafficStatisticsVo;
import com.drcnet.highway.entity.DataImportTask;
import com.drcnet.highway.entity.TrafficStatistics;
import com.drcnet.highway.exception.MyException;
import com.drcnet.highway.util.EntityUtil;
import com.drcnet.highway.util.templates.BaseService;
import com.drcnet.highway.util.templates.MyMapper;
import com.drcnet.highway.vo.TranfficStatisticsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/14 17:47
 * @Description:
 */
@Service
@Slf4j
public class TrafficStatisticsService implements BaseService<TrafficStatistics, Integer> {

    private Lock incrementLock = new ReentrantLock();
    private Lock newDataLock = new ReentrantLock();

    @Resource
    private LocalVariableConfig localVariableConfig;

    @Resource
    private TrafficStatisticsMapper thisMapper;
    @Resource
    private DataImportTaskMapper dataImportTaskMapper;
    @Resource
    private StationDicMapper stationDicMapper;

    @Override
    public MyMapper<TrafficStatistics> getMapper() {
        return thisMapper;
    }

    /**
     * 统计所有通行次数
     */
    @Transactional
    public void statisticsAllTraffic() {
        if (newDataLock.tryLock()) {
            try {
                //删减表
                thisMapper.truncate();
                //统计每天每个出站口的数据
                thisMapper.insertCkStatisticData();
                //统计每天每个出口站的数据
                thisMapper.insertRkStatisticData();
                //查询出口数据里的进站数据
                List<TrafficStatistics> trafficStatistics = thisMapper.selectRkStatisticData();
                for (TrafficStatistics trafficStatistic : trafficStatistics) {
                    LocalDate currentDay = trafficStatistic.getCurrentDay();
                    Integer stationId = trafficStatistic.getStationId();
                    if (stationId == null || currentDay == null) {
                        continue;
                    }
                    TrafficStatistics query = new TrafficStatistics();
                    query.setStationId(stationId);
                    query.setCurrentDay(currentDay);
                    query.setBoundType(trafficStatistic.getBoundType());
                    TrafficStatistics res = thisMapper.selectOne(query);
                    if (res == null) {
                        trafficStatistic.setAmount(0);
                        thisMapper.insertSelective(trafficStatistic);
                    } else {
                        TrafficStatistics update = new TrafficStatistics();
                        update.setInAmount(trafficStatistic.getInAmount());
                        update.setId(res.getId());
                        thisMapper.updateByPrimaryKeySelective(update);
                    }
                }

            } catch (Exception e) {
                log.error("{}", e);
                throw new MyException(TipsConsts.SERVER_ERROR);
            } finally {
                newDataLock.unlock();
            }
        } else {
            throw new MyException("正在统计中，请勿重复点击");
        }
    }

    /**
     * 获得站点统计数据
     */
    public TranfficStatisticsVo listStationTrafficStatistics(DateSearchDto dateSearchDto, Integer type, Integer flag) {
        List<Integer> stationIdList = stationDicMapper.getCurrentStationId(localVariableConfig.getEnterpriseCode());

        TranfficStatisticsVo tranfficStatisticsVo = new TranfficStatisticsVo();
        if (dateSearchDto.getEndDate() == null) {
            //查询最新一天
            LocalDate newestDay = thisMapper.getNewestDay(type,stationIdList);
            if (newestDay == null) {
                return tranfficStatisticsVo;
            }
            dateSearchDto.setEndDate(newestDay);
            if (dateSearchDto.getBeginDate() == null) {
                dateSearchDto.setBeginDate(newestDay.minusMonths(1));
            }
        }
        EntityUtil.copyNotNullFields(dateSearchDto, tranfficStatisticsVo);

        List<TrafficStatisticsVo> periodAmountDtos = new ArrayList<>();
        if (flag == 0) {
            periodAmountDtos = thisMapper.listStationTrafficStatistics(dateSearchDto, type,stationIdList);
        } else if (flag == 1) {
            periodAmountDtos = thisMapper.listDateTrafficStatistics(dateSearchDto, type,stationIdList);
        }
        tranfficStatisticsVo.setAmountList(periodAmountDtos);
        return tranfficStatisticsVo;
    }

    /**
     * 增量数据统计
     *
     * @param statisticsList
     * @param trafficStatisticsService
     */
    @Async("taskExecutor")
    public void incrementDataStatistics(List<TrafficStatistics> statisticsList, Integer taskId, TrafficStatisticsService trafficStatisticsService) {
        if (CollectionUtils.isEmpty(statisticsList)) {
            return;
        }
        incrementLock.lock();
        try {
            trafficStatisticsService.incrementDataStatistics(statisticsList);
            DataImportTask task = new DataImportTask();
            task.setId(taskId);
            task.setStatisticFlag(true);
            dataImportTaskMapper.updateByPrimaryKeySelective(task);
        } catch (Exception e) {
            log.error("{}", e);
        } finally {
            incrementLock.unlock();
        }
    }

    @Transactional
    public void incrementDataStatistics(List<TrafficStatistics> statisticsList) {
        LocalDateTime now = LocalDateTime.now();
        for (TrafficStatistics trafficStatistics : statisticsList) {
            TrafficStatistics res = thisMapper.selectByUniqueKey(trafficStatistics);
            if (res == null) {
                trafficStatistics.setCreateTime(now);
                trafficStatistics.setUseFlag(true);
                thisMapper.insertSelective(trafficStatistics);
            } else {
                res.setAmount(res.getAmount() + trafficStatistics.getAmount());
                res.setUpdateTime(now);
                thisMapper.updateByPrimaryKeySelective(res);
            }
        }
    }
}
