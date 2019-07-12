package com.drcnet.highway.entity;

import java.io.Serializable;
import javax.persistence.*;

@Table(name = "tietou_original_2019")
public class TietouOriginal2019 implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String entime;

    private String rk;

    private String envlp;

    private String envt;

    private String envc;

    private String extime;

    private String ck;

    private String vlp;

    private String vc;

    private String vt;

    private String exlane;

    private String oper;

    private String lastmoney;

    private String freemoney;

    private String totalweight;

    private String axlenum;

    private String tolldistance;

    private String card;

    private String flagstationinfo;

    private String realflagstationinfo;

    private String inv;

    private static final long serialVersionUID = 1L;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return entime
     */
    public String getEntime() {
        return entime;
    }

    /**
     * @param entime
     */
    public void setEntime(String entime) {
        this.entime = entime == null ? null : entime.trim();
    }

    /**
     * @return rk
     */
    public String getRk() {
        return rk;
    }

    /**
     * @param rk
     */
    public void setRk(String rk) {
        this.rk = rk == null ? null : rk.trim();
    }

    /**
     * @return envlp
     */
    public String getEnvlp() {
        return envlp;
    }

    /**
     * @param envlp
     */
    public void setEnvlp(String envlp) {
        this.envlp = envlp == null ? null : envlp.trim();
    }

    /**
     * @return envt
     */
    public String getEnvt() {
        return envt;
    }

    /**
     * @param envt
     */
    public void setEnvt(String envt) {
        this.envt = envt == null ? null : envt.trim();
    }

    /**
     * @return envc
     */
    public String getEnvc() {
        return envc;
    }

    /**
     * @param envc
     */
    public void setEnvc(String envc) {
        this.envc = envc == null ? null : envc.trim();
    }

    /**
     * @return extime
     */
    public String getExtime() {
        return extime;
    }

    /**
     * @param extime
     */
    public void setExtime(String extime) {
        this.extime = extime == null ? null : extime.trim();
    }

    /**
     * @return ck
     */
    public String getCk() {
        return ck;
    }

    /**
     * @param ck
     */
    public void setCk(String ck) {
        this.ck = ck == null ? null : ck.trim();
    }

    /**
     * @return vlp
     */
    public String getVlp() {
        return vlp;
    }

    /**
     * @param vlp
     */
    public void setVlp(String vlp) {
        this.vlp = vlp == null ? null : vlp.trim();
    }

    /**
     * @return vc
     */
    public String getVc() {
        return vc;
    }

    /**
     * @param vc
     */
    public void setVc(String vc) {
        this.vc = vc == null ? null : vc.trim();
    }

    /**
     * @return vt
     */
    public String getVt() {
        return vt;
    }

    /**
     * @param vt
     */
    public void setVt(String vt) {
        this.vt = vt == null ? null : vt.trim();
    }

    /**
     * @return exlane
     */
    public String getExlane() {
        return exlane;
    }

    /**
     * @param exlane
     */
    public void setExlane(String exlane) {
        this.exlane = exlane == null ? null : exlane.trim();
    }

    /**
     * @return oper
     */
    public String getOper() {
        return oper;
    }

    /**
     * @param oper
     */
    public void setOper(String oper) {
        this.oper = oper == null ? null : oper.trim();
    }

    /**
     * @return lastmoney
     */
    public String getLastmoney() {
        return lastmoney;
    }

    /**
     * @param lastmoney
     */
    public void setLastmoney(String lastmoney) {
        this.lastmoney = lastmoney == null ? null : lastmoney.trim();
    }

    /**
     * @return freemoney
     */
    public String getFreemoney() {
        return freemoney;
    }

    /**
     * @param freemoney
     */
    public void setFreemoney(String freemoney) {
        this.freemoney = freemoney == null ? null : freemoney.trim();
    }

    /**
     * @return totalweight
     */
    public String getTotalweight() {
        return totalweight;
    }

    /**
     * @param totalweight
     */
    public void setTotalweight(String totalweight) {
        this.totalweight = totalweight == null ? null : totalweight.trim();
    }

    /**
     * @return axlenum
     */
    public String getAxlenum() {
        return axlenum;
    }

    /**
     * @param axlenum
     */
    public void setAxlenum(String axlenum) {
        this.axlenum = axlenum == null ? null : axlenum.trim();
    }

    /**
     * @return tolldistance
     */
    public String getTolldistance() {
        return tolldistance;
    }

    /**
     * @param tolldistance
     */
    public void setTolldistance(String tolldistance) {
        this.tolldistance = tolldistance == null ? null : tolldistance.trim();
    }

    /**
     * @return card
     */
    public String getCard() {
        return card;
    }

    /**
     * @param card
     */
    public void setCard(String card) {
        this.card = card == null ? null : card.trim();
    }

    /**
     * @return flagstationinfo
     */
    public String getFlagstationinfo() {
        return flagstationinfo;
    }

    /**
     * @param flagstationinfo
     */
    public void setFlagstationinfo(String flagstationinfo) {
        this.flagstationinfo = flagstationinfo == null ? null : flagstationinfo.trim();
    }

    /**
     * @return realflagstationinfo
     */
    public String getRealflagstationinfo() {
        return realflagstationinfo;
    }

    /**
     * @param realflagstationinfo
     */
    public void setRealflagstationinfo(String realflagstationinfo) {
        this.realflagstationinfo = realflagstationinfo == null ? null : realflagstationinfo.trim();
    }

    /**
     * @return inv
     */
    public String getInv() {
        return inv;
    }

    /**
     * @param inv
     */
    public void setInv(String inv) {
        this.inv = inv == null ? null : inv.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", entime=").append(entime);
        sb.append(", rk=").append(rk);
        sb.append(", envlp=").append(envlp);
        sb.append(", envt=").append(envt);
        sb.append(", envc=").append(envc);
        sb.append(", extime=").append(extime);
        sb.append(", ck=").append(ck);
        sb.append(", vlp=").append(vlp);
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
        sb.append("]");
        return sb.toString();
    }
}