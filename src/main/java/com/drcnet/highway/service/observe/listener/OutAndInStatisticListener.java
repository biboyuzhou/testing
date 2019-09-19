package com.drcnet.highway.service.observe.listener;

import com.drcnet.highway.constants.enumtype.CalculateStateEnum;
import com.drcnet.highway.service.dataclean.TietouCleanService;
import com.drcnet.highway.service.observe.StartCalculateEvent;
import com.drcnet.highway.util.DateUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/8/13 14:11
 * @Desc:
 **/
@Component
public class OutAndInStatisticListener extends BaseListener implements SmartApplicationListener {
    @Resource
    private TietouCleanService tietouCleanService;

    @Override
    public void doWork(Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId, Integer previousCarId, Integer previousEndMonth, Integer taskId) {
        System.out.println("this is no.7 step...");
        String currentMonth = DateUtils.getCurrentMonth();
        Integer endMonth = Integer.parseInt(currentMonth);
        for (int i = previousEndMonth; i <= endMonth; i++) {
            tietouCleanService.calcOutAndInByJava(i);
        }

        updateTaskState(taskId, CalculateStateEnum.OUT_AND_IN_STATISTIC.getCode());
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
        return CalculateStateEnum.OUT_AND_IN_STATISTIC.getCode();
    }
}
