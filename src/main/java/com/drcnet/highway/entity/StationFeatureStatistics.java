package com.drcnet.highway.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Table(name = "station_feature_statistics")
public class StationFeatureStatistics implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ck_id")
    private Integer ckId;

    @Column(name = "rk_id")
    private Integer rkId;

    /**
     * 出战站名
     */
    private String ck;

    /**
     * 进站站名
     */
    private String rk;

    private Integer total;

    @Column(name = "speed_cheating")
    private Integer speedCheating;

    @Column(name = "avg_speed")
    private BigDecimal avgSpeed;

    @Column(name = "cheating_rate")
    private BigDecimal cheatingRate;

    @Column(name = "k1_speed")
    private BigDecimal k1Speed;

    @Column(name = "k2_speed")
    private BigDecimal k2Speed;

    @Column(name = "k3_speed")
    private BigDecimal k3Speed;

    @Column(name = "k4_speed")
    private BigDecimal k4Speed;

    @Column(name = "k5_speed")
    private BigDecimal k5Speed;

    @Column(name = "h1_speed")
    private BigDecimal h1Speed;

    @Column(name = "h2_speed")
    private BigDecimal h2Speed;

    @Column(name = "h3_speed")
    private BigDecimal h3Speed;

    @Column(name = "h4_speed")
    private BigDecimal h4Speed;

    @Column(name = "h5_speed")
    private BigDecimal h5Speed;

    @Transient
    private BigDecimal currentVcAvgSpeed;

    @Transient
    private Integer vc;

    private static final long serialVersionUID = 1L;

    /**
     * 根据vc设置路段对应车型的平均速度
     *
     * @param vc 车型
     */
    public void generateCurrentVcAvgSpeed(Integer vc) {
        if (vc == null || vc < 1 || vc > 15 || (vc > 5 && vc < 11)) {
            return;
        }
        if (vc>10){
            switch (vc){
                case 11:
                    currentVcAvgSpeed = h1Speed;
                    break;
                case 12:
                    currentVcAvgSpeed = h2Speed;
                    break;
                case 13:
                    currentVcAvgSpeed = h3Speed;
                    break;
                case 14:
                    currentVcAvgSpeed = h4Speed;
                    break;
                default:
                    currentVcAvgSpeed = h5Speed;
            }
        }else {
            switch (vc){
                case 1:
                    currentVcAvgSpeed = k1Speed;
                    break;
                case 2:
                    currentVcAvgSpeed = k2Speed;
                    break;
                case 3:
                    currentVcAvgSpeed = k3Speed;
                    break;
                case 4:
                    currentVcAvgSpeed = k4Speed;
                    break;
                default:
                    currentVcAvgSpeed = k5Speed;
            }
        }
    }

    public BigDecimal getAvgSpeedByVc(Integer vc){
        BigDecimal vcAvgSpeed;
        switch (vc) {
            case 1:
                vcAvgSpeed = getK1Speed();
                break;
            case 2:
                vcAvgSpeed = getK2Speed();
                break;
            case 3:
                vcAvgSpeed = getK3Speed();
                break;
            case 4:
                vcAvgSpeed = getK4Speed();
                break;
            case 5:
                vcAvgSpeed = getK5Speed();
                break;
            case 11:
                vcAvgSpeed = getH1Speed();
                break;
            case 12:
                vcAvgSpeed = getH2Speed();
                break;
            case 13:
                vcAvgSpeed = getH3Speed();
                break;
            case 14:
                vcAvgSpeed = getH4Speed();
                break;
            case 15:
                vcAvgSpeed = getH5Speed();
                break;
            default:
                vcAvgSpeed = null;
        }
        return vcAvgSpeed;
    }
}