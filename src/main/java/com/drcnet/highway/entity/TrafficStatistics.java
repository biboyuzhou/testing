package com.drcnet.highway.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table(name = "traffic_statistics")
@NoArgsConstructor
public class TrafficStatistics implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 站点id
     */
    @Column(name = "station_id")
    private Integer stationId;

    @Column(name = "station_name")
    private String stationName;

    /**
     * 0入口，1出口
     */
    @Column(name = "bound_type")
    private Integer boundType;

    /**
     * 日期
     */
    @Column(name = "current_day")
    private LocalDate currentDay;

    /**
     * 通行次数
     */
    private Integer amount;

    @Column(name = "in_amount")
    private Integer inAmount;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "use_flag")
    private Boolean useFlag;

    private static final long serialVersionUID = 1L;

}