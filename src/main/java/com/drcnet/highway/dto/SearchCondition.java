package com.drcnet.highway.dto;

import lombok.Data;

/**
 * @Author: penghao
 * @CreateTime: 2019/1/21 10:24
 * @Description:
 */
@Data
public class SearchCondition {
    //原始数据
    private boolean original;
    //先出后进
    private boolean outAndIn;
    //同站进出
    private boolean sameStationInOut;
    //高频车次
    private boolean highFrequency;
    //车牌不一致
    private boolean carNoDiffer;
    //车型不一致
    private boolean carTypeDiffer;
    //进站车牌号
    private String inCarNo;
    //出站车牌号
    private String outCarNo;

    public boolean hasTrue() {
        return original || outAndIn || sameStationInOut || highFrequency || carNoDiffer || carTypeDiffer;
    }

    /**
     * 查询需要查询的任务数量
     * @return
     */
    public int searchAmount(){
        int i = 0;
        if (original) i++;
        if (outAndIn) i++;
        if (sameStationInOut) i++;
        if (highFrequency) i++;
        if (carNoDiffer) i++;
        if (carTypeDiffer) i++;
        return i;
    }
}
