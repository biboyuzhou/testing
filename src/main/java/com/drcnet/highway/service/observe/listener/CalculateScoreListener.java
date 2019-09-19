package com.drcnet.highway.service.observe.listener;

import com.drcnet.highway.constants.enumtype.CalculateStateEnum;
import com.drcnet.highway.service.ScriptExecuteService;
import com.drcnet.highway.service.observe.StartCalculateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/8/13 14:51
 * @Desc:
 **/
@Component
@Slf4j
public class CalculateScoreListener extends BaseListener implements SmartApplicationListener {
    @Resource
    private ScriptExecuteService scriptExecuteService;

    @Override
    public void doWork(Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId, Integer previousCarId, Integer previousEndMonth, Integer taskId) {
        System.out.println("this is no.11 step...");
        scriptExecuteService.executeArithmeticScript(taskId);
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
        return CalculateStateEnum.CALCULATE_SCORE.getCode();
    }
}
