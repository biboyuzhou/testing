package com.drcnet.highway.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Table(name = "tietou_feature_statistic_month")
public class TietouFeatureStatisticMonth implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "month_time")
    private Integer monthTime;

    private String vlp;

    @Column(name = "vlp_id")
    private Integer vlpId;

    @Column(name = "same_car_number")
    private Integer sameCarNumber;

    private Integer speed;

    @Column(name = "same_car_type")
    private Integer sameCarType;

    @Column(name = "same_car_situation")
    private Integer sameCarSituation;

    @Column(name = "short_dis_overweight")
    private Integer shortDisOverweight;

    @Column(name = "long_dis_lightweight")
    private Integer longDisLightweight;

    @Column(name = "diff_flagstation_info")
    private Integer diffFlagstationInfo;

    @Column(name = "same_station")
    private Integer sameStation;

    @Column(name = "same_time_range_again")
    private Integer sameTimeRangeAgain;

    @Column(name = "min_out_in")
    private Integer minOutIn;

    @Column(name = "flagstation_lost")
    private Integer flagstationLost;

    @Column(name = "different_zhou")
    private Integer differentZhou;

    private Short label;

    private Integer score;

    /**
     * 0货车，1客车
     */
    @Column(name = "car_type")
    private Integer carType;

    @Column(name = "use_flag")
    private Boolean useFlag;

    private Integer total;

    @Column(name = "transit_times")
    private Integer transitTimes;

    private static final long serialVersionUID = 1L;

}