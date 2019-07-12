package com.drcnet.highway.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.drcnet.highway.constants.TimeConsts.GMT8;
import static com.drcnet.highway.constants.TimeConsts.TIME_FORMAT;

@Data
@Table(name = "tietou_same_station_frequently")
public class TietouSameStationFrequently implements Serializable {
    /**
     * 短时间先出后进记录表
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 车牌号id
     */
    @JsonProperty("carNoId")
    @Column(name = "vlp_id")
    private Integer vlpId;

    @JsonProperty("car_no")
    @Column(name = "vlp")
    private String vlp;
    /**
     * 站点ID
     */
    @Column(name = "station_id")
    private Integer stationId;

    /**
     * 收费站点名
     */
    @JsonProperty("stationName")
    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    @Column(name = "toll_station_name")
    private String tollStationName;

    /**
     * 出站时间
     */
    @JsonProperty("extime")
    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    @Column(name = "out_time")
    private LocalDateTime outTime;

    /**
     * 进站时间
     */
    @JsonProperty("entime")
    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    @Column(name = "in_time")
    private LocalDateTime inTime;

    /**
     * 间隔时间 秒数
     */
    @Column(name = "interval_time")
    private Integer intervalTime;

    /**
     * 月份 201901
     */
    @Column(name = "month_time")
    private Integer monthTime;

    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "in_id")
    private Integer inId;

    @Column(name = "out_id")
    private Integer outId;

    @Column(name = "last_distance")
    private Integer lastDistance;

    @Column(name = "next_distance")
    private Integer nextDistance;

    @Column(name = "last_weight")
    private Integer lastWeight;

    @Column(name = "next_weight")
    private Integer nextWeight;

    @Column(name = "last_in_station_id")
    private Integer lastInStationId;

    @Column(name = "last_in_station_name")
    private String lastInStationName;

    @Column(name = "next_out_station_id")
    private Integer nextOutStationId;

    @Column(name = "next_out_station_name")
    private String nextOutStationName;

    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    @Column(name = "last_entime")
    private LocalDateTime lastEntime;

    @JsonFormat(pattern = TIME_FORMAT,timezone = GMT8)
    @Column(name = "next_extime")
    private LocalDateTime nextExtime;

    private static final long serialVersionUID = 1L;

}