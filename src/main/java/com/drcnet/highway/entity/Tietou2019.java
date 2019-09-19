package com.drcnet.highway.entity;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "tietou_2019")
@Data
public class Tietou2019 implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "month_time")
    private Integer monthTime;

    /**
     * 进站时间
     */
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
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
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
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

    @Column(name = "weight_limitation")
    private Integer weightLimitation;

    @Column(name = "second_flag")
    private Byte secondFlag;

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", monthTime=").append(monthTime);
        sb.append(", entime=").append(entime);
        sb.append(", rk=").append(rk);
        sb.append(", rkId=").append(rkId);
        sb.append(", envlp=").append(envlp);
        sb.append(", envlpId=").append(envlpId);
        sb.append(", envt=").append(envt);
        sb.append(", envc=").append(envc);
        sb.append(", extime=").append(extime);
        sb.append(", ck=").append(ck);
        sb.append(", ckId=").append(ckId);
        sb.append(", vlp=").append(vlp);
        sb.append(", vlpId=").append(vlpId);
        sb.append(", vc=").append(vc);
        sb.append(", vt=").append(vt);
        sb.append(", exlane=").append(exlane);
        sb.append(", oper=").append(oper);
        sb.append(", lastmoney=").append(lastmoney);
        sb.append(", freemoney=").append(freemoney);
        sb.append(", totalweight=").append(totalweight);
        sb.append(", axlenum=").append(axlenum);
        sb.append(", tolldistance=").append(tolldistance);
        sb.append(", card=").append(card);
        sb.append(", flagstationinfo=").append(flagstationinfo);
        sb.append(", realflagstationinfo=").append(realflagstationinfo);
        sb.append(", inv=").append(inv);
        sb.append(", weightLimitation=").append(weightLimitation);
        sb.append(", secondFlag=").append(secondFlag);
        sb.append("]");
        return sb.toString();
    }
}