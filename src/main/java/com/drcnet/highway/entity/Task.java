package com.drcnet.highway.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Data
public class Task implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 任务类型，1导入出口数据，2导入入口数据
     * @see com.drcnet.highway.constants.enumtype.TaskTypeEnum
     */
    @Column(name = "task_type")
    private Integer taskType;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 开始时间
     */
    @Column(name = "begin_time")
    private Date beginTime;

    /**
     * 完成时间
     */
    @Column(name = "finish_time")
    private Date finishTime;

    /**
     * 任务状态，0未开始，1进行中，2已完成，3执行失败
     * 数据计算时状态值
     * @see com.drcnet.highway.constants.enumtype.CalculateStateEnum
     */
    private Integer state;

    /**
     * 本次任务计算数据条数
     */
    @Column(name = "cal_num")
    private Integer calNum;

    private static final long serialVersionUID = 1L;

}