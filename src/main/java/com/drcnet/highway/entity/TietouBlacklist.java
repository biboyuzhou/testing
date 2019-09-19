package com.drcnet.highway.entity;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "tietou_blacklist")
public class TietouBlacklist implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 车牌ID
     */
    @Column(name = "car_no_id")
    private Integer carNoId;

    /**
     * 车牌号
     */
    @Column(name = "car_no")
    private String carNo;

    /**
     * 作弊类得分
     */
    private BigDecimal cheating;

    /**
     * 违规类得分
     */
    private BigDecimal violation;

    private BigDecimal score;

    /**
     * 风险类型
     */
    @Column(name = "risk_flag")
    private String riskFlag;

    /**
     * 逃费行为
     */
    @Column(name = "escape_behavior")
    private String escapeBehavior;

    /**
     * 风险描述
     */
    private String description;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "use_flag")
    private Boolean useFlag;

    private static final long serialVersionUID = 1L;

}