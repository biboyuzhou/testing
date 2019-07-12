package com.drcnet.highway.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "tietou_feature_extraction_standard_score")
public class TietouFeatureExtractionStandardScore implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "car_num")
    private String carNum;

    @Column(name = "car_num_id")
    private Integer carNumId;

    @Column(name = "month_time")
    private Integer monthTime;

    @Column(name = "same_car_number")
    private BigDecimal sameCarNumber;

    private BigDecimal speed;

    @Column(name = "same_car_type")
    private BigDecimal sameCarType;

    @Column(name = "same_car_situation")
    private BigDecimal sameCarSituation;

    @Column(name = "short_dis_overweight")
    private BigDecimal shortDisOverweight;

    @Column(name = "long_dis_lightweight")
    private BigDecimal longDisLightweight;

    @Column(name = "diff_flagstation_info")
    private BigDecimal diffFlagstationInfo;

    @Column(name = "same_station")
    private BigDecimal sameStation;

    @Column(name = "same_time_range_again")
    private BigDecimal sameTimeRangeAgain;

    @Column(name = "min_out_in")
    private BigDecimal minOutIn;

    @Column(name = "flagstation_lost")
    private BigDecimal flagstationLost;

    @Column(name = "different_zhou")
    private BigDecimal differentZhou;

    private BigDecimal label;

    private BigDecimal score;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;

}