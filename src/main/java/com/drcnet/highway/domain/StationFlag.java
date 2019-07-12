package com.drcnet.highway.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author jack
 * @Date: 2019/6/3 12:50
 * @Desc:
 **/
@Setter
@Getter
public class StationFlag implements Serializable {

    private static final long serialVersionUID = -8853644232356462645L;

    /**
     * 标志站id
     */
    private String id;

    /**
     * 理论次数
     */
    private int theoreticalFrequency;

    /**
     * 实际c出现次数
     */
    private int actualFrequency;

    /**
     * 实际异常次数
     */
    private int actualAbnormal;

    /**
     * 理论异常次数
     * @param stationFlag
     * @return
     */
     public int getAbnomalCount(StationFlag stationFlag) {
        return stationFlag.getTheoreticalFrequency() - stationFlag.getActualFrequency();
    }


    @Override
    public String toString() {
        return "StationFlag{" +
                "id=" + id +
                ", actualAbnormal=" + actualAbnormal +
                ", theoreticalFrequency=" + theoreticalFrequency +
                ", actualFrequency=" + actualFrequency  +
                '}';
    }
}
