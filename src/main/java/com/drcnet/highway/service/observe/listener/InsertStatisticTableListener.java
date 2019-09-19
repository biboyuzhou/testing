package com.drcnet.highway.service.observe.listener;

import com.drcnet.highway.constants.enumtype.CalculateStateEnum;
import com.drcnet.highway.service.dataclean.TietouCleanService;
import com.drcnet.highway.service.observe.StartCalculateEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/8/13 14:16
 * @Desc:
 **/
@Component
public class InsertStatisticTableListener extends BaseListener implements SmartApplicationListener {
    @Resource
    private TietouCleanService tietouCleanService;

    @Override
    public void doWork(Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId, Integer previousCarId, Integer previousEndMonth, Integer taskId) {
        System.out.println("this is no.8 step...");
        tietouCleanService.truncateStatisticTable();
        tietouCleanService.insertStatisticTableData();

        updateTaskState(taskId, CalculateStateEnum.INSERT_STATISTIC_TABLE.getCode());
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
        return CalculateStateEnum.INSERT_STATISTIC_TABLE.getCode();
    }
}
