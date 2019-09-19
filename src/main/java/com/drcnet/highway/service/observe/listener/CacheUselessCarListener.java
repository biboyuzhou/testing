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
public class CacheUselessCarListener extends BaseListener implements SmartApplicationListener {
    @Resource
    private TietouCleanService tietouCleanService;

    @Override
    public void doWork(Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId, Integer previousCarId, Integer previousEndMonth, Integer taskId) {
        System.out.println("this is no.4 step...");
        //因可能添加了白名单车辆，所以需重新缓存异常及白名单车牌
        /*if (maxCarDicId.equals(previousCarId)) {
            updateTaskState(taskId, CalculateStateEnum.CACHE_USELESS_CAR.getCode());
            log.info("------------CacheUselessCarListener 执行成功! 当前car_dic表最大id为：{}，前次执行的最大id为：{}，无新数据，本次不执行新计算！", maxCarDicId, previousCarId);
            return;
        }*/

        //无效车牌的数据不多，删除缓存后重新执行，从第一条数据开始
        tietouCleanService.initUseFlagFalseCarDic2Cache(1);

        updateTaskState(taskId, CalculateStateEnum.CACHE_USELESS_CAR.getCode());
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
        return CalculateStateEnum.CACHE_USELESS_CAR.getCode();
    }
}
