package com.drcnet.highway.service.observe.listener;

import com.drcnet.highway.constants.enumtype.CalculateStateEnum;
import com.drcnet.highway.service.dataclean.TietouCleanService;
import com.drcnet.highway.service.observe.StartCalculateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/8/13 13:40
 * @Desc:
 **/
@Component
@Slf4j
public class UpdateMaxAndMinWeightListener extends BaseListener implements SmartApplicationListener {
    @Resource
    private TietouCleanService tietouCleanService;

    @Override
    public void doWork(Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId, Integer previousCarId, Integer previousEndMonth, Integer taskId) {
        System.out.println("this is no.3 step...");

        if (maxCarDicId > previousCarId) {
            tietouCleanService.updateMaxAndMinWeight(previousCarId, maxCarDicId);
            updateTaskState(taskId, CalculateStateEnum.MAX_AND_MIN_WEIGHT_STATISTIC.getCode());
        } else {
            updateTaskState(taskId, CalculateStateEnum.MAX_AND_MIN_WEIGHT_STATISTIC.getCode());
            log.info("------------UpdateMaxAndMinWeightListener 执行成功! 当前car_dic表最大id为：{}，前次执行的最大id为：{}，无新数据，本次不执行新计算！", maxCarDicId, previousCarId);

        }


    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType == StartCalculateEvent.class;
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    @Override
    public int getOrder() {
        return CalculateStateEnum.MAX_AND_MIN_WEIGHT_STATISTIC.getCode();
    }
}
