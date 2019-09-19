package com.drcnet.highway.service.schedule;

import com.drcnet.highway.config.LocalVariableConfig;
import com.drcnet.highway.constants.CacheKeyConsts;
import com.drcnet.highway.constants.enumtype.*;
import com.drcnet.highway.dao.*;
import com.drcnet.highway.entity.Task;
import com.drcnet.highway.service.*;
import com.drcnet.highway.service.observe.StartCalculateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Author jack
 * @Date: 2019/8/13 17:21
 * @Desc:
 **/
@Component
@Slf4j
public class CalculateScheduleTask {


    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private TietouMapper tietouMapper;
    @Resource
    private TietouCarDicMapper tietouCarDicMapper;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private TaskMapper taskMapper;
    @Resource
    private LocalVariableConfig localVariableConfig;

    @Resource
    private Tietou2019Mapper tietou2019Mapper;
    @Resource
    private StationDicMapper stationDicMapper;

    @Resource
    private TietouService tietouService;
    @Resource
    private TietouExtractionService tietouExtractionService;
    @Resource
    private TietouSameStationFrequentlyService frequentlyService;
    @Resource
    private TietouStatisticsService tietouStatisticsService;
    @Resource
    private TietouScoreGyhService tietouScoreGyhService;

    /**
     * 3.添加定时任务
     * 或直接指定时间间隔，例如：每天凌晨1点执行
     * @Scheduled(fixedRate=5000)
     */
    @Scheduled(cron = "0 30 12 * * ?")
    public void configureTasksAm() {
        exectueTask();
    }


    /**
     * 3.添加定时任务
     * 或直接指定时间间隔，例如：每天凌晨1点执行
     * @Scheduled(fixedRate=5000)
     */
    @Scheduled(cron = "0 30 1 * * ?")
    public void configureTasksPm() {
        exectueTask();
    }



    public void exectueTask() {
        if (!localVariableConfig.getEnterpriseCode().equals(EnterpriseEnum.SECOND_ROUND.getCode())) {
            return;
        }

        Integer isExecute = tietouMapper.selectExecuteConfig(ScheduleTaskEnum.CALCULATE.getCode());
        if (isExecute.equals(YesNoEnum.NO.getCode())) {
            log.info("定时任务开关未开，本次不执行！");
            return;
        }
        List<Task> taskList = taskMapper.selectUnFinishTask();
        if (!CollectionUtils.isEmpty(taskList)) {
            log.info("还有未执行完成的任务，本次不执行！");
            return;
        }
        Integer maxTietouId = tietouMapper.selectMaxId();
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(CacheKeyConsts.PREVIOUS_Id_CACHE);
        Integer previousTietouId = hashOperations.get(localVariableConfig.getPreviousTietouId()) == null ? 1 : (Integer)hashOperations.get(localVariableConfig.getPreviousTietouId()) ;
        if (maxTietouId.equals(previousTietouId)) {
            log.info("---------当前tietou表最大id为：{}，前次执行的最大id为：{}，无新数据，本次不执行新计算！", maxTietouId, previousTietouId);
            return;
        }

        Integer maxCarDicId = tietouCarDicMapper.getMaxId();
        Integer previousCarId = hashOperations.get(localVariableConfig.getPreviousCarId()) == null ? 1 : (Integer) hashOperations.get(localVariableConfig.getPreviousCarId());
        Integer previousEndMonth = hashOperations.get(localVariableConfig.getPreviousEndMonth()) == null ? 201901 : (Integer) hashOperations.get(localVariableConfig.getPreviousEndMonth());

        Task task = new Task();
        task.setName("数据计算");
        task.setTaskType(TaskTypeEnum.CALCULATE_DATA.getCode());
        task.setCreateTime(new Date());
        task.setBeginTime(new Date());
        task.setCalNum(maxTietouId - previousTietouId);
        task.setState(CalculateStateEnum.BEGIN.getCode());
        taskMapper.insertSelective(task);

        Integer taskId = task.getId();

        StartCalculateEvent startCalculateEvent = new StartCalculateEvent(this, maxTietouId, maxCarDicId, previousTietouId, previousCarId, previousEndMonth, taskId);
        applicationContext.publishEvent(startCalculateEvent);
        System.out.println("执行静态定时任务时间: " + LocalDateTime.now());
    }


