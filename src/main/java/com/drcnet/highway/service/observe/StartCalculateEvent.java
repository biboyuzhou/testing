package com.drcnet.highway.service.observe;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author jack
 * @Date: 2019/8/13 10:20
 * @Desc:
 **/
@Getter
public class StartCalculateEvent extends ApplicationEvent {

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


    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public StartCalculateEvent(Object source, Integer maxTietouId, Integer maxCarDicId, Integer previousTietouId,
                               Integer previousCarId, Integer previousEndMonth, Integer taskId) {
        super(source);
        this.maxTietouId = maxTietouId;
        this.maxCarDicId = maxCarDicId;
        this.previousTietouId = previousTietouId;
        this.previousCarId = previousCarId;
        this.previousEndMonth = previousEndMonth;
        this.taskId = taskId;
    }
}
