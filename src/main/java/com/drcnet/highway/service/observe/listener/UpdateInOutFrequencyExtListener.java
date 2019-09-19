package com.drcnet.highway.service.observe.listener;

import com.drcnet.highway.config.LocalVariableConfig;
import com.drcnet.highway.constants.CacheKeyConsts;
import com.drcnet.highway.constants.enumtype.CalculateStateEnum;
import com.drcnet.highway.service.TietouSameStationFrequentlyService;
import com.drcnet.highway.service.observe.StartCalculateEvent;
import com.drcnet.highway.util.DateUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author jack
 * @Date: 2019/8/13 14:30
 * @Desc:
 **/
@Component
public class UpdateInOutFrequencyExtListener extends BaseListener implements SmartApplicationListener {
    @Resource
    private TietouSameStationFrequentlyService tietouSameStationFrequentlyService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private LocalVariableConfig localVariableConfig;

    @Override
    public void doWork(Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId, Integer previousCarId, Integer previousEndMonth, Integer taskId) {
        System.out.println("this is no.10 step...");
        //tietouSameStationFrequentlyService.setInOutFrequencyExt();

        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(CacheKeyConsts.PREVIOUS_Id_CACHE);
        hashOperations.put(localVariableConfig.getPreviousTietouId(), maxTietouId);
        hashOperations.put(localVariableConfig.getPreviousCarId(), maxCarDicId);
        String currentMonth = DateUtils.getCurrentMonth();
        Integer endMonth = Integer.parseInt(currentMonth);
        hashOperations.put(localVariableConfig.getPreviousEndMonth(), endMonth);
        updateTaskState(taskId, CalculateStateEnum.UPDATE_IN_OUT_FREQUENCY_EXT.getCode());
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
        return CalculateStateEnum.UPDATE_IN_OUT_FREQUENCY_EXT.getCode();
    }
}
