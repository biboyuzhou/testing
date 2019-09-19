package com.drcnet.highway.entity;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
    private Double routeAvgSpeed;

    @Transient
    private StationFeatureStatistics stationFeature;

    /**
     * 出站时间减进站时间的分钟数
     */
    @Transient
    private BigDecimal minute;

    @Transient
    private boolean tietouFlag;

    public BigDecimal getMinute() {
        if (entime != null && extime != null) {
            long end = extime.toEpochSecond(ZoneOffset.of("+8"));
            long start = entime.toEpochSecond(ZoneOffset.of("+8"));
            return new BigDecimal(end - start).divide(new BigDecimal(60), 0, BigDecimal.ROUND_DOWN);
        }
        return null;
    }

    public boolean isUseful(){
        return entime != null && extime != null && !StringUtils.isBlank(vlp) && rkId != null && ckId != null;
    }

    private static final long serialVersionUID = 1L;
}