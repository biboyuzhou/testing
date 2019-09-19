package com.drcnet.highway.dto.request;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/8/12 11:09
 * @Desc:
 **/
public class ChangeCardQueryDto extends PagingDto implements Serializable {

    private static final long serialVersionUID = -2082725530624721004L;

    /**
     * 进口卡号
     */
    private String inCard;
    /**
     * 出口卡号
     */
    private String outCard;
    /**
     * 进口车牌号
     */
    private String inCarNo;
    /**
     * 出口车牌号
     */
    private String outCarNo;
    /**
     * 入口日期
     */
    private String beginDate;
    /**
     * 出口日期
     */
    private String endDate;

    /**
     * 确认换卡,0：待定；1：换卡
     */
    private Integer changeCardConfirm;

    private Integer envlpId;

    private Integer vlpId;

    public String getInCard() {
        return inCard;
    }

    public void setInCard(String inCard) {
        this.inCard = inCard;
    }

    public String getOutCard() {
        return outCard;
    }

    public void setOutCard(String outCard) {
        this.outCard = outCard;
    }

    public String getInCarNo() {
        return inCarNo;
    }

    public void setInCarNo(String inCarNo) {
        this.inCarNo = inCarNo;
    }

    public String getOutCarNo() {
        return outCarNo;
    }

    public void setOutCarNo(String outCarNo) {
        this.outCarNo = outCarNo;
    }

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Integer getChangeCardConfirm() {
        return changeCardConfirm;
    }

    public void setChangeCardConfirm(Integer changeCardConfirm) {
        this.changeCardConfirm = changeCardConfirm;
    }

    public Integer getEnvlpId() {
        return envlpId;
    }

    public void setEnvlpId(Integer envlpId) {
        this.envlpId = envlpId;
    }

    public Integer getVlpId() {
        return vlpId;
    }

    public void setVlpId(Integer vlpId) {
        this.vlpId = vlpId;
    }
}
