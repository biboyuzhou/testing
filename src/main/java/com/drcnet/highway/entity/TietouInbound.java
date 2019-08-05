package com.drcnet.highway.entity;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.*;

@Table(name = "tietou_inbound")
@Data
public class TietouInbound implements Serializable {
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
     * 进站通道
     */
    private String inlane;

    /**
     * 总重
     */
    private Integer totalweight;

    /**
     * 轴数
     */
    private Integer axlenum;

    /**
     * 卡号
     */
    private String card;

    /**
     * 流水号
     */
    private String inv;

    /**
     * 进站时间
     */
    @JsonFormat(pattern = TimeConsts.TIME_FORMAT,timezone = TimeConsts.GMT8)
    private LocalDateTime creatime;

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
        sb.append(", inlane=").append(inlane);
        sb.append(", totalweight=").append(totalweight);
        sb.append(", axlenum=").append(axlenum);
        sb.append(", card=").append(card);
        sb.append(", inv=").append(inv);
        sb.append("]");
        return sb.toString();
    }
}