package com.drcnet.highway.domain.es;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author jack
 * @Date: 2019/10/12 15:59
 * @Desc:
 **/
@Data
public class EsTietouExtraction implements Serializable {
    private static final long serialVersionUID = -8886719641435892321L;

    private Integer id;

    private Integer monthTime;

    /**
     * 进站时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime entime;

    /**
     * 进站站名
     */
    private String rk;

    private Integer rkId;

    /**
     * 进站车牌
     */
    private String envlp;

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
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime extime;

    /**
     * 出战站名
     */
    private String ck;

    private Integer ckId;

    /**
     * 出战车牌
     */
    private String vlp;

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

    /**
     * 是否同站进出（1表示是）
     */
    private Integer sameStation;

    private Integer lowSpeed;

    private Integer highSpeed;
    /**
     * 进出车牌是否相同（相同则为1）
     */
    private Integer sameCarNumber;

    /**
     * 进出车型是否一致（一致则为1）
     */
    private Integer sameCarType;

    /**
     * 车情是否一致（一致则为1）
     */
    private Integer sameCarSituation;

    /**
     * 是否短途重载
     */
    private Integer shortDisOverweight;

    /**
     * 是否长途轻载
     */
    private Integer longDisLightweight;

    /**
     * 该车牌是否有不同的轴数
     */
    private Integer differentZhou;

    /**
     * 标志与真实标志是否一致
     */
    private Integer diffFlagstationInfo;

    private Integer minOutIn;

    private Integer sameTimeRangeAgain;

    private Integer flagstationLost;

    private String sameRouteMark;

    /**
     * 0货车，1客车
     */
    private Integer carType;
}
