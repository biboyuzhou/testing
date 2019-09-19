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
 * @Date: 2019/8/13 14:02
 * @Desc:
 **/
@Component
@Slf4j
public class TenRiskStatisticListener extends BaseListener implements SmartApplicationListener {
    @Resource
    private TietouCleanService tietouCleanService;


    @Override
    public void doWork(Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId, Integer previousCarId, Integer previousEndMonth, Integer taskId) {
        System.out.println("this is no.5 step...");
        //上次执行的tietouId这次不再执行
        tietouCleanService.insertExtractionData(previousTietouId + 1, maxTietouId);

        updateTaskState(taskId, CalculateStateEnum.TEN_RISK_STATISTIC.getCode());
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
        return CalculateStateEnum.TEN_RISK_STATISTIC.getCode();
    }


}