    /**
     * 3.添加定时任务
     * 或直接指定时间间隔，例如：每天凌晨1点执行
     * @Scheduled(fixedRate=5000)
     */
    @Scheduled(cron = "0 44 10 * * ?")
    public void pullCurrentRoadDataFromAll() {

        doPullCurrentRoadDataFromAll();

    }

    public void doPullCurrentRoadDataFromAll() {
        Integer isExecute = tietouMapper.selectExecuteConfig(ScheduleTaskEnum.PULL.getCode());
        if (isExecute.equals(YesNoEnum.NO.getCode())) {
            log.info("定时任务开关未开，本次不执行！");
            return;
        }
        Integer currentTietouId = tietouMapper.selectCurrentMaxId();
        List<Integer> stationIdList = stationDicMapper.getCurrentStationId(localVariableConfig.getEnterpriseCode());
        Integer allMaxTietouId = tietou2019Mapper.selectMaxCurrentTietouId(stationIdList);
        log.info("currentTietouId：{}， allMaxTietouId： {}", currentTietouId, allMaxTietouId);
        if (currentTietouId != null && currentTietouId.equals(allMaxTietouId)) {
            log.info("当前路段tietou最大id与tietou_2019中当前路段的最大id值一样，无更新数据，本次不用重新拉取数据！");
            return;
        }
        //1.
        pullTietouDataFromAll(currentTietouId, allMaxTietouId, stationIdList);
        log.info("----------------------拉取铁头的数据完成！");

        //2.拉取extraction
        pullExtractionDataFromAll(currentTietouId, allMaxTietouId);
        log.info("----------------------拉取extraction的数据完成！");

        //3.拉取同站先出后进数据
        pullSameStationFrequentlyFromAll(stationIdList);
        log.info("----------------------拉取同站先出后进的数据完成！");

        //4.处理statistic数据，先truncate表，再insert，再从总数据中拷贝分数到当前表，在生成gyh表数据
        generateStatisticData();
        log.info("----------------------拉取statistic的数据完成！");

        //5.生成gyh表数据
        insertStatisticGyhData();
        log.info("----------------------拉取gyh的数据完成！");
    }

    /**
     *  拉取tietoudata
     * @param currentTietouId
     * @param allMaxTietouId
     * @param stationIdList
     */
    private void pullTietouDataFromAll(Integer currentTietouId, Integer allMaxTietouId, List<Integer> stationIdList) {
         tietouService.pullTietouDataFromAll(currentTietouId, allMaxTietouId, stationIdList);
    }

    /**
     * 拉取extraction data
     * @param currentTietouId
     * @param allMaxTietouId
     */
    private void pullExtractionDataFromAll(Integer currentTietouId, Integer allMaxTietouId) {
        tietouExtractionService.pullExtractionDataFromAll(currentTietouId, allMaxTietouId);
    }

    /**
     * 拉取SameStationFrequently data
     * @param stationIdList
     */
    private void pullSameStationFrequentlyFromAll(List<Integer> stationIdList) {
        frequentlyService.pullSameStationFrequentlyFromAll(stationIdList);
    }

    /**
     * 重新生成tietou_feature_statistic表的数据
     */
    private void generateStatisticData() {
        tietouStatisticsService.generateStatisticData();
    }

    /**
     * 重新生成tietou_feature_statistic_gyh表的数据
     */
    private void insertStatisticGyhData() {
        tietouScoreGyhService.generateStatisticGyhData();
    }
}
