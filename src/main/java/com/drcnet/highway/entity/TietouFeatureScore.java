package com.drcnet.highway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "tietou_feature_score201812")
public class TietouFeatureScore implements Serializable {
    /**
     * 行车记录id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 同站进出风险
     */
    @Column(name = "same_station")
    private Integer sameStation;

    /**
     * 平均速度异常风险
     */
    private Integer speed;

    /**
     * 进出车牌不一致风险
     */
    @Column(name = "same_car_number")
    private Integer sameCarNumber;

    /**
     * 进出车型不一致风险
     */
    @Column(name = "same_car_type")
    private Integer sameCarType;

    /**
     * 进出车情不一致风险
     */
    @Column(name = "same_car_situation")
    private Integer sameCarSituation;

    /**
     * 短途重载风险
     */
    @Column(name = "short_dis_overweight")
    private Integer shortDisOverweight;

    /**
     * 长途轻载风险
     */
    @Column(name = "long_dis_lightweight")
    private Integer longDisLightweight;

    /**
     * 轴数不同风险
     */
    @Column(name = "different_zhou")
    private Integer differentZhou;

    /**
     * 路段标志异常风险
     */
    @Column(name = "diff_flagstation_info")
    private Integer diffFlagstationInfo;

    private static final long serialVersionUID = 1L;

}