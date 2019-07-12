package com.drcnet.highway.vo;

import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/18 14:46
 * @Description:
 */
@Data
public class MainPageAmountVo {
    //出入站车牌不一致
    private Long inOutCarNoNotSame;
    //出入站车型不一致
    private Long inOutCarTypeNotSame;
    //出入站数量差距100
    private Long inOutAmountDiffer;
    //同站先出后进
    private Long outAndInAmount;
    //同站进出
    private Long sameStationInAndOut;
    //频繁进出
    private Long frequentAmount;
    //总数
    private Long total;

    public void calcTotals() {
        total = inOutCarNoNotSame + inOutCarTypeNotSame + inOutAmountDiffer + outAndInAmount + sameStationInAndOut + frequentAmount;
    }

}
