package com.drcnet.highway.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name = "tietou_outbound")
public class TietouOutbound implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "month_time")
    private Integer monthTime;

    /**
     * 进站时间
     */
    private LocalDateTime entime;

    /**
     * 进站站名
     */
    private String rk;

    @Column(name = "rk_id")
    private Integer rkId;

    /**
     * 进站车牌
     */
    private String envlp;

    @Column(name = "envlp_id")
    private Integer envlpId;

    /**
     * 进站车情
     */
    private Integer envt;

    /**
     * 进站车型
     */
    private Integer envc;

    /**
     * 出站时间
     */
    private LocalDateTime extime;

    /**
     * 出战站名
     */
    private String ck;

    @Column(name = "ck_id")
    private Integer ckId;

    /**
     * 出战车牌
     */
    private String vlp;

    @Column(name = "vlp_id")
    private Integer vlpId;

    /**
     * 出战车型
     */
    private Integer vc;

    /**
     * 出战车情
     */
    private Integer vt;

    /**
     * 出站通道
     */
    private String exlane;

    /**
     * 操作人员
     */
    private String oper;

    /**
     * 费用
     */
    private BigDecimal lastmoney;

    private BigDecimal freemoney;

    /**
     * 总重
     */
    private Integer totalweight;

    /**
     * 轴数
     */
    private Integer axlenum;

    /**
     * 总里程
     */
    private Integer tolldistance;

    /**
     * 卡号
     */
    private String card;

    /**
     * 路段标志
     */
    private String flagstationinfo;

    /**
     * 实际路段标志
     */
    private String realflagstationinfo;

    private String inv;

    @Column(name = "unique_key")
    private String uniqueKey;

    @Column(name = "weight_limitation")
    private Integer weightLimitation;

    private static final long serialVersionUID = 1L;

}