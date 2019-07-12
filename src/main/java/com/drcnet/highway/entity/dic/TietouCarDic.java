package com.drcnet.highway.entity.dic;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table(name = "car_dic")
public class TietouCarDic implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 车牌号id
     */
    @Column(name = "car_no")
    private String carNo;

    /**
     * 省份
     */
    private Integer region;

    /**
     * 1为执法车辆
     */
    private Integer status;

    /**
     * 新增记录的时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 首次录入系统的月度批次
     */
//    @Column(name = "first_month_time")
    @Transient
    private Integer firstMonthTime;

    @Column(name = "use_flag")
    private Boolean useFlag;

    @Column(name = "white_flag")
    private Boolean whiteFlag;

    @Column(name = "car_type")
    private Integer carType;

    @Column(name = "axlenum")
    private Integer axlenum;

    @Column(name = "car_type_in")
    private Integer carTypeIn;

    @Column(name = "axlenum_in")
    private Integer axlenumIn;

    @Column(name = "weight_min")
    private Integer weightMin;

    @Column(name = "weight_max")
    private Integer weightMax;
    private static final long serialVersionUID = 1L;

}