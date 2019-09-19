package com.drcnet.highway.dto.response;

import com.drcnet.highway.constants.TimeConsts;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author jack
 * @Date: 2019/8/12 13:05
 * @Desc:
 **/
@Data
public class ChangeCardResponse implements Serializable {
    private static final long serialVersionUID = 1954784909377920158L;

    /**
     * 入口数据id
     */
    private Integer inId;

    /**
     * 入口卡号
     */
    private String inCard;

    /**
     * 换卡确认结果
     */
    private Integer changeCardConfirm;

    //-----以下为出口数据
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

    private Integer rkId;

    private Integer ckId;

    private String vlp;

    private Integer vlpId;

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
}
