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
public class CarFlag implements Serializable {

    private static final long serialVersionUID = -8853644232356462645L;

    /**
     * 标志站id
     */
    private int carId;

    /**
     * 进站次数
     */
    private int enter;

    /**
     * 出站次数
     */
    private int exit;

    /**
     * 标志站次数
     */
    private int flag;

    /**
     * 跟二绕产生关系次数
     */
    private int secondRound;

    /**
     * 预警分数
     */
    private int score;

    /**
     * 理论异常次数
     * @return
     */
     public int getSecondRound() {
        return enter + exit + flag;
    }


    @Override
    public String toString() {
        return "CarFlag{" +
                "carId=" + carId +
                ", enter=" + enter +
                ", exit=" + exit +
                ", flag=" + flag  +
                ", score=" + score  +
                ", secondRound=" + secondRound  +
                '}';
    }
}
