package com.drcnet.highway.dto.response;

import com.drcnet.highway.entity.TietouOrigin;
import lombok.Data;

import java.util.List;

/**
 * @Author jack
 * @Date: 2019/6/19 16:19
 * @Desc: 时间重叠、速度异常、车牌不一致综合风险
 **/
@Data
public class CompositeRiskDto {
    /**
     * 时间重叠、速度异常、车牌不一致综合风险
     */
    private List<TietouOrigin> allRiskList;
    /**
     * 车牌不一致、时间重叠风险
     */
    private List<TietouOrigin> diffCarNoAndSameTimeList;
    /**
     * 车牌不一致、速度异常风险
     */
    private List<TietouOrigin> diffCarNoAndSpeedList;
    /**
     * 速度异常、时间重叠风险
     */
    private List<TietouOrigin> speedAndSameTimeList;

    /**
     * 综合风险总数
     */
    private Integer totalNum;

    public Integer getTotalNum() {
        return allRiskList.size() + diffCarNoAndSameTimeList.size() + diffCarNoAndSpeedList.size() + speedAndSameTimeList.size();
    }
}
