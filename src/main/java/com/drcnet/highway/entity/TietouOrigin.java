package com.drcnet.highway.entity;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "tietou")
public class TietouOrigin implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDateTime entime;

    private String rk;

    private String envlp;

    private Integer envt;

    private Integer envc;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDateTime extime;

    private String ck;

    @Column(name = "rk_id")
    private Integer rkId;

    @Column(name = "ck_id")
    private Integer ckId;

    private String vlp;

    @Column(name = "vlp_id")
    private Integer vlpId;

    @Column(name = "envlp_id")
    private Integer envlpId;

    private Integer vc;

    private Integer vt;

    private String exlane;

    private String oper;

    private BigDecimal lastmoney;

    private BigDecimal freemoney;

    private Integer totalweight;

    private Integer axlenum;

    private Integer tolldistance;

    private String card;

    private String flagstationinfo;

    private String realflagstationinfo;

    private String inv;

    @Transient
    private Long time;

    @Column(name = "weight_limitation")
    private Integer weightLimitation;

    @Transient
    private Boolean mark;

    @Column(name = "month_time")
    private Integer monthTime;

    @Transient
    private String sameRouteMark;

    @Transient
    private Integer speed;

    @Transient
    private StationFeatureStatistics stationFeature;

    private static final long serialVersionUID = 1L;


}