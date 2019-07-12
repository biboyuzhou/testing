package com.drcnet.highway.entity;

import com.drcnet.highway.annotation.ColumnName;
import com.drcnet.highway.constants.TimeConsts;
import com.drcnet.highway.dto.CheatingViolationDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Table(name = "tietou_feature_statistic_gyh")
public class TietouFeatureStatisticGyh implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 月度时间 yyyy-MM
     */
    @Column(name = "month_time")
    private Integer monthTime;

    /**
     * 车牌ID
     */
    @JsonProperty("carNoId")
    @Column(name = "vlp_id")
    private Integer vlpId;

    /**
     * 车牌号
     */
    @ColumnName("车牌号")
    @JsonProperty("carNo")
    @Column(name = "vlp")
    private String vlp;

    /**
     * 作弊类得分
     */
    @ColumnName("作弊类得分")
    private BigDecimal cheating;

    /**
     * 违规类得分
     */
    @ColumnName("违规类得分")
    private BigDecimal violation;

    /**
     * 综合得分
     */
    @ColumnName("综合得分")
    private BigDecimal score;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT, timezone = TimeConsts.GMT8)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = TimeConsts.TIME_FORMAT, timezone = TimeConsts.GMT8)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

    @Transient
    private Boolean blackFlag;

    public TietouFeatureStatisticGyh(CheatingViolationDto dto) {
        vlpId = dto.getCarNoId();
        vlp = dto.getCarNo();
        cheating = dto.getCheating();
        violation = dto.getViolation();
        score = dto.getScore();
    }
}