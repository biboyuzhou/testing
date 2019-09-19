package com.drcnet.highway.service.observe.listener;

import com.drcnet.highway.service.TaskService;
import com.drcnet.highway.service.observe.ListenerConfig;
import com.drcnet.highway.service.observe.StartCalculateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Vector;

/**
 * @Author jack
 * @Date: 2019/8/13 10:23
 * @Desc:
 **/
@Component
@Slf4j
public abstract class BaseListener implements SmartApplicationListener {
    /**
     * 当前最大tietouid
     */
    private Integer maxTietouId ;

    /**
     * 当前最大carid
     */
    private Integer maxCarDicId ;

    /**
     *前次执行到的tietouId
     */
    private Integer previousTietouId ;

    /**
     * 前次执行到的carId
     */
    private Integer previousCarId ;

    /**
     * 前次执行到的月份
     */
    private Integer previousEndMonth ;

    /**
     * 本次数据计算任务id
     */
    private Integer taskId;

    @Resource
    private TaskService taskService;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        StartCalculateEvent event = (StartCalculateEvent) applicationEvent;
        maxTietouId = event.getMaxTietouId();
        maxCarDicId = event.getMaxCarDicId();
        previousTietouId = event.getPreviousTietouId();
        previousCarId = event.getPreviousCarId();
        previousEndMonth = event.getPreviousEndMonth();
        taskId = event.getTaskId();
        if (maxTietouId == null) {
            maxTietouId = 1;
        }
        doWork(maxTietouId, maxCarDicId, previousTietouId, previousCarId, previousEndMonth, taskId);
    }

    /**
     * 做业务逻辑的抽象方法
     * @param maxTietouId
     * @param maxCarDicId
     * @param previousTietouId
     * @param previousCarId
     * @param previousEndMonth
     * @param startId
     */
    public abstract void doWork(Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId, Integer previousCarId, Integer previousEndMonth, Integer startId);

    /**
     * 获取当前listener的类型
     * @return
     */
    public abstract String getType();

    /**
     * 判断是否该执行该listener
     * @param type
     * @return
     */
    boolean isExecute(String type) {
        Vector<String> listenerVector = ListenerConfig.listenerMap.get(1);
        if (!listenerVector.contains(type)) {
            return true;
        }
        return false;
    }

    public void updateTaskState(Integer taskId, Integer state) {
        int result = taskService.updateTaskState(taskId, state);
        if (result == 1) {
            log.info("------------------更新task状态成功， taskId：{}，state：{}！", taskId, state);
        }
    }
}
