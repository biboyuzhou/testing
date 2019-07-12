package com.drcnet.highway.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Optional;

@Data
@Table(name = "tietou_feature_statistic")
public class TietouFeatureStatistic implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vlp")
    private String vlp;

    @Column(name = "vlp_id")
    private Integer vlpId;

    /**
     * 0货车，1客车
     */
    @Column(name = "car_type")
    private Integer carType;

    @Column(name = "same_car_number")
    private Integer sameCarNumber;

    private Integer speed;

    @Column(name = "low_speed")
    private Integer lowSpeed;

    @Column(name = "high_speed")
    private Integer highSpeed;

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

    private Integer total;

    private Short label;

    private Integer score;

    @Column(name = "use_flag")
    private Boolean useFlag;

    @Column(name = "transit_times")
    private Integer transitTimes;

    @Column(name = "is_free_car")
    private Integer isFreeCar;

    private static final long serialVersionUID = 1L;

    public void sumFeatures(TietouFeatureStatistic other){
        this.differentZhou += getOrZero(other.getDifferentZhou());
        this.diffFlagstationInfo += getOrZero(other.getDiffFlagstationInfo());
        this.flagstationLost += getOrZero(other.getFlagstationLost());
        this.longDisLightweight += getOrZero(other.getLongDisLightweight());
        this.shortDisOverweight += getOrZero(other.getShortDisOverweight());
        this.sameCarNumber += getOrZero(other.getSameCarNumber());
        this.sameCarSituation += getOrZero(other.getSameCarSituation());
        this.sameCarType += getOrZero(other.getSameCarType());
        this.sameStation += getOrZero(other.getSameStation());
        this.sameTimeRangeAgain += getOrZero(other.getSameTimeRangeAgain());
        this.minOutIn += getOrZero(other.getMinOutIn());
        this.speed += getOrZero(other.getSpeed());
        this.total += getOrZero(other.getTotal());
        this.transitTimes += getOrZero(other.getTransitTimes());
    }

    private int getOrZero(Integer num){
        return Optional.ofNullable(num).orElse(0);
    }
}